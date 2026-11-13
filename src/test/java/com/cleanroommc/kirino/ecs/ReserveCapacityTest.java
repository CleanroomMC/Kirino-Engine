package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ReserveCapacityTest {
    @Test
    void poolReservation_expandsColumnsWithoutPublishingRowsOrChangingReferences() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(Value.class), 4);
        PrimitiveArray values = pool.getArray(Value.class, "value");
        pool.reserveCapacity(1024);
        assertEquals(1024, pool.getCurrentSize());
        assertEquals(0, pool.getRows().limit);
        assertSame(values, pool.getArray(Value.class, "value"));
        assertEquals(1024, values.length());
        assertTrue(pool.getRows().isOccupied(0) == false);
        pool.addEntity(7, payload(713));
        assertEquals(1, pool.getRows().limit);
        assertEquals(713, values.getInt(0));
        assertEquals(1024, pool.getCurrentSize());
    }

    @Test
    void reservation_isMonotonicAndDoesNotShrinkExistingCapacity() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 4);
        pool.reserveCapacity(1024);
        pool.reserveCapacity(16);
        assertEquals(1024, pool.getCurrentSize());
        pool.trimCapacity();
        assertEquals(4, pool.getCurrentSize());
    }

    @Test
    void reservation_rejectsInvalidCapacityAndActiveExecutions() {
        HeapPool pool = new HeapPool(registry(), List.of(Value.class), 4);
        assertThrows(IllegalArgumentException.class, () -> pool.reserveCapacity(-1));
        assertThrows(IllegalArgumentException.class, () -> pool.reserveCapacity(PoolCapacity.MAX_CAPACITY + 1));
        pool.beginExecution();
        assertThrows(IllegalStateException.class, () -> pool.reserveCapacity(1024));
        pool.endExecution();
        assertDoesNotThrow(() -> pool.reserveCapacity(1024));
    }

    @Test
    void managerReservation_createsExactArchetypePoolBeforeCommitAndPreservesEntities() {
        EntityManager manager = new EntityManager(registry());
        manager.reserveCapacity(List.of(Value.class), 1024);
        assertEquals(1, manager.startQuery(manager.newQuery().with(Value.class)).size());
        var handle = manager.createEntity(payload(19));
        manager.flush();
        ArchetypeDataPool pool = manager.startQuery(manager.newQuery().with(Value.class)).getFirst();
        assertEquals(1024, pool.getCurrentSize());
        assertEquals(1, pool.getRows().limit);
        assertEquals(19, ((Value) pool.getComponent(handle.index, Value.class)).value);
    }

    @Test
    void managerReservation_rejectsDuplicateComponentsAndLifecycleStop() {
        EntityManager manager = new EntityManager(registry());
        assertThrows(IllegalArgumentException.class, () -> manager.reserveCapacity(List.of(Value.class, Value.class), 8));
        assertThrows(IllegalArgumentException.class, () -> manager.reserveCapacity(List.of(Value.class), -1));
        assertEquals(0, manager.startQuery(manager.newQuery().with(Value.class)).size());
        manager.lifecycle.finish(false);
        assertThrows(IllegalStateException.class, () -> manager.reserveCapacity(List.of(Value.class), 8));
    }

    @Test
    void managerReservation_rejectsWhileAnyExecutionIsActive() {
        EntityManager manager = new EntityManager(registry());
        EntityManager.ExecutionScope scope = manager.beginExecution(manager.newQuery());
        try {
            assertThrows(IllegalStateException.class,
                () -> manager.reserveCapacity(List.of(Value.class), 1024));
            assertEquals(0, manager.startQuery(manager.newQuery().with(Value.class)).size());
        } finally {
            scope.close();
        }
        assertDoesNotThrow(() -> manager.reserveCapacity(List.of(Value.class), 1024));
    }

    @Test
    void managerReservation_doesNotPublishArchetypeWhenPoolCreationFails() {
        AtomicBoolean factoryCalled = new AtomicBoolean();
        EntityManager manager = new EntityManager(registry(), components -> {
            factoryCalled.set(true);
            throw new IllegalStateException("pool factory failure");
        });
        assertThrows(IllegalStateException.class, () -> manager.reserveCapacity(List.of(Value.class), 1024));
        assertTrue(factoryCalled.get());
        assertEquals(0, manager.startQuery(manager.newQuery().with(Value.class)).size());
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
