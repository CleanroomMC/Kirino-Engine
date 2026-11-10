package com.cleanroommc.kirino.engine.render.usage.scene.scheduler;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class MeshletGpuPipelineSchedulerTest {
    @TempDir Path home;
    private DebugDataHandle<?> previousTimeline;

    @BeforeEach
    void initialize() throws Throwable {
        RendererTestSupport.initializeConfig(home);
        previousTimeline = RendererTestSupport.installInactiveTimeline();
    }

    @AfterEach
    void restore() throws Throwable { RendererTestSupport.setTimeline(previousTimeline); }

    @Test
    void pendingWrite_isNotPublishedEvenWhenFlowReportsNotExecuting() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        fixture.update();
        assertEquals(1, fixture.flow.runs.size());
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        fixture.flow.current().complete(null);
        fixture.update();
        assertEquals(1, fixture.registry.finishes);
        assertEquals(1, fixture.compute.dispatches);
        assertFalse(fixture.registry.writing);
        fixture.update();
        assertEquals(1, fixture.registry.finishes);
        assertEquals(1, fixture.compute.dispatches);
    }

    @Test
    void failedWrite_isReportedEvenWhileShaderRunsAndNeverPublishesOrRetries() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        fixture.compute.running = true;
        IllegalStateException failure = new IllegalStateException("write failed");
        fixture.flow.current().completeExceptionally(failure);
        fixture.scheduler.computeResult.update = true;
        assertSame(failure, assertThrows(IllegalStateException.class, fixture::update));
        assertFalse(fixture.scheduler.computeResult.update);
        fixture.compute.running = false;
        fixture.registry.changes = true;
        assertSame(failure, assertThrows(IllegalStateException.class, fixture::update));
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        assertEquals(1, fixture.registry.begins);
        assertEquals(1, fixture.flow.runs.size());
    }

    @Test
    void cancelledWrite_isNotSuccessfulCompletion() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        fixture.flow.current().cancel(false);
        assertThrows(CancellationException.class, fixture::update);
        assertThrows(CancellationException.class, fixture::update);
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        assertEquals(1, fixture.registry.begins);
    }

    @Test
    void initialExecutorRejection_doesNotReplayBeginWriting() throws Throwable {
        Fixture fixture = new Fixture();
        RejectedExecutionException failure = new RejectedExecutionException("executor stopped");
        fixture.flow.launchFailure = failure;
        assertSame(failure, assertThrows(RejectedExecutionException.class, fixture::update));
        fixture.flow.launchFailure = null;
        assertSame(failure, assertThrows(RejectedExecutionException.class, fixture::update));
        assertEquals(1, fixture.registry.begins);
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        assertTrue(fixture.flow.runs.isEmpty());
    }

    @Test
    void failedFinishWriting_doesNotRepeatPartiallyAppliedPublication() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        AssertionError failure = new AssertionError("finish failed");
        fixture.registry.finishFailure = failure;
        fixture.flow.current().complete(null);
        assertSame(failure, assertThrows(AssertionError.class, fixture::update));
        assertSame(failure, assertThrows(AssertionError.class, fixture::update));
        assertEquals(1, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
    }

    @Test
    void secondWrite_requiresItsOwnSuccessAndCannotReuseFirstResult() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        fixture.flow.current().complete(null);
        fixture.registry.changes = true;
        fixture.update();
        assertEquals(2, fixture.flow.runs.size());
        assertEquals(1, fixture.registry.finishes);
        assertEquals(1, fixture.compute.dispatches);
        fixture.compute.running = false;
        fixture.update();
        assertEquals(1, fixture.registry.finishes);
        IllegalArgumentException failure = new IllegalArgumentException("second write failed");
        fixture.flow.current().completeExceptionally(failure);
        assertSame(failure, assertThrows(IllegalArgumentException.class, fixture::update));
        assertEquals(1, fixture.registry.finishes);
        assertEquals(1, fixture.compute.dispatches);
        assertEquals(2, fixture.registry.begins);
    }

    @Test
    void writingWithoutOwnedFlowResult_isRejectedInsteadOfPublished() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.registry.writing = true;
        fixture.registry.changes = false;
        assertThrows(IllegalStateException.class, fixture::update);
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        assertTrue(fixture.flow.runs.isEmpty());
    }

    @Test
    void idleFinishPath_alsoWaitsForItsOwnSuccessfulResult() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.update();
        fixture.fsm.reset();
        fixture.makeIdle();
        fixture.update();
        assertEquals(0, fixture.registry.finishes);
        fixture.flow.current().completeExceptionally(new IllegalStateException("write failed"));
        assertThrows(IllegalStateException.class, fixture::update);
        assertEquals(0, fixture.registry.finishes);
        assertEquals(0, fixture.compute.dispatches);
        assertEquals(MeshletGpuPipelineFSM.State.IDLE, fixture.fsm.getState());
    }

    private static final class Fixture {
        final TestRegistry registry = new TestRegistry();
        final TestCompute compute = new TestCompute();
        final RendererTestSupport.Flow flow = new RendererTestSupport.Flow();
        final MeshletGpuPipelineFSM fsm = new MeshletGpuPipelineFSM();
        final MeshletGpuPipelineScheduler scheduler;

        Fixture() throws Throwable {
            ResourceLayout layout = RendererTestSupport.layout();
            var registrySlot = layout.slot(MeshletGpuRegistry.class);
            var computeSlot = layout.slot(MeshletComputeSystem.class);
            ResourceStorage storage = RendererTestSupport.storage();
            storage.put(registrySlot, registry);
            storage.put(computeSlot, compute);
            makeIdle();
            scheduler = new MeshletGpuPipelineScheduler(fsm, storage, registrySlot, computeSlot, flow, Runnable::run);
        }

        void makeIdle() {
            while (fsm.getState() == MeshletGpuPipelineFSM.State.INITIAL_WAIT) {
                fsm.next();
            }
        }
        void update() { assertFalse(scheduler.update(scheduler.computeResult)); }
    }

    private static final class TestRegistry extends MeshletGpuRegistry {
        boolean changes = true;
        boolean writing;
        boolean finished;
        int begins;
        int finishes;
        AssertionError finishFailure;

        @Override public boolean hasMeshletChanges() { return changes; }
        @Override public boolean isWriting() { return writing; }
        @Override public boolean isFinishedWritingOnce() { return finished; }
        @Override public int getMeshletCount() { return 1; }
        @Override public void beginWriting() {
            assertFalse(writing);
            begins++;
            writing = true;
            changes = false;
        }
        @Override public void finishWriting() {
            assertTrue(writing);
            finishes++;
            if (finishFailure != null) {
                throw finishFailure;
            }
            writing = false;
            finished = true;
        }
        @Override public void beginComputing() {
            assertTrue(finished);
            assertFalse(writing);
        }
    }

    private static final class TestCompute extends MeshletComputeSystem {
        boolean running;
        int dispatches;
        TestCompute() { super(null, null); }
        @Override public boolean isShaderRunning() { return running; }
        @Override public boolean tryPullResult() { return false; }
        @Override public void startDispatch(ResourceStorage storage, MeshletGpuRegistry registry, int count) {
            assertFalse(running);
            dispatches++;
            running = true;
        }
    }
}
