package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.platform.scene.callback.*;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuWriterContext;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletRenderPayload;
import com.cleanroommc.kirino.engine.render.platform.scene.scheduler.MeshletGpuPipelineScheduler;
import com.cleanroommc.kirino.engine.render.platform.scene.scheduler.TerrainCpuPipelineScheduler;
import com.cleanroommc.kirino.engine.render.platform.scene.scheduler.WorldControlScheduler;
import com.cleanroommc.kirino.engine.render.platform.task.system.*;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;

import java.util.concurrent.Executor;

public class MinecraftScene extends CleanWorld {

    // callbacks will be executed at the end of the update - EntityManager.flush() to be exact

    private final ResourceStorage storage;

    private final Executor systemFlowExecutor;

    private final ResourceSlot<GizmosManager> gizmosManager;
    private final MinecraftCamera camera;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry; // allocate/dispose id at the end of update by the callback
    private final ResourceSlot<MeshletComputeSystem> meshletComputeSystem;

    // subsystem
    private final DiffingContainer diffing;
    private final WorldControl worldControl;

    // fsm
    private final TerrainCpuPipelineFSM terrainFsm;
    private final MeshletGpuPipelineFSM meshletFsm;
    private final WorldControlFSM worldFsm;

    // schedulers
    private final TerrainCpuPipelineScheduler terrainScheduler;
    private final MeshletGpuPipelineScheduler meshletScheduler;
    private final WorldControlScheduler worldScheduler;

    // system flows
    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<MeshletDebugSystem> meshletDebugSystem;
    private final SingleFlow<MeshletBufferWriteSystem> meshletBufferWriteSystem;

    // callbacks
    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;
    private final CallbackDrivenChunkDelta chunkDelta;

    public MinecraftScene(
            ResourceStorage storage,
            EntityManager entityManager,
            JobScheduler jobScheduler,
            ResourceSlot<BlockMeshGenerator> blockMeshGenerator,
            ResourceSlot<GizmosManager> gizmosManager,
            MinecraftCamera camera,
            ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry,
            ResourceSlot<MeshletComputeSystem> meshletComputeSystem,
            Executor systemFlowExecutor,
            Executor systemExecutor) {

        super(entityManager, jobScheduler);

        this.storage = storage;

        this.systemFlowExecutor = systemFlowExecutor;
        this.gizmosManager = gizmosManager;
        this.camera = camera;
        this.meshletGpuRegistry = meshletGpuRegistry;
        this.meshletComputeSystem = meshletComputeSystem;

        terrainFsm = new TerrainCpuPipelineFSM();
        meshletFsm = new MeshletGpuPipelineFSM();
        worldFsm = new WorldControlFSM();

        chunkPrioritizationSystem = SingleFlow.newBuilder(this, ChunkPrioritizationSystem.class)
                .addTransition(new ChunkPrioritizationSystem(camera, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFsm::next)
                .build();

        chunkMeshletGenSystem = SingleFlow.newBuilder(this, ChunkMeshletGenSystem.class)
                .addTransition(new ChunkMeshletGenSystem(storage, blockMeshGenerator, new MeshletDestroyCallback(storage, meshletGpuRegistry), new MeshletCreateCallback(storage, meshletGpuRegistry), systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFsm::next)
                .build();

        chunkDelta = new CallbackDrivenChunkDelta();
        chunkDestroyCallback = new ChunkDestroyCallback(chunkDelta);
        chunkCreateCallback = new ChunkCreateCallback(chunkDelta);

        meshletDestroySystem = SingleFlow.newBuilder(this, MeshletDestroySystem.class)
                .addTransition(new MeshletDestroySystem(chunkDelta, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(() -> {
                    chunkDelta.chunksDestroyedLastFrame.clear();
                    terrainFsm.next();
                })
                .build();

        meshletDebugSystem = SingleFlow.newBuilder(this, MeshletDebugSystem.class)
                .addTransition(new MeshletDebugSystem(storage, gizmosManager, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();

        meshletBufferWriteSystem = SingleFlow.newBuilder(this, MeshletBufferWriteSystem.class)
                .addTransition(new MeshletBufferWriteSystem(new MeshletGpuWriterContext(storage, meshletGpuRegistry), systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();

        diffing = new DiffingContainer(camera);

        worldControl = new WorldControl(
                terrainFsm,
                meshletFsm,
                worldFsm,
                entityManager,
                chunkDestroyCallback,
                chunkCreateCallback,
                chunkMeshletGenSystem);

        terrainScheduler = new TerrainCpuPipelineScheduler(
                terrainFsm,
                chunkPrioritizationSystem,
                meshletDestroySystem,
                chunkMeshletGenSystem,
                systemFlowExecutor);
        meshletScheduler = new MeshletGpuPipelineScheduler(
                meshletFsm,
                storage,
                meshletGpuRegistry,
                meshletComputeSystem,
                meshletBufferWriteSystem,
                systemFlowExecutor);
        worldScheduler = new WorldControlScheduler(
                worldFsm,
                worldControl);
    }

    //<editor-fold desc="hooks">
    /**
     * Block update hook. May be triggered at any time.
     *
     * @see KirinoClientCore#RenderGlobal$notifyBlockUpdate(int, int, int, IBlockState, IBlockState)
     */
    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {

    }

    /**
     * Light update hook. May be triggered at any time.
     *
     * @see KirinoClientCore#RenderGlobal$notifyLightUpdate(int, int, int)
     */
    public void notifyLightUpdate(int x, int y, int z) {

    }
    //</editor-fold>

    //<editor-fold desc="meshlet render info">
    private MeshletRenderPayload meshletRenderPayload = new MeshletRenderPayload(0, 0);

    public MeshletRenderPayload getMeshletRenderPayload() {
        return meshletRenderPayload;
    }

    public boolean isMeshletRenderReady() {
        return meshletFsm.isPullResultReady();
    }
    //</editor-fold>

    /**
     * Must call this method and then {@link #update()} in a row in a per frame basis.
     *
     * @param minecraftWorld The world from <code>Minecraft.getMinecraft().world</code>
     */
    public void tryUpdateWorld(WorldClient minecraftWorld) {
        worldControl.tryUpdateWorld(minecraftWorld);
    }

    @Override
    public void update() {
        if (!worldControl.isWorldReady()) {
            return;
        }

        KirinoClientDebug.MeshletGpuTimeline$worldTick();

        if (worldScheduler.update(worldScheduler.newWorldHint)) {
            super.update();
            return;
        }

        boolean newWorld = worldScheduler.newWorldHint.newWorld;
        boolean cameraMoved = diffing.updateCameraPos();
        boolean renderDisChanged = diffing.updateForegroundRenderDis();
        boolean cameraChanged = diffing.updateCameraViewProj();

        terrainScheduler.updateHint.cameraMoved = cameraMoved;
        terrainScheduler.updateHint.renderDisChanged = renderDisChanged;
        terrainScheduler.updateHint.newWorld = newWorld;
        // we just updated diffing; oldForegroundRenderDis isn't old
        terrainScheduler.updateHint.foregroundRenderDis = diffing.getOldForegroundRenderDis();
        if (terrainScheduler.updateHint.chunkDelta == null) {
            terrainScheduler.updateHint.chunkDelta = chunkDelta;
        }

        if (terrainScheduler.update(terrainScheduler.updateHint)) {
            super.update();
            return;
        }

//        if (chunkPopulationChange || cameraChanged) {
//            // basic culling
//            // todo: integrate MinecraftCulling
//        }

        // test
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && ++counter == 20 && !meshletDebugSystem.isExecuting()) {
            counter = 0;
            storage.get(gizmosManager).clearMeshlets();
            meshletDebugSystem.execute();
        }

        if (meshletScheduler.update(meshletScheduler.computeResult)) {
            super.update();
            return;
        }

        if (meshletScheduler.computeResult.update) {
            meshletRenderPayload = new MeshletRenderPayload(
                    meshletScheduler.computeResult.uintVertexCount,
                    meshletScheduler.computeResult.uintIndexCount);
        }

        super.update();
    }

    static int counter = 0;
}
