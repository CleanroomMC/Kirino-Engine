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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Timeout(15)
class FlowExecutionApiTest {
    @TempDir Path home;

    @Test
    void failedWorker_waitsForOtherWorkersAndSkipsDownstreamCallbacks() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        RuntimeException first = new IllegalStateException("first worker");
        AssertionError second = new AssertionError("second worker");
        List<String> events = new ArrayList<>();
        TaskSystem system = new TaskSystem((self, entities, scheduler) ->
            launch(scheduler, entities, workers, index -> {
                if (index == 0) throw first;
                throw second;
            }));
        var flow = builder(runtime, system)
            .setEndNodeCallback(() -> events.add("end"))
            .setFinishCallback(() -> events.add("success"))
            .setFailureCallback(failure -> {
                assertSame(first, failure);
                runtime.entityManager.flush();
                events.add("failure");
            }).build();
        var result = flow.executeAsync(Runnable::run);
        assertEquals(2, workers.pending.size());
        workers.runNext();
        assertPending(flow, result);
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        assertTrue(events.isEmpty());
        workers.runNext();
        assertSame(first, failure(result));
        assertArrayEquals(new Throwable[]{second}, first.getSuppressed());
        assertEquals(List.of("failure"), events);
        assertFalse(flow.isExecuting());
    }

    @Test
    void systemThrowsBeforeRegisteringHandle_drainsAutomaticallyTrackedJobs() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        IllegalArgumentException primary = new IllegalArgumentException("system");
        IllegalStateException worker = new IllegalStateException("worker");
        IllegalStateException callback = new IllegalStateException("callback");
        AtomicInteger failures = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            launch(scheduler, entities, workers, index -> { if (index == 0) throw worker; });
            throw primary;
        });
        var flow = builder(runtime, system).setFinishCallback(() -> fail("Unexpected success"))
            .setFailureCallback(failure -> {
                assertSame(primary, failure);
                failures.incrementAndGet();
                throw callback;
            }).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        workers.runNext();
        assertPending(flow, result);
        workers.drain();
        assertSame(primary, failure(result));
        assertArrayEquals(new Throwable[]{worker, callback}, primary.getSuppressed());
        assertEquals(1, failures.get());
        assertFalse(flow.isExecuting());
        runtime.entityManager.flush();
    }

    @Test
    void partialJobRejection_withoutReturnedHandle_keepsAcceptedWorkInFlow() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        RejectedExecutionException rejected = new RejectedExecutionException("second worker");
        Executor partial = task -> {
            if (workers.pending.isEmpty()) workers.execute(task);
            else throw rejected;
        };
        AtomicInteger finished = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) ->
            launch(scheduler, entities, partial, index -> finished.incrementAndGet()));
        var flow = builder(runtime, system).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        assertEquals(1, workers.pending.size());
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        workers.drain();
        assertSame(rejected, failure(result));
        assertEquals(1, finished.get());
        assertFalse(flow.isExecuting());
        runtime.entityManager.flush();
    }

    @Test
    void repeatedRegistrations_appendAndNoExecutionsDoesNotEraseWork() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> first = new CompletableFuture<>();
        CompletableFuture<Void> second = new CompletableFuture<>();
        IllegalStateException primary = new IllegalStateException("first");
        IllegalArgumentException secondary = new IllegalArgumentException("second");
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            self.register(first);
            self.register(new JobScheduler.ExecutionHandle(second, 1, false));
            self.clear();
        });
        var flow = builder(runtime, system).build();
        var result = flow.executeAsync(Runnable::run);
        first.completeExceptionally(primary);
        assertPending(flow, result);
        second.completeExceptionally(secondary);
        assertSame(primary, failure(result));
        assertArrayEquals(new Throwable[]{secondary}, primary.getSuppressed());
        assertFalse(flow.isExecuting());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void modifyingJobObserver_doesNotHideActualFailureOrReleaseFlow(boolean cancel) throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        IllegalStateException actualFailure = new IllegalStateException("actual worker");
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            var handle = launch(scheduler, entities, workers, index -> { throw actualFailure; });
            if (cancel) assertTrue(handle.future().cancel(true));
            else assertTrue(handle.future().complete(null));
            self.register(handle);
        });
        var flow = builder(runtime, system).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        workers.runNext();
        assertPending(flow, result);
        workers.drain();
        assertSame(actualFailure, failure(result));
        assertFalse(flow.isExecuting());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void modifyingFlowObserverBeforeQueuedStart_doesNotSkipActualExecution(boolean cancel) throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor orchestration = new QueueExecutor();
        QueueExecutor workers = new QueueExecutor();
        AtomicInteger executed = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) ->
            launch(scheduler, entities, workers, index -> executed.incrementAndGet()));
        var flow = builder(runtime, system).setFinishCallback(finished::incrementAndGet).build();
        var observer = flow.executeAsync(orchestration);
        if (cancel) assertTrue(observer.cancel(true));
        else assertTrue(observer.complete(null));
        assertTrue(flow.isExecuting());
        orchestration.drain();
        assertTrue(flow.isExecuting());
        assertThrows(IllegalStateException.class, runtime.entityManager::flush);
        workers.drain();
        orchestration.drain();
        assertFalse(flow.isExecuting());
        assertEquals(2, executed.get());
        assertEquals(1, finished.get());
        runtime.entityManager.flush();
        flow.executeAsync(Runnable::run);
        workers.drain();
        assertFalse(flow.isExecuting());
        assertEquals(2, finished.get());
    }

    @Test
    void sameSingleThreadExecutor_runsFlowAndItsJobsWithoutBlocking() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        AtomicInteger executed = new AtomicInteger();
        AtomicInteger finished = new AtomicInteger();
        var executor = Executors.newSingleThreadExecutor();
        try {
            TaskSystem system = new TaskSystem((self, entities, scheduler) ->
                launch(scheduler, entities, executor, index -> executed.incrementAndGet()));
            var flow = builder(runtime, system).setFinishCallback(finished::incrementAndGet).build();
            flow.executeAsync(executor).get(5, TimeUnit.SECONDS);
            assertFalse(flow.isExecuting());
            assertEquals(2, executed.get());
            assertEquals(1, finished.get());
            flow.executeAsync(executor).get(5, TimeUnit.SECONDS);
            assertEquals(4, executed.get());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void synchronousFailure_waitsForRunningJobsBeforeThrowingOriginalError() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        CountDownLatch entered = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch updateFailed = new CountDownLatch(1);
        AssertionError original = new AssertionError("system");
        var workers = Executors.newFixedThreadPool(2);
        var caller = Executors.newSingleThreadExecutor();
        try {
            TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
                launch(scheduler, entities, workers, index -> {
                    entered.countDown();
                    await(release);
                });
                updateFailed.countDown();
                throw original;
            });
            var flow = builder(runtime, system).build();
            var result = caller.submit(() -> assertSame(original, assertThrows(AssertionError.class, flow::execute)));
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertTrue(updateFailed.await(5, TimeUnit.SECONDS));
            assertTrue(flow.isExecuting());
            assertFalse(result.isDone());
            assertThrows(IllegalStateException.class, runtime.entityManager::flush);
            release.countDown();
            result.get(5, TimeUnit.SECONDS);
            assertFalse(flow.isExecuting());
        } finally {
            release.countDown();
            workers.shutdownNow();
            caller.shutdownNow();
            assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
            assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void sharedSystem_cannotBeEnteredByAnotherFlowWhileFirstOwnsIt() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        AtomicInteger updates = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            updates.incrementAndGet();
            self.register(task);
        });
        var first = builder(runtime, system).build();
        var second = builder(runtime, system).build();
        var pending = first.executeAsync(Runnable::run);
        assertThrows(IllegalStateException.class, second::execute);
        assertPending(first, pending);
        assertFalse(second.isExecuting());
        assertEquals(1, updates.get());
        task.complete(null);
        pending.join();
        second.execute();
        assertEquals(2, updates.get());
    }

    @Test
    void activeRegistration_rejectsOtherThreadsAndLateChanges() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            self.register(task);
            entered.countDown();
            await(release);
        });
        var flow = builder(runtime, system).build();
        try {
            var result = flow.executeAsync(executor);
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertThrows(IllegalStateException.class, system::clear);
            assertThrows(IllegalStateException.class, () -> system.register(CompletableFuture.completedFuture(null)));
            release.countDown();
            executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
            assertPending(flow, result);
            assertThrows(IllegalStateException.class, system::clear);
            task.complete(null);
            result.get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            task.complete(null);
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void rejectedDownstreamStage_reportsFailureAfterAcceptedJobsFinish() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        AtomicInteger stages = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        RejectedExecutionException rejected = new RejectedExecutionException("end stage");
        Executor orchestration = task -> {
            if (stages.incrementAndGet() <= 2) task.run();
            else throw rejected;
        };
        TaskSystem system = new TaskSystem((self, entities, scheduler) ->
            launch(scheduler, entities, workers, index -> {}));
        var flow = builder(runtime, system).setEndNodeCallback(() -> fail("Rejected end ran"))
            .setFailureCallback(actual -> {
                assertSame(rejected, actual);
                failures.incrementAndGet();
            }).build();
        var result = flow.executeAsync(orchestration);
        workers.runNext();
        assertPending(flow, result);
        workers.drain();
        assertSame(rejected, failure(result));
        assertEquals(1, failures.get());
        assertFalse(flow.isExecuting());
        runtime.entityManager.flush();
    }

    @Test
    void callbackLifecycle_keepsGuardDuringCallbackAndReleasesBeforePublishingCompletion() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> self.register(task));
        AtomicReference<SingleFlow<TaskSystem>> reference = new AtomicReference<>();
        var flow = builder(runtime, system).setFinishCallback(() -> {
            assertTrue(reference.get().isExecuting());
            assertThrows(IllegalStateException.class, reference.get()::execute);
            assertThrows(IllegalStateException.class, system::clear);
        }).build();
        reference.set(flow);
        var result = flow.executeAsync(Runnable::run);
        var observed = result.thenRun(() -> {
            assertFalse(flow.isExecuting());
            system.clear();
        });
        task.complete(null);
        observed.join();
        flow.execute();
    }

    @Test
    void callbackErrors_preservePrimaryAndDoNotPoisonNextRun() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        IllegalStateException primary = new IllegalStateException("finish");
        AssertionError secondary = new AssertionError("failure handler");
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {});
        var flow = builder(runtime, system).setFinishCallback(() -> {
            if (successes.getAndIncrement() == 0) throw primary;
        }).setFailureCallback(actual -> {
            assertSame(primary, actual);
            failures.incrementAndGet();
            throw secondary;
        }).build();
        assertSame(primary, failure(flow.executeAsync(Runnable::run)));
        assertArrayEquals(new Throwable[]{secondary}, primary.getSuppressed());
        assertFalse(flow.isExecuting());
        flow.execute();
        assertEquals(2, successes.get());
        assertEquals(1, failures.get());
    }

    @Test
    void checkedSystemFailure_isReportedAndSystemCanRunAgain() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        Exception primary = new Exception("checked");
        AtomicInteger updates = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            if (updates.getAndIncrement() == 0) sneakyThrow(primary);
        });
        var flow = builder(runtime, system).build();
        assertSame(primary, assertThrows(CompletionException.class, flow::execute).getCause());
        assertFalse(flow.isExecuting());
        flow.execute();
        assertEquals(2, updates.get());
    }

    @Test
    void builderReuse_snapshotsFailureCallback() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        List<String> events = new ArrayList<>();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> { throw new IllegalStateException(); });
        var builder = builder(runtime, system).setFailureCallback(failure -> events.add("first"));
        var first = builder.build();
        var second = builder.setFailureCallback(failure -> events.add("second")).build();
        assertThrows(IllegalStateException.class, first::execute);
        assertThrows(IllegalStateException.class, second::execute);
        assertEquals(List.of("first", "second"), events);
    }

    @Test
    void failedIndependentBarrier_drainsWorkStartedOnAnotherEdge() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        IllegalStateException primary = new IllegalStateException("barrier");
        AtomicInteger failures = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) ->
            launch(scheduler, entities, workers, index -> {}));
        var flow = builder(runtime, system)
            .addBarrierNode("independent", () -> { throw primary; })
            .addDummyTransition(SystemExeFlowGraph.START_NODE, "independent")
            .addDummyTransition("independent", SystemExeFlowGraph.END_NODE)
            .setFailureCallback(actual -> {
                assertSame(primary, actual);
                failures.incrementAndGet();
            }).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        assertEquals(0, failures.get());
        workers.drain();
        assertSame(primary, failure(result));
        assertEquals(1, failures.get());
        assertFalse(flow.isExecuting());
    }

    @Test
    void nestedFailingFlow_restoresOuterTrackingAndClearsItAfterUpdate() throws Exception {
        CleanECSRuntime runtime = runtimeWithRows();
        QueueExecutor workers = new QueueExecutor();
        IllegalStateException nestedFailure = new IllegalStateException("nested system");
        var nested = builder(runtime, new TaskSystem((self, entities, scheduler) -> { throw nestedFailure; })).build();
        AtomicInteger updates = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            if (updates.getAndIncrement() == 0) {
                assertSame(nestedFailure, failure(nested.executeAsync(Runnable::run)));
                launch(scheduler, entities, workers, index -> {});
            }
        });
        var flow = builder(runtime, system).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        workers.drain();
        result.join();
        var outside = launch(runtime.jobScheduler, runtime.entityManager, workers, index -> {});
        var next = flow.executeAsync(Runnable::run);
        assertTrue(next.isDone());
        next.join();
        assertFalse(outside.future().isDone());
        workers.drain();
        outside.future().join();
    }

    @Test
    void invalidRegistration_doesNotRemoveAcceptedTasksOrRetainCallerArray() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            CompletableFuture<Void>[] array = new CompletableFuture[]{task};
            self.register(array);
            array[0] = CompletableFuture.completedFuture(null);
            assertThrows(NullPointerException.class,
                () -> self.register(new JobScheduler.ExecutionHandle(null, 1, false)));
            assertThrows(NullPointerException.class,
                () -> self.register(new CompletableFuture[]{null}));
        });
        var flow = builder(runtime, system).build();
        var result = flow.executeAsync(Runnable::run);
        assertPending(flow, result);
        task.complete(null);
        result.join();
    }

    @Test
    void manuallyRegisteredPendingWork_preventsManagedUpdateFromClearingIt() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        AtomicInteger updates = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> updates.incrementAndGet());
        system.register(task);
        var flow = builder(runtime, system).build();
        assertThrows(IllegalStateException.class, flow::execute);
        assertFalse(flow.isExecuting());
        assertEquals(0, updates.get());
        task.complete(null);
        flow.execute();
        assertEquals(1, updates.get());
    }

    @Test
    void flowFailureCallback_runsBeforeReleaseAndCompletionObserverCanReenter() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        CompletableFuture<Void> task = new CompletableFuture<>();
        AtomicInteger updates = new AtomicInteger();
        TaskSystem system = new TaskSystem((self, entities, scheduler) -> {
            if (updates.getAndIncrement() == 0) self.register(task);
        });
        AtomicReference<SingleFlow<TaskSystem>> reference = new AtomicReference<>();
        var flow = builder(runtime, system).setFailureCallback(actual -> {
            assertTrue(reference.get().isExecuting());
            assertThrows(IllegalStateException.class, reference.get()::execute);
        }).build();
        reference.set(flow);
        var result = flow.executeAsync(Runnable::run);
        var observed = result.handle((unused, failure) -> {
            assertFalse(flow.isExecuting());
            flow.execute();
            return null;
        });
        task.completeExceptionally(new IllegalStateException("external"));
        observed.join();
        assertEquals(2, updates.get());
        assertFalse(flow.isExecuting());
    }

    private CleanECSRuntime runtimeWithRows() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home, ControlledJob.class);
        runtime.entityManager.createEntity(new RuntimeApiTest.Value());
        runtime.entityManager.createEntity(new RuntimeApiTest.Value());
        runtime.entityManager.flush();
        return runtime;
    }

    private static JobScheduler.ExecutionHandle launch(JobScheduler scheduler, EntityManager entities,
                                                       Executor workers, IntConsumer action) {
        return scheduler.executeParallelJob(entities, ControlledJob.class, Map.of("action", action), workers);
    }

    private static SystemExeFlowGraph.Builder<SingleFlow<TaskSystem>> builder(CleanECSRuntime runtime, TaskSystem system) {
        return SingleFlow.newBuilder(new World(runtime), TaskSystem.class)
            .addTransition(system, SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE);
    }

    private static void assertPending(SystemExeFlowGraph flow, CompletableFuture<Void> result) {
        assertTrue(flow.isExecuting());
        assertFalse(result.isDone());
    }

    private static Throwable failure(CompletableFuture<Void> result) {
        assertTrue(result.isDone());
        return assertThrows(CompletionException.class, result::join).getCause();
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interrupted);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable failure) throws T { throw (T) failure; }

    private static final class QueueExecutor implements Executor {
        final ArrayDeque<Runnable> pending = new ArrayDeque<>();
        @Override public void execute(Runnable task) { pending.addLast(task); }
        void runNext() { pending.removeFirst().run(); }
        void drain() { while (!pending.isEmpty()) runNext(); }
    }

    private static final class World extends CleanWorld {
        World(CleanECSRuntime runtime) { super(runtime.entityManager, runtime.jobScheduler); }
        @Override public void update() { flushECS(); }
    }

    @FunctionalInterface
    private interface Update { void run(TaskSystem self, EntityManager entities, JobScheduler scheduler); }

    private static final class TaskSystem extends CleanSystem {
        private final Update action;
        TaskSystem(Update action) { this.action = action; }
        @Override public void update(EntityManager entities, JobScheduler scheduler) { action.run(this, entities, scheduler); }
        void register(CompletableFuture<Void> future) { execution.updateExecutions(new CompletableFuture[]{future}); }
        void register(CompletableFuture<Void>[] futures) { execution.updateExecutions(futures); }
        void register(JobScheduler.ExecutionHandle handle) { execution.updateExecutions(handle); }
        void clear() { execution.noExecutions(); }
    }

    public static class ControlledJob implements ParallelJob {
        @JobDataQuery(componentClass = RuntimeApiTest.Value.class, fieldAccessChain = "value")
        private PrimitiveArray values;
        @JobExternalDataQuery
        private IntConsumer action;

        @Override public void query(EntityQuery query) { query.with(RuntimeApiTest.Value.class); }
        @Override public int estimateWorkload(int index) { return KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread; }
        @Override public void execute(EntityCommands commands, int index, int entityID, int ordinal) {
            action.accept(index);
            values.setInt(index, values.getInt(index) + 1);
        }
    }
}
