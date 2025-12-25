package com.cleanroommc.kirino.engine.render.scene;

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
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.engine.render.task.system.ChunkPrioritizationSystem;
import com.cleanroommc.kirino.engine.render.task.system.MeshletDebugSystem;
import com.cleanroommc.kirino.engine.render.task.system.MeshletDestroySystem;
import com.cleanroommc.kirino.utils.Reference;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.ChunkPos;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

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

        private final MeshletGpuRegistry meshletGpuRegistry;

        MeshletDestroyCallback(MeshletGpuRegistry meshletGpuRegistry) {
            this.meshletGpuRegistry = meshletGpuRegistry;
        }

        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            MeshletComponent meshletComponent = (MeshletComponent) destroyContext.getComponent(MeshletComponent.class);
            meshletGpuRegistry.disposeMeshletID(meshletComponent.gpuId);
        }
    }

    // ----------------------------------------

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final Executor systemFlowExecutor;

    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;

    private final TerrainFSM terrainFsm;

    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<MeshletDebugSystem> meshletDebugSystem;

    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;

    private final List<ChunkPosKey> chunksDestroyedLastFrame;

    private int newWorldFrameCounter = 0;
    private final AtomicBoolean newChunksAdded = new AtomicBoolean(false); // not necessarily thread-safe, but must be a reference
    private boolean newWorld = false;
    private boolean rebuildWorld = false;
    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    public MinecraftScene(
            EntityManager entityManager,
            JobScheduler jobScheduler,
            Reference<BlockMeshGenerator> blockMeshGenerator,
            GizmosManager gizmosManager,
            MinecraftCamera camera,
            MeshletGpuRegistry meshletGpuRegistry,
            Executor systemFlowExecutor,
            Executor systemExecutor) {

        super(entityManager, jobScheduler);
        this.systemFlowExecutor = systemFlowExecutor;
        this.gizmosManager = gizmosManager;
        this.camera = camera;

        terrainFsm = new TerrainFSM();

        chunkPrioritizationSystem = SingleFlow.newBuilder(this, ChunkPrioritizationSystem.class)
                .addTransition(new ChunkPrioritizationSystem(camera, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFsm::next)
                .build();

        chunkMeshletGenSystem = SingleFlow.newBuilder(this, ChunkMeshletGenSystem.class)
                .addTransition(new ChunkMeshletGenSystem(gizmosManager, blockMeshGenerator, meshletGpuRegistry, new MeshletDestroyCallback(meshletGpuRegistry), systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
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
                .addTransition(new MeshletDebugSystem(gizmosManager, systemExecutor), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();
    }

    public void tryUpdateWorld(WorldClient minecraftWorld) {
        if (minecraftWorld != null && minecraftChunkProvider != minecraftWorld.getChunkProvider()) {
            rebuildWorld = true;
            this.minecraftWorld = minecraftWorld;
            minecraftChunkProvider = minecraftWorld.getChunkProvider();
            minecraftChunkProvider.loadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkComponent chunkComponent = new ChunkComponent();
                    chunkComponent.chunkPosX = x;
                    chunkComponent.chunkPosY = i;
                    chunkComponent.chunkPosZ = z;
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
                }
            };
            minecraftChunkProvider.unloadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkPosKey key = new ChunkPosKey(x, i, z);
                    CleanEntityHandle handle = chunkHandles.get(key);
                    if (handle != null) {
                        handle.tryDestroy();
                        chunkHandles.remove(key);
                    }
                }
            };
            chunkMeshletGenSystem.getSystem().setChunkProvider(minecraftChunkProvider);
            chunkMeshletGenSystem.getSystem().setWorld(minecraftWorld);
            // all changes are buffered and will be consumed at the end of the update
        }
    }

    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {

    }

    public void notifyLightUpdate(int x, int y, int z) {

    }

    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldRenderDis = 0;
    private final FloatBuffer oldViewRot = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer oldProjection = BufferUtils.createFloatBuffer(16);

    @Override
    public void update() {
        if (minecraftWorld == null) {
            return;
        }
        if (rebuildWorld) {
            rebuildWorld = false;
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
                    chunkHandles.put(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ), entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
                }
            }
            entityManager.flush();
            newWorld = true;
            return;
        }
        if (newWorld) {
            if (newWorldFrameCounter++ >= KirinoCore.KIRINO_CONFIG_HUB.worldInitFrames) {
                newWorldFrameCounter = 0;
            } else {
                entityManager.flush();
                return;
            }
        }

        Vector3f camPos = camera.getWorldOffset();
        boolean cameraMoved = false;
        if (camPos.x != oldCamX || camPos.y != oldCamY || camPos.z != oldCamZ) {
            if (Math.sqrt(
                    (camPos.x - oldCamX) * (camPos.x - oldCamX) +
                    (camPos.y - oldCamY) * (camPos.y - oldCamY) +
                    (camPos.z - oldCamZ) * (camPos.z - oldCamZ)) >= KirinoCore.KIRINO_CONFIG_HUB.chunkUpdateDisplacement) {
                cameraMoved = true;
                oldCamX = camPos.x;
                oldCamY = camPos.y;
                oldCamZ = camPos.z;
            }
        }

        int renderDis = MINECRAFT.gameSettings.renderDistanceChunks;
        boolean renderDisChanged = false;
        if (oldRenderDis != renderDis) {
            renderDisChanged = true;
            oldRenderDis = renderDis;
        }

        FloatBuffer viewRot = camera.getViewRotationBuffer();
        FloatBuffer projection = camera.getProjectionBuffer();
        boolean cameraChanged = false;
        if (!oldViewRot.equals(viewRot) || !oldProjection.equals(projection)) {
            cameraChanged = true;
            oldViewRot.position(0).put(viewRot).flip();
            oldProjection.position(0).put(projection).flip();
        }

        boolean chunkPopulationChange = cameraMoved || renderDisChanged || newWorld || newChunksAdded.get();

        if (terrainFsm.getState() == TerrainFSM.State.IDLE && chunkPopulationChange) {
            // consume new chunks
            newChunksAdded.compareAndSet(true, false);

            // lod fallout distance = 16
            // so the counter target is also renderDis
            terrainFsm.setMeshletGenCounter(renderDis);
            terrainFsm.prioritizeChunks();

            // compute the lod of every loaded chunk
            // callback: terrainFSM.next()
            chunkPrioritizationSystem.executeAsync(systemFlowExecutor);
        }

        if (terrainFsm.getState() == TerrainFSM.State.MESHLET_GEN_TASK) {
            chunkMeshletGenSystem.getSystem().setLod(terrainFsm.getMeshletGenCounter());
            // callback: terrainFSM.next()
            chunkMeshletGenSystem.executeAsync(systemFlowExecutor);
        }

        if (chunkPopulationChange || cameraChanged) {
            // basic culling
            // todo: integrate MinecraftCulling
        }

        // consume new world
        if (newWorld) {
            newWorld = false;
        }

        if (terrainFsm.getState() == TerrainFSM.State.IDLE && !chunksDestroyedLastFrame.isEmpty()) {
            terrainFsm.destroyMeshlets();
            // callback: terrainFSM.next()
            // must be blocking to prevent chunksDestroyedLastFrame from being modified (race)
            meshletDestroySystem.execute();
        }

        super.update();
    }
}
