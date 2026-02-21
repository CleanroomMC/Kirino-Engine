package com.cleanroommc.kirino.engine.render.platform.scene.scheduler;

import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.engine.render.platform.scene.ChunkPosKey;
import com.cleanroommc.kirino.engine.render.platform.scene.callback.CallbackDrivenChunkDelta;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.engine.render.platform.task.system.ChunkPrioritizationSystem;
import com.cleanroommc.kirino.engine.render.platform.task.system.MeshletDestroySystem;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.Executor;

public final class TerrainCpuPipelineScheduler implements UpdateScheduler{

    public static class UpdateHint {
        public boolean cameraMoved;
        public boolean renderDisChanged;
        public boolean newWorld;
        public int foregroundRenderDis;
        public CallbackDrivenChunkDelta chunkDelta = null;
    }

    public final UpdateHint updateHint = new UpdateHint();

    private final TerrainCpuPipelineFSM terrainFsm;
    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final Executor systemFlowExecutor;

    public TerrainCpuPipelineScheduler(
            TerrainCpuPipelineFSM terrainFsm,
            SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem,
            SingleFlow<MeshletDestroySystem> meshletDestroySystem,
            SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem,
            Executor systemFlowExecutor) {

        this.terrainFsm = terrainFsm;
        this.chunkPrioritizationSystem = chunkPrioritizationSystem;
        this.meshletDestroySystem = meshletDestroySystem;
        this.chunkMeshletGenSystem = chunkMeshletGenSystem;
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

        UpdateHint hint = (UpdateHint) payload;

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

            // compute the lod of every loaded chunk
            // callback: terrainFsm.next() (MESHLET_GEN_TASK)
            chunkPrioritizationSystem.executeAsync(systemFlowExecutor);
        }
        //</editor-fold>

        //<editor-fold desc="process MESHLET_GEN_TASK -> IDLE">
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.MESHLET_GEN_TASK && !chunkMeshletGenSystem.isExecuting()) {
            chunkMeshletGenSystem.getSystem().setLod(terrainFsm.getMeshletGenCounter());
            // callback: terrainFsm.next() (MESHLET_GEN_TASK; finally IDLE); it also increments meshlet gen counter
            chunkMeshletGenSystem.executeAsync(systemFlowExecutor);
        }
        //</editor-fold>

        //<editor-fold desc="trigger MESHLET_DESTROY_TASK -> IDLE">
        // chunksDestroyedLastFrame will be modified at the end of update by ChunkDestroyCallback
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && !hint.chunkDelta.chunksDestroyedLastFrame.isEmpty()) {
            terrainFsm.destroyMeshlets();
            // callback: terrainFsm.next() (IDLE)
            // must be blocking to prevent chunksDestroyedLastFrame from being modified (race)
            meshletDestroySystem.execute();
        }
        //</editor-fold>

        return false;
    }
}
