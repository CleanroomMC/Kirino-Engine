package com.cleanroommc.kirino.engine.render.usage.scene.scheduler;

import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.engine.render.usage.scene.callback.CallbackDrivenChunkDelta;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.engine.render.usage.task.system.ChunkPrioritizationSystem;
import com.cleanroommc.kirino.engine.render.usage.task.system.MeshletDestroySystem;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;

public final class TerrainCpuPipelineScheduler implements UpdateScheduler {

    public static class UpdateHint {
        public boolean cameraMoved;
        public boolean renderDisChanged;
        public boolean newWorld;
        public int foregroundRenderDis;
        public CallbackDrivenChunkDelta chunkDelta = null;
    }

    public final UpdateHint updateHint = new UpdateHint();

    private final TerrainCpuPipelineFSM terrainFsm;
    private final SystemExeFlowGraph chunkPrioritizationSystem;
    private final SystemExeFlowGraph meshletDestroySystem;
    private final SystemExeFlowGraph chunkMeshletGenSystem;
    private final IntConsumer setLod;
    private final Executor systemFlowExecutor;
    private @Nullable CompletableFuture<Void> pending;
    private TerrainCpuPipelineFSM.@Nullable State pendingStage;
    private @Nullable Throwable failure;

    public TerrainCpuPipelineScheduler(
            TerrainCpuPipelineFSM terrainFsm,
            SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem,
            SingleFlow<MeshletDestroySystem> meshletDestroySystem,
            SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem,
            Executor systemFlowExecutor) {

        this(terrainFsm, chunkPrioritizationSystem, meshletDestroySystem, chunkMeshletGenSystem,
            chunkMeshletGenSystem.getSystem()::setLod, systemFlowExecutor);
    }

    TerrainCpuPipelineScheduler(TerrainCpuPipelineFSM terrainFsm, SystemExeFlowGraph chunkPrioritizationSystem,
                                SystemExeFlowGraph meshletDestroySystem, SystemExeFlowGraph chunkMeshletGenSystem,
                                IntConsumer setLod, Executor systemFlowExecutor) {
        this.terrainFsm = terrainFsm;
        this.chunkPrioritizationSystem = chunkPrioritizationSystem;
        this.meshletDestroySystem = meshletDestroySystem;
        this.chunkMeshletGenSystem = chunkMeshletGenSystem;
        this.setLod = setLod;
        this.systemFlowExecutor = systemFlowExecutor;
    }

    /**
     * @param payload Must be a non-null {@link UpdateHint}
     */
    @Override
    public boolean update(@Nullable Object payload) {
        Preconditions.checkNotNull(payload);
        Preconditions.checkArgument(payload instanceof UpdateHint,
                "Payload must be an instance of \"TerrainCpuPipelineScheduler.UpdateHint\".");

        if (failure != null) {
            throw fail(failure);
        }

        try {
            return update((UpdateHint) payload);
        } catch (Throwable updateFailure) {
            throw fail(updateFailure);
        }
    }

    private boolean update(UpdateHint hint) {
        if (pending != null) {
            if (!pending.isDone()) {
                return false;
            }

            pending.join();
            Preconditions.checkState(terrainFsm.getState() == pendingStage,
                "The terrain stage changed while its flow was running.");

            terrainFsm.next();
            pending = null;
            pendingStage = null;
        }

        // newChunksAdded will be modified at the end of update by ChunkCreateCallback
        boolean chunkPopulationChange = hint.cameraMoved || hint.renderDisChanged || hint.newWorld || hint.chunkDelta.newChunksAdded;

        //<editor-fold desc="trigger CHUNK_PRIORITIZATION_TASK -> MESHLET_GEN_TASK">
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && chunkPopulationChange) {
            // consume newChunksAdded
            if (hint.chunkDelta.newChunksAdded) {
                hint.chunkDelta.newChunksAdded = false;
            }

            // lod fallout distance = 16
            // so the counter target is also render distance
            // i.e. generate meshlets based on the loaded chunks til the render distance
            terrainFsm.setMeshletGenCounter(hint.foregroundRenderDis); // we just updated the render distance; it's up to date
            terrainFsm.prioritizeChunks();

            pendingStage = terrainFsm.getState();
            pending = chunkPrioritizationSystem.executeAsync(systemFlowExecutor);
        }
        //</editor-fold>

        //<editor-fold desc="process MESHLET_GEN_TASK -> IDLE">
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.MESHLET_GEN_TASK) {
            setLod.accept(terrainFsm.getMeshletGenCounter());
            pendingStage = terrainFsm.getState();
            pending = chunkMeshletGenSystem.executeAsync(systemFlowExecutor);
        }
        //</editor-fold>

        //<editor-fold desc="trigger MESHLET_DESTROY_TASK -> IDLE">
        // chunksDestroyedLastFrame will be modified at the end of update by ChunkDestroyCallback
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && !hint.chunkDelta.chunksDestroyedLastFrame.isEmpty()) {
            terrainFsm.destroyMeshlets();
            // must be blocking to prevent chunksDestroyedLastFrame from being modified (race)
            meshletDestroySystem.execute();
            hint.chunkDelta.chunksDestroyedLastFrame.clear();
            terrainFsm.next();
        }
        //</editor-fold>

        return false;
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
