package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JobBindingTest {
    @TempDir
    static Path home;

    @BeforeAll
    static void initializeEnvironment() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            homeField.set(null, home.toFile());
            Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunch;
        }
    }

    @Test
    void aliasesAndInheritedPrivateFields_bindEveryFieldAcrossArchetypes() {
        ComponentRegistry components = components();
        JobRegistry jobs = jobs(components);
        List<CountingPool> pools = new ArrayList<>();
        EntityManager entities = new EntityManager(components, types -> {
            CountingPool pool = new CountingPool(components, types);
            pools.add(pool);
            return pool;
        });
        CleanEntityHandle first = entities.createEntity(value(10));
        CleanEntityHandle second = entities.createEntity(new Marker(), value(20));
        entities.flush();
        Probe probe = new Probe(1);

        var handle = new JobExecutor(jobs).executeParallelJob(entities, AliasJob.class,
            Map.of("probe", probe, "increment", 7), Runnable::run);
        handle.future().join();

        assertEquals(2, handle.totalThreadCount());
        assertFalse(handle.async());
        assertEquals(17, first.readComponent(Value.class).orElseThrow().value);
        assertEquals(27, second.readComponent(Value.class).orElseThrow().value);
        assertEquals(2, probe.visits);
        assertEquals(2, pools.size());
        for (CountingPool pool : pools) {
            assertEquals(2, pool.columnResolutions);
        }
        entities.flush();
    }

    @Test
    void asyncPartitions_resolveColumnsAndCaptureExternalReferencesBeforeSubmission() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        ComponentRegistry components = components();
        CountingPool pool = new CountingPool(components, List.of(Value.class));
        EntityManager entities = new EntityManager(components, types -> pool);
        CleanEntityHandle[] handles = new CleanEntityHandle[8];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = entities.createEntity(value(i));
        }
        entities.flush();
        Probe captured = new Probe(KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread);
        Probe replacement = new Probe(1);
        Map<String, Object> external = new HashMap<>(Map.of("probe", captured, "increment", 7));
        List<Runnable> pending = new ArrayList<>();

        var execution = RuntimeBootstrap.newJobScheduler(jobs(components)).executeParallelJob(entities,
            AliasJob.class, external, task -> {
                pending.add(task);
                external.put("increment", 99);
                external.put("probe", replacement);
            });

        assertTrue(execution.async());
        assertTrue(pending.size() > 1);
        assertEquals(pending.size(), execution.totalThreadCount());
        assertEquals(2, pool.columnResolutions);
        assertThrows(IllegalStateException.class, entities::flush);
        for (int i = pending.size() - 1; i >= 0; i--) {
            pending.get(i).run();
        }
        execution.future().join();
        assertEquals(8, captured.visits);
        assertEquals(0, replacement.visits);
        for (int i = 0; i < handles.length; i++) {
            assertEquals(i + 7, handles[i].readComponent(Value.class).orElseThrow().value);
        }
        entities.flush();
    }

    @Test
    void repeatedLaunches_afterResizeTrimAndMigration_useCurrentPoolDataAndInputs() {
        ComponentRegistry components = components();
        EntityManager entities = new EntityManager(components);
        JobExecutor executor = new JobExecutor(jobs(components));
        CleanEntityHandle entity = entities.createEntity(value(10));
        entities.flush();
        run(executor, entities, 3);
        assertEquals(13, entity.readComponent(Value.class).orElseThrow().value);

        List<CleanEntityHandle> temporary = new ArrayList<>();
        for (int i = 0; i < 350; i++) {
            temporary.add(entities.createEntity(value(i)));
        }
        entities.flush();
        for (CleanEntityHandle handle : temporary) {
            assertTrue(handle.tryDestroy());
        }
        entities.flush();
        run(executor, entities, 5);
        assertEquals(18, entity.readComponent(Value.class).orElseThrow().value);

        assertTrue(entity.tryAddComponent(new Marker()));
        entities.flush();
        run(executor, entities, 9);
        assertEquals(27, entity.readComponent(Value.class).orElseThrow().value);
    }

    @Test
    void bindingFailure_afterAcceptedTasks_keepsEveryPoolGuardUntilTasksFinish() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        ComponentRegistry components = components();
        int[] resolutions = {0};
        EntityManager entities = new EntityManager(components, types -> {
            CountingPool pool = new CountingPool(components, types);
            pool.beforeResolve = () -> {
                if (++resolutions[0] == 3) {
                    throw new IllegalStateException("expected binding failure");
                }
            };
            return pool;
        });
        entities.createEntity(value(1));
        entities.createEntity(value(2));
        entities.createEntity(value(3), new Marker());
        entities.createEntity(value(4), new Marker());
        entities.flush();
        List<Runnable> pending = new ArrayList<>();
        Probe probe = new Probe(KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread);

        IllegalStateException failure = assertThrows(IllegalStateException.class,
            () -> new JobExecutor(jobs(components)).executeParallelJob(entities, AliasJob.class,
                Map.of("increment", 1, "probe", probe), pending::add));

        assertEquals("expected binding failure", failure.getMessage());
        assertEquals(2, pending.size());
        assertThrows(IllegalStateException.class, entities::flush);
        pending.getFirst().run();
        assertThrows(IllegalStateException.class, entities::flush);
        pending.getLast().run();
        entities.flush();
        assertEquals(2, probe.visits);
    }

    @Test
    void maximumRowWorkload_accumulatesAndPartitionsWithoutIntegerOverflow() {
        ComponentRegistry components = components();
        EntityManager entities = new EntityManager(components);
        JobRegistry registry = new JobRegistry(components);
        registry.registerParallelJob(HugeWorkloadJob.class);
        int entityCount = Math.min(256, Runtime.getRuntime().availableProcessors() * 2 + 4);
        CleanEntityHandle[] handles = new CleanEntityHandle[entityCount];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = entities.createEntity(value(i));
        }
        entities.flush();
        Probe probe = new Probe(1);
        List<Runnable> pending = new ArrayList<>();

        var execution = new JobExecutor(registry).executeParallelJob(entities, HugeWorkloadJob.class,
            Map.of("probe", probe, "increment", 7), pending::add);

        if (Runtime.getRuntime().availableProcessors() > 1) {
            assertTrue(execution.async());
            assertEquals(pending.size(), execution.totalThreadCount());
            assertTrue(pending.size() > 1);
            assertThrows(IllegalStateException.class, entities::flush);
            for (Runnable task : pending) {
                task.run();
            }
        } else {
            assertFalse(execution.async());
            assertTrue(pending.isEmpty());
        }
        execution.future().join();
        assertEquals(handles.length, probe.visits);
        for (int i = 0; i < handles.length; i++) {
            assertEquals(i + 7, handles[i].readComponent(Value.class).orElseThrow().value);
        }
        entities.flush();
    }

    @Test
    void externalBindings_supportPrimitiveNullableAndHiddenSuperclassFields() {
        ComponentRegistry components = components();
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(ExternalJob.class);
        JobDefinition definition = jobs.getDefinition(ExternalJob.class);
        Map<String, Object> input = new HashMap<>();
        input.put("number", 42);
        input.put("nullable", null);
        input.put("shared", "value");
        Object[] values = definition.resolveExternalData(input);
        input.put("number", 99);
        input.put("shared", "replacement");
        ExternalJob job = (ExternalJob) definition.instantiate(
            definition.resolveColumns(new HeapPool(components, List.of(), 0)), values);

        assertEquals(42, job.number);
        assertNull(job.nullable);
        assertEquals("value", job.shared);
        assertEquals("value", ((ExternalBase) job).shared);
        assertThrows(IllegalArgumentException.class, () -> definition.resolveExternalData(null));
        assertThrows(IllegalArgumentException.class, () -> definition.resolveExternalData(Map.of()));
    }

    @Test
    void registration_invalidFieldsDoNotPublishPartialDefinitions() {
        JobRegistry registry = new JobRegistry(components());
        for (Class<? extends ParallelJob> type : List.of(InvalidPathJob.class,
            ConflictingAnnotationsJob.class, FinalFieldJob.class, WrongTypeJob.class,
            UnregisteredComponentJob.class, AbstractJob.class, NoDefaultConstructorJob.class)) {
            assertThrows(RuntimeException.class, () -> registry.registerParallelJob(type), type.getName());
            assertNull(registry.getDefinition(type), type.getName());
        }
        assertNull(registry.getDefinition(AliasJob.class));
        registry.registerParallelJob(AliasJob.class);
        assertEquals(2, registry.getDefinition(AliasJob.class).dataBindings.length);
    }

    @Test
    void staticAnnotatedFields_areIgnoredAndUnknownJobsAreRejectedBeforeExecution() {
        ComponentRegistry components = components();
        JobRegistry registry = new JobRegistry(components);
        registry.registerParallelJob(StaticJob.class);
        JobDefinition definition = registry.getDefinition(StaticJob.class);
        assertEquals(0, definition.dataBindings.length);
        assertEquals(0, definition.externalBindings.length);
        EntityManager entities = new EntityManager(components);
        assertThrows(IllegalStateException.class, () -> new JobExecutor(registry).executeParallelJob(
            entities, AliasJob.class, null, Runnable::run));
        entities.flush();
    }

    private static void run(JobExecutor executor, EntityManager entities, int increment) {
        executor.executeParallelJob(entities, AliasJob.class,
            Map.of("increment", increment, "probe", new Probe(1)), Runnable::run).future().join();
    }

    private static JobRegistry jobs(ComponentRegistry components) {
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(AliasJob.class);
        return jobs;
    }

    private static ComponentRegistry components() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("value", Value.class, new MemberLayout("value"), "int");
        components.registerComponent("marker", Marker.class, new MemberLayout());
        components.lock();
        return components;
    }

    private static Value value(int number) {
        Value value = new Value();
        value.value = number;
        return value;
    }

    private static final class CountingPool extends ArchetypeDataPool {
        private final HeapPool delegate;
        int columnResolutions;
        Runnable beforeResolve = () -> {};

        CountingPool(ComponentRegistry components, List<Class<? extends CleanComponent>> types) {
            super(components, types, 1);
            delegate = new HeapPool(components, types, 1);
        }

        @Override
        public boolean containsEntity(int entity) {
            return delegate.containsEntity(entity);
        }

        @Override
        public CleanComponent getComponent(int entity, Class<? extends CleanComponent> component) {
            return delegate.getComponent(entity, component);
        }

        @Override
        public void setComponent(int entity, CleanComponent component) {
            delegate.setComponent(entity, component);
        }

        @Override
        public void addEntity(int entity, CleanComponent[] components) {
            delegate.addEntity(entity, components);
            currentSize = delegate.getCurrentSize();
        }

        @Override
        public void removeEntity(int entity) {
            delegate.removeEntity(entity);
        }

        @Override
        void addEntityFrom(int entity, ArchetypeDataPool source, CleanComponent addition) {
            delegate.addEntityFrom(entity, source instanceof CountingPool pool ? pool.delegate : source, addition);
            currentSize = delegate.getCurrentSize();
        }

        @Override
        void trimCapacity() {
            delegate.trimCapacity();
            currentSize = delegate.getCurrentSize();
        }

        @Override
        public void beginExecution() {
            delegate.beginExecution();
        }

        @Override
        public void endExecution() {
            delegate.endExecution();
        }

        @Override
        public PrimitiveArray getArray(Class<? extends CleanComponent> component, String... path) {
            return delegate.getArray(component, path);
        }

        @Override
        public PrimitiveArray getArray(Class<? extends CleanComponent> component, SchemaLayout.Column column) {
            beforeResolve.run();
            columnResolutions++;
            return delegate.getArray(component, column);
        }

        @Override
        public PoolRows getRows() { return delegate.getRows(); }

        @Override
        public int getEntityID(int row) { return delegate.getEntityID(row); }

        @Override
        public String getSnapshot() { return delegate.getSnapshot(); }
    }

    public static class Value implements CleanComponent {
        public int value;
    }

    public static class Marker implements CleanComponent {}

    public static class Probe {
        final int workload;
        int visits;

        Probe(int workload) {
            this.workload = workload;
        }
    }

    public static class BaseJob implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "value")
        private PrimitiveArray values;
        @JobExternalDataQuery
        private int increment;
        @JobExternalDataQuery
        private Probe probe;

        public void execute(EntityCommands commands, int row, int entity, int partition) {
            values.setInt(row, values.getInt(row) + increment);
            probe.visits++;
        }

        public void query(EntityQuery query) {
            query.with(Value.class);
        }

        public int estimateWorkload(int row) {
            return probe.workload;
        }
    }

    public static class AliasJob extends BaseJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "value")
        private PrimitiveArray alias;

        @Override
        public void execute(EntityCommands commands, int row, int entity, int partition) {
            assertSame(((BaseJob) this).values, alias);
            super.execute(commands, row, entity, partition);
        }
    }

    public static class HugeWorkloadJob extends BaseJob {
        @Override
        public int estimateWorkload(int row) {
            return Integer.MAX_VALUE;
        }
    }

    public static class ExternalBase implements ParallelJob {
        @JobExternalDataQuery
        private String shared;

        public void execute(EntityCommands commands, int row, int entity, int partition) {}
        public void query(EntityQuery query) {}
        public int estimateWorkload(int row) { return 1; }
    }

    public static class ExternalJob extends ExternalBase {
        @JobExternalDataQuery
        private String shared;
        @JobExternalDataQuery
        private int number;
        @JobExternalDataQuery
        private Object nullable;
    }

    public static class InvalidPathJob extends BaseJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "absent")
        private PrimitiveArray invalid;
    }

    public static class ConflictingAnnotationsJob extends BaseJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "value")
        @JobExternalDataQuery
        private PrimitiveArray invalid;
    }

    public static class FinalFieldJob extends BaseJob {
        @JobExternalDataQuery
        private final int invalid = 0;
    }

    public static class WrongTypeJob extends BaseJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "value")
        private String invalid;
    }

    public static class UnregisteredComponentJob extends BaseJob {
        @JobDataQuery(componentClass = CleanComponent.class, fieldAccessChain = "value")
        private PrimitiveArray invalid;
    }

    public abstract static class AbstractJob extends BaseJob {}

    public static class NoDefaultConstructorJob extends BaseJob {
        public NoDefaultConstructorJob(int value) {}
    }

    public static class StaticJob implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "absent")
        private static String ignored;
        @JobExternalDataQuery
        private static final Object alsoIgnored = null;

        public void execute(EntityCommands commands, int row, int entity, int partition) {}
        public void query(EntityQuery query) {}
        public int estimateWorkload(int row) { return 1; }
    }
}
