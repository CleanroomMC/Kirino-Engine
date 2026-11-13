package com.cleanroommc.kirino.engine.render.usage.scene.scheduler;

import com.cleanroommc.kirino.engine.render.usage.scene.ChunkPosKey;
import com.cleanroommc.kirino.engine.render.usage.scene.callback.CallbackDrivenChunkDelta;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.TerrainCpuPipelineFSM;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;

class TerrainCpuPipelineSchedulerTest {
    @Test
    void successfulStages_advanceOnlyWhenPolledOnRenderThreadAndNeverLaunchTwice() throws Exception {
        Fixture fixture = new Fixture();
        fixture.start();
        fixture.update();
        assertEquals(1, fixture.priority.runs.size());
        assertEquals(TerrainCpuPipelineFSM.State.CHUNK_PRIORITIZATION_TASK, fixture.fsm.getState());
        Thread worker = new Thread(() -> fixture.priority.current().complete(null));
        worker.start();
        worker.join(5000);
        assertFalse(worker.isAlive());
        assertEquals(TerrainCpuPipelineFSM.State.CHUNK_PRIORITIZATION_TASK, fixture.fsm.getState());
        fixture.update();
        assertEquals(List.of(0), fixture.lods);
        fixture.update();
        assertEquals(1, fixture.generation.runs.size());
        fixture.generation.current().complete(null);
        fixture.update();
        assertEquals(List.of(0, 1), fixture.lods);
        assertEquals(1, fixture.fsm.getMeshletGenCounter());
        fixture.generation.current().complete(null);
        fixture.update();
        assertEquals(TerrainCpuPipelineFSM.State.IDLE, fixture.fsm.getState());
        assertEquals(2, fixture.generation.runs.size());
        fixture.update();
        assertEquals(2, fixture.generation.runs.size());
    }

    @Test
    void failedGeneration_doesNotAdvanceOrRetryEvenAfterFsmReset() {
        Fixture fixture = new Fixture();
        fixture.startGeneration();
        IllegalStateException failure = new IllegalStateException("generation failed");
        fixture.generation.current().completeExceptionally(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, fixture::update));
        assertEquals(0, fixture.fsm.getMeshletGenCounter());
        fixture.fsm.reset();
        fixture.hint.newWorld = true;
        assertSame(failure, assertThrows(IllegalStateException.class, fixture::update));
        assertEquals(1, fixture.generation.runs.size());
        assertEquals(1, fixture.priority.runs.size());
    }

    @Test
    void failedPrioritization_neverStartsGeneration() {
        Fixture fixture = new Fixture();
        fixture.start();
        AssertionError failure = new AssertionError("priority failed");
        fixture.priority.current().completeExceptionally(failure);
        assertSame(failure, assertThrows(AssertionError.class, fixture::update));
        assertSame(failure, assertThrows(AssertionError.class, fixture::update));
        assertTrue(fixture.generation.runs.isEmpty());
        assertEquals(TerrainCpuPipelineFSM.State.CHUNK_PRIORITIZATION_TASK, fixture.fsm.getState());
    }

    @Test
    void failedDestroy_keepsDeltaAndNeverRetriesPartialWork() {
        Fixture fixture = new Fixture();
        fixture.hint.chunkDelta.chunksDestroyedLastFrame.add(new ChunkPosKey(1, 2, 3));
        IllegalArgumentException failure = new IllegalArgumentException("destroy failed");
        fixture.destroy.synchronousFailure = failure;
        assertSame(failure, assertThrows(IllegalArgumentException.class, fixture::update));
        assertEquals(1, fixture.hint.chunkDelta.chunksDestroyedLastFrame.size());
        assertEquals(TerrainCpuPipelineFSM.State.MESHLET_DESTROY_TASK, fixture.fsm.getState());
        assertSame(failure, assertThrows(IllegalArgumentException.class, fixture::update));
        assertEquals(1, fixture.destroy.synchronousRuns);
    }

    @Test
    void successfulDestroy_clearsDeltaAndAdvancesAfterSynchronousWorkReturns() {
        Fixture fixture = new Fixture();
        fixture.hint.chunkDelta.chunksDestroyedLastFrame.add(new ChunkPosKey(1, 2, 3));
        fixture.destroy.synchronousAction = () -> {
            assertEquals(TerrainCpuPipelineFSM.State.MESHLET_DESTROY_TASK, fixture.fsm.getState());
            assertEquals(1, fixture.hint.chunkDelta.chunksDestroyedLastFrame.size());
        };
        fixture.update();
        assertTrue(fixture.hint.chunkDelta.chunksDestroyedLastFrame.isEmpty());
        assertEquals(TerrainCpuPipelineFSM.State.IDLE, fixture.fsm.getState());
        fixture.update();
        assertEquals(1, fixture.destroy.synchronousRuns);
    }

    @Test
    void initialSubmissionRejection_isStickyAndDoesNotStartNewStages() {
        Fixture fixture = new Fixture();
        RejectedExecutionException failure = new RejectedExecutionException("executor stopped");
        fixture.priority.launchFailure = failure;
        assertSame(failure, assertThrows(RejectedExecutionException.class, fixture::start));
        fixture.priority.launchFailure = null;
        fixture.fsm.reset();
        assertSame(failure, assertThrows(RejectedExecutionException.class, fixture::update));
        assertTrue(fixture.priority.runs.isEmpty());
        assertTrue(fixture.generation.runs.isEmpty());
    }

    @Test
    void cancelledCompletion_doesNotCountAsSuccessfulStage() {
        Fixture fixture = new Fixture();
        fixture.startGeneration();
        fixture.generation.current().cancel(false);
        assertThrows(CancellationException.class, fixture::update);
        assertThrows(CancellationException.class, fixture::update);
        assertEquals(1, fixture.generation.runs.size());
        assertEquals(0, fixture.fsm.getMeshletGenCounter());
    }

    @Test
    void stageChangedDuringExecution_doesNotApplyStaleSuccessToNewState() {
        Fixture fixture = new Fixture();
        fixture.start();
        fixture.fsm.reset();
        fixture.priority.current().complete(null);
        assertThrows(IllegalStateException.class, fixture::update);
        assertEquals(TerrainCpuPipelineFSM.State.IDLE, fixture.fsm.getState());
        assertTrue(fixture.generation.runs.isEmpty());
        assertThrows(IllegalStateException.class, fixture::update);
    }

    private static final class Fixture {
        final Thread renderThread = Thread.currentThread();
        final TerrainCpuPipelineFSM fsm = new TerrainCpuPipelineFSM() {
            @Override public void next() {
                assertSame(renderThread, Thread.currentThread());
                super.next();
            }
        };
        final RendererTestSupport.Flow priority = new RendererTestSupport.Flow();
        final RendererTestSupport.Flow generation = new RendererTestSupport.Flow();
        final RendererTestSupport.Flow destroy = new RendererTestSupport.Flow();
        final List<Integer> lods = new ArrayList<>();
        final TerrainCpuPipelineScheduler scheduler = new TerrainCpuPipelineScheduler(fsm, priority, destroy,
            generation, lod -> {
                assertSame(renderThread, Thread.currentThread());
                lods.add(lod);
            }, Runnable::run);
        final TerrainCpuPipelineScheduler.UpdateHint hint = scheduler.updateHint;

        Fixture() {
            hint.chunkDelta = new CallbackDrivenChunkDelta();
            hint.foregroundRenderDis = 2;
        }

        void update() { assertFalse(scheduler.update(hint)); }
        void start() {
            hint.cameraMoved = true;
            update();
            hint.cameraMoved = false;
        }
        void startGeneration() {
            start();
            priority.current().complete(null);
            update();
        }
    }
}
