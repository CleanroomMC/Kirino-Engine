package com.cleanroommc.kirino.engine.render.usage.scene;

import com.cleanroommc.kirino.ecs.api.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.MeshletGpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.TerrainCpuPipelineFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.fsm.WorldControlFSM;
import com.cleanroommc.kirino.engine.render.usage.scene.scheduler.RendererTestSupport;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class WorldControlTest {
    private static final MethodHandle LOAD = ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadChunkCallback", BiConsumer.class);
    private static final MethodHandle UNLOAD = ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "unloadChunkCallback", BiConsumer.class);
    private static final MethodHandle SET_LOAD = ReflectionUtils.getFieldSetter(ChunkProviderClient.class, "loadChunkCallback", BiConsumer.class);
    private static final MethodHandle CHUNKS = ReflectionUtils.getFieldGetter(ChunkProviderClient.class, "loadedChunks", Long2ObjectMap.class);
    @TempDir Path home;
    private DebugDataHandle<?> previous;

    @BeforeEach void setup() throws Throwable {
        RendererTestSupport.initializeConfig(home);
        previous = RendererTestSupport.installInactiveTimeline();
    }
    @AfterEach void restore() throws Throwable { RendererTestSupport.setTimeline(previous); }

    @Test
    void detach_removesOwnedCallbacksAndCapturedCallbacksCannotSubmitLateCommands() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.control.attach(null, fixture.provider);
        var load = callback(LOAD, fixture.provider);
        var unload = callback(UNLOAD, fixture.provider);
        load.accept(1, 2);
        assertEquals(16, fixture.creates.get());
        fixture.control.detach();
        assertNull(callback(LOAD, fixture.provider));
        assertNull(callback(UNLOAD, fixture.provider));
        load.accept(2, 3);
        unload.accept(1, 2);
        assertEquals(16, fixture.creates.get());
        assertEquals(0, fixture.destroys.get());
        fixture.control.detach();
        fixture.control.releaseWorldReferences();
        assertNull(fixture.boundProvider.get());
        assertThrows(IllegalStateException.class, () -> fixture.control.attach(null, fixture.provider));
    }

    @Test
    void foreignCallback_isNotOverwrittenOrRemoved() throws Throwable {
        Fixture fixture = new Fixture();
        BiConsumer<Integer, Integer> foreign = (x, z) -> {};
        SET_LOAD.invokeExact(fixture.provider, (BiConsumer) foreign);
        assertThrows(IllegalStateException.class, () -> fixture.control.attach(null, fixture.provider));
        assertSame(foreign, callback(LOAD, fixture.provider));
        SET_LOAD.invokeExact(fixture.provider, (BiConsumer) null);
        fixture.control.attach(null, fixture.provider);
        SET_LOAD.invokeExact(fixture.provider, (BiConsumer) foreign);
        fixture.control.detach();
        assertSame(foreign, callback(LOAD, fixture.provider));
        assertNull(callback(UNLOAD, fixture.provider));
    }

    @Test
    void rebuild_doesNotDestroyOrDuplicateChunksLoadedBetweenAttachAndFirstUpdate() throws Throwable {
        Fixture fixture = new Fixture();
        chunks(fixture.provider).put(ChunkPos.asLong(4, -7), null);
        fixture.control.attach(null, fixture.provider);
        callback(LOAD, fixture.provider).accept(4, -7);
        callback(LOAD, fixture.provider).accept(4, -7);
        fixture.control.rebuildWorld();
        assertEquals(16, fixture.creates.get());
        assertEquals(0, fixture.destroys.get());
        assertEquals(WorldControlFSM.State.NEW_WORLD_INITIAL_WAIT, fixture.fsm.getState());
        fixture.control.detach();
    }

    @Test
    void earlyChunkUnloadedBeforeRebuild_isNotRecreatedFromOldSnapshot() throws Throwable {
        Fixture fixture = new Fixture();
        long key = ChunkPos.asLong(8, 9);
        chunks(fixture.provider).put(key, null);
        fixture.control.attach(null, fixture.provider);
        chunks(fixture.provider).remove(key);
        callback(UNLOAD, fixture.provider).accept(8, 9);
        fixture.control.rebuildWorld();
        assertEquals(0, fixture.creates.get());
        fixture.control.detach();
    }

    @Test
    void callbacksFromWrongThread_areRejectedBeforeSubmittingCommands() throws Throwable {
        Fixture fixture = new Fixture();
        fixture.control.attach(null, fixture.provider);
        var load = callback(LOAD, fixture.provider);
        try (var executor = java.util.concurrent.Executors.newSingleThreadExecutor()) {
            executor.submit(() -> assertThrows(IllegalStateException.class, () -> load.accept(1, 1))).get();
        }
        assertEquals(0, fixture.creates.get());
        fixture.control.detach();
    }

    @SuppressWarnings("unchecked")
    private static BiConsumer<Integer, Integer> callback(MethodHandle getter, ChunkProviderClient provider) throws Throwable {
        return (BiConsumer<Integer, Integer>) getter.invokeExact(provider);
    }
    @SuppressWarnings("unchecked")
    private static Long2ObjectMap<Chunk> chunks(ChunkProviderClient provider) throws Throwable {
        return (Long2ObjectMap<Chunk>) CHUNKS.invokeExact(provider);
    }

    private static final class Fixture {
        final ChunkProviderClient provider = new ChunkProviderClient(null);
        final AtomicInteger creates = new AtomicInteger();
        final AtomicInteger destroys = new AtomicInteger();
        final AtomicReference<ChunkProviderClient> boundProvider = new AtomicReference<>();
        final WorldControlFSM fsm = new WorldControlFSM();
        final EntityManager manager = (EntityManager) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{EntityManager.class}, (proxy, method, args) -> {
                if (method.getName().equals("createEntity")) {
                    creates.incrementAndGet();
                    return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{CleanEntityHandle.class},
                        (handle, operation, values) -> {
                            if (operation.getName().equals("tryDestroy")) {
                                destroys.incrementAndGet();
                                return true;
                            }
                            throw new AssertionError(operation.getName());
                        });
                }
                throw new AssertionError(method.getName());
            });
        final WorldControl control = new WorldControl(new TerrainCpuPipelineFSM(), new MeshletGpuPipelineFSM(), fsm,
            manager, null, null, world -> {}, boundProvider::set);
    }
}
