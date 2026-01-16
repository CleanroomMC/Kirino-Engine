package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.callback.IEntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.IEntityDestroyCallback;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.graphics.view.GraphicsWorldViewImpl;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuWriterContext;
import com.cleanroommc.kirino.engine.render.platform.task.system.*;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.ChunkPos;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class MinecraftScene extends CleanWorld {

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

    // ----------------------------------------

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final ResourceStorage storage;

    private final Executor systemFlowExecutor;

    private final ResourceSlot<GizmosManager> gizmosManager;
    private final MinecraftCamera camera;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

    private final TerrainCpuPipelineFSM terrainFsm;
    private final MeshletGpuPipelineFSM meshletFsm;
    private final WorldControlFSM worldFsm;

    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<MeshletDebugSystem> meshletDebugSystem;
    private final SingleFlow<MeshletBufferWriteSystem> meshletBufferWriteSystem;

    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;

    private final List<ChunkPosKey> chunksDestroyedLastFrame;

    private int newWorldFrameCounter = 0;
    private final AtomicBoolean newChunksAdded = new AtomicBoolean(false); // not necessarily thread-safe, but must be a reference
    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    public MinecraftScene(
            ResourceStorage storage,
            EntityManager entityManager,
            JobScheduler jobScheduler,
            ResourceSlot<BlockMeshGenerator> blockMeshGenerator,
            ResourceSlot<GizmosManager> gizmosManager,
            MinecraftCamera camera,
            ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry,
            Executor systemFlowExecutor,
            Executor systemExecutor) {

        super(entityManager, jobScheduler);

        this.storage = storage;

        this.systemFlowExecutor = systemFlowExecutor;
        this.gizmosManager = gizmosManager;
        this.camera = camera;
        this.meshletGpuRegistry = meshletGpuRegistry;

        terrainFsm = new TerrainCpuPipelineFSM();
        meshletFsm = new MeshletGpuPipelineFSM();
        worldFsm = new WorldControlFSM();

        chunkPrioritizationSystem = SingleFlow.newBuilder(this, ChunkPrioritizationSystem.class)
                .addTransition(new ChunkPrioritizationSystem(camera, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFsm::next)
                .build();

        chunkMeshletGenSystem = SingleFlow.newBuilder(this, ChunkMeshletGenSystem.class)
                .addTransition(new ChunkMeshletGenSystem(storage, gizmosManager, blockMeshGenerator, meshletGpuRegistry, new MeshletDestroyCallback(storage, meshletGpuRegistry), systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
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
                    storage.get(meshletGpuRegistry).finishWriting(); // may not finish immediately, wait for compute result
                    computeReady = true;
                })
                .build();
    }

    /**
     * Block update hook. May be triggered at any time.
     *
     * @see KirinoCore#RenderGlobal$notifyBlockUpdate(int, int, int, IBlockState, IBlockState)
     */
    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {

    }

    /**
     * Light update hook. May be triggered at any time.
     *
     * @see KirinoCore#RenderGlobal$notifyLightUpdate(int, int, int)
     */
    public void notifyLightUpdate(int x, int y, int z) {

    }

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
            minecraftChunkProvider.loadChunkCallback = (x, z) -> {
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
            };
            minecraftChunkProvider.unloadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkPosKey key = new ChunkPosKey(x, i, z);
                    CleanEntityHandle handle = chunkHandles.get(key);
                    if (handle != null) {
                        // all changes are buffered and will be consumed at the end of the update - EntityManager.flush() to be exact
                        handle.tryDestroy();
                        chunkHandles.remove(key);
                    }
                }
            };
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
        for (Long chunkKey : minecraftChunkProvider.getLoadedChunks().keySet()) {
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

    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldRenderDis = 0;
    private final FloatBuffer oldViewRot = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer oldProjection = BufferUtils.createFloatBuffer(16);

    private boolean updateCameraPos() {
        Vector3f camPos = camera.getWorldOffset();
        if (camPos.x != oldCamX || camPos.y != oldCamY || camPos.z != oldCamZ) {
            if (Math.sqrt(
                    (camPos.x - oldCamX) * (camPos.x - oldCamX) +
                            (camPos.y - oldCamY) * (camPos.y - oldCamY) +
                            (camPos.z - oldCamZ) * (camPos.z - oldCamZ)) >= KirinoCore.KIRINO_CONFIG_HUB.chunkUpdateDisplacement) {
                oldCamX = camPos.x;
                oldCamY = camPos.y;
                oldCamZ = camPos.z;
                return true;
            }
        }
        return false;
    }

    private boolean updateRenderDis() {
        int renderDis = MINECRAFT.gameSettings.renderDistanceChunks;
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

        // test
        if (terrainFsm.getState() == TerrainCpuPipelineFSM.State.IDLE) {
            if (debug) {
                debug = false;

                storage.get(meshletGpuRegistry).beginWriting();
                // callback: meshletGpuRegistry.finishWriting(); computeReady = true
                meshletBufferWriteSystem.executeAsync(systemFlowExecutor);
            }
        }

        if (computeReady) {
            if (compute) {
                compute = false;

                KirinoCore.LOGGER.info("start compute");
                if (ssboOut1 == null) {
                    ssboOut1 = new SSBOView(new GLBuffer());
                    ssboOut2 = new SSBOView(new GLBuffer());
                    ByteBuffer byteBufferOut1 = BufferUtils.createByteBuffer(storage.get(meshletGpuRegistry).getMeshletCount() * 256 * 32);
                    ByteBuffer byteBufferOut2 = BufferUtils.createByteBuffer(storage.get(meshletGpuRegistry).getMeshletCount() * 256 * 36 * 4);

                    ssboOut1.bind();
                    ssboOut1.uploadDirectly(byteBufferOut1); // automatically visible
                    ssboOut2.bind();
                    ssboOut2.uploadDirectly(byteBufferOut2); // automatically visible

                    GL30.glBindBufferBase(storage.get(meshletGpuRegistry).getConsumeTarget().target(), 0, storage.get(meshletGpuRegistry).getConsumeTarget().bufferID);
                    GL30.glBindBufferBase(ssboOut1.target(), 1, ssboOut1.bufferID);
                    GL30.glBindBufferBase(ssboOut2.target(), 2, ssboOut2.bufferID);

                    computeShaderProgram.use();

                    GL42.glMemoryBarrier(GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT); // make persistently mapped buffer visible
                    GL43.glDispatchCompute(1, 1, 1);
                    GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT); // only needed for subsequent ssbo reading in shaders

                    long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
                    // block
                    int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000_000L);
                    if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
                        KirinoCore.LOGGER.info("finished compute");
                        GraphicsWorldViewImpl.debug = true;
                    }
                    GL32C.glDeleteSync(fence);
                }
            }
        }

        super.update();
    }

    static boolean debug = true;
    static boolean computeReady = false;
    static boolean compute = true;
    static SSBOView ssboOut1 = null;
    static SSBOView ssboOut2 = null;
    public static ShaderProgram computeShaderProgram;
}
