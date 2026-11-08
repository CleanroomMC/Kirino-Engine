package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.*;
import com.cleanroommc.kirino.ecs.storage.*;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class PoolExecutionTest {
    @TempDir Path home;

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
        assertEquals(3, fixture.column.length());
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

    private Fixture fixture() throws Exception {
        var field = FMLInjectionData.class.getDeclaredField("minecraftHome"); field.setAccessible(true);
        Object previous = field.get(null); File previousLaunch = Launch.minecraftHome;
        try {
            field.set(null, home.toFile()); Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            field.set(null, previous); Launch.minecraftHome = previousLaunch;
        }
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT)); fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("value"), "float"); registry.lock();
        HeapPool pool = new HeapPool(registry, List.of(Value.class), 1, 2, 1);
        pool.addEntity(1, new CleanComponent[]{new Value()}); pool.addEntity(2, new CleanComponent[]{new Value()});
        EntityManager entities = new EntityManager(registry) {
            @Override public List<ArchetypeDataPool> startQuery(EntityQuery query) { return List.of(pool); }
        };
        JobRegistry jobs = new JobRegistry(registry);
        jobs.registerParallelJob(Job.class); jobs.registerParallelJob(ThrowingJob.class); jobs.registerParallelJob(InvalidJob.class);
        jobs.registerParallelJob(CheckedWorkloadJob.class); jobs.registerParallelJob(CheckedTaskJob.class);
        return new Fixture(pool, entities, new JobScheduler(jobs), pool.getArray(Value.class, "value"));
    }

    private record Fixture(HeapPool pool, EntityManager entities, JobScheduler scheduler, PrimitiveArray column) {}
    public static class Value implements CleanComponent { public float value; }
    public static class Job implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = {"value"}) public PrimitiveArray values;
        public void execute(EntityManager manager, int index, int entity, int thread) { values.setFloat(index, values.getFloat(index) + 1); }
        public void query(EntityQuery query) {}
        public int estimateWorkload(int index) { return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread; }
    }
    public static class ThrowingJob extends Job {
        @Override public void execute(EntityManager manager, int index, int entity, int thread) { throw new IllegalStateException("expected"); }
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
        public void execute(EntityManager manager, int index, int entity, int thread) {
        }
    }

    public static class CheckedTaskJob extends Job {
        @Override
        public void execute(EntityManager manager, int index, int entity, int thread) {
            PoolExecutionTest.<RuntimeException>raise(new Exception("checked task failure"));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> int raise(Throwable failure) throws T {
        throw (T) failure;
    }
}
