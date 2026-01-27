package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.callback.IEntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.IEntityDestroyCallback;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
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
import org.lwjgl.opengl.*;

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

    static class ChunkDestroyCallback implements IEntityDestroyCallback {

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

    static class ChunkCreateCallback implements IEntityCreateCallback {

        private final AtomicBoolean newChunksAdded;

        ChunkCreateCallback(AtomicBoolean newChunksAdded) {
            this.newChunksAdded = newChunksAdded;
        }

        @Override
        public void beforeCreate(@NonNull EntityCreateContext createContext) {
            newChunksAdded.set(true);
        }
    }

    public static class MeshletDestroyCallback implements IEntityDestroyCallback {

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

    public static class MeshletCreateCallback implements IEntityCreateCallback {

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
                .setFinishCallback(() -> {
                    // finish immediately after writing -> compute process -> judge if the next writing is needed (any changes?)
                    storage.get(meshletGpuRegistry).finishWriting();
                })
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
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    //<editor-fold desc="world related methods">
    /**
     * Must be called before {@link #update()}.
     *
     * @param minecraftWorld The world from <code>Minecraft.getMinecraft().world</code>
     */
    public void tryUpdateWorld(WorldClient minecraftWorld) {
        if (minecraftWorld != null && minecraftChunkProvider != minecraftWorld.getChunkProvider()) {
            worldFsm.reset(); // NO_WORLD
            this.minecraftWorld = minecraftWorld;
            minecraftChunkProvider = minecraftWorld.getChunkProvider();

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

    // todo: more rebuild, including meshlets
    private void rebuildWorld() {
        for (CleanEntityHandle handle : chunkHandles.values()) {
            handle.tryDestroy();
        }
        chunkHandles.clear();
        for (Long chunkKey : MethodHolder.getLoadedChunks(minecraftChunkProvider).keySet()) {
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
    }
    //</editor-fold>

    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldRenderDis = 0;
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

    private boolean updateRenderDis() {
        int renderDis = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
        if (oldRenderDis != renderDis) {
            oldRenderDis = renderDis;
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

    @Override
    public void update() {
        if (minecraftWorld == null) {
            return;
        }

        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_REBUILD) {
            rebuildWorld();
            // finish this update immediately to consume ecs-entity side effects
            entityManager.flush();
            return;
        }

        boolean newWorld = false;
        if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
            worldFsm.next(); // NEW_WORLD_INITIAL_WAIT or IDLE
            if (worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT) {
                // wait; abort this update
                entityManager.flush();
                return;
            } else {
                newWorld = true; // continue running
            }
        }

        boolean cameraMoved = updateCameraPos();
        boolean renderDisChanged = updateRenderDis();
        boolean cameraChanged = updateCameraViewProj();

        boolean chunkPopulationChange = cameraMoved || renderDisChanged || newWorld || newChunksAdded.get();

        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && chunkPopulationChange) {
            // consume new chunks
            newChunksAdded.compareAndSet(true, false);

            // lod fallout distance = 16
            // so the counter target is also renderDis
            terrainFsm.setMeshletGenCounter(oldRenderDis);
            terrainFsm.prioritizeChunks();

            // compute the lod of every loaded chunk
            // callback: terrainFsm.next() (MESHLET_GEN_TASK)
            chunkPrioritizationSystem.executeAsync(systemFlowExecutor);
        }

        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.MESHLET_GEN_TASK && !chunkMeshletGenSystem.isExecuting()) {
            chunkMeshletGenSystem.getSystem().setLod(terrainFsm.getMeshletGenCounter());
            // callback: terrainFsm.next() (MESHLET_GEN_TASK; finally IDLE)
            chunkMeshletGenSystem.executeAsync(systemFlowExecutor);
        }

        if (chunkPopulationChange || cameraChanged) {
            // basic culling
            // todo: integrate MinecraftCulling
        }

        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && !chunksDestroyedLastFrame.isEmpty()) {
            terrainFsm.destroyMeshlets();
            // callback: terrainFsm.next() (IDLE)
            // must be blocking to prevent chunksDestroyedLastFrame from being modified (race)
            meshletDestroySystem.execute();
        }

//        // test
//        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE && ++counter == 18) {
//            counter = 0;
//            storage.get(gizmosManager).clearBlocks();
//            meshletDebugSystem.execute();
//        }

//        // test
//        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE) {
//            if (debug) {
//                debug = false;
//
//                storage.get(meshletGpuRegistry).beginWriting();
//                // callback: meshletGpuRegistry.finishWriting(); computeReady = true
//                meshletBufferWriteSystem.executeAsync(systemFlowExecutor);
//            }
//        }

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
