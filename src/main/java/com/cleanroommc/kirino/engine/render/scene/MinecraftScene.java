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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class MinecraftScene extends CleanWorld {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;

    public MinecraftScene(EntityManager entityManager, JobScheduler jobScheduler, GizmosManager gizmosManager, MinecraftCamera camera) {
        super(entityManager, jobScheduler);
        this.gizmosManager = gizmosManager;
        this.camera = camera;
    }

    static class ChunkDestroyCallback implements IEntityDestroyCallback {
        @Override
        public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
            ChunkComponent chunkComponent = (ChunkComponent) destroyContext.getComponent(ChunkComponent.class);
            //KirinoCore.LOGGER.info("chunk destroyed: " + chunkComponent.chunkPosX + ", " + chunkComponent.chunkPosY + ", " + chunkComponent.chunkPosZ);
        }
    }

    record ChunkPosKey(int x, int y, int z) {
    }

    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    private static final ChunkDestroyCallback CHUNK_DESTROY_CALLBACK = new ChunkDestroyCallback();

    private boolean rebuildWorld = false;
    private ChunkProviderClient chunkProvider = null;

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
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(CHUNK_DESTROY_CALLBACK, chunkComponent));
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
            // all changes are buffered and will be consumed at the end of this update
        }
    }

    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {
    }

    public void notifyLightUpdate(int x, int y, int z) {
    }

    // temp
    ChunkPrioritizationSystem chunkPrioritizationSystem = null;
    ChunkMeshletGenSystem chunkMeshletGenSystem = null;

    private boolean newWorld = true;
    private boolean newChunksAdded = false;
    private float oldCamX = 0f, oldCamY = 0f, oldCamZ = 0f;
    private int oldRenderDis = 0;
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
                    chunkHandles.put(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ), entityManager.createEntity(CHUNK_DESTROY_CALLBACK, chunkComponent));
                }
            }
            entityManager.flush();
            newWorld = true;
            return;
        }

        if (chunkPrioritizationSystem == null) {
            chunkPrioritizationSystem = new ChunkPrioritizationSystem(camera);
        }
        if (chunkMeshletGenSystem == null) {
            chunkMeshletGenSystem = new ChunkMeshletGenSystem(chunkProvider, gizmosManager);
        }

        Vector3f camPos = camera.getWorldOffset();
        boolean cameraMoved = false;
        if (camPos.x != oldCamX || camPos.y != oldCamY || camPos.z != oldCamZ) {
            cameraMoved = true;
            oldCamX = camPos.x;
            oldCamY = camPos.y;
            oldCamZ = camPos.z;
        }

        int renderDis = MINECRAFT.gameSettings.renderDistanceChunks;
        boolean renderDisChanged = false;
        if (oldRenderDis != renderDis) {
            renderDisChanged = true;
            oldRenderDis = renderDis;
        }

        if (cameraMoved || renderDisChanged || newWorld || newChunksAdded) {
            chunkMeshletGen = true;
            lodLevelToBeWorkedOn = 0;

            if (newChunksAdded) {
                newChunksAdded = false;
            }

            chunkPrioritizationSystem.update(entityManager, jobScheduler);
        } else {
            lodLevelToBeWorkedOn++;
            // lod fallout distance: 16
            if (lodLevelToBeWorkedOn >= renderDis) {
                lodLevelToBeWorkedOn = 0;
                chunkMeshletGen = false;
            }
        }

        if (chunkMeshletGen) {
            chunkMeshletGenSystem.setLod(lodLevelToBeWorkedOn);
            chunkMeshletGenSystem.update(entityManager, jobScheduler);

            KirinoCore.LOGGER.info("meshlets gen'd");
        }

        if (newWorld) {
            newWorld = false;
        }

        super.update();
    }
}
