package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class FlowApiTest {
    @TempDir Path home;

    @Test
    void flowFromExternalPackage_runsRegisteredJobBetweenBarrierCallbacks() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        World world = new World(runtime);
        var entity = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
        world.update();
        List<String> events = new ArrayList<>();
        RecordingSystem system = new RecordingSystem(events);
        SystemExeFlowGraph.Builder<SingleFlow<RecordingSystem>> builder = SingleFlow.newBuilder(world, RecordingSystem.class);
        SingleFlow<RecordingSystem> flow = builder
            .addBarrierNode("after", () -> events.add("after"))
            .addTransition(system, SystemExeFlowGraph.START_NODE, "after")
            .addDummyTransition("after", SystemExeFlowGraph.END_NODE)
            .setStartNodeCallback(() -> events.add("start"))
            .setEndNodeCallback(() -> events.add("end"))
            .setFinishCallback(() -> events.add("finish"))
            .build();
        assertSame(system, flow.getSystem());
        flow.execute();
        assertEquals(List.of("start", "system", "after", "end", "finish"), events);
        assertEquals(1, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        assertFalse(flow.isExecuting());
    }

    @Test
    void asyncExecution_blocksReentryUntilTaskCompletesAndCanBeReused() throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        World world = new World(runtime);
        var entity = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
        world.update();
        List<String> events = new ArrayList<>();
        var flow = simpleFlow(world, new RecordingSystem(events));
        List<Runnable> pending = new ArrayList<>();
        var completion = flow.executeAsync(pending::add);
        assertTrue(flow.isExecuting());
        assertFalse(completion.isDone());
        assertEquals(1, pending.size());
        assertThrows(IllegalStateException.class, flow::execute);
        assertThrows(IllegalStateException.class, () -> flow.executeAsync(pending::add));
        pending.getFirst().run();
        completion.join();
        assertFalse(flow.isExecuting());
        assertEquals(List.of("system"), events);
        flow.execute();
        assertEquals(List.of("system", "system"), events);
        assertEquals(2, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
    }

    @Test
    void builderReuse_changedCallbacksDoNotMutatePreviouslyBuiltFlow() throws Exception {
        World world = new World(RuntimeApiTest.runtime(home));
        List<String> events = new ArrayList<>();
        RecordingSystem system = new RecordingSystem(events);
        var builder = SingleFlow.newBuilder(world, RecordingSystem.class)
            .addTransition(system, SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE)
            .setStartNodeCallback(() -> events.add("first-start"))
            .setEndNodeCallback(() -> events.add("first-end"))
            .setFinishCallback(() -> events.add("first-finish"));
        var first = builder.build();
        var second = builder.setStartNodeCallback(() -> events.add("second-start"))
            .setEndNodeCallback(() -> events.add("second-end"))
            .setFinishCallback(() -> events.add("second-finish"))
            .build();
        first.execute();
        assertEquals(List.of("first-start", "system", "first-end", "first-finish"), events);
        events.clear();
        second.execute();
        assertEquals(List.of("second-start", "system", "second-end", "second-finish"), events);
    }

    @Test
    void executorRejection_releasesExecutionStateBeforeNextRun() throws Exception {
        World world = new World(RuntimeApiTest.runtime(home));
        List<String> events = new ArrayList<>();
        var flow = simpleFlow(world, new RecordingSystem(events));
        RejectedExecutionException rejected = new RejectedExecutionException("test rejection");
        assertSame(rejected, assertThrows(RejectedExecutionException.class,
            () -> flow.executeAsync(task -> { throw rejected; })));
        assertFalse(flow.isExecuting());
        assertTrue(events.isEmpty());
        flow.execute();
        assertEquals(List.of("system"), events);
    }

    @Test
    void systemFailure_propagatesAndRunsFinishCallbackBeforeReleasingExecutionState() throws Exception {
        World world = new World(RuntimeApiTest.runtime(home));
        IllegalStateException failure = new IllegalStateException("test system failure");
        List<String> events = new ArrayList<>();
        var flow = SingleFlow.newBuilder(world, FailingSystem.class)
            .addTransition(new FailingSystem(failure), SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE)
            .setFinishCallback(() -> events.add("finish"))
            .build();
        assertSame(failure, assertThrows(IllegalStateException.class, flow::execute));
        assertEquals(List.of("finish"), events);
        assertFalse(flow.isExecuting());
        CompletionException completion = assertThrows(CompletionException.class,
            () -> flow.executeAsync(Runnable::run).join());
        assertSame(failure, completion.getCause());
        assertEquals(List.of("finish", "finish"), events);
        assertFalse(flow.isExecuting());
    }

    @Test
    void invalidBuilder_missingSystemOrCycleIsRejectedThroughPublicInterface() throws Exception {
        World world = new World(RuntimeApiTest.runtime(home));
        assertThrows(IllegalStateException.class, () -> SingleFlow.newBuilder(world, RecordingSystem.class).build());
        var builder = SingleFlow.newBuilder(world, RecordingSystem.class)
            .addBarrierNode("cycle", null)
            .addTransition(new RecordingSystem(new ArrayList<>()), SystemExeFlowGraph.START_NODE, "cycle")
            .addDummyTransition("cycle", "cycle")
            .addDummyTransition("cycle", SystemExeFlowGraph.END_NODE);
        assertThrows(IllegalStateException.class, builder::build);
    }

    private static SingleFlow<RecordingSystem> simpleFlow(World world, RecordingSystem system) {
        return SingleFlow.newBuilder(world, RecordingSystem.class)
            .addTransition(system, SystemExeFlowGraph.START_NODE, SystemExeFlowGraph.END_NODE).build();
    }

    private static final class World extends CleanWorld {
        World(CleanECSRuntime runtime) { super(runtime.entityManager, runtime.jobScheduler); }
        @Override public void update() { flushECS(); }
    }

    private static final class RecordingSystem extends CleanSystem {
        private final List<String> events;
        RecordingSystem(List<String> events) { this.events = events; }
        @Override public void update(EntityManager entities, JobScheduler scheduler) {
            events.add("system");
            execution.updateExecutions(scheduler.executeParallelJob(entities,
                RuntimeApiTest.IncrementJob.class, null, Runnable::run));
        }
    }

    private static final class FailingSystem extends CleanSystem {
        private final IllegalStateException failure;
        FailingSystem(IllegalStateException failure) { this.failure = failure; }
        @Override public void update(EntityManager entities, JobScheduler scheduler) { throw failure; }
    }
}
