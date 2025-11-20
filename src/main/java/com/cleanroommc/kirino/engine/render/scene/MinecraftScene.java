package com.cleanroommc.kirino.engine.render.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.system.ChunkMeshletGenSystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MinecraftScene extends CleanWorld {
    private final GizmosManager gizmosManager;
    private final MinecraftCamera camera;

    public MinecraftScene(EntityManager entityManager, JobScheduler jobScheduler, GizmosManager gizmosManager, MinecraftCamera camera) {
        super(entityManager, jobScheduler);
        this.gizmosManager = gizmosManager;
        this.camera = camera;
    }

    record ChunkPosKey(int x, int y, int z) {
    }

    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    private static final ChunkComponent CHUNK_COMPONENT_0 = new ChunkComponent();
    private static final ChunkComponent CHUNK_COMPONENT_1 = new ChunkComponent();

    private boolean rebuildWorld = false;
    private ChunkProviderClient chunkProvider = null;

    public void tryUpdateChunkProvider(ChunkProviderClient chunkProvider) {
        if (this.chunkProvider != chunkProvider) {
            rebuildWorld = true;
            this.chunkProvider = chunkProvider;
            this.chunkProvider.loadChunkCallback = (x, z) -> {
                for (int i = 0; i < 16; i++) {
                    CHUNK_COMPONENT_0.chunkPosX = x;
                    CHUNK_COMPONENT_0.chunkPosY = i;
                    CHUNK_COMPONENT_0.chunkPosZ = z;
                    chunkHandles.put(new ChunkPosKey(x, i, z), entityManager.createEntity(CHUNK_COMPONENT_0));
                }
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
            // no need to & must not flush immediately
        }
    }

    public void notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {
    }

    public void notifyLightUpdate(int x, int y, int z) {
    }

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
                    CHUNK_COMPONENT_1.chunkPosX = ChunkPos.getX(chunkKey);
                    CHUNK_COMPONENT_1.chunkPosY = i;
                    CHUNK_COMPONENT_1.chunkPosZ = ChunkPos.getZ(chunkKey);
                    chunkHandles.put(new ChunkPosKey(CHUNK_COMPONENT_1.chunkPosX, CHUNK_COMPONENT_1.chunkPosY, CHUNK_COMPONENT_1.chunkPosZ), entityManager.createEntity(CHUNK_COMPONENT_1));
                }
            }
            // no need to & must not flush immediately
        }

        // test
        if (c == 3) {
            StopWatch stopWatch = StopWatch.createStarted();

            (new ChunkMeshletGenSystem(chunkProvider, gizmosManager)).update(entityManager, jobScheduler);

            stopWatch.stop();
            KirinoCore.LOGGER.info("executed!!! " + stopWatch.getTime(TimeUnit.MILLISECONDS) + " ms");
            entityManager.abort();
        }
        c++;

        super.update();
    }

    static int c = 0;
}
