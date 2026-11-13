package com.cleanroommc.kirino.engine.render.usage.scene;

import com.cleanroommc.kirino.KirinoClientDebug;
import com.cleanroommc.kirino.ecs.api.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.usage.scene.callback.ChunkCreateCallback;
import com.cleanroommc.kirino.engine.render.usage.scene.callback.ChunkDestroyCallback;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.usage.task.system.ChunkMeshletGenSystem;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.chunk.Chunk;

import java.lang.invoke.MethodHandle;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class WorldControl {

    private final TerrainCpuPipelineFSM terrainFsm;
    private final MeshletGpuPipelineFSM meshletFsm;
    private final WorldControlFSM worldFsm;
    private final EntityManager entityManager;
    private final ChunkDestroyCallback chunkDestroyCallback;
    private final ChunkCreateCallback chunkCreateCallback;
    private final Consumer<WorldClient> setWorld;
    private final Consumer<ChunkProviderClient> setProvider;

    WorldControl(
            TerrainCpuPipelineFSM terrainFsm,
            MeshletGpuPipelineFSM meshletFsm,
            WorldControlFSM worldFsm,
            EntityManager entityManager,
            ChunkDestroyCallback chunkDestroyCallback,
            ChunkCreateCallback chunkCreateCallback,
            SingleFlow<ChunkMeshletGenSystem> chunkMeshletGenSystem) {
        this(terrainFsm, meshletFsm, worldFsm, entityManager, chunkDestroyCallback, chunkCreateCallback,
            chunkMeshletGenSystem.getSystem()::setWorld, chunkMeshletGenSystem.getSystem()::setChunkProvider);
    }

    WorldControl(TerrainCpuPipelineFSM terrainFsm, MeshletGpuPipelineFSM meshletFsm, WorldControlFSM worldFsm,
                 EntityManager entityManager, ChunkDestroyCallback destroyCallback, ChunkCreateCallback createCallback,
                 Consumer<WorldClient> setWorld, Consumer<ChunkProviderClient> setProvider) {
        this.terrainFsm = terrainFsm;
        this.meshletFsm = meshletFsm;
        this.worldFsm = worldFsm;
        this.entityManager = entityManager;
        this.chunkDestroyCallback = destroyCallback;
        this.chunkCreateCallback = createCallback;
        this.setWorld = setWorld;
        this.setProvider = setProvider;
    }

    private WorldClient minecraftWorld = null;
    private ChunkProviderClient minecraftChunkProvider = null;
    private Map<Long, Chunk> cachedEarlyChunks = null; // put when loading a new world; consumed right after
    private final Map<ChunkPosKey, CleanEntityHandle> chunkHandles = new HashMap<>();
    private BiConsumer<Integer, Integer> loadCallback;
    private BiConsumer<Integer, Integer> unloadCallback;
    private volatile boolean detached;
    private Thread bindingThread;

    public boolean isWorldReady() {
        return minecraftWorld != null;
    }

    public void tryUpdateWorld(WorldClient minecraftWorld) {
        Preconditions.checkState(!detached, "This world session has been detached.");
        // switching dim will trigger this too
        if (minecraftWorld != null && minecraftChunkProvider != minecraftWorld.getChunkProvider()) {
            Preconditions.checkState(this.minecraftWorld == null, "Switch worlds through the scene lifecycle owner.");
            attach(minecraftWorld, minecraftWorld.getChunkProvider());
        }
    }

    void attach(WorldClient minecraftWorld, ChunkProviderClient provider) {
        Preconditions.checkState(!detached && minecraftChunkProvider == null, "A world is already attached or retired.");
        Preconditions.checkState(MethodHolder.getLoadChunkCallback(provider) == null
            && MethodHolder.getUnloadChunkCallback(provider) == null,
            "The chunk provider already has an owner for its callbacks.");

        // entrypoint: load in a new world
        KirinoClientDebug.MeshletGpuTimeline$loadInNewWorld();

        worldFsm.reset(); // NO_WORLD
        this.minecraftWorld = minecraftWorld;
        minecraftChunkProvider = provider;
        bindingThread = Thread.currentThread();

        cachedEarlyChunks = new HashMap<>(MethodHolder.getLoadedChunks(minecraftChunkProvider));

        loadCallback = (x, z) -> {
            if (detached) {
                return;
            }
            requireBindingThread();
            for (int i = 0; i < 16; i++) {
                ChunkComponent chunkComponent = new ChunkComponent();
                chunkComponent.chunkPosX = x;
                chunkComponent.chunkPosY = i;
                chunkComponent.chunkPosZ = z;
                ChunkPosKey key = new ChunkPosKey(x, i, z);
                if (!chunkHandles.containsKey(key)) {
                    chunkHandles.put(key, entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
                }
            }
        };

        unloadCallback = (x, z) -> {
            if (detached) {
                return;
            }
            requireBindingThread();
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
        MethodHolder.setLoadChunkCallback(provider, loadCallback);
        MethodHolder.setUnloadChunkCallback(provider, unloadCallback);

        setProvider.accept(provider);
        setWorld.accept(minecraftWorld);
        worldFsm.next(); // NEW_WORLD_REBUILD
    }

    public void detach() {
        if (bindingThread != null) {
            requireBindingThread();
        }
        detached = true;
        if (minecraftChunkProvider != null) {
            if (MethodHolder.getLoadChunkCallback(minecraftChunkProvider) == loadCallback) {
                MethodHolder.setLoadChunkCallback(minecraftChunkProvider, null);
            }
            if (MethodHolder.getUnloadChunkCallback(minecraftChunkProvider) == unloadCallback) {
                MethodHolder.setUnloadChunkCallback(minecraftChunkProvider, null);
            }
        }
        loadCallback = null;
        unloadCallback = null;
        cachedEarlyChunks = null;
        chunkHandles.clear();
        minecraftWorld = null;
        minecraftChunkProvider = null;
    }

    private void requireBindingThread() {
        Preconditions.checkState(Thread.currentThread() == bindingThread,
            "World callbacks must run on the owning render thread.");
    }

    public void releaseWorldReferences() {
        Preconditions.checkState(detached, "Detach the world before releasing system references.");
        setWorld.accept(null);
        setProvider.accept(null);
    }

    public void rebuildWorld() {
        Preconditions.checkState(!detached, "This world session has been detached.");
        requireBindingThread();
        Preconditions.checkState(worldFsm.getState() == WorldControlFSM.State.NEW_WORLD_REBUILD,
                "The world control FSM must have a \"NEW_WORLD_REBUILD\" state during this method call.");

        // add early chunks (for those chunks that were there before load/unload callback setup)
        for (Long chunkKey : cachedEarlyChunks.keySet()) {
            if (!MethodHolder.getLoadedChunks(minecraftChunkProvider).containsKey(chunkKey.longValue())) {
                continue;
            }
            for (int i = 0; i < 16; i++) {
                ChunkComponent chunkComponent = new ChunkComponent();
                chunkComponent.chunkPosX = getX(chunkKey);
                chunkComponent.chunkPosY = i;
                chunkComponent.chunkPosZ = getZ(chunkKey);
                ChunkPosKey key = new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ);
                if (!chunkHandles.containsKey(key)) {
                    chunkHandles.put(key, entityManager.createEntity(chunkDestroyCallback, chunkCreateCallback, chunkComponent));
                }
            }
        }
        cachedEarlyChunks = null;

        worldFsm.next(); // NEW_WORLD_INITIAL_WAIT
        terrainFsm.reset();
        meshletFsm.reset();
    }

    private static int getX(long key) {
        return (int) (key & 0xFFFFFFFFL);
    }

    private static int getZ(long key) {
        return (int) ((key >>> 32) & 0xFFFFFFFFL);
    }

    private static final class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getFieldSetter(ChunkProviderClient.class, "loadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldSetter(ChunkProviderClient.class, "unloadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "unloadChunkCallback", BiConsumer.class),
                    ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadedChunks", "field_73236_b", Long2ObjectMap.class));

            Preconditions.checkNotNull(DELEGATE.loadChunkCallbackSetter);
            Preconditions.checkNotNull(DELEGATE.unloadChunkCallbackSetter);
            Preconditions.checkNotNull(DELEGATE.loadedChunksGetter);
        }

        @SuppressWarnings("unchecked")
        static BiConsumer<Integer, Integer> getLoadChunkCallback(ChunkProviderClient provider) {
            try {
                return (BiConsumer<Integer, Integer>) DELEGATE.loadGetter.invokeExact(provider);
            } catch (Throwable cause) {
                throw new RuntimeException(cause);
            }
        }

        @SuppressWarnings("unchecked")
        static BiConsumer<Integer, Integer> getUnloadChunkCallback(ChunkProviderClient provider) {
            try {
                return (BiConsumer<Integer, Integer>) DELEGATE.unloadGetter.invokeExact(provider);
            } catch (Throwable cause) {
                throw new RuntimeException(cause);
            }
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
                MethodHandle loadGetter,
                MethodHandle unloadGetter,
                MethodHandle loadedChunksGetter) {
        }
    }
}
