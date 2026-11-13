package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class HeapPoolRowsTest {
    @Test
    void borrowedRows_growthAndTrim_keepDescriptorAndRetainedColumnIdentity() {
        HeapPool pool = pool();
        PoolRows rows = pool.getRows();
        PrimitiveArray values = pool.getArray(Value.class, "value");
        for (int i = 0; i < 256; i++) pool.addEntity(i, payload(i));
        assertSame(rows, pool.getRows());
        assertEquals(256, rows.limit);
        assertEquals(pool.getCurrentSize(), rows.nextFree.length);
        assertEquals(pool.getCurrentSize(), rows.previousFree.length);
        for (int i = 0; i < 255; i++) pool.removeEntity(i);
        assertTrue(rows.isOccupied(255));
        assertFalse(rows.isOccupied(0));
        pool.removeEntity(255);
        assertEquals(0, rows.limit);
        pool.trimCapacity();
        assertEquals(0, rows.nextFree.length);
        assertEquals(0, rows.previousFree.length);
        pool.addEntity(Integer.MIN_VALUE, payload(41));
        pool.addEntity(Integer.MAX_VALUE, payload(42));
        pool.addEntity(0, payload(43));
        assertSame(rows, pool.getRows());
        assertSame(values, pool.getArray(Value.class, "value"));
        assertEquals(Integer.MIN_VALUE, pool.getEntityID(0));
        assertEquals(Integer.MAX_VALUE, pool.getEntityID(1));
        assertEquals(0, pool.getEntityID(2));
        assertEquals(41, values.getInt(0));
        assertEquals(42, values.getInt(1));
        assertEquals(43, values.getInt(2));
    }

    @Test
    void randomChangesAndMaintenance_preserveMappingsAndPayloadsAcrossResize() {
        HeapPool pool = pool();
        Map<Integer, Integer> expected = new HashMap<>();
        Random random = new Random(0x6b6972696e6fL);
        for (int step = 0; step < 10000; step++) {
            int id = random.nextInt(127);
            if (expected.containsKey(id)) {
                pool.removeEntity(id);
                expected.remove(id);
            } else {
                pool.addEntity(id, payload(step));
                expected.put(id, step);
            }
            if (step % 31 == 0) pool.trimCapacity();
            if (step % 47 == 0) verify(pool, expected);
        }
        verify(pool, expected);
        for (int id : expected.keySet()) pool.removeEntity(id);
        assertEquals(0, pool.getRows().limit);
        pool.trimCapacity();
        assertEquals(0, pool.getCurrentSize());
        pool.addEntity(1, payload(999));
        assertEquals(999, pool.getArray(Value.class, "value").getInt(0));
    }

    @Test
    void invalidInsertionAndActiveGuards_doNotConsumeOrChangeFreeRows() {
        HeapPool pool = pool();
        for (int i = 0; i < 8; i++) pool.addEntity(i, payload(i));
        pool.removeEntity(3);
        PoolRows rows = pool.getRows();
        assertEquals(3, rows.nextRow());
        assertThrows(NullPointerException.class, () -> pool.addEntity(9, new CleanComponent[]{null}));
        assertEquals(3, rows.nextRow());
        int[] borrowed = rows.nextFree;
        pool.beginExecution();
        assertThrows(IllegalStateException.class, () -> pool.addEntity(9, payload(9)));
        assertThrows(IllegalStateException.class, () -> pool.removeEntity(7));
        assertThrows(IllegalStateException.class, pool::trimCapacity);
        assertSame(borrowed, rows.nextFree);
        assertEquals(8, rows.limit);
        pool.endExecution();
        pool.addEntity(9, payload(9));
        assertEquals(9, pool.getEntityID(3));
    }

    private static void verify(HeapPool pool, Map<Integer, Integer> expected) {
        PoolRows rows = pool.getRows();
        int visited = 0;
        PrimitiveArray values = pool.getArray(Value.class, "value");
        for (int row = 0; row < rows.limit; row++) {
            if (rows.nextFree[row] != 0) {
                int hole = row;
                assertThrows(IllegalStateException.class, () -> pool.getEntityID(hole));
            } else {
                int id = pool.getEntityID(row);
                assertEquals(expected.get(id), values.getInt(row));
                assertEquals(expected.get(id), ((Value) pool.getComponent(id, Value.class)).value);
                visited++;
            }
        }
        assertEquals(expected.size(), visited);
        for (int id = 0; id < 127; id++) assertEquals(expected.containsKey(id), pool.containsEntity(id));
    }

    private static HeapPool pool() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("value", Value.class, new MemberLayout("value"), "int");
        components.lock();
        return new HeapPool(components, List.of(Value.class), 0);
    }

    private static CleanComponent[] payload(int value) {
        Value component = new Value();
        component.value = value;
        return new CleanComponent[]{component};
    }

    public static class Value implements CleanComponent {
        public int value;
    }
}
