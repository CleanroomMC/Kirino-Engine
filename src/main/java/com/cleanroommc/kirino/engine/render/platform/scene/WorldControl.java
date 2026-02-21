package com.cleanroommc.kirino.engine.render.platform.scene;

import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.callback.ChunkCreateCallback;
import com.cleanroommc.kirino.engine.render.platform.scene.callback.ChunkDestroyCallback;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.platform.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.platform.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class WorldControl {

    private final TerrainCpuPipelineFSM terrainFsm;
    private final MeshletGpuPipelineFSM meshletFsm;
    private final WorldControlFSM worldFsm;
    private final EntityManager entityManager;
    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;
    private final SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem;

    WorldControl(
            TerrainCpuPipelineFSM terrainFsm,
            MeshletGpuPipelineFSM meshletFsm,
            WorldControlFSM worldFsm,
            EntityManager entityManager,
            ChunkDestroyCallback chunkDestroyCallback,
            ChunkCreateCallback chunkCreateCallback,
            SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem) {

        this.terrainFsm = terrainFsm;
        this.meshletFsm = meshletFsm;
        this.worldFsm = worldFsm;
        this.entityManager = entityManager;
        this.chunkDestroyCallback = chunkDestroyCallback;
        this.chunkCreateCallback = chunkCreateCallback;
        this.chunkMeshletGenSystem = chunkMeshletGenSystem;
    }

    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private Map<Long, Chunk> cachedEarlyChunks = null; // put when loading a new world; consumed right after
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();

    public boolean isWorldReady() {
        return minecraftWorld != null;
    }

    /**
     * If a new world is detected, it updates world control FSM, installs chunk load/unload callbacks,
     * and updates the world handle of the meshlet gen system.
     *
     * <p>Note: {@link MinecraftScene#update()} must be called after this method in a row
     * to consume world changes.</p>
     * <p>Note: this method must be called in a per frame basis.</p>
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

    /**
     * It deletes old data from the ECS storage, advances the world control FSM,
     * and resets all other FSM.
     *
     * <p>Note: must be called after a successful {@link #tryUpdateWorld(WorldClient)}.
     * Querying the world control FSM can tell whether {@link #tryUpdateWorld(WorldClient)} resets the world.</p>
     */
    public void rebuildWorld() {
        Preconditions.checkState(worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_REBUILD,
                "The world control FSM must have a \"NEW_WORLD_REBUILD\" state during this method call.");

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
