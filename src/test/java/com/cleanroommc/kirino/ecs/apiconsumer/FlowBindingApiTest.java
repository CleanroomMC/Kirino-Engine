package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.ExecutionContainer;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class FlowBindingApiTest {
    @TempDir Path home;

    @ParameterizedTest
    @ValueSource(strings = {"legacy", "world", "services"})
    void builderFactories_bindTheOwnersServicesAndKeepRuntimeEpochChecks(String entry) throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            World world = new World(runtime);
            var entity = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            Step step = new Step(runtime);
            SystemExeFlowGraph.Builder<SingleFlow<Step>> builder = switch (entry) {
                case "legacy" -> SingleFlow.newBuilder(world, Step.class);
                case "world" -> world.newSingleFlowBuilder(Step.class);
                case "services" -> CleanSystem.newSingleFlowBuilder(runtime.entityManager, runtime.jobScheduler, Step.class);
                default -> throw new AssertionError(entry);
            };
            SingleFlow<Step> old = builder.addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE).build();
            old.execute();
            assertSame(step, old.getSystem());
            assertEquals(1, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
            runtime.reset();
            assertThrows(IllegalStateException.class, old::execute);
            var replacement = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            builder.build().execute();
            assertEquals(1, replacement.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        }
    }

    @Test
    void shadowedSubclassFields_doNotReplaceTheBoundWorldServicesOrSystemLease() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home); CleanECSRuntime foreign = runtime.newSession()) {
            World world = new World(runtime);
            world.entityManager = foreign.entityManager;
            world.jobScheduler = foreign.jobScheduler;
            CompletableFuture<Void> external = new CompletableFuture<>();
            ShadowedStep step = new ShadowedStep(runtime, external);
            var first = world.newSingleFlowBuilder(ShadowedStep.class)
                .addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE).build();
            var second = world.newSingleFlowBuilder(ShadowedStep.class)
                .addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE).build();
            CompletableFuture<Void> result = first.executeAsync(Runnable::run);
            try {
                assertFalse(result.isDone());
                assertTrue(first.isExecuting());
                assertThrows(IllegalStateException.class, second::execute);
                assertTrue(first.isExecuting());
                assertFalse(second.isExecuting());
            } finally {
                external.complete(null);
            }
            result.join();
            second.execute();
            assertEquals(2, step.updates.get());
        }
    }

    @Test
    void bindingDoesNotAcquireTheSystemUntilExecution_andManualWorkRemainsProtected() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            CompletableFuture<Void> external = new CompletableFuture<>();
            ShadowedStep step = new ShadowedStep(runtime, CompletableFuture.completedFuture(null));
            step.register(external);
            var flow = CleanSystem.newSingleFlowBuilder(runtime.entityManager, runtime.jobScheduler, ShadowedStep.class)
                .addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE).build();
            assertThrows(IllegalStateException.class, flow::execute);
            assertEquals(0, step.updates.get());
            external.complete(null);
            flow.execute();
            assertEquals(1, step.updates.get());
        }
    }

    @Test
    void directFactory_queuedFlowRemainsAdmittedAfterObserverCancellationUntilDrain() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            Step step = new Step(runtime);
            var flow = CleanSystem.newSingleFlowBuilder(runtime.entityManager, runtime.jobScheduler, Step.class)
                .addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE).build();
            ArrayDeque<Runnable> queue = new ArrayDeque<>();
            var result = flow.executeAsync(queue::addLast);
            result.cancel(false);
            var drained = runtime.quiesce();
            try {
                assertFalse(drained.isDone());
                assertTrue(flow.isExecuting());
            } finally {
                while (!queue.isEmpty()) {
                    queue.removeFirst().run();
                }
            }
            assertThrows(CompletionException.class, drained::join);
            assertFalse(flow.isExecuting());
            try {
                runtime.reset();
                fail("Reset should report the stopped flow failure after cleanup");
            } catch (IllegalStateException expected) {
                assertEquals(CleanECSRuntime.State.RUNNING, runtime.state());
            }
        }
    }

    @Test
    void replacingBarrierCallback_keepsItsEdgesAndPreviouslyBuiltSnapshots() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            List<String> events = new ArrayList<>();
            var builder = new World(runtime).newSingleFlowBuilder(Step.class)
                .addBarrierNode("after", () -> events.add("old"))
                .addTransition(new Step(runtime), SingleFlow.START_NODE, "after")
                .addDummyTransition("after", SingleFlow.END_NODE);
            var old = builder.build();
            var next = builder.addBarrierNode("after", () -> events.add("new")).build();
            old.execute();
            next.execute();
            assertEquals(List.of("old", "new"), events);
        }
    }

    @Test
    void failedBuild_doesNotChangeAnExistingSnapshotAndCanBeRepaired() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            AtomicInteger after = new AtomicInteger();
            var builder = new World(runtime).newSingleFlowBuilder(Step.class)
                .addTransition(new Step(runtime), SingleFlow.START_NODE, SingleFlow.END_NODE);
            var original = builder.build();
            builder.addBarrierNode("later", after::incrementAndGet)
                .addDummyTransition(SingleFlow.START_NODE, "later");
            assertThrows(IllegalStateException.class, builder::build);
            original.execute();
            assertEquals(0, after.get());
            builder.addDummyTransition("later", SingleFlow.END_NODE).build().execute();
            assertEquals(1, after.get());
            original.execute();
            assertEquals(1, after.get());
        }
    }

    @Test
    void invalidTransition_doesNotConsumeTheSingleSystemSlot() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            var builder = new World(runtime).newSingleFlowBuilder(Step.class);
            assertThrows(NullPointerException.class,
                () -> builder.addTransition(new Step(runtime), SingleFlow.START_NODE, "missing"));
            assertDoesNotThrow(() -> builder.addTransition(new Step(runtime), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build().execute());
        }
    }

    @Test
    void singleFlow_stillRejectsMultipleSystemsOrTheWrongConcreteType() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            var builder = new World(runtime).newSingleFlowBuilder(Step.class);
            assertThrows(IllegalArgumentException.class, () -> builder.addTransition(
                new ShadowedStep(runtime, CompletableFuture.completedFuture(null)), SingleFlow.START_NODE, SingleFlow.END_NODE));
            Step step = new Step(runtime);
            builder.addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE);
            assertThrows(IllegalStateException.class,
                () -> builder.addTransition(step, SingleFlow.START_NODE, SingleFlow.END_NODE));
            assertDoesNotThrow(() -> builder.build().execute());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"deadEnd", "unreachable", "startIncoming", "endOutgoing"})
    void builder_rejectsPathsThatViolateStartAndEndBarriers(String problem) throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            var builder = new World(runtime).newSingleFlowBuilder(Step.class)
                .addTransition(new Step(runtime), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .addBarrierNode("other", null);
            switch (problem) {
                case "deadEnd" -> builder.addDummyTransition(SingleFlow.START_NODE, "other");
                case "unreachable" -> builder.addDummyTransition("other", SingleFlow.END_NODE);
                case "startIncoming" -> builder.addDummyTransition("other", SingleFlow.START_NODE);
                case "endOutgoing" -> builder.addDummyTransition(SingleFlow.END_NODE, "other");
                default -> throw new AssertionError(problem);
            }
            assertThrows(IllegalStateException.class, builder::build);
        }
    }

    private static final class World extends CleanWorld {
        private EntityManager entityManager;
        private JobScheduler jobScheduler;
        World(CleanECSRuntime runtime) { super(runtime.entityManager, runtime.jobScheduler); }
        public void update() { flushECS(); }
    }

    private static class Step extends CleanSystem {
        final CleanECSRuntime expected;
        Step(CleanECSRuntime expected) { this.expected = expected; }
        public void update(EntityManager entities, JobScheduler scheduler) {
            assertSame(expected.entityManager, entities);
            assertSame(expected.jobScheduler, scheduler);
            execution.updateExecutions(scheduler.executeParallelJob(entities,
                RuntimeApiTest.IncrementJob.class, null, Runnable::run));
        }
    }

    private static final class ShadowedStep extends Step {
        private final ExecutionContainer execution = null;
        final CompletableFuture<Void> external;
        final AtomicInteger updates = new AtomicInteger();
        ShadowedStep(CleanECSRuntime runtime, CompletableFuture<Void> external) {
            super(runtime);
            this.external = external;
        }
        public void update(EntityManager entities, JobScheduler scheduler) {
            super.update(entities, scheduler);
            register(external);
            updates.incrementAndGet();
        }
        void register(CompletableFuture<Void> work) { super.execution.updateExecutions(new CompletableFuture[]{work}); }
    }
}
