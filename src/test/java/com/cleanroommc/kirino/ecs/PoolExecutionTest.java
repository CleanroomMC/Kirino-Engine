package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;

import com.cleanroommc.kirino.config.KirinoConfig;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PoolExecutionTest {
    @TempDir Path home;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void fragmentedPool_syncAndCancelledAsync_visitEachLiveRowOnce(boolean asynchronous) throws Exception {
        if (asynchronous) assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        fixture();
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(Value.class), 0);
        EntityManager entities = new EntityManager(registry, components -> pool);
        CleanEntityHandle[] handles = new CleanEntityHandle[64];
        for (int i = 0; i < handles.length; i++) {
            Value value = new Value();
            value.value = i + 100;
            handles[i] = entities.createEntity(value);
        }
        entities.flush();
        for (int i = 0; i < handles.length; i += 3) assertTrue(handles[i].tryDestroy());
        entities.flush();
        PoolRows rows = pool.getRows();
        int[] borrowed = rows.nextFree;
        RowProbe probe = new RowProbe(asynchronous);
        JobRegistry jobs = new JobRegistry(registry);
        jobs.registerParallelJob(RowAuditJob.class);
        List<Runnable> pending = new ArrayList<>();
        var handle = RuntimeBootstrap.newJobScheduler(jobs).executeParallelJob(entities,
            RowAuditJob.class, Map.of("probe", probe), pending::add);
        assertEquals(asynchronous, handle.async());
        if (asynchronous) {
            assertFalse(pending.isEmpty());
            assertTrue(handle.future().cancel(false));
            assertThrows(IllegalStateException.class, entities::flush);
            for (int i = pending.size() - 1; i >= 0; i--) {
                pending.get(i).run();
                if (i != 0) assertThrows(IllegalStateException.class, entities::flush);
                assertSame(borrowed, rows.nextFree);
            }
        } else {
            assertTrue(pending.isEmpty());
            handle.future().join();
        }
        entities.flush();
        for (int i = 0; i < handles.length; i++) {
            assertEquals(i % 3 == 0 ? 0 : 1, probe.visits[i], "row=" + i);
            if (i % 3 != 0) {
                assertEquals(handles[i].index, probe.ids[i]);
                assertEquals(i + 101f, handles[i].readComponent(Value.class).orElseThrow().value, 0f);
            }
        }
    }

    @Test
    void asyncExecution_cancellationKeepsStructuralGuardUntilActualTasksFinish() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        List<Runnable> pending = new ArrayList<>();
        var handle = fixture.scheduler.executeParallelJob(fixture.entities, Job.class, null, pending::add);
        assertTrue(handle.async()); assertEquals(2, pending.size());
        assertThrows(IllegalStateException.class, () -> fixture.pool.addEntity(3, new CleanComponent[]{new Value()}));
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        assertTrue(handle.future().cancel(false));
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        pending.getFirst().run();
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        pending.getLast().run();
        fixture.entities.flush();
        fixture.pool.addEntity(3, new CleanComponent[]{new Value()});
        assertEquals(1f, fixture.column.getFloat(0), 0f);
        assertEquals(1f, fixture.column.getFloat(1), 0f);
        assertSame(fixture.column, fixture.pool.getArray(Value.class, "value"));
        assertEquals(fixture.pool.getCurrentSize(), fixture.column.length());
        assertTrue(fixture.column.length() >= 3);
    }

    @Test
    void executorRejection_afterAcceptedTask_releasesOnlyWhenAcceptedTaskFinishes() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        List<Runnable> pending = new ArrayList<>();
        Executor rejecting = task -> {
            if (!pending.isEmpty()) { throw new RejectedExecutionException("expected"); }
            pending.add(task);
        };
        assertThrows(RejectedExecutionException.class,
            () -> fixture.scheduler.executeParallelJob(fixture.entities, Job.class, null, rejecting));
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        pending.getFirst().run();
        fixture.pool.removeEntity(1);
        assertFalse(fixture.pool.containsEntity(1));
    }

    @Test
    void taskFailure_releasesGuardAfterAllTasksComplete() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        List<Runnable> pending = new ArrayList<>();
        var handle = fixture.scheduler.executeParallelJob(fixture.entities, ThrowingJob.class, null, pending::add);
        pending.getFirst().run();
        assertFalse(handle.future().isDone());
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        pending.getLast().run();
        assertThrows(CompletionException.class, handle.future()::join);
        fixture.pool.removeEntity(1);
    }

    @Test
    void invalidWorkload_releasesGuardWithoutSchedulingTasks() throws Exception {
        Fixture fixture = fixture();
        assertThrows(IllegalStateException.class,
            () -> fixture.scheduler.executeParallelJob(fixture.entities, InvalidJob.class, null, Runnable::run));
        fixture.pool.removeEntity(1);
        assertFalse(fixture.pool.containsEntity(1));
    }

    @Test
    void synchronousTaskFailure_releasesGuardBeforeReturningException() throws Exception {
        Fixture fixture = fixture();
        fixture.pool.removeEntity(2);
        assertThrows(IllegalStateException.class,
            () -> fixture.scheduler.executeParallelJob(fixture.entities, ThrowingJob.class, null, Runnable::run));
        fixture.pool.removeEntity(1);
        assertFalse(fixture.pool.containsEntity(1));
    }

    @Test
    void checkedPreparationFailure_afterAcceptedTask_keepsGuardUntilTaskFinishes() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        List<Runnable> pending = new ArrayList<>();

        CompletionException failure = assertThrows(CompletionException.class,
            () -> fixture.scheduler.executeParallelJob(fixture.entities, CheckedWorkloadJob.class, null, pending::add));

        assertEquals("checked workload failure", failure.getCause().getMessage());
        assertEquals(1, pending.size());
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        pending.getFirst().run();
        fixture.entities.flush();
        fixture.pool.removeEntity(1);
    }

    @Test
    void checkedSynchronousTaskFailure_preservesCauseAndReleasesGuard() throws Exception {
        Fixture fixture = fixture();
        fixture.pool.removeEntity(2);

        CompletionException failure = assertThrows(CompletionException.class,
            () -> fixture.scheduler.executeParallelJob(fixture.entities, CheckedTaskJob.class, null, Runnable::run));

        assertEquals("checked task failure", failure.getCause().getMessage());
        fixture.entities.flush();
        fixture.pool.removeEntity(1);
    }

    @Test
    void nestedExecutionGuards_requireMatchingReleases() throws Exception {
        Fixture fixture = fixture();
        fixture.pool.beginExecution(); fixture.pool.beginExecution();
        fixture.pool.endExecution();
        assertThrows(IllegalStateException.class, () -> fixture.pool.removeEntity(1));
        fixture.pool.endExecution();
        fixture.pool.removeEntity(1);
        assertThrows(IllegalStateException.class, fixture.pool::endExecution);
    }

    @Test
    void jobCommands_cancellationKeepsWriterOpenUntilActualCompletionAndThenRejectsReuse() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        fixture();
        ComponentRegistry registry = registry();
        EntityManager entities = new EntityManager(registry);
        CleanEntityHandle first = entities.createEntity(new Value());
        CleanEntityHandle second = entities.createEntity(new Value());
        entities.flush();
        JobRegistry jobs = new JobRegistry(registry);
        jobs.registerParallelJob(CommandJob.class);
        JobScheduler scheduler = RuntimeBootstrap.newJobScheduler(jobs);
        CommandProbe probe = new CommandProbe();
        List<Runnable> pending = new ArrayList<>();

        var execution = scheduler.executeParallelJob(entities, CommandJob.class, Map.of("probe", probe), pending::add);
        assertEquals(2, pending.size());
        pending.getFirst().run();
        EntityCommands writer = probe.writers.getFirst();
        assertTrue(execution.future().cancel(false));
        assertThrows(IllegalStateException.class, entities::flush);
        pending.getLast().run();

        assertEquals(2, probe.writers.size());
        assertSame(writer, probe.writers.getLast());
        assertFalse(first.valid());
        assertFalse(second.valid());
        assertThrows(IllegalStateException.class, () -> writer.createEntity(new Value()));
        assertThrows(IllegalStateException.class, () -> writer.tryDestroy(first.index));
        entities.flush();
        assertEquals(2, probe.created.size());
        for (var created : probe.created) {
            assertTrue(created.isAlive());
            assertEquals(7f, created.readComponent(Value.class).orElseThrow().value, 0f);
        }
    }

    private Fixture fixture() throws Exception {
        var field = FMLInjectionData.class.getDeclaredField("minecraftHome"); field.setAccessible(true);
        Object previous = field.get(null); File previousLaunch = Launch.minecraftHome;
        try {
            field.set(null, home.toFile()); Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            field.set(null, previous); Launch.minecraftHome = previousLaunch;
        }
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(Value.class), 1);
        EntityManager entities = new EntityManager(registry, components ->
            components.equals(List.of(Value.class)) ? pool : new HeapPool(registry, components, 100));
        CleanEntityHandle temporary = entities.createEntity();
        assertEquals(1, entities.createEntity(new Value()).index);
        assertEquals(2, entities.createEntity(new Value()).index);
        assertTrue(temporary.tryDestroy());
        entities.flush();
        JobRegistry jobs = new JobRegistry(registry);
        jobs.registerParallelJob(Job.class); jobs.registerParallelJob(ThrowingJob.class); jobs.registerParallelJob(InvalidJob.class);
        jobs.registerParallelJob(CheckedWorkloadJob.class); jobs.registerParallelJob(CheckedTaskJob.class);
        return new Fixture(pool, entities, RuntimeBootstrap.newJobScheduler(jobs), pool.getArray(Value.class, "value"));
    }

    private static ComponentRegistry registry() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("value"), "float");
        registry.lock();
        return registry;
    }

    public static class CommandProbe {
        final List<EntityCommands> writers = new ArrayList<>();
        final List<com.cleanroommc.kirino.ecs.api.CleanEntityHandle> created = new ArrayList<>();
    }

    public static class RowProbe {
        final boolean asynchronous;
        final int[] visits = new int[64];
        final int[] ids = new int[64];

        RowProbe(boolean asynchronous) { this.asynchronous = asynchronous; }
    }

    public static class RowAuditJob implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = {"value"})
        public PrimitiveArray values;
        @JobExternalDataQuery
        public RowProbe probe;

        @Override
        public void execute(EntityCommands commands, int row, int id, int ordinal) {
            probe.visits[row]++;
            probe.ids[row] = id;
            values.setFloat(row, values.getFloat(row) + 1);
        }

        @Override
        public void query(EntityQuery query) { query.with(Value.class); }

        @Override
        public int estimateWorkload(int row) {
            return probe.asynchronous ? KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread : 1;
        }
    }

    public static class CommandJob implements ParallelJob {
        @JobExternalDataQuery
        public CommandProbe probe;

        @Override
        public void execute(EntityCommands commands, int index, int entityID, int threadOrdinal) {
            probe.writers.add(commands);
            assertTrue(commands.tryDestroy(entityID));
            assertFalse(commands.tryDestroy(entityID));
            Value value = new Value();
            value.value = 7f;
            probe.created.add(commands.createEntity(value));
        }

        @Override
        public void query(EntityQuery query) {
            query.with(Value.class);
        }

        @Override
        public int estimateWorkload(int index) {
            return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        }
    }

    private record Fixture(HeapPool pool, EntityManager entities, JobScheduler scheduler, PrimitiveArray column) {}
    public static class Value implements CleanComponent { public float value; }
    public static class Job implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = {"value"}) public PrimitiveArray values;
        public void execute(EntityCommands manager, int index, int entity, int thread) { values.setFloat(index, values.getFloat(index) + 1); }
        public void query(EntityQuery query) { query.with(Value.class); }
        public int estimateWorkload(int index) { return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread; }
    }
    public static class ThrowingJob extends Job {
        @Override public void execute(EntityCommands manager, int index, int entity, int thread) { throw new IllegalStateException("expected"); }
    }
    public static class InvalidJob extends Job {
        @Override public int estimateWorkload(int index) { return 0; }
    }

    public static class CheckedWorkloadJob extends Job {
        private int estimates;

        @Override
        public int estimateWorkload(int index) {
            if (++estimates == 4) {
                return PoolExecutionTest.<RuntimeException>raise(new Exception("checked workload failure"));
            }
            return super.estimateWorkload(index);
        }

        @Override
        public void execute(EntityCommands manager, int index, int entity, int thread) {
        }
    }

    public static class CheckedTaskJob extends Job {
        @Override
        public void execute(EntityCommands manager, int index, int entity, int thread) {
            PoolExecutionTest.<RuntimeException>raise(new Exception("checked task failure"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> int raise(Throwable failure) throws T {
        throw (T) failure;
    }
}
