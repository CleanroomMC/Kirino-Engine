package com.cleanroommc.kirino.engine.render.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.IEntityDestroyCallback;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.minecraft.utils.BlockMeshGenerator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public class MinecraftScene extends CleanWorld {

    public record ChunkPosKey(int x, int y, int z) {
    }

    // will be executed at the end of the update - EntityManager.flush() to be exact
    static class ChunkDestroyCallback implements IEntityDestroyCallback {
        private final List<ChunkPosKey> chunksDestroyedLastFrame;

        public ChunkDestroyCallback(List<ChunkPosKey> chunksDestroyedLastFrame) {
            this.chunksDestroyedLastFrame = chunksDestroyedLastFrame;
        }

        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            ChunkComponent chunkComponent = (ChunkComponent) destroyContext.getComponent(ChunkComponent.class);
            chunksDestroyedLastFrame.add(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ));
        }
    }

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final Executor systemFlowExecutor;

    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;

    private final TerrainFSM terrainFSM;

    private final SingleFlow<ChunkPrioritizationSystem> chunkPrioritizationSystem;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;
    private final SingleFlow<MeshletDestroySystem> meshletDestroySystem;
    private final SingleFlow<MeshletDebugSystem> meshletDebugSystem;

    private final ChunkDestroyCallback chunkDestroyCallback;
    private final List<ChunkPosKey> chunksDestroyedLastFrame;

    public MinecraftScene(
            EntityManager entityManager,
            JobScheduler jobScheduler,
            Reference<BlockMeshGenerator> blockMeshGenerator,
            GizmosManager gizmosManager,
            MinecraftCamera camera,
            Executor systemFlowExecutor) {

        super(entityManager, jobScheduler);
        this.systemFlowExecutor = systemFlowExecutor;
        this.gizmosManager = gizmosManager;
        this.camera = camera;

        terrainFSM = new TerrainFSM();

        chunkPrioritizationSystem = SingleFlow.newBuilder(this, ChunkPrioritizationSystem.class)
                .addTransition(new ChunkPrioritizationSystem(camera), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFSM::next)
                .build();

        chunkMeshletGenSystem = SingleFlow.newBuilder(this, ChunkMeshletGenSystem.class)
                .addTransition(new ChunkMeshletGenSystem(gizmosManager, blockMeshGenerator), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(terrainFSM::next)
                .build();

        chunksDestroyedLastFrame = new CopyOnWriteArrayList<>();
        chunkDestroyCallback = new ChunkDestroyCallback(chunksDestroyedLastFrame);

        meshletDestroySystem = SingleFlow.newBuilder(this, MeshletDestroySystem.class)
                .addTransition(new MeshletDestroySystem(chunksDestroyedLastFrame), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .setFinishCallback(() -> {
                    chunksDestroyedLastFrame.clear();
                    terrainFSM.next();
                })
                .build();

        meshletDebugSystem = SingleFlow.newBuilder(this, MeshletDebugSystem.class)
                .addTransition(new MeshletDebugSystem(gizmosManager), SingleFlow.START_NODE, SingleFlow.END_NODE)
                .build();
    }

    private int newWorldFrameCounter = 0;
    private boolean newWorld = false;
    private boolean newChunksAdded = false;
    private boolean rebuildWorld = false;
    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

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
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(chunkDestroyCallback, chunkComponent));
                }

                // todo: move it to ecs EntityManager add callback
                newChunksAdded = true;
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
                    chunkHandles.put(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ), entityManager.createEntity(chunkDestroyCallback, chunkComponent));
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

        boolean chunkPopulationChange = cameraMoved || renderDisChanged || newWorld || newChunksAdded;

        if (terrainFSM.getState() == TerrainFSM.State.IDLE && chunkPopulationChange) {
            // consume new chunks
            if (newChunksAdded) {
                newChunksAdded = false;
            }

            // lod fallout distance = 16
            // so the counter target is also renderDis
            terrainFSM.setMeshletGenCounter(renderDis);
            terrainFSM.prioritizeChunks();

            // compute the lod of every loaded chunk
            // callback: terrainFSM.next()
            chunkPrioritizationSystem.executeAsync(systemFlowExecutor);
        }

        if (terrainFSM.getState() == TerrainFSM.State.MESHLET_GEN_TASK) {
            chunkMeshletGenSystem.getSystem().setLod(terrainFSM.getMeshletGenCounter());
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

        // debug
        if (++counter % 18 == 0) {
            gizmosManager.clearBlocks();
            meshletDebugSystem.execute();
        }

        if (terrainFSM.getState() == TerrainFSM.State.IDLE && !chunksDestroyedLastFrame.isEmpty()) {
            terrainFSM.destroyMeshlets();
            // callback: terrainFSM.next()
            meshletDestroySystem.executeAsync(systemFlowExecutor);
        }

        super.update();
    }

    static int counter = 0;
}
