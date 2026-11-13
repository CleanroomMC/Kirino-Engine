package com.cleanroommc.kirino.engine.render.usage;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.ecs.apiconsumer.RuntimeApiTest;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class WorldSceneRuntimeTest {
    @TempDir Path home;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void switchWorld_cancelledJobObserverStillDrainsBeforeResetWithoutTouchingParent(boolean gpuCompletesFirst)
        throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        CleanECSRuntime parent = RuntimeApiTest.runtime(home, SceneJob.class);
        CleanECSRuntime scene = parent.newSession();
        var parentEntity = parent.entityManager.createEntity(new RuntimeApiTest.Value());
        parent.entityManager.flush();
        Backend backend = new Backend(scene);
        WorldSceneLifecycle<Object> lifecycle = new WorldSceneLifecycle<>(backend);
        QueueExecutor workers = new QueueExecutor();
        try {
            Object first = new Object();
            Object next = new Object();
            lifecycle.update(first);
            var oldFlow = backend.flow;
            List<CleanEntityHandle> oldEntities = List.copyOf(backend.entities);
            var execution = scene.jobScheduler.executeParallelJob(scene.entityManager, SceneJob.class, null, workers);
            assertTrue(execution.async());
            execution.future().cancel(true);

            lifecycle.update(next);
            assertEquals(CleanECSRuntime.State.QUIESCING, scene.state());
            assertEquals(0, backend.resets);
            assertTrue(backend.destroyedValues.isEmpty());
            assertEquals(1, backend.attachments);
            assertTrue(parentEntity.isAlive());
            assertEquals(CleanECSRuntime.State.RUNNING, parent.state());

            if (gpuCompletesFirst) {
                backend.gpuReady = true;
            }
            workers.runNext();
            lifecycle.update(next);
            assertEquals(0, backend.resets);
            assertTrue(backend.destroyedValues.isEmpty());
            workers.drain();
            assertEquals(CleanECSRuntime.State.QUIESCENT, scene.state());
            if (!gpuCompletesFirst) {
                lifecycle.update(next);
                assertEquals(0, backend.resets);
                backend.gpuReady = true;
            }
            lifecycle.update(next);

            assertEquals(List.of(1, 1), backend.destroyedValues);
            assertEquals(1, backend.resets);
            assertEquals(2, backend.attachments);
            assertSame(next, backend.world);
            assertEquals(2, backend.updates);
            assertThrows(IllegalStateException.class, oldFlow::execute);
            oldEntities.forEach(entity -> assertFalse(entity.valid()));
            backend.entities.forEach(entity -> assertTrue(entity.isAlive()));
            assertTrue(parentEntity.isAlive());
            assertEquals(0, parentEntity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        } finally {
            workers.drain();
            backend.gpuReady = true;
            lifecycle.close();
            parent.close();
        }
    }

    private static final class Backend implements WorldSceneLifecycle.Backend<Object> {
        final CleanECSRuntime runtime;
        final Thread owner = Thread.currentThread();
        final List<CleanEntityHandle> entities = new ArrayList<>();
        final List<Integer> destroyedValues = new ArrayList<>();
        SingleFlow<Step> flow;
        Object world;
        boolean gpuReady;
        int resets;
        int attachments;
        int updates;

        Backend(CleanECSRuntime runtime) { this.runtime = runtime; }

        public void attach(Object world) {
            this.world = world;
            attachments++;
            for (int i = 0; i < 2; i++) {
                entities.add(runtime.entityManager.createEntity(context -> {
                    assertSame(owner, Thread.currentThread());
                    destroyedValues.add(context.getComponent(RuntimeApiTest.Value.class).value);
                }, null, new RuntimeApiTest.Value()));
            }
            runtime.entityManager.flush();
            flow = SingleFlow.newBuilder(new World(runtime), Step.class)
                .addTransition(new Step(() -> updates++), SingleFlow.START_NODE, SingleFlow.END_NODE).build();
        }

        public void detach() { world = null; }
        public CompletableFuture<Void> quiesce() { return runtime.quiesce(); }
        public void beginGpuDrain() { gpuReady = false; }
        public boolean gpuReady() { return gpuReady; }
        public void awaitGpu() { gpuReady = true; }
        public void reset() {
            runtime.reset();
            entities.clear();
            resets++;
        }
        public void close() { runtime.close(); }
        public void update() { flow.execute(); }
    }

    private static final class QueueExecutor implements Executor {
        final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        public void execute(Runnable task) { tasks.addLast(task); }
        void runNext() { tasks.removeFirst().run(); }
        void drain() { while (!tasks.isEmpty()) { runNext(); } }
    }

    private static final class World extends CleanWorld {
        World(CleanECSRuntime runtime) { super(runtime.entityManager, runtime.jobScheduler); }
        public void update() { flushECS(); }
    }

    private static final class Step extends CleanSystem {
        final Runnable action;
        Step(Runnable action) { this.action = action; }
        public void update(EntityManager manager, JobScheduler scheduler) { action.run(); }
    }

    public static class SceneJob extends RuntimeApiTest.IncrementJob {
        public int estimateWorkload(int index) { return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread; }
    }
}
