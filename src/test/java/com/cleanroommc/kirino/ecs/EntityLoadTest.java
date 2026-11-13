package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(120)
class EntityLoadTest {
    @BeforeAll
    static void initializeSide() { CleanroomEnvironment.setSide(Side.CLIENT); }

    @ParameterizedTest
    @ValueSource(strings = {"forward", "reverse", "shuffled"})
    void hundredThousandEntities_destroyAndReusePreservesEveryIdentityAndValue(String order) {
        EntityManager entities = new EntityManager(registry());
        int count = 100000;
        CleanEntityHandle[] old = populate(entities, count);
        entities.flush();
        assertValues(entities, count, 0);
        int[] indices = order(count, order);
        for (int index : indices) {
            assertTrue(old[index].tryDestroy());
        }
        entities.flush();
        assertValues(entities, 0, 0);
        CleanEntityHandle[] replacement = populate(entities, count);
        entities.flush();
        assertValues(entities, count, 0);
        for (int i = 0; i < count; i++) {
            assertFalse(old[i].valid());
            assertFalse(old[i].tryDestroy());
            assertTrue(replacement[i].isAlive());
        }
        entities.clearEntities(false);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 50, 90})
    void fragmentedPools_setMigrateAndRefillDoesNotVisitOrReviveHoles(int removedPercent) {
        EntityManager entities = new EntityManager(registry());
        int count = 32768;
        CleanEntityHandle[] handles = populate(entities, count);
        entities.flush();
        int alive = 0;
        for (int i = 0; i < count; i++) {
            if (i % 10 < removedPercent / 10) {
                assertTrue(handles[i].tryDestroy());
            } else {
                alive++;
            }
        }
        entities.flush();
        int traversed = 0;
        try (var execution = entities.beginExecution(entities.newQuery().with(Value.class))) {
            for (var pool : execution.pools()) {
                PoolRows rows = pool.getRows();
                var values = pool.getArray(Value.class, "id");
                for (int row = 0; row < rows.limit; row++) {
                    if (rows.nextFree[row] == 0) {
                        int id = pool.getEntityID(row);
                        assertEquals(id, values.getInt(row));
                        assertTrue(id % 10 >= removedPercent / 10);
                        values.setInt(row, id + 1000000);
                        traversed++;
                    }
                }
            }
        }
        assertEquals(alive, traversed);
        for (int i = 0; i < count; i++) {
            if (i % 10 >= removedPercent / 10) {
                assertTrue(handles[i].tryAddComponent(new Marker()));
            }
        }
        entities.flush();
        for (int i = 0; i < count; i++) {
            if (i % 10 >= removedPercent / 10) {
                assertEquals(i + 1000000, handles[i].readComponent(Value.class).orElseThrow().id);
                assertTrue(handles[i].tryRemoveComponent(Marker.class));
            } else {
                assertFalse(handles[i].valid());
                entities.createEntity(value(-i - 1));
            }
        }
        entities.flush();
        assertEquals(count, countRows(entities));
        for (int i = 0; i < count; i++) {
            if (i % 10 < removedPercent / 10) {
                assertFalse(handles[i].valid());
            } else {
                assertFalse(handles[i].contains(Marker.class));
                assertEquals(i + 1000000, handles[i].readComponent(Value.class).orElseThrow().id);
            }
        }
        entities.clearEntities(false);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 4, 8})
    void concurrentProducers_andFlusherKeepProjectedCommandsCallbacksAndValuesConsistent(int producers) throws Exception {
        EntityManager entities = new EntityManager(registry());
        int perProducer = 1024;
        AtomicInteger creates = new AtomicInteger();
        AtomicInteger destroys = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(producers);
        List<Future<CleanEntityHandle[]>> results = new ArrayList<>();
        try (var workers = Executors.newFixedThreadPool(producers + 1)) {
            Future<?> flusher = workers.submit(() -> {
                await(start);
                while (done.getCount() != 0) {
                    entities.flush();
                    Thread.yield();
                }
                entities.flush();
            });
            for (int p = 0; p < producers; p++) {
                final int producer = p;
                results.add(workers.submit(() -> {
                    await(start);
                    try {
                        CleanEntityHandle[] handles = new CleanEntityHandle[perProducer];
                        for (int i = 0; i < perProducer; i++) {
                            int number = producer * perProducer + i;
                            var handle = entities.createEntity(context -> {
                                assertEquals(number + 1000000, context.getInt(Value.class, "id"));
                                destroys.incrementAndGet();
                            }, context -> {
                                assertEquals(number, context.getComponent(Value.class).id);
                                creates.incrementAndGet();
                            }, value(number));
                            handles[i] = handle;
                            assertTrue(handle.trySetComponent(value(number + 1000000)));
                            assertTrue(handle.tryAddComponent(new Marker()));
                            assertTrue(handle.tryRemoveComponent(Marker.class));
                            if ((i & 1) == 0) {
                                assertTrue(handle.tryDestroy());
                            }
                        }
                        return handles;
                    } finally {
                        done.countDown();
                    }
                }));
            }
            start.countDown();
            for (int p = 0; p < producers; p++) {
                var handles = results.get(p).get(60, TimeUnit.SECONDS);
                for (int i = 0; i < perProducer; i++) {
                    if ((i & 1) == 0) {
                        assertFalse(handles[i].valid());
                    }
                }
            }
            flusher.get(60, TimeUnit.SECONDS);
            assertEquals(producers * perProducer, creates.get());
            assertEquals(producers * perProducer / 2, destroys.get());
            assertEquals(producers * perProducer / 2, countRows(entities));
            for (int p = 0; p < producers; p++) {
                var handles = results.get(p).get();
                for (int i = 1; i < perProducer; i += 2) {
                    assertEquals(p * perProducer + i + 1000000, handles[i].readComponent(Value.class).orElseThrow().id);
                    assertFalse(handles[i].contains(Marker.class));
                }
            }
        }
        assertNull(entities.clearEntities(false));
        assertEquals(producers * perProducer, destroys.get());
    }

    @Test
    void twentyFiveThousandRealMeshlets_preserveNestedColumnsAcrossMigrationAndCallbacks() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType scalar : ScalarType.values()) {
            fields.registerFieldType(scalar.name().toLowerCase(Locale.ROOT), scalar.clazz, new FieldDef(scalar));
        }
        StructScanningEvent structEvent = new StructScanningEvent();
        structEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.struct");
        for (var plan : StructScanningHelper.scanStructClasses(structEvent, fields)) {
            Class<?> type = Class.forName(plan.structClass());
            structs.registerStructType(plan.structName(), type, plan.memberLayout(), plan.structDef());
            fields.registerFieldType(plan.structName(), type, new FieldDef(plan.structName()));
        }
        structs.lock(); fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        ComponentScanningEvent event = new ComponentScanningEvent();
        event.register("com.cleanroommc.kirino.engine.render.usage.ecs.component");
        for (var plan : ComponentScanningHelper.scanComponentClasses(event, fields)) {
            components.registerComponent(plan.componentName(), Class.forName(plan.componentClass()).asSubclass(CleanComponent.class),
                plan.memberLayout(), plan.fieldTypeNames());
        }
        components.registerComponent("marker", Marker.class, new MemberLayout());
        components.lock();
        EntityManager entities = new EntityManager(components);
        int count = 25000;
        var handles = new CleanEntityHandle[count];
        MeshletComponent meshlet = new MeshletComponent();
        meshlet.meshletId = 713;
        meshlet.block31.blockInfo.zMinusFaceTexCoord3 = Integer.MAX_VALUE;
        meshlet.normal.x = Float.intBitsToFloat(0x7fc01234);
        AtomicInteger destroyed = new AtomicInteger();
        for (int i = 0; i < count; i++) {
            handles[i] = entities.createEntity(context -> {
                assertEquals(713, context.getInt(MeshletComponent.class, "meshletId"));
                assertEquals(Integer.MAX_VALUE, context.getInt(MeshletComponent.class, "block31", "blockInfo", "zMinusFaceTexCoord3"));
                assertEquals(0x7fc01234, Float.floatToRawIntBits(context.getFloat(MeshletComponent.class, "normal", "x")));
                destroyed.incrementAndGet();
            }, null, meshlet);
        }
        entities.flush();
        for (var handle : handles) {
            assertTrue(handle.tryAddComponent(new Marker()));
        }
        entities.flush();
        for (var handle : handles) {
            assertTrue(handle.tryRemoveComponent(Marker.class));
        }
        entities.flush();
        for (int id : order(count, "shuffled")) {
            assertTrue(handles[id].tryDestroy());
        }
        entities.flush();
        assertEquals(count, destroyed.get());
        for (var handle : handles) {
            assertFalse(handle.valid());
        }
        assertNull(entities.clearEntities(false));
    }

    private static ComponentRegistry registry() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT)); fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("id"), "int");
        registry.registerComponent("marker", Marker.class, new MemberLayout()); registry.lock();
        return registry;
    }

    private static CleanEntityHandle[] populate(EntityManager entities, int count) {
        CleanEntityHandle[] handles = new CleanEntityHandle[count];
        for (int i = 0; i < count; i++) {
            handles[i] = entities.createEntity(value(i));
        }
        return handles;
    }

    private static Value value(int id) { Value value = new Value(); value.id = id; return value; }

    private static int countRows(EntityManager entities) {
        int count = 0;
        for (var pool : entities.startQuery(entities.newQuery().with(Value.class))) {
            var rows = pool.getRows();
            for (int row = 0; row < rows.limit; row++) {
                if (rows.nextFree[row] == 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void assertValues(EntityManager entities, int count, int offset) {
        long checksum = 0;
        boolean[] seen = new boolean[count];
        for (var pool : entities.startQuery(entities.newQuery().with(Value.class))) {
            var rows = pool.getRows();
            var values = pool.getArray(Value.class, "id");
            for (int row = 0; row < rows.limit; row++) {
                if (rows.nextFree[row] == 0) {
                    int value = values.getInt(row);
                    assertTrue(value >= offset && value < offset + count);
                    assertFalse(seen[value - offset]);
                    seen[value - offset] = true;
                    checksum += value;
                }
            }
        }
        assertEquals(count, countRows(entities));
        assertEquals((long) count * (count - 1) / 2 + (long) offset * count, checksum);
    }

    private static int[] order(int count, String direction) {
        int[] values = new int[count];
        for (int i = 0; i < count; i++) {
            values[i] = direction.equals("reverse") ? count - 1 - i : i;
        }
        if (direction.equals("shuffled")) {
            Random random = new Random(713);
            for (int i = count - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int value = values[i]; values[i] = values[j]; values[j] = value;
            }
        }
        return values;
    }

    private static void await(CountDownLatch latch) {
        try { assertTrue(latch.await(30, TimeUnit.SECONDS)); }
        catch (InterruptedException failure) { Thread.currentThread().interrupt(); throw new AssertionError(failure); }
    }

    public static class Value implements CleanComponent { public int id; }
    public static class Marker implements CleanComponent {}
}
