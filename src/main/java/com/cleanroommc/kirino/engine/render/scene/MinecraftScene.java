package com.cleanroommc.kirino.engine.render.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.IEntityDestroyCallback;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.engine.render.task.system.ChunkPrioritizationSystem;
import com.cleanroommc.kirino.engine.render.task.system.MeshletDebugSystem;
import com.cleanroommc.kirino.engine.render.task.system.MeshletDestroySystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.time.StopWatch;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MinecraftScene extends CleanWorld {

    record ChunkPosKey(int x, int y, int z) {
    }

    // will be executed at the end of the update
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

    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;
    private final ChunkPrioritizationSystem chunkPrioritizationSystem;
    private final ChunkMeshletGenSystem chunkMeshletGenSystem;
    private final MeshletDestroySystem meshletDestroySystem;
    private final MeshletDebugSystem meshletDebugSystem;

    private final ChunkDestroyCallback chunkDestroyCallback;
    private final List<ChunkPosKey> chunksDestroyedLastFrame;

    public MinecraftScene(EntityManager entityManager, JobScheduler jobScheduler, GizmosManager gizmosManager, MinecraftCamera camera) {
        super(entityManager, jobScheduler);
        this.gizmosManager = gizmosManager;
        this.camera = camera;
        chunkPrioritizationSystem = new ChunkPrioritizationSystem(camera);
        chunkMeshletGenSystem = new ChunkMeshletGenSystem(gizmosManager);
        meshletDestroySystem = new MeshletDestroySystem();
        meshletDebugSystem = new MeshletDebugSystem(gizmosManager);

        chunksDestroyedLastFrame = new ArrayList<>();
        chunkDestroyCallback = new ChunkDestroyCallback(chunksDestroyedLastFrame);
    }

    private int newWorldFrameCounter = 0;
    private boolean newWorld = false;
    private boolean newChunksAdded = false;
    private boolean rebuildWorld = false;
    private ChunkProviderClient chunkProvider = null;
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    public void tryUpdateChunkProvider(ChunkProviderClient chunkProvider) {
        if (this.chunkProvider != chunkProvider) {
            rebuildWorld = true;
            this.chunkProvider = chunkProvider;
            this.chunkProvider.loadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkComponent chunkComponent = new ChunkComponent();
                    chunkComponent.chunkPosX = x;
                    chunkComponent.chunkPosY = i;
                    chunkComponent.chunkPosZ = z;
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(chunkDestroyCallback, chunkComponent));
                }
                newChunksAdded = true;
            };
            this.chunkProvider.unloadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    ChunkPosKey key = new ChunkPosKey(x, i, z);
                    CleanEntityHandle handle = chunkHandles.get(key);
                    if (handle != null) {
                        handle.tryDestroy();
                        chunkHandles.remove(key);
                    }
                }
            };
            chunkMeshletGenSystem.setChunkProvider(this.chunkProvider);
            // all changes are buffered and will be consumed at the end of this update
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

    private boolean chunkMeshletGen = false;
    private int lodLevelToBeWorkedOn = 0;

    @Override
    public void update() {
        if (rebuildWorld) {
            rebuildWorld = false;
            for (CleanEntityHandle handle : chunkHandles.values()) {
                handle.tryDestroy();
            }
            chunkHandles.clear();
            for (Long chunkKey : chunkProvider.getLoadedChunks().keySet()) {
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

        boolean chunkUpdate = cameraMoved || renderDisChanged || newWorld || newChunksAdded;

        if (chunkUpdate) {
            chunkMeshletGen = true;
            lodLevelToBeWorkedOn = 0;

            if (newChunksAdded) {
                newChunksAdded = false;
            }

            // compute the lod of every loaded chunk
            chunkPrioritizationSystem.update(entityManager, jobScheduler);
        } else {
            // lod fallout distance: 16
            if (++lodLevelToBeWorkedOn >= renderDis) {
                lodLevelToBeWorkedOn = 0;
                chunkMeshletGen = false;
            }
        }

        if (chunkMeshletGen) {
            chunkMeshletGenSystem.setLod(lodLevelToBeWorkedOn);
            chunkMeshletGenSystem.update(entityManager, jobScheduler);
        }

        if (chunkUpdate || cameraChanged) {
            // basic culling
        }

        if (newWorld) {
            newWorld = false;
        }

        // debug
        if (++counter % 18 == 0) {
            gizmosManager.clearBlocks();

            StopWatch stopWatch = StopWatch.createStarted();
            meshletDebugSystem.update(entityManager, jobScheduler);
            stopWatch.stop();
//            KirinoCore.LOGGER.info("stopwatch: " + stopWatch.getTime(TimeUnit.MILLISECONDS));
        }

        if (!chunksDestroyedLastFrame.isEmpty()) {
            for (ChunkPosKey chunkPos : chunksDestroyedLastFrame) {
                meshletDestroySystem.setTargetChunkPosX(chunkPos.x);
                meshletDestroySystem.setTargetChunkPosY(chunkPos.y);
                meshletDestroySystem.setTargetChunkPosZ(chunkPos.z);
                meshletDestroySystem.update(entityManager, jobScheduler);
            }
            chunksDestroyedLastFrame.clear();
        }

        super.update();
    }

    static int counter = 0;
}
