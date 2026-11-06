package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.entity.EcsFixture.GpuRegistryStub;
import com.cleanroommc.kirino.ecs.entity.EcsFixture.Tag;
import com.cleanroommc.kirino.ecs.entity.EcsFixture.Tiny;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Flush regression tests using terrain meshlets and smaller bookkeeping-only components.
 */
class EntityManagerFlushTest {
    /**
     * Loading captures contain roughly 20k-30k live meshlets.
     */
    private static final int MESHLETS = 20_000;

    private final EcsFixture fixture = new EcsFixture();

    /**
     * MeshletDestroyJob visits entities in FORWARD order.
     */
    enum Order {
        FORWARD, REVERSE, SHUFFLED, EVERY_OTHER_THEN_REST
    }

    private List<CleanEntityHandle> createMeshlets(EntityManager manager, GpuRegistryStub gpu, int count) {
        EntityCreateCallback create = EcsFixture.createCallback(gpu);
        EntityDestroyCallback destroy = EcsFixture.destroyCallback(gpu);
        List<CleanEntityHandle> handles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            handles.add(manager.createEntity(destroy, create, fixture.template(i)));
        }
        return handles;
    }

    private ArchetypeDataPool onlyPool(EntityManager manager, Class<? extends com.cleanroommc.kirino.ecs.component.CleanComponent> with) {
        List<ArchetypeDataPool> pools = manager.startQuery(manager.newQuery().with(with));
        assertEquals(1, pools.size());
        return pools.get(0);
    }

    private static int[] order(Order order, int count) {
        int[] ids = new int[count];
        for (int i = 0; i < count; i++) ids[i] = i;
        switch (order) {
            case FORWARD -> {
            }
            case REVERSE -> {
                for (int i = 0; i < count; i++) ids[i] = count - 1 - i;
            }
            case SHUFFLED -> {
                Random random = new Random(0xEC5L);
                for (int i = count - 1; i > 0; i--) {
                    int j = random.nextInt(i + 1);
                    int t = ids[i];
                    ids[i] = ids[j];
                    ids[j] = t;
                }
            }
            case EVERY_OTHER_THEN_REST -> {
                // Remove even slots first to leave holes between the survivors.
                int k = 0;
                for (int i = 0; i < count; i += 2) ids[k++] = i;
                for (int i = 1; i < count; i += 2) ids[k++] = i;
            }
        }
        return ids;
    }

    @Test
    void flush_noCommands_isNoOpAndKeepsStorageEmpty() {
        EntityManager manager = fixture.newManager();
        manager.flush();
        assertTrue(manager.startQuery(manager.newQuery().with(MeshletComponent.class)).isEmpty());
    }

    @Test
    void flush_createMeshletsBeforeFlush_nothingIsStoredYet() {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        createMeshlets(manager, gpu, 1_000);
        assertTrue(manager.startQuery(manager.newQuery().with(MeshletComponent.class)).isEmpty());
        assertEquals(0, gpu.allocations());
    }

    @Test
    void flush_createTwentyThousandMeshlets_everyEntityStoredWithItsCallbackIdAndTemplateData() {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        List<CleanEntityHandle> handles = createMeshlets(manager, gpu, MESHLETS);

        manager.flush();

        assertEquals(MESHLETS, gpu.allocations());
        assertEquals(MESHLETS, gpu.distinctAllocated());
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);
        ArrayRange range = pool.getArrayRange();
        assertEquals(0, range.start);
        assertEquals(MESHLETS, range.end);
        assertTrue(range.deprecatedIndexes.isEmpty());

        PrimitiveArray ids = pool.getArray(MeshletComponent.class, "meshletId");
        PrimitiveArray chunkX = pool.getArray(MeshletComponent.class, "chunkPosX");
        PrimitiveArray chunkY = pool.getArray(MeshletComponent.class, "chunkPosY");
        PrimitiveArray lastBlockTex = pool.getArray(MeshletComponent.class, "block31", "blockInfo", "xPlusFaceTexCoord0");
        for (int i = 0; i < MESHLETS; i++) {
            CleanEntityHandle handle = handles.get(i);
            assertTrue(handle.valid());
            assertEquals(i, handle.index, "fresh manager hands out sequential entity ids");
            int slot = i;
            assertEquals(handle.index, pool.getEntityID(slot).orElseThrow());
            assertEquals(i, ids.getInt(slot), "callback ids are allocated in command order");
            assertEquals(i & 255, chunkX.getInt(slot));
            assertEquals((i & 255) * 2 - 5, chunkY.getInt(slot));
            assertEquals((i & 255) * 100 + 31, lastBlockTex.getInt(slot));
        }
    }

    @Test
    void flush_createMeshlets_fullComponentRoundTripsThroughPool() {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        List<CleanEntityHandle> handles = createMeshlets(manager, gpu, 512);
        manager.flush();
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);
        for (int i : new int[]{0, 1, 255, 256, 511}) {
            MeshletComponent read = (MeshletComponent) pool.getComponent(handles.get(i).index, MeshletComponent.class);
            MeshletComponent expected = fixture.template(i);
            assertEquals(i, read.meshletId);
            assertEquals(expected.chunkPosX, read.chunkPosX);
            assertEquals(expected.blockCount, read.blockCount);
            assertEquals(expected.aabb.xMin, read.aabb.xMin, 0f);
            assertEquals(expected.normal, read.normal);
            for (int b = 0; b < 32; b++) {
                assertEquals(expected.getBlock(b).positionAndFaceMask, read.getBlock(b).positionAndFaceMask);
                assertEquals(expected.getBlock(b).blockInfo.xPlusFaceTexCoord0, read.getBlock(b).blockInfo.xPlusFaceTexCoord0);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Order.class)
    void flush_destroyAllMeshlets_callbackSeesEachIdOnceAndHandlesExpire(Order order) {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        int count = 10_000;
        List<CleanEntityHandle> handles = createMeshlets(manager, gpu, count);
        manager.flush();
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);

        for (int id : order(order, count)) {
            assertTrue(handles.get(id).tryDestroy());
            assertFalse(handles.get(id).valid(), "handle expires immediately, before flush");
        }
        assertEquals(count, pool.getArrayRange().end, "destruction is deferred");
        manager.flush();

        assertEquals(count, gpu.disposals());
        BitSet seen = new BitSet(count);
        for (int id : gpu.disposedIds) {
            assertFalse(seen.get(id), "meshlet id " + id + " disposed twice");
            seen.set(id);
        }
        assertEquals(count, seen.cardinality());
        for (CleanEntityHandle handle : handles) {
            assertFalse(handle.valid());
            assertFalse(pool.containsEntity(handle.index));
        }
        // Shuffled removal may leave an empty range with end > 0. Check live slots as well.
        assertEquals(0, liveEntities(pool));
        for (int slot = 0; slot < pool.getArrayRange().end; slot++) {
            assertFalse(pool.getEntityID(slot).isPresent(), "slot " + slot + " still maps to an entity");
        }
    }

    @ParameterizedTest
    @EnumSource(Order.class)
    void flush_destroyThenRecreate_reusesEntityIdsWithNewGenerationAndFreshData(Order order) {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        int count = 6_000;
        List<CleanEntityHandle> first = createMeshlets(manager, gpu, count);
        manager.flush();
        for (int id : order(order, count)) first.get(id).tryDestroy();
        manager.flush();

        List<CleanEntityHandle> second = createMeshlets(manager, gpu, count);
        manager.flush();

        BitSet ids = new BitSet(count);
        for (CleanEntityHandle handle : second) {
            assertTrue(handle.index < count, "entity ids are recycled, not leaked");
            assertEquals(1, handle.generation);
            assertTrue(handle.valid());
            ids.set(handle.index);
        }
        assertEquals(count, ids.cardinality(), "every recycled id used exactly once");
        for (CleanEntityHandle stale : first) {
            assertFalse(stale.valid(), "old handle must not alias the recycled entity");
            assertFalse(stale.tryDestroy());
        }
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);
        PrimitiveArray chunkX = pool.getArray(MeshletComponent.class, "chunkPosX");
        ArrayRange range = pool.getArrayRange();
        assertTrue(range.end <= count, "recreated meshlets reuse pool slots instead of growing the pool");
        // Recycled entity IDs no longer follow creation order.
        int[] ordinalOfEntity = new int[count];
        for (int k = 0; k < count; k++) ordinalOfEntity[second.get(k).index] = k;
        int live = 0;
        for (int slot = 0; slot < range.end; slot++) {
            if (range.deprecatedIndexes.contains(slot)) continue;
            int entity = pool.getEntityID(slot).orElseThrow();
            assertEquals(ordinalOfEntity[entity] & 255, chunkX.getInt(slot));
            live++;
        }
        assertEquals(count, live);
    }

    @Test
    void flush_chunkChurnInOneFrame_survivorsKeepDataAndNewMeshletsFillTheHole() {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        int world = 8_000;
        List<CleanEntityHandle> handles = createMeshlets(manager, gpu, world);
        manager.flush();
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);

        // A chunk was rebuilt: its 800 meshlets are contiguous in creation order and get replaced in the same frame.
        int from = 3_000, to = 3_800;
        for (int i = from; i < to; i++) assertTrue(handles.get(i).tryDestroy());
        List<CleanEntityHandle> replacement = createMeshlets(manager, gpu, to - from);
        manager.flush();

        assertEquals(to - from, gpu.disposals());
        ArrayRange range = pool.getArrayRange();
        assertEquals(world, range.end, "destroys are enqueued first, so replacements reuse the freed slots");
        assertTrue(range.deprecatedIndexes.isEmpty());
        PrimitiveArray chunkX = pool.getArray(MeshletComponent.class, "chunkPosX");
        for (int slot = 0; slot < world; slot++) {
            int entity = pool.getEntityID(slot).orElseThrow();
            boolean survivor = entity < from || (entity >= to && entity < world);
            if (survivor) {
                assertEquals(entity & 255, chunkX.getInt(slot), "survivor " + entity + " must be untouched");
            }
        }
        for (CleanEntityHandle handle : replacement) {
            assertTrue(handle.valid());
            assertTrue(pool.containsEntity(handle.index));
        }
    }

    @Test
    void flush_repeatedFrames_bufferIsDrainedAndWorldSizeIsStable() {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        List<CleanEntityHandle> world = createMeshlets(manager, gpu, 4_000);
        manager.flush();
        ArchetypeDataPool pool = onlyPool(manager, MeshletComponent.class);
        int capacityAfterLoad = pool.getCurrentSize();

        List<CleanEntityHandle> live = new ArrayList<>(world);
        Random random = new Random(7);
        for (int frame = 0; frame < 60; frame++) {
            int batch = 1 + random.nextInt(64);
            for (int k = 0; k < batch; k++) {
                CleanEntityHandle victim = live.remove(random.nextInt(live.size()));
                assertTrue(victim.tryDestroy());
            }
            live.addAll(createMeshlets(manager, gpu, batch));
            manager.flush();
            manager.flush(); // Check that the first flush drained the buffer.
            assertEquals(live.size(), liveEntities(pool));
        }
        assertTrue(pool.getCurrentSize() <= capacityAfterLoad * 2,
            "steady-state churn must not keep growing the pool: " + pool.getCurrentSize() + " vs " + capacityAfterLoad);
        assertEquals(gpu.allocations() - gpu.disposals(), live.size());
    }

    private static int liveEntities(ArchetypeDataPool pool) {
        ArrayRange range = pool.getArrayRange();
        int live = 0;
        for (int slot = range.start; slot < range.end; slot++) {
            if (!range.deprecatedIndexes.contains(slot)) live++;
        }
        return live;
    }

    @Test
    void flush_addThenRemoveComponent_movesEntitiesBetweenArchetypesKeepingData() {
        EntityManager manager = fixture.newManager();
        int count = 5_000;
        List<CleanEntityHandle> handles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Tiny tiny = new Tiny();
            tiny.a = i;
            tiny.b = i + 0.5f;
            handles.add(manager.createEntity(tiny));
        }
        manager.flush();

        for (int i = 0; i < count; i += 2) {
            Tag tag = new Tag();
            tag.tag = -i;
            assertTrue(handles.get(i).tryAddComponent(tag));
        }
        manager.flush();

        assertEquals(count / 2, totalEntities(manager, manager.newQuery().with(Tiny.class).with(Tag.class)));
        assertEquals(count / 2, totalEntities(manager, manager.newQuery().with(Tiny.class).without(Tag.class)));
        for (int i = 0; i < count; i++) {
            CleanEntityHandle handle = handles.get(i);
            assertTrue(handle.valid());
            ArchetypeDataPool pool = (i % 2 == 0)
                ? onlyPool2(manager, manager.newQuery().with(Tiny.class).with(Tag.class))
                : onlyPool2(manager, manager.newQuery().with(Tiny.class).without(Tag.class));
            Tiny tiny = (Tiny) pool.getComponent(handle.index, Tiny.class);
            assertEquals(i, tiny.a);
            assertEquals(i + 0.5f, tiny.b, 0f);
            if (i % 2 == 0) {
                assertEquals(-i, ((Tag) pool.getComponent(handle.index, Tag.class)).tag);
            }
        }

        for (int i = 0; i < count; i += 2) {
            assertTrue(handles.get(i).tryRemoveComponent(Tag.class));
        }
        manager.flush();
        assertEquals(0, totalEntities(manager, manager.newQuery().with(Tag.class)));
        assertEquals(count, totalEntities(manager, manager.newQuery().with(Tiny.class)));
        ArchetypeDataPool tinyPool = onlyPool2(manager, manager.newQuery().with(Tiny.class).without(Tag.class));
        for (int i = 0; i < count; i++) {
            assertEquals(i, ((Tiny) tinyPool.getComponent(handles.get(i).index, Tiny.class)).a);
        }
    }

    private static int totalEntities(EntityManager manager, EntityQuery query) {
        int total = 0;
        for (ArchetypeDataPool pool : manager.startQuery(query)) total += liveEntities(pool);
        return total;
    }

    private static ArchetypeDataPool onlyPool2(EntityManager manager, EntityQuery query) {
        List<ArchetypeDataPool> pools = manager.startQuery(query);
        assertEquals(1, pools.size());
        return pools.get(0);
    }

    @Test
    void flush_setComponent_overwritesOnlyTheTargetEntity() {
        EntityManager manager = fixture.newManager();
        List<CleanEntityHandle> handles = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            Tiny tiny = new Tiny();
            tiny.a = i;
            handles.add(manager.createEntity(tiny));
        }
        manager.flush();
        Tiny replacement = new Tiny();
        replacement.a = -1;
        replacement.b = 2.5f;
        assertTrue(handles.get(500).trySetComponent(replacement));
        manager.flush();
        ArchetypeDataPool pool = onlyPool(manager, Tiny.class);
        for (int i = 0; i < 1_000; i++) {
            Tiny read = (Tiny) pool.getComponent(handles.get(i).index, Tiny.class);
            assertEquals(i == 500 ? -1 : i, read.a);
        }
        assertEquals(2.5f, ((Tiny) pool.getComponent(500, Tiny.class)).b, 0f);
    }

    @Test
    void createEntity_concurrentProducers_allCommandsSurviveOneFlush() throws Exception {
        EntityManager manager = fixture.newManager();
        int threads = 8, perThread = 5_000;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<java.util.concurrent.Future<List<CleanEntityHandle>>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(executor.submit(() -> {
                List<CleanEntityHandle> mine = new ArrayList<>(perThread);
                start.await();
                for (int i = 0; i < perThread; i++) mine.add(manager.createEntity(new Tiny()));
                return mine;
            }));
        }
        start.countDown();
        BitSet ids = new BitSet(threads * perThread);
        for (var future : futures) {
            for (CleanEntityHandle handle : future.get(60, TimeUnit.SECONDS)) {
                assertFalse(ids.get(handle.index), "entity id " + handle.index + " handed out twice");
                ids.set(handle.index);
            }
        }
        executor.shutdownNow();
        assertEquals(threads * perThread, ids.cardinality());

        manager.flush();
        assertEquals(threads * perThread, liveEntities(onlyPool(manager, Tiny.class)));
    }

    @Test
    void createEntity_concurrentDestroyers_allEntitiesGoneAfterOneFlush() throws Exception {
        EntityManager manager = fixture.newManager();
        int total = 40_000;
        List<CleanEntityHandle> handles = new ArrayList<>(total);
        for (int i = 0; i < total; i++) handles.add(manager.createEntity(new Tiny()));
        manager.flush();

        Collections.shuffle(handles, new Random(3));
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<java.util.concurrent.Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            int lane = t;
            futures.add(executor.submit(() -> {
                for (int i = lane; i < total; i += threads) assertTrue(handles.get(i).tryDestroy());
            }));
        }
        for (var future : futures) future.get(60, TimeUnit.SECONDS);
        executor.shutdownNow();
        manager.flush();
        assertEquals(0, liveEntities(onlyPool(manager, Tiny.class)));
    }
}
