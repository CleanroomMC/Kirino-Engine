package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.cleanroommc.kirino.ecs.api.CleanECSRuntime.State.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Timeout(15)
class RuntimeLifecycleApiTest {
    @TempDir Path home;

    @Test
    void close_discardsPendingCommandsAndDestroysCommittedEntitiesExactlyOnce() throws Exception {
        CleanECSRuntime runtime = runtime();
        AtomicInteger creates = new AtomicInteger();
        AtomicInteger destroys = new AtomicInteger();
        var live = runtime.entityManager.createEntity(context -> {
            assertEquals(7, context.getComponent(RuntimeApiTest.Value.class).value);
            destroys.incrementAndGet();
            assertThrows(IllegalStateException.class, runtime::close);
        }, null, value(7));
        runtime.entityManager.flush();
        live.trySetComponent(value(99));
        live.tryDestroy();
        var reserved = runtime.entityManager.createEntity(context -> fail("Reserved entity has no committed lifetime"),
            context -> creates.incrementAndGet(), value(1));
        runtime.close();
        assertEquals(CLOSED, runtime.state());
        assertFalse(live.valid());
        assertFalse(reserved.valid());
        assertEquals(0, creates.get());
        assertEquals(1, destroys.get());
        runtime.close();
        assertEquals(1, destroys.get());
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        assertThrows(IllegalStateException.class, runtime::reset);
    }

    @Test
    void reset_invalidatesOldHandlesAndFlowsAndPreservesRegistrations() throws Exception {
        CleanECSRuntime runtime = runtime();
        var old = runtime.entityManager.createEntity(value(1));
        runtime.entityManager.flush();
        AtomicInteger updates = new AtomicInteger();
        var oldFlow = flow(runtime, new System(() -> updates.incrementAndGet()));
        oldFlow.execute();
        runtime.reset();
        assertEquals(RUNNING, runtime.state());
        var replacement = runtime.entityManager.createEntity(value(2));
        runtime.entityManager.flush();
        assertFalse(old.valid());
        assertFalse(old.tryDestroy());
        assertFalse(old.contains(RuntimeApiTest.Value.class));
        assertTrue(old.readComponent(RuntimeApiTest.Value.class).isEmpty());
        assertThrows(IllegalStateException.class, oldFlow::execute);
        assertFalse(oldFlow.isExecuting());
        flow(runtime, new System(() -> updates.incrementAndGet())).execute();
        runtime.jobScheduler.executeParallelJob(runtime.entityManager, RuntimeApiTest.IncrementJob.class,
            null, Runnable::run).future().join();
        assertEquals(3, replacement.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        runtime.reset();
        var third = runtime.entityManager.createEntity(value(3));
        assertFalse(old.valid());
        assertFalse(replacement.valid());
        assertTrue(third.valid());
        assertEquals(2, updates.get());
        runtime.close();
    }

    @Test
    void quiesce_ignoresObserverCancellationAndLetsAcceptedWritersFinish() throws Exception {
        CleanECSRuntime runtime = runtime();
        QueueExecutor workers = new QueueExecutor();
        AtomicInteger destroys = new AtomicInteger();
        for (int i = 0; i < 2; i++) {
            runtime.entityManager.createEntity(context -> {
                assertEquals(1, context.getComponent(RuntimeApiTest.Value.class).value);
                destroys.incrementAndGet();
            }, null, value(0));
        }
        runtime.entityManager.flush();
        Probe probe = new Probe();
        AtomicInteger pendingCreates = new AtomicInteger();
        probe.action = () -> probe.commands.get().createEntity(null, context -> pendingCreates.incrementAndGet(), value(4));
        var handle = launch(runtime, workers, probe);
        assumeTrue(handle.async());
        handle.future().cancel(true);
        var cancelled = runtime.quiesce();
        cancelled.cancel(true);
        var manuallyCompleted = runtime.quiesce();
        manuallyCompleted.complete(null);
        var observer = runtime.quiesce();
        assertEquals(QUIESCING, runtime.state());
        assertFalse(observer.isDone());
        assertThrows(IllegalStateException.class, () -> runtime.entityManager.createEntity(value(2)));
        assertThrows(IllegalStateException.class, () -> launch(runtime, workers, probe));
        workers.runNext();
        assertFalse(observer.isDone());
        workers.drain();
        observer.join();
        assertEquals(QUIESCENT, runtime.state());
        runtime.close();
        assertEquals(2, destroys.get());
        assertEquals(0, pendingCreates.get());
        assertThrows(IllegalStateException.class, () -> probe.commands.get().createEntity(value(1)));
    }

    @Test
    void queuedFlow_isCountedAndFailsBeforeCallingUserCodeAfterStop() throws Exception {
        CleanECSRuntime runtime = runtime();
        QueueExecutor queue = new QueueExecutor();
        AtomicInteger updates = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        var graph = SingleFlow.newBuilder(new World(runtime), System.class)
            .addTransition(new System(updates::incrementAndGet), SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE)
            .setStartNodeCallback(updates::incrementAndGet)
            .setFinishCallback(() -> fail("Stopped graph completed successfully"))
            .setFailureCallback(failure -> failures.incrementAndGet()).build();
        var result = graph.executeAsync(queue);
        result.cancel(false);
        var stopped = runtime.quiesce();
        assertFalse(stopped.isDone());
        queue.drain();
        assertThrows(CompletionException.class, stopped::join);
        assertFalse(graph.isExecuting());
        assertEquals(0, updates.get());
        assertEquals(1, failures.get());
        assertThrows(IllegalStateException.class, runtime::close);
        assertEquals(CLOSED, runtime.state());
    }

    @Test
    void quiesce_waitsForExternalSystemWorkAndTerminalCallback() throws Exception {
        CleanECSRuntime runtime = runtime();
        CompletableFuture<Void> external = new CompletableFuture<>();
        AtomicInteger failures = new AtomicInteger();
        System system = new System(() -> {});
        system.external = external;
        var graph = SingleFlow.newBuilder(new World(runtime), System.class)
            .addTransition(system, SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE)
            .setFailureCallback(failure -> failures.incrementAndGet()).build();
        graph.executeAsync(Runnable::run);
        var stopped = runtime.quiesce();
        assertFalse(stopped.isDone());
        external.complete(null);
        assertThrows(CompletionException.class, stopped::join);
        assertEquals(1, failures.get());
        assertEquals(QUIESCENT, runtime.state());
        assertThrows(IllegalStateException.class, runtime::close);
    }

    @Test
    void close_waitsForJobsAndRunsDestroyCallbacksOnClosingThread() throws Exception {
        CleanECSRuntime runtime = runtime();
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger destroys = new AtomicInteger();
        AtomicReference<Thread> closingThread = new AtomicReference<>();
        for (int i = 0; i < 2; i++) {
            runtime.entityManager.createEntity(context -> {
                assertSame(closingThread.get(), Thread.currentThread());
                destroys.incrementAndGet();
            }, null, value(0));
        }
        runtime.entityManager.flush();
        var workers = Executors.newFixedThreadPool(2);
        var closer = Executors.newSingleThreadExecutor();
        try {
            Probe probe = new Probe();
            probe.action = () -> {
                entered.countDown();
                await(release);
            };
            launch(runtime, workers, probe);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            var stopped = runtime.quiesce();
            var closed = closer.submit(() -> {
                closingThread.set(Thread.currentThread());
                runtime.close();
            });
            assertFalse(stopped.isDone());
            assertFalse(closed.isDone());
            assertEquals(0, destroys.get());
            release.countDown();
            closed.get(5, TimeUnit.SECONDS);
            assertEquals(2, destroys.get());
            assertEquals(CLOSED, runtime.state());
        } finally {
            release.countDown();
            workers.shutdownNow();
            closer.shutdownNow();
            assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            assertTrue(closer.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void ownExecution_cannotSynchronouslyCloseOrResetItsRuntime() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        Probe probe = new Probe();
        probe.action = () -> {
            assertThrows(IllegalStateException.class, runtime::close);
            assertThrows(IllegalStateException.class, runtime::reset);
            assertEquals(RUNNING, runtime.state());
        };
        launch(runtime, Runnable::run, probe).future().join();
        flow(runtime, new System(probe.action)).execute();
        var graph = SingleFlow.newBuilder(new World(runtime), System.class)
            .addTransition(new System(() -> {}), SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE)
            .setFinishCallback(probe.action).build();
        graph.execute();
        runtime.close();
    }

    @Test
    void entityCallback_cannotStartLifecycleTransitionDuringCommit() throws Exception {
        CleanECSRuntime runtime = runtime();
        runtime.entityManager.createEntity(null, context -> {
            assertThrows(IllegalStateException.class, runtime::quiesce);
            assertThrows(IllegalStateException.class, runtime::close);
            assertThrows(IllegalStateException.class, runtime::reset);
        }, value(1));
        runtime.entityManager.flush();
        assertEquals(RUNNING, runtime.state());
        runtime.close();
    }

    @Test
    void completionObserver_canCloseRuntimeWithoutWaitingForItsOwnLease() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        QueueExecutor queue = new QueueExecutor();
        var result = launch(runtime, queue, new Probe()).future().thenRun(runtime::close);
        queue.drain();
        result.join();
        assertEquals(CLOSED, runtime.state());
    }

    @Test
    void flowCompletionObserver_canCloseRuntime() throws Exception {
        CleanECSRuntime runtime = runtime();
        QueueExecutor queue = new QueueExecutor();
        var result = flow(runtime, new System(() -> {})).executeAsync(queue).thenRun(runtime::close);
        queue.drain();
        result.join();
        assertEquals(CLOSED, runtime.state());
    }

    @Test
    void cleanupErrors_areAggregatedAllEntitiesAreReleasedAndCallbacksAreNotReplayed() throws Exception {
        CleanECSRuntime runtime = runtime();
        IllegalStateException first = new IllegalStateException("first cleanup");
        AssertionError second = new AssertionError("second cleanup");
        AtomicInteger attempts = new AtomicInteger();
        runtime.entityManager.createEntity(context -> {
            attempts.incrementAndGet();
            throw first;
        }, null, value(1));
        runtime.entityManager.createEntity(context -> {
            attempts.incrementAndGet();
            throw second;
        }, null, value(2));
        runtime.entityManager.flush();
        assertSame(first, assertThrows(IllegalStateException.class, runtime::close));
        assertArrayEquals(new Throwable[]{second}, first.getSuppressed());
        assertEquals(2, attempts.get());
        assertEquals(CLOSED, runtime.state());
        assertSame(first, assertThrows(IllegalStateException.class, runtime::close));
        assertEquals(2, attempts.get());
    }

    @Test
    void failedJobsDuringDrain_areReportedWhileSuccessfulResetCreatesEmptyEpoch() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        QueueExecutor workers = new QueueExecutor();
        Probe probe = new Probe();
        IllegalArgumentException error = new IllegalArgumentException("worker");
        probe.action = () -> { throw error; };
        launch(runtime, workers, probe);
        var drained = runtime.quiesce();
        workers.drain();
        assertSame(error, assertThrows(CompletionException.class, drained::join).getCause());
        assertSame(error, assertThrows(IllegalArgumentException.class, runtime::reset));
        assertEquals(RUNNING, runtime.state());
        var next = runtime.entityManager.createEntity(value(9));
        runtime.entityManager.flush();
        assertEquals(9, next.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        runtime.close();
    }

    @Test
    void failedCommit_canBeResetWithoutReplayingUncommittedCallbacks() throws Exception {
        CleanECSRuntime runtime = runtime();
        AtomicInteger creates = new AtomicInteger();
        var old = runtime.entityManager.createEntity(null, context -> {
            creates.incrementAndGet();
            throw new IllegalStateException("create failed");
        }, value(1));
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        runtime.reset();
        assertEquals(RUNNING, runtime.state());
        assertFalse(old.valid());
        assertEquals(1, creates.get());
        var next = runtime.entityManager.createEntity(value(2));
        runtime.entityManager.flush();
        assertTrue(next.isAlive());
        runtime.close();
    }

    @Test
    void cleanupFailureDuringReset_keepsRuntimeClosed() throws Exception {
        CleanECSRuntime runtime = runtime();
        runtime.entityManager.createEntity(context -> { throw new IllegalStateException("cleanup"); }, null, value(1));
        runtime.entityManager.flush();
        assertThrows(IllegalStateException.class, runtime::reset);
        assertEquals(CLOSED, runtime.state());
        assertThrows(IllegalStateException.class, () -> runtime.entityManager.createEntity(value(2)));
    }

    @Test
    void destroyCallbackThatAlreadyFailedDuringFlush_isNotReplayedByReset() throws Exception {
        CleanECSRuntime runtime = runtime();
        AtomicInteger calls = new AtomicInteger();
        var handle = runtime.entityManager.createEntity(context -> {
            calls.incrementAndGet();
            throw new IllegalStateException("destroy failed");
        }, null, value(1));
        runtime.entityManager.flush();
        handle.tryDestroy();
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        runtime.reset();
        assertEquals(1, calls.get());
        assertEquals(RUNNING, runtime.state());
        runtime.close();
        assertEquals(1, calls.get());
    }

    @Test
    void nestedRuntimeExecution_cannotWaitForItsOuterRuntime() throws Exception {
        CleanECSRuntime outer = runtime();
        CleanECSRuntime inner = runtime();
        var innerFlow = flow(inner, new System(() -> {
            assertThrows(IllegalStateException.class, outer::close);
            assertThrows(IllegalStateException.class, outer::reset);
            assertThrows(IllegalStateException.class, inner::close);
        }));
        flow(outer, new System(innerFlow::execute)).execute();
        assertEquals(RUNNING, outer.state());
        assertEquals(RUNNING, inner.state());
        outer.close();
        inner.close();
    }

    @Test
    void overlappingJobAndFlowFailures_areAggregatedWithoutCircularSuppression() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        QueueExecutor workers = new QueueExecutor();
        IllegalStateException systemFailure = new IllegalStateException("system");
        IllegalArgumentException jobFailure = new IllegalArgumentException("job");
        Probe probe = new Probe();
        probe.action = () -> { throw jobFailure; };
        var graph = flow(runtime, new System(() -> {
            launch(runtime, workers, probe);
            throw systemFailure;
        }));
        var result = graph.executeAsync(Runnable::run);
        var stopped = runtime.quiesce();
        workers.drain();
        assertSame(systemFailure, assertThrows(CompletionException.class, result::join).getCause());
        assertSame(systemFailure, assertThrows(CompletionException.class, stopped::join).getCause());
        assertArrayEquals(new Throwable[]{jobFailure}, systemFailure.getSuppressed());
        assertEquals(0, jobFailure.getSuppressed().length);
        assertSame(systemFailure, assertThrows(IllegalStateException.class, runtime::close));
    }

    @Test
    void partialSubmissionRejection_isDrainedWithoutReturnedHandle() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        QueueExecutor queue = new QueueExecutor();
        RejectedExecutionException rejected = new RejectedExecutionException("second task");
        Executor partial = task -> {
            if (queue.tasks.isEmpty()) {
                queue.execute(task);
            } else {
                throw rejected;
            }
        };
        assertSame(rejected, assertThrows(RejectedExecutionException.class, () -> launch(runtime, partial, new Probe())));
        var drained = runtime.quiesce();
        assertFalse(drained.isDone());
        queue.drain();
        assertSame(rejected, assertThrows(CompletionException.class, drained::join).getCause());
        assertSame(rejected, assertThrows(RejectedExecutionException.class, runtime::close));
        assertEquals(CLOSED, runtime.state());
    }

    @Test
    void rejectedInitialFlowSubmission_releasesRuntimeAdmission() throws Exception {
        CleanECSRuntime runtime = runtime();
        var graph = flow(runtime, new System(() -> {}));
        assertThrows(RejectedExecutionException.class, () -> graph.executeAsync(task -> { throw new RejectedExecutionException(); }));
        assertTrue(runtime.quiesce().isDone());
        runtime.close();
    }

    @Test
    void runtimeScheduler_rejectsForeignManagerBeforeCallingUserJobCode() throws Exception {
        CleanECSRuntime first = runtime();
        CleanECSRuntime second = runtime();
        assertThrows(IllegalArgumentException.class, () -> first.jobScheduler.executeParallelJob(
            second.entityManager, RuntimeApiTest.IncrementJob.class, null, Runnable::run));
        first.close();
        second.close();
    }

    @Test
    void preparationBeforeScopeAcquisition_isStillIncludedInQuiescence() throws Exception {
        CleanECSRuntime runtime = runtime();
        CountDownLatch queryEntered = new CountDownLatch(1);
        CountDownLatch releaseQuery = new CountDownLatch(1);
        BlockingQueryJob.action = () -> {
            queryEntered.countDown();
            await(releaseQuery);
        };
        var submitter = Executors.newSingleThreadExecutor();
        try {
            var submitted = submitter.submit(() -> runtime.jobScheduler.executeParallelJob(runtime.entityManager,
                BlockingQueryJob.class, null, Runnable::run));
            assertTrue(queryEntered.await(5, TimeUnit.SECONDS));
            var stopped = runtime.quiesce();
            assertFalse(stopped.isDone());
            assertEquals(QUIESCING, runtime.state());
            releaseQuery.countDown();
            assertInstanceOf(IllegalStateException.class,
                assertThrows(java.util.concurrent.ExecutionException.class, () -> submitted.get(5, TimeUnit.SECONDS)).getCause());
            assertThrows(CompletionException.class, stopped::join);
            assertEquals(QUIESCENT, runtime.state());
            assertThrows(IllegalStateException.class, runtime::close);
            assertEquals(CLOSED, runtime.state());
        } finally {
            releaseQuery.countDown();
            BlockingQueryJob.action = () -> {};
            submitter.shutdownNow();
            assertTrue(submitter.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void concurrentClose_callsCleanUpEachEntityOnce() throws Exception {
        CleanECSRuntime runtime = runtime();
        rows(runtime);
        QueueExecutor workers = new QueueExecutor();
        launch(runtime, workers, new Probe());
        var closers = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        try {
            var first = closers.submit(() -> { ready.countDown(); runtime.close(); });
            var second = closers.submit(() -> { ready.countDown(); runtime.close(); });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            assertFalse(first.isDone());
            assertFalse(second.isDone());
            workers.drain();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
            assertEquals(CLOSED, runtime.state());
        } finally {
            workers.drain();
            closers.shutdownNow();
            assertTrue(closers.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    private CleanECSRuntime runtime() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        return RuntimeApiTest.runtime(home, Job.class, BlockingQueryJob.class);
    }

    private static void rows(CleanECSRuntime runtime) {
        runtime.entityManager.createEntity(value(0));
        runtime.entityManager.createEntity(value(0));
        runtime.entityManager.flush();
    }

    private static RuntimeApiTest.Value value(int number) {
        var value = new RuntimeApiTest.Value();
        value.value = number;
        return value;
    }

    private static JobScheduler.ExecutionHandle launch(CleanECSRuntime runtime, Executor executor, Probe probe) {
        return runtime.jobScheduler.executeParallelJob(runtime.entityManager, Job.class, Map.of("probe", probe), executor);
    }

    private static SingleFlow<System> flow(CleanECSRuntime runtime, System system) {
        return SingleFlow.newBuilder(new World(runtime), System.class)
            .addTransition(system, SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE).build();
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interrupted);
        }
    }

    private static final class QueueExecutor implements Executor {
        final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        @Override public void execute(Runnable task) { tasks.addLast(task); }
        void runNext() { tasks.removeFirst().run(); }
        void drain() { while (!tasks.isEmpty()) { runNext(); } }
    }

    private static final class World extends CleanWorld {
        World(CleanECSRuntime runtime) { super(runtime.entityManager, runtime.jobScheduler); }
        @Override public void update() { flushECS(); }
    }

    private static final class System extends CleanSystem {
        final Runnable action;
        CompletableFuture<Void> external;
        System(Runnable action) { this.action = action; }
        @Override public void update(EntityManager manager, JobScheduler scheduler) {
            action.run();
            if (external != null) {
                execution.updateExecutions(new CompletableFuture[]{external});
            }
        }
    }

    public static final class Probe {
        final AtomicReference<EntityCommands> commands = new AtomicReference<>();
        Runnable action = () -> {};
    }

    public static class Job implements ParallelJob {
        @JobDataQuery(componentClass = RuntimeApiTest.Value.class, fieldAccessChain = "value")
        private PrimitiveArray values;
        @JobExternalDataQuery private Probe probe;
        @Override public void query(EntityQuery query) { query.with(RuntimeApiTest.Value.class); }
        @Override public int estimateWorkload(int index) { return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread; }
        @Override public void execute(EntityCommands commands, int index, int entityID, int ordinal) {
            probe.commands.set(commands);
            probe.action.run();
            values.setInt(index, values.getInt(index) + 1);
        }
    }

    public static class BlockingQueryJob extends RuntimeApiTest.IncrementJob {
        static volatile Runnable action = () -> {};
        @Override public void query(EntityQuery query) {
            action.run();
            super.query(query);
        }
    }
}
