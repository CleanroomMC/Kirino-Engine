package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeapPoolCapacityTest {
    @Test
    void growth_zeroInitialCapacity_preservesRowsWithLogarithmicResizeCount() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 0);
        PrimitiveArray column = pool.getArray(Value.class, "value");
        int resizes = 0;
        int previous = 0;
        for (int i = 0; i < 1000; i++) {
            pool.addEntity(i, payload(i));
            if (previous != pool.getCurrentSize()) {
                previous = pool.getCurrentSize();
                resizes++;
            }
            assertSame(column, pool.getArray(Value.class, "value"));
            assertEquals(pool.getCurrentSize(), column.length());
        }
        assertTrue(resizes < 24, "Resize count: " + resizes);
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, column.getInt(i));
            assertEquals(i, pool.getEntityID(i));
        }
    }

    @Test
    void removalAndCompaction_onlyExplicitMaintenanceShrinksAndKeepsHeadroom() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 8);
        for (int i = 0; i < 128; i++) pool.addEntity(i, payload(i));
        int capacity = pool.getCurrentSize();
        int remaining = capacity / 4;
        for (int i = 127; i >= remaining; i--) pool.removeEntity(i);
        assertEquals(capacity, pool.getCurrentSize());
        pool.trimCapacity();
        assertEquals(remaining * 2, pool.getCurrentSize());
        int compacted = pool.getCurrentSize();
        for (int i = 0; i < 128; i++) {
            pool.addEntity(1000, payload(1000));
            pool.removeEntity(1000);
            pool.trimCapacity();
            assertEquals(compacted, pool.getCurrentSize());
        }
        for (int i = remaining - 1; i >= 0; i--) pool.removeEntity(i);
        pool.trimCapacity();
        assertEquals(8, pool.getCurrentSize());
    }

    @Test
    void compaction_fragmentedRows_preservesHighestLiveRowAndReusesHoles() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 0);
        PrimitiveArray column = pool.getArray(Value.class, "value");
        for (int i = 0; i < 128; i++) pool.addEntity(i, payload(i));
        int capacity = pool.getCurrentSize();
        for (int i = 0; i < 127; i++) pool.removeEntity(i);
        pool.trimCapacity();
        assertEquals(capacity, pool.getCurrentSize());
        assertEquals(127, column.getInt(127));
        pool.addEntity(2000, payload(2000));
        assertEquals(2000, pool.getEntityID(126));
        pool.removeEntity(127);
        pool.removeEntity(2000);
        assertEquals(0, pool.getRows().limit);
        pool.trimCapacity();
        assertEquals(0, column.length());
        pool.addEntity(3000, payload(3000));
        assertSame(column, pool.getArray(Value.class, "value"));
        assertEquals(3000, column.getInt(0));
    }

    @Test
    void compaction_activeExecution_rejectsUntilAllGuardsAreReleased() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 0);
        for (int i = 0; i < 32; i++) pool.addEntity(i, payload(i));
        for (int i = 31; i >= 1; i--) pool.removeEntity(i);
        int capacity = pool.getCurrentSize();
        pool.beginExecution();
        pool.beginExecution();
        assertThrows(IllegalStateException.class, pool::trimCapacity);
        pool.endExecution();
        assertThrows(IllegalStateException.class, pool::trimCapacity);
        assertEquals(capacity, pool.getCurrentSize());
        pool.endExecution();
        pool.trimCapacity();
        assertEquals(2, pool.getCurrentSize());
    }

    @Test
    void manager_deleteAndRefillInOneFlush_trimsOnlyAfterFinalRowCount() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(Value.class), 8);
        EntityManager entities = new EntityManager(registry, components -> pool);
        CleanEntityHandle[] handles = new CleanEntityHandle[128];
        for (int i = 0; i < handles.length; i++) handles[i] = entities.createEntity(payload(i));
        entities.flush();
        int capacity = pool.getCurrentSize();
        for (int i = 127; i >= 0; i--) assertTrue(handles[i].tryDestroy());
        for (int i = 0; i < handles.length; i++) {
            handles[i] = entities.createEntity(null,
                context -> assertEquals(capacity, pool.getCurrentSize()), payload(i + 1000));
        }
        entities.flush();
        assertEquals(capacity, pool.getCurrentSize());
        for (int i = 0; i < handles.length; i++) {
            assertEquals(i + 1000, handles[i].readComponent(Value.class).orElseThrow().value);
        }
        for (int i = 127; i >= 0; i--) assertTrue(handles[i].tryDestroy());
        assertEquals(capacity, pool.getCurrentSize());
        entities.flush();
        assertEquals(8, pool.getCurrentSize());
        entities.flush();
        assertEquals(8, pool.getCurrentSize());
    }

    @Test
    void manager_migrationOutOfPool_shrinksSourceAfterAllTransfers() {
        ComponentRegistry registry = registry();
        HeapPool source = new HeapPool(registry, List.of(Value.class), 0);
        EntityManager entities = new EntityManager(registry, components ->
            components.equals(List.of(Value.class)) ? source : new HeapPool(registry, components, 0));
        CleanEntityHandle[] handles = new CleanEntityHandle[64];
        for (int i = 0; i < handles.length; i++) handles[i] = entities.createEntity(payload(i));
        entities.flush();
        PrimitiveArray retained = source.getArray(Value.class, "value");
        assertTrue(retained.length() >= 64);
        for (int i = handles.length - 1; i >= 0; i--) assertTrue(handles[i].tryRemoveComponent(Value.class));
        entities.flush();
        assertEquals(0, retained.length());
        for (int i = 0; i < handles.length; i++) {
            assertTrue(handles[i].isAlive());
            assertFalse(handles[i].contains(Value.class));
            assertTrue(handles[i].tryAddComponent(payload(i)[0]));
        }
        entities.flush();
        assertSame(retained, source.getArray(Value.class, "value"));
        for (int i = 0; i < handles.length; i++) {
            assertEquals(i, handles[i].readComponent(Value.class).orElseThrow().value);
        }
    }

    @Test
    void constructor_invalidCapacity_rejectsWithoutAllocatingColumns() {
        ComponentRegistry registry = registry();
        assertThrows(IllegalArgumentException.class, () -> new HeapPool(registry, List.of(Value.class), -1));
        assertThrows(IllegalArgumentException.class,
            () -> new HeapPool(registry, List.of(), PoolCapacity.MAX_CAPACITY + 1));
    }

    private static CleanComponent[] payload(int value) {
        Value component = new Value();
        component.value = value;
        return new CleanComponent[]{component};
    }

    private static ComponentRegistry registry() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("value"), "int");
        registry.lock();
        return registry;
    }

    public static class Value implements CleanComponent {
        public int value;
    }
}
