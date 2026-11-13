package com.cleanroommc.kirino.engine.render.usage.scene.scheduler;

import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.engine.render.usage.debug.data.MeshletGpuTimeline;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.usage.task.system.MeshletBufferWriteSystem;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public final class MeshletGpuPipelineScheduler implements UpdateScheduler {

    public static class ComputeResult {
        public boolean update;
        public int vertexCount;
        public int indexCount;
    }

    public final ComputeResult computeResult = new ComputeResult();

    private final MeshletGpuPipelineFSM meshletFsm;
    private final ResourceStorage storage;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;
    private final ResourceSlot<MeshletComputeSystem> meshletComputeSystem;
    private final SystemExeFlowGraph meshletBufferWriteSystem;
    private final Executor systemFlowExecutor;
    private @Nullable CompletableFuture<Void> writing;
    private @Nullable Throwable failure;

    public MeshletGpuPipelineScheduler(
            MeshletGpuPipelineFSM meshletFsm,
            ResourceStorage storage,
            ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry,
            ResourceSlot<MeshletComputeSystem> meshletComputeSystem,
            SingleFlow<MeshletBufferWriteSystem> meshletBufferWriteSystem,
            Executor systemFlowExecutor) {

        this(meshletFsm, storage, meshletGpuRegistry, meshletComputeSystem,
            (SystemExeFlowGraph) meshletBufferWriteSystem, systemFlowExecutor);
    }

    MeshletGpuPipelineScheduler(MeshletGpuPipelineFSM meshletFsm, ResourceStorage storage,
                               ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry,
                               ResourceSlot<MeshletComputeSystem> meshletComputeSystem,
                               SystemExeFlowGraph meshletBufferWriteSystem, Executor systemFlowExecutor) {
        this.meshletFsm = meshletFsm;
        this.storage = storage;
        this.meshletGpuRegistry = meshletGpuRegistry;
        this.meshletComputeSystem = meshletComputeSystem;
        this.meshletBufferWriteSystem = meshletBufferWriteSystem;
        this.systemFlowExecutor = systemFlowExecutor;
    }

    /**
     * @param payload Must be a non-null {@link ComputeResult}
     */
    @Override
    public boolean update(@Nullable Object payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkArgument(payload instanceof ComputeResult,
                "Payload must be an instance of \"MeshletGpuPipelineScheduler.ComputeResult\".");

        // the result will be modified inside "process COMPUTABLE -> IDLE"
        ComputeResult result = (ComputeResult) payload;
        result.update = false;

        if (failure != null) {
            throw fail(failure);
        }

        try {
            return update(result);
        } catch (Throwable updateFailure) {
            throw fail(updateFailure);
        }
    }

    private boolean update(ComputeResult result) {
        if (writing != null && writing.isDone()) {
            writing.join();
        }

        //<editor-fold desc="process INITIAL_WAIT -> IDLE">
        if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.INITIAL_WAIT) {
            meshletFsm.next(); // INITIAL_WAIT or IDLE
            if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.INITIAL_WAIT) {
                // wait; abort this update
                return true;
            }
        }
        //</editor-fold>

        //<editor-fold desc="either process IDLE /OR/ trigger IDLE -> COMPUTABLE">
        if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.IDLE) {
            if (storage.get(meshletGpuRegistry).hasMeshletChanges()) {
                if (storage.get(meshletGpuRegistry).isWriting()) {
                    meshletFsm.next(); // COMPUTABLE

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_ALREADY_WRITING);
                } else {
                    KirinoClientDebug.MeshletGpuTimeline$beginWriting();

                    beginWriting();
                    meshletFsm.next(); // COMPUTABLE

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_BEGIN_WRITING);
                }
            } else {
                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_NO_MESHLET_UPDATE);

                if (!storage.get(meshletComputeSystem).isShaderRunning()
                        && storage.get(meshletGpuRegistry).isWriting()
                        && writingSucceeded()) {
                    KirinoClientDebug.MeshletGpuTimeline$finishWriting();

                    storage.get(meshletGpuRegistry).finishWriting();
                    writing = null;
                    meshletFsm.next(); // COMPUTABLE

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_FINISH_WRITING);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="process COMPUTABLE -> IDLE">
        if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.COMPUTABLE) {
            // before dispatching compute, finish existing writing task if possible
            if (!storage.get(meshletComputeSystem).isShaderRunning()
                    && storage.get(meshletGpuRegistry).isWriting()
                    && writingSucceeded()) {
                KirinoClientDebug.MeshletGpuTimeline$finishWriting();

                storage.get(meshletGpuRegistry).finishWriting();
                writing = null;

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_FINISH_WRITING);
            }

            // start dispatching if possible
            if (!storage.get(meshletComputeSystem).isShaderRunning()
                    && storage.get(meshletGpuRegistry).isFinishedWritingOnce()
                    && !storage.get(meshletGpuRegistry).isWriting()) {
                KirinoClientDebug.MeshletGpuTimeline$beginComputing();

                storage.get(meshletGpuRegistry).beginComputing();
                storage.get(meshletComputeSystem).startDispatch(
                        storage,
                        storage.get(meshletGpuRegistry),
                        storage.get(meshletGpuRegistry).getMeshletCount());

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_BEGIN_COMPUTING);

                // start the next writing task right after the dispatch signal if needed. maximize throughput
                if (storage.get(meshletGpuRegistry).hasMeshletChanges()) {
                    KirinoClientDebug.MeshletGpuTimeline$beginWriting();

                    beginWriting();

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_BEGIN_WRITING);
                }
            }

            // pull compute result
            if (storage.get(meshletComputeSystem).isShaderRunning()
                    && storage.get(meshletComputeSystem).tryPullResult()) {
                KirinoClientDebug.MeshletGpuTimeline$finishComputing();

                storage.get(meshletGpuRegistry).finishComputing();
                result.update = true;
                result.vertexCount = storage.get(meshletComputeSystem).getVertexCount();
                result.indexCount = storage.get(meshletComputeSystem).getIndexCount();

                // todo: move gl calls somewhere else
                // draw commands will be submitted subsequently. next update is definitely valid for the next compute (bind bases to different buffers)
                // since the next bind base is strictly after the draw commands
                GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getVertexConsumeTarget().target(), 1, storage.get(meshletGpuRegistry).getVertexConsumeTarget().bufferID);
                GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getIndexConsumeTarget().target(), 2, storage.get(meshletGpuRegistry).getIndexConsumeTarget().bufferID);
                GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getDrawIndexConsumeTarget().target(), 5, storage.get(meshletGpuRegistry).getDrawIndexConsumeTarget().bufferID);
                meshletFsm.next(); // IDLE

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_FINISH);
            }
        }
        //</editor-fold>

        return false;
    }

    private void beginWriting() {
        Preconditions.checkState(writing == null, "The previous writing execution has not been consumed.");

        storage.get(meshletGpuRegistry).beginWriting();
        writing = meshletBufferWriteSystem.executeAsync(systemFlowExecutor);
    }

    private boolean writingSucceeded() {
        Preconditions.checkState(writing != null, "Writing has no associated flow execution.");
        if (!writing.isDone()) {
            return false;
        }

        writing.join();
        return true;
    }

    private RuntimeException fail(Throwable cause) {
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        if (failure == null) {
            failure = cause;
        }
        Throwables.throwIfUnchecked(failure);
        return new CompletionException(failure);
    }
}
