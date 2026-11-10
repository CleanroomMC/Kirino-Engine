package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler.ExecutionHandle;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class JobWorkloadTest {
    @TempDir
    static Path home;

    @BeforeAll
    static void initializeEnvironment() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var field = FMLInjectionData.class.getDeclaredField("minecraftHome");
        field.setAccessible(true);
        Object previous = field.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            field.set(null, home.toFile());
            Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            field.set(null, previous);
            Launch.minecraftHome = previousLaunch;
        }
    }

    @ParameterizedTest
    @CsvSource({"false, false", "false, true", "true, false", "true, true"})
    void estimate_uniformOrMixedFragmentedPools_callsEachLiveRowOnce(boolean async, boolean mixed) {
        if (async) {
            assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        }
        Fixture fixture = fixture();
        int base = async ? KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread : 1;
        CleanEntityHandle[] handles = new CleanEntityHandle[96];
        boolean[] live = new boolean[handles.length];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = fixture.entities.createEntity(value(i, base * (mixed && i % 2 != 0 ? 2 : 1)));
            live[i] = i % 3 != 0;
        }
        fixture.entities.flush();
        for (int i = 0; i < handles.length; i++) {
            if (!live[i]) {
                assertTrue(handles[i].tryDestroy());
            }
        }
        fixture.entities.flush();
        Probe probe = new Probe(handles.length);
        List<Runnable> pending = new ArrayList<>();

        ExecutionHandle execution = launch(fixture, probe, pending::add);

        assertEquals(async, execution.async());
        for (int i = 0; i < handles.length; i++) {
            assertEquals(live[i] ? 1 : 0, probe.estimates[i], "estimate id=" + i);
        }
        if (async) {
            assertEquals(pending.size(), execution.totalThreadCount());
            assertTrue(execution.future().cancel(false));
            for (int i = pending.size() - 1; i >= 0; i--) {
                assertThrows(IllegalStateException.class, fixture.entities::flush);
                pending.get(i).run();
            }
        } else {
            assertTrue(pending.isEmpty());
            execution.future().join();
        }
        fixture.entities.flush();
        for (int i = 0; i < handles.length; i++) {
            assertEquals(live[i] ? 1 : 0, probe.visits[i], "execute id=" + i);
            assertEquals(live[i] ? 1 : 0, probe.estimates[i], "final estimate id=" + i);
        }
    }

    @Test
    void partitioning_mixedWeightsAndHoles_preservesThresholdAndRemainder() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        int target = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        int[] weights = {target / 4, 1, target / 4, target - 2 * (target / 4), 1, target / 4};
        CleanEntityHandle[] handles = new CleanEntityHandle[weights.length];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = fixture.entities.createEntity(value(i, weights[i]));
        }
        fixture.entities.flush();
        assertTrue(handles[1].tryDestroy());
        assertTrue(handles[4].tryDestroy());
        fixture.entities.flush();
        Probe probe = new Probe(handles.length);
        List<Runnable> pending = new ArrayList<>();

        ExecutionHandle execution = launch(fixture, probe, pending::add);

        assertEquals(2, pending.size());
        pending.getLast().run();
        pending.getFirst().run();
        execution.future().join();
        assertArrayEquals(new int[]{0, -1, 0, 0, -1, 1}, probe.partitions);
        assertArrayEquals(new int[]{1, 0, 1, 1, 0, 1}, probe.visits);
        fixture.entities.flush();
    }

    @Test
    void lateMixedEstimate_preservesUniformPrefixAcrossHoles() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        int target = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        CleanEntityHandle[] handles = new CleanEntityHandle[129];
        for (int i = 0; i < handles.length; i++) {
            handles[i] = fixture.entities.createEntity(value(i, i == 128 ? 1 : target));
        }
        fixture.entities.flush();
        for (int i = 0; i < handles.length - 1; i++) {
            if (i % 4 != 0) {
                assertTrue(handles[i].tryDestroy());
            }
        }
        fixture.entities.flush();
        Probe probe = new Probe(handles.length);
        List<Runnable> pending = new ArrayList<>();

        ExecutionHandle execution = launch(fixture, probe, pending::add);

        assertTrue(pending.size() > 1);
        for (Runnable task : pending) {
            task.run();
        }
        execution.future().join();
        for (int i = 0; i < handles.length; i++) {
            int expected = i % 4 == 0 ? 1 : 0;
            assertEquals(expected, probe.estimates[i], "estimate id=" + i);
            assertEquals(expected, probe.visits[i], "execute id=" + i);
        }
        fixture.entities.flush();
    }

    @Test
    void inlineTasks_changeExternalEstimateInput_partitioningUsesCapturedEstimates() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        int target = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        for (int i = 0; i < 4; i++) {
            fixture.entities.createEntity(value(i, target));
        }
        fixture.entities.flush();
        Probe probe = new Probe(4);
        probe.changeEstimateScale = true;

        ExecutionHandle execution = launch(fixture, probe, Runnable::run);

        execution.future().join();
        int size = Runtime.getRuntime().availableProcessors() >= 4 ? 1 : 2;
        assertEquals(4 / size, execution.totalThreadCount());
        for (int i = 0; i < 4; i++) {
            assertEquals(i / size, probe.partitions[i]);
            assertEquals(1, probe.estimates[i]);
            assertEquals(1, probe.visits[i]);
        }
        fixture.entities.flush();
    }

    @Test
    void repeatedLaunchesAndDifferentPools_refreshEstimatesAndResetUniformState() {
        Fixture fixture = fixture();
        List<CleanEntityHandle> handles = new ArrayList<>();
        int nextID = 0;
        for (int archetype = 0; archetype < 4; archetype++) {
            int count = new int[]{17, 39, 3, 1}[archetype];
            for (int row = 0; row < count; row++) {
                Value value = value(nextID++, archetype % 2 == 0 ? row + 1 : 1);
                CleanEntityHandle handle = switch (archetype) {
                    case 0 -> fixture.entities.createEntity(value);
                    case 1 -> fixture.entities.createEntity(value, new Marker());
                    case 2 -> fixture.entities.createEntity(value, new OtherMarker());
                    default -> fixture.entities.createEntity(value, new Marker(), new OtherMarker());
                };
                handles.add(handle);
            }
        }
        fixture.entities.flush();
        assertTrue(handles.getLast().tryDestroy());
        fixture.entities.flush();
        Probe first = new Probe(nextID);
        launch(fixture, first, Runnable::run).future().join();
        for (int i = 0; i < handles.size() - 1; i++) {
            assertEquals(1, first.estimates[i]);
            assertEquals(1, first.visits[i]);
            Value copy = handles.get(i).readComponent(Value.class).orElseThrow();
            copy.weight = i % 2 == 0 ? Integer.MAX_VALUE : 1;
            assertTrue(handles.get(i).trySetComponent(copy));
        }
        fixture.entities.flush();
        Probe second = new Probe(nextID);
        launch(fixture, second, Runnable::run).future().join();
        for (int i = 0; i < handles.size() - 1; i++) {
            assertEquals(1, second.estimates[i]);
            assertEquals(1, second.visits[i]);
        }
        assertEquals(0, first.estimates[nextID - 1]);
        assertEquals(0, second.estimates[nextID - 1]);
        fixture.entities.flush();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void invalidEstimate_afterValidRows_rejectsPoolBeforeSubmission(int invalid) {
        Fixture fixture = fixture();
        fixture.entities.createEntity(value(0, 10000));
        fixture.entities.createEntity(value(1, invalid));
        fixture.entities.flush();
        Probe probe = new Probe(2);
        List<Runnable> pending = new ArrayList<>();

        assertThrows(IllegalStateException.class, () -> launch(fixture, probe, pending::add));

        assertTrue(pending.isEmpty());
        assertArrayEquals(new int[]{1, 1}, probe.estimates);
        assertArrayEquals(new int[2], probe.visits);
        fixture.entities.flush();
    }

    @Test
    void checkedEstimateFailure_inLaterPool_keepsPreviouslySubmittedWorkProtected() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = fixture();
        int target = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        for (int i = 0; i < 4; i++) {
            if (i < 2) {
                fixture.entities.createEntity(value(i, target));
            } else {
                fixture.entities.createEntity(value(i, target), new Marker());
            }
        }
        fixture.entities.flush();
        Probe probe = new Probe(4);
        probe.failAt = 3;
        List<Runnable> pending = new ArrayList<>();

        CompletionException failure = assertThrows(CompletionException.class,
            () -> launch(fixture, probe, pending::add));

        assertEquals("checked estimate failure", failure.getCause().getMessage());
        assertEquals(2, pending.size());
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        pending.getFirst().run();
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        pending.getLast().run();
        fixture.entities.flush();
        assertEquals(2, Arrays.stream(probe.visits).sum());
        assertEquals(3, Arrays.stream(probe.estimates).sum());
    }

    @Test
    void simultaneousLaunches_sameExecutor_keepPreparationIndependent() throws Exception {
        Fixture first = fixture();
        EntityManager secondManager = new EntityManager(first.components);
        Fixture second = new Fixture(first.components, secondManager, first.executor);
        for (int i = 0; i < 32; i++) {
            first.entities.createEntity(value(i, i + 1));
            second.entities.createEntity(value(i, Integer.MAX_VALUE - i));
        }
        first.entities.flush();
        second.entities.flush();
        CountDownLatch bothPreparing = new CountDownLatch(2);
        Probe firstProbe = new Probe(32);
        Probe secondProbe = new Probe(32);
        firstProbe.bothPreparing = bothPreparing;
        secondProbe.bothPreparing = bothPreparing;

        try (var launchers = Executors.newFixedThreadPool(2)) {
            var firstResult = launchers.submit(() -> launch(first, firstProbe, Runnable::run));
            var secondResult = launchers.submit(() -> launch(second, secondProbe, Runnable::run));
            firstResult.get(5, TimeUnit.SECONDS).future().join();
            secondResult.get(5, TimeUnit.SECONDS).future().join();
        }
        for (int i = 0; i < 32; i++) {
            assertEquals(1, firstProbe.estimates[i]);
            assertEquals(1, firstProbe.visits[i]);
            assertEquals(1, secondProbe.estimates[i]);
            assertEquals(1, secondProbe.visits[i]);
        }
        first.entities.flush();
        second.entities.flush();
    }

    @Test
    void reentrantPreparation_sameExecutor_preservesBothWorkloadPlans() {
        assumeTrue(Runtime.getRuntime().availableProcessors() >= 3);
        Fixture fixture = partitionFixture();
        Probe outer = new Probe(4);
        Probe inner = new Probe(4);
        inner.estimateScale = 2;
        ExecutionHandle[] nested = new ExecutionHandle[1];
        outer.onEstimateID = 3;
        outer.onEstimate = () -> nested[0] = launch(fixture, inner, Runnable::run);

        ExecutionHandle result = launch(fixture, outer, Runnable::run);

        result.future().join();
        assertNotNull(nested[0]);
        nested[0].future().join();
        assertArrayEquals(new int[]{0, 0, 0, 1}, outer.partitions);
        assertArrayEquals(new int[]{0, 0, 1, 2}, inner.partitions);
        assertArrayEquals(new int[]{1, 1, 1, 1}, outer.estimates);
        assertArrayEquals(new int[]{1, 1, 1, 1}, inner.estimates);
        assertArrayEquals(new int[]{1, 1, 1, 1}, outer.visits);
        assertArrayEquals(new int[]{1, 1, 1, 1}, inner.visits);
        fixture.entities.flush();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pendingCancelledOrRejectedLaunch_bufferReuseDoesNotReleaseItsStructuralGuard(boolean rejected) {
        assumeTrue(Runtime.getRuntime().availableProcessors() >= 3);
        Fixture fixture = partitionFixture();
        Probe first = new Probe(4);
        List<Runnable> firstTasks = new ArrayList<>();
        if (rejected) {
            assertThrows(RejectedExecutionException.class, () -> launch(fixture, first, task -> {
                if (!firstTasks.isEmpty()) {
                    throw new RejectedExecutionException("expected");
                }
                firstTasks.add(task);
            }));
        } else {
            ExecutionHandle cancelled = launch(fixture, first, firstTasks::add);
            assertTrue(cancelled.future().cancel(false));
        }
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        Probe second = new Probe(4);
        second.estimateScale = 2;
        List<Runnable> secondTasks = new ArrayList<>();

        ExecutionHandle next = launch(fixture, second, secondTasks::add);

        assertEquals(3, secondTasks.size());
        for (Runnable task : secondTasks) {
            task.run();
        }
        next.future().join();
        assertArrayEquals(new int[]{0, 0, 1, 2}, second.partitions);
        assertArrayEquals(new int[]{1, 1, 1, 1}, second.estimates);
        assertArrayEquals(new int[]{1, 1, 1, 1}, second.visits);
        assertThrows(IllegalStateException.class, fixture.entities::flush);
        for (Runnable task : firstTasks) {
            task.run();
        }
        assertArrayEquals(rejected ? new int[]{1, 1, 1, 0} : new int[]{1, 1, 1, 1}, first.visits);
        fixture.entities.flush();
    }

    @Test
    void failedMixedPreparation_reusedBufferDoesNotKeepPreviousEstimates() {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        Fixture fixture = partitionFixture();
        Probe failed = new Probe(4);
        failed.failAt = 4;
        List<Runnable> pending = new ArrayList<>();
        assertThrows(CompletionException.class, () -> launch(fixture, failed, pending::add));
        assertTrue(pending.isEmpty());
        fixture.entities.flush();
        Probe next = new Probe(4);

        ExecutionHandle execution = launch(fixture, next, Runnable::run);

        execution.future().join();
        assertArrayEquals(new int[]{0, 0, 0, 1}, next.partitions);
        assertArrayEquals(new int[]{1, 1, 1, 1}, next.estimates);
        assertArrayEquals(new int[]{1, 1, 1, 1}, next.visits);
        fixture.entities.flush();
    }

    private static Fixture partitionFixture() {
        Fixture fixture = fixture();
        int target = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
        int quarter = target / 4;
        int[] weights = {quarter, quarter, target - 2 * quarter, quarter};
        for (int i = 0; i < weights.length; i++) {
            fixture.entities.createEntity(value(i, weights[i]));
        }
        fixture.entities.flush();
        return fixture;
    }

    private static ExecutionHandle launch(Fixture fixture, Probe probe, Executor executor) {
        return fixture.executor.executeParallelJob(fixture.entities, Job.class,
            Map.of("probe", probe), executor);
    }

    private static Fixture fixture() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("value", Value.class, new MemberLayout("id", "weight"), "int", "int");
        components.registerComponent("marker", Marker.class, new MemberLayout());
        components.registerComponent("other", OtherMarker.class, new MemberLayout());
        components.lock();
        JobRegistry registry = new JobRegistry(components);
        registry.registerParallelJob(Job.class);
        return new Fixture(components, new EntityManager(components), new JobExecutor(registry));
    }

    private static Value value(int id, int weight) {
        Value value = new Value();
        value.id = id;
        value.weight = weight;
        return value;
    }

    private record Fixture(ComponentRegistry components, EntityManager entities, JobExecutor executor) {}

    public static class Value implements CleanComponent {
        public int id;
        public int weight;
    }

    public static class Marker implements CleanComponent {}
    public static class OtherMarker implements CleanComponent {}

    public static class Probe {
        final int[] estimates;
        final int[] visits;
        final int[] partitions;
        int totalEstimates;
        int failAt = -1;
        boolean changeEstimateScale;
        int estimateScale = 1;
        CountDownLatch bothPreparing;
        int onEstimateID = -1;
        Runnable onEstimate;

        Probe(int size) {
            estimates = new int[size];
            visits = new int[size];
            partitions = new int[size];
            Arrays.fill(partitions, -1);
        }
    }

    public static class Job implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "id")
        private PrimitiveArray ids;
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "weight")
        private PrimitiveArray weights;
        @JobExternalDataQuery
        private Probe probe;

        public void query(EntityQuery query) { query.with(Value.class); }

        public int estimateWorkload(int row) {
            int id = ids.getInt(row);
            assertEquals(1, ++probe.estimates[id], "Repeated estimate for id=" + id);
            if (++probe.totalEstimates == probe.failAt) {
                return JobWorkloadTest.<RuntimeException>raise(new Exception("checked estimate failure"));
            }
            if (id == probe.onEstimateID && probe.onEstimate != null) {
                Runnable action = probe.onEstimate;
                probe.onEstimate = null;
                action.run();
            }
            if (probe.totalEstimates == 1 && probe.bothPreparing != null) {
                probe.bothPreparing.countDown();
                try {
                    assertTrue(probe.bothPreparing.await(3, TimeUnit.SECONDS));
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(failure);
                }
            }
            return weights.getInt(row) * probe.estimateScale;
        }

        public void execute(EntityCommands commands, int row, int entity, int partition) {
            int id = ids.getInt(row);
            probe.visits[id]++;
            probe.partitions[id] = partition;
            if (probe.changeEstimateScale) {
                probe.estimateScale = 0;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> int raise(Throwable failure) throws T {
        throw (T) failure;
    }
}
