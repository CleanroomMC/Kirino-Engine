package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.platform.debug.data.impl.MeshletGpuTimeline;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuWriterContext;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletRenderPayload;
import com.cleanroommc.kirino.engine.render.platform.task.system.*;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import java.lang.invoke.MethodHandle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class MinecraftScene extends CleanWorld {

    //<editor-fold desc="inner classes">
    public record ChunkPosKey(int x, int y, int z) {
    }

    // callbacks will be executed at the end of the update - EntityManager.flush() to be exact

    static class ChunkDestroyCallback implements EntityDestroyCallback {

        private final List<ChunkPosKey> chunksDestroyedLastFrame;

        ChunkDestroyCallback(List<ChunkPosKey> chunksDestroyedLastFrame) {
            this.chunksDestroyedLastFrame = chunksDestroyedLastFrame;
        }

        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            ChunkComponent chunkComponent = (ChunkComponent) destroyContext.getComponent(ChunkComponent.class);
            chunksDestroyedLastFrame.add(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ));
        }
    }

    static class ChunkCreateCallback implements EntityCreateCallback {

        private final AtomicBoolean newChunksAdded;

        ChunkCreateCallback(AtomicBoolean newChunksAdded) {
            this.newChunksAdded = newChunksAdded;
        }

        @Override
        public void beforeCreate(@NonNull EntityCreateContext createContext) {
            newChunksAdded.set(true);
        }
    }

    public static class MeshletDestroyCallback implements EntityDestroyCallback {

        private final ResourceStorage storage;
        private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

        MeshletDestroyCallback(ResourceStorage storage, ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {
            this.storage = storage;
            this.meshletGpuRegistry = meshletGpuRegistry;
        }

        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            MeshletComponent meshletComponent = (MeshletComponent) destroyContext.getComponent(MeshletComponent.class);
            storage.get(meshletGpuRegistry).disposeMeshletID(meshletComponent.meshletId);
        }
    }

    public static class MeshletCreateCallback implements EntityCreateCallback {

        private final ResourceStorage storage;
        private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

        MeshletCreateCallback(ResourceStorage storage, ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {
            this.storage = storage;
            this.meshletGpuRegistry = meshletGpuRegistry;
        }

        @Override
        public void beforeCreate(@NonNull EntityCreateContext createContext) {
            MeshletComponent meshletComponent = (MeshletComponent) createContext.getComponent(MeshletComponent.class);
            storage.get(meshletGpuRegistry).allocateMeshletID(meshletComponent);
        }
    }
    //</editor-fold>

    private final ResourceStorage storage;

    private final Executor systemFlowExecutor;

    private final ResourceSlot<GizmosManager> gizmosManager;
    private final MinecraftCamera camera;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry; // allocate/dispose id at the end of update by the callback
    private final ResourceSlot<MeshletComputeSystem> meshletComputeSystem;

    // fsm
    private final TerrainCpuPipelineFSM terrainFsm;
    private final MeshletGpuPipelineFSM meshletFsm;
    private final WorldControlFSM worldFsm;

    // system flows
    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<MeshletDebugSystem> meshletDebugSystem;
    private final SingleFlow<MeshletBufferWriteSystem> meshletBufferWriteSystem;

    // callbacks
    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;
    private final List<ChunkPosKey> chunksDestroyedLastFrame; // will be modified at the end of update by the callback
    private final AtomicBoolean newChunksAdded = new AtomicBoolean(false); // will be modified at the end of update by the callback

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

        chunksDestroyedLastFrame = new ArrayList<>();
        chunkDestroyCallback = new ChunkDestroyCallback(chunksDestroyedLastFrame);
        chunkCreateCallback = new ChunkCreateCallback(newChunksAdded);

        meshletDestroySystem = SingleFlow.newBuilder(this, MeshletDestroySystem.class)
                .addTransition(new MeshletDestroySystem(chunksDestroyedLastFrame, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(() -> {
                    chunksDestroyedLastFrame.clear();
                    terrainFsm.next();
                })
                .build();

        meshletDebugSystem = SingleFlow.newBuilder(this, MeshletDebugSystem.class)
                .addTransition(new MeshletDebugSystem(storage, gizmosManager, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();

        meshletBufferWriteSystem = SingleFlow.newBuilder(this, MeshletBufferWriteSystem.class)
                .addTransition(new MeshletBufferWriteSystem(new MeshletGpuWriterContext(storage, meshletGpuRegistry), systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();
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

    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private Map<Long, Chunk> cachedEarlyChunks = null; // put when loading a new world; consumed right after
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    //<editor-fold desc="world related methods">
    /**
     * Must call this method and then {@link #update()} in a row.
     *
     * @param minecraftWorld The world from <code>Minecraft.getMinecraft().world</code>
     */
    public void tryUpdateWorld(WorldClient minecraftWorld) {
        // switching dim will trigger this too
        if (minecraftWorld != null && minecraftChunkProvider != minecraftWorld.getChunkProvider()) {

            // entrypoint: load in a new world
            KirinoClientDebug.MeshletGpuTimeline$loadInNewWorld();

            worldFsm.reset(); // NO_WORLD
            this.minecraftWorld = minecraftWorld;
            minecraftChunkProvider = minecraftWorld.getChunkProvider();

            cachedEarlyChunks = new HashMap<>(MethodHolder.getLoadedChunks(minecraftChunkProvider));

            MethodHolder.setLoadChunkCallback(minecraftChunkProvider, (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkComponent chunkComponent = new ChunkComponent();
                    chunkComponent.chunkPosX = x;
                    chunkComponent.chunkPosY = i;
                    chunkComponent.chunkPosZ = z;
                    chunkHandles.put(
                            new ChunkPosKey(x, i, z),
                            // all changes are buffered and will be consumed at the end of the update - EntityManager.flush() to be exact
                            entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
                }
            });

            MethodHolder.setUnloadChunkCallback(minecraftChunkProvider, (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkPosKey key = new ChunkPosKey(x, i, z);
                    CleanEntityHandle handle = chunkHandles.get(key);
                    if (handle != null) {
                        // all changes are buffered and will be consumed at the end of the update - EntityManager.flush() to be exact
                        handle.tryDestroy();
                        chunkHandles.remove(key);
                    }
                }
            });

            chunkMeshletGenSystem.getSystem().setChunkProvider(minecraftChunkProvider);
            chunkMeshletGenSystem.getSystem().setWorld(minecraftWorld);
            worldFsm.next(); // NEW_WORLD_REBUILD
        }
    }

    private void rebuildWorld() {
        // destroy existing chunk components
        for (CleanEntityHandle handle : chunkHandles.values()) {
            handle.tryDestroy();
        }
        chunkHandles.clear();

        // todo: destroy exisiting meshlet components

        // add early chunks (for those chunks that were there before load/unload callback setup)
        for (Long chunkKey : cachedEarlyChunks.keySet()) {
            for (int i = 0; i < 16; i++) {
                ChunkComponent chunkComponent = new ChunkComponent();
                chunkComponent.chunkPosX = ChunkPos.getX(chunkKey);
                chunkComponent.chunkPosY = i;
                chunkComponent.chunkPosZ = ChunkPos.getZ(chunkKey);
                chunkHandles.put(
                        new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ),
                        entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
            }
        }

        worldFsm.next(); // NEW_WORLD_INITIAL_WAIT
        terrainFsm.reset();
        meshletFsm.reset();
    }
    //</editor-fold>

    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldForegroundRenderDis = 0;
    private final FloatBuffer oldViewRot = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer oldProjection = BufferUtils.createFloatBuffer(16);

    //<editor-fold desc="diffing">
    private boolean updateCameraPos() {
        Vector3f camPos = camera.getWorldOffset();
        if (camPos.x != oldCamX || camPos.y != oldCamY || camPos.z != oldCamZ) {
            if (Math.sqrt(
                    (camPos.x - oldCamX) * (camPos.x - oldCamX) +
                            (camPos.y - oldCamY) * (camPos.y - oldCamY) +
                            (camPos.z - oldCamZ) * (camPos.z - oldCamZ)) >= KirinoCommonCore.KIRINO_CONFIG_HUB.getChunkUpdateDisplacement()) {
                oldCamX = camPos.x;
                oldCamY = camPos.y;
                oldCamZ = camPos.z;
                return true;
            }
        }
        return false;
    }

    private boolean updateForegroundRenderDis() {
        int renderDis = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
        renderDis = Math.max(renderDis, KirinoCommonCore.KIRINO_CONFIG_HUB.getForegroundRenderDistance());
        if (oldForegroundRenderDis != renderDis) {
            oldForegroundRenderDis = renderDis;
            return true;
        }
        return false;
    }

    private boolean updateCameraViewProj() {
        FloatBuffer viewRot = camera.getViewRotationBuffer();
        FloatBuffer projection = camera.getProjectionBuffer();
        if (!oldViewRot.equals(viewRot) || !oldProjection.equals(projection)) {
            oldViewRot.position(0).put(viewRot).flip();
            oldProjection.position(0).put(projection).flip();
            return true;
        }
        return false;
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

    @Override
    public void update() {
        if (minecraftWorld == null) {
            return;
        }

        KirinoClientDebug.MeshletGpuTimeline$worldTick();

        //<editor-fold desc="process NEW_WORLD_REBUILD -> NEW_WORLD_INITIAL_WAIT">
        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_REBUILD) {
            rebuildWorld();
            // finish this update immediately to consume ecs-entity side effects
            super.update();
            return;
        }
        //</editor-fold>

        // this flag will be modified inside "process NEW_WORLD_INITIAL_WAIT"
        boolean newWorld = false;

        //<editor-fold desc="process NEW_WORLD_INITIAL_WAIT -> IDLE">
        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
            worldFsm.next(); // NEW_WORLD_INITIAL_WAIT or IDLE
            if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
                // wait; abort this update
                super.update();
                return;
            } else {
                newWorld = true; // continue running
            }
        }
        //</editor-fold>

        // ----- worldFsm finishes its duty here -----

        boolean cameraMoved = updateCameraPos();
        boolean renderDisChanged = updateForegroundRenderDis();
        boolean cameraChanged = updateCameraViewProj();

        // newChunksAdded will be modified at the end of update by ChunkCreateCallback
        boolean chunkPopulationChange = cameraMoved || renderDisChanged || newWorld || newChunksAdded.get();

        //<editor-fold desc="trigger CHUNK_PRIORITIZATION_TASK -> MESHLET_GEN_TASK">
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && chunkPopulationChange) {
            // consume newChunksAdded
            newChunksAdded.compareAndSet(true, false);

            // lod fallout distance = 16
            // so the counter target is also render distance
            // i.e. generate meshlets based on the loaded chunks til the render distance
            terrainFsm.setMeshletGenCounter(oldForegroundRenderDis); // we just updated the render distance; it's up to date
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

        if (chunkPopulationChange || cameraChanged) {
            // basic culling
            // todo: integrate MinecraftCulling
        }

        //<editor-fold desc="trigger MESHLET_DESTROY_TASK -> IDLE">
        // chunksDestroyedLastFrame will be modified at the end of update by ChunkDestroyCallback
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && !chunksDestroyedLastFrame.isEmpty()) {
            terrainFsm.destroyMeshlets();
            // callback: terrainFsm.next() (IDLE)
            // must be blocking to prevent chunksDestroyedLastFrame from being modified (race)
            meshletDestroySystem.execute();
        }
        //</editor-fold>

//        // test
//        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && ++counter == 18) {
//            counter = 0;
//            storage.get(gizmosManager).clearBlocks();
//            meshletDebugSystem.execute();
//        }

        // ----- terrainFsm finishes its duty here -----

        //<editor-fold desc="process INITIAL_WAIT -> IDLE">
        if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.INITIAL_WAIT) {
            meshletFsm.next(); // INITIAL_WAIT or IDLE
            if (meshletFsm.getState() == MeshletGpuPipelineFSM.State.INITIAL_WAIT) {
                // wait; abort this update
                super.update();
                return;
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

                    storage.get(meshletGpuRegistry).beginWriting();
                    meshletBufferWriteSystem.executeAsync(systemFlowExecutor);
                    meshletFsm.next(); // COMPUTABLE

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_BEGIN_WRITING);
                }
            } else {
                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.IDLE_NO_MESHLET_UPDATE);

                if (!storage.get(meshletComputeSystem).isShaderRunning()
                        && storage.get(meshletGpuRegistry).isWriting()
                        && !meshletBufferWriteSystem.isExecuting()) {
                    KirinoClientDebug.MeshletGpuTimeline$finishWriting();

                    storage.get(meshletGpuRegistry).finishWriting();
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
                    && !meshletBufferWriteSystem.isExecuting()) {
                KirinoClientDebug.MeshletGpuTimeline$finishWriting();

                storage.get(meshletGpuRegistry).finishWriting();

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_FINISH_WRITING);
            }

            // todo: fail safe
            // start dispatching if possible
            if (!storage.get(meshletComputeSystem).isShaderRunning()
                    && storage.get(meshletGpuRegistry).isFinishedWritingOnce()
                    && !storage.get(meshletGpuRegistry).isWriting()) {
                KirinoClientDebug.MeshletGpuTimeline$beginComputing();

                storage.get(meshletGpuRegistry).beginComputing();
                storage.get(meshletComputeSystem).startDispatch(storage, storage.get(meshletGpuRegistry));

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_BEGIN_COMPUTING);

                // start the next writing task right after the dispatch signal if needed. maximize throughput
                if (storage.get(meshletGpuRegistry).hasMeshletChanges()) {
                    KirinoClientDebug.MeshletGpuTimeline$beginWriting();

                    storage.get(meshletGpuRegistry).beginWriting();
                    meshletBufferWriteSystem.executeAsync(systemFlowExecutor);

                    KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_BEGIN_WRITING);
                }
            }

            // pull compute result
            if (storage.get(meshletComputeSystem).isShaderRunning()
                    && storage.get(meshletComputeSystem).tryPullResult()) {
                KirinoClientDebug.MeshletGpuTimeline$finishComputing();

                storage.get(meshletGpuRegistry).finishComputing();
                meshletRenderPayload = new MeshletRenderPayload(
                        storage.get(meshletComputeSystem).getUintVertexCount(),
                        storage.get(meshletComputeSystem).getUintIndexCount());

                // todo: move gl calls somewhere else
                // draw commands will be submitted subsequently. next update is definitely valid for the next compute (bind bases to different buffers)
                // since the next bind base is strictly after the draw commands
                GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getVertexConsumeTarget().target(), 1, storage.get(meshletGpuRegistry).getVertexConsumeTarget().bufferID);
                GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getIndexConsumeTarget().target(), 2, storage.get(meshletGpuRegistry).getIndexConsumeTarget().bufferID);
                meshletFsm.next(); // IDLE

                KirinoClientDebug.MeshletGpuTimeline$pushFrameState(MeshletGpuTimeline.State.COMPUTABLE_FINISH);
            }
        }
        //</editor-fold>

        super.update();
    }

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldSetter(ChunkProviderClient.class, "loadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldSetter(ChunkProviderClient.class, "unloadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadedChunks", "field_73236_b", Long2ObjectMap.class));

            Preconditions.checkNotNull(DELEGATE.loadChunkCallbackSetter);
            Preconditions.checkNotNull(DELEGATE.unloadChunkCallbackSetter);
            Preconditions.checkNotNull(DELEGATE.loadedChunksGetter);
        }

        static void setLoadChunkCallback(ChunkProviderClient chunkProvider, BiConsumer<Integer, Integer> callback) {
            try {
                DELEGATE.loadChunkCallbackSetter.invokeExact(chunkProvider, callback);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        static void setUnloadChunkCallback(ChunkProviderClient chunkProvider, BiConsumer<Integer, Integer> callback) {
            try {
                DELEGATE.unloadChunkCallbackSetter.invokeExact(chunkProvider, callback);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        static Long2ObjectMap<Chunk> getLoadedChunks(ChunkProviderClient chunkProvider) {
            Long2ObjectMap<Chunk> result;
            try {
                result = (Long2ObjectMap<Chunk>) DELEGATE.loadedChunksGetter.invokeExact(chunkProvider);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
                MethodHandle loadChunkCallbackSetter,
                MethodHandle unloadChunkCallbackSetter,
                MethodHandle loadedChunksGetter) {
        }
    }
}
