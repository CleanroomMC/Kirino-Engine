package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityIdLookupTest {
    @Test
    void getEntityID_allIntValues_preservesZeroAndExtremeIds() {
        HeapPool pool = pool();
        int[] ids = {0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, 1000};
        for (int i = 0; i < ids.length; i++) {
            pool.addEntity(ids[i], List.of());
            assertEquals(ids[i], pool.getEntityID(i));
        }
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(-1));
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(ids.length));
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(Integer.MAX_VALUE));
    }

    @Test
    void getEntityID_removedAndReusedRows_preservesAbsenceAndNewId() {
        HeapPool pool = pool();
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(0));
        pool.addEntity(1000, List.of()); pool.addEntity(2000, List.of());
        pool.removeEntity(1000);
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(0));
        assertEquals(2000, pool.getEntityID(1));
        pool.addEntity(3000, List.of());
        assertEquals(3000, pool.getEntityID(0));
        pool.removeEntity(2000);
        assertThrows(IllegalStateException.class, () -> pool.getEntityID(1));
    }

    private static HeapPool pool() {
        return new HeapPool(new ComponentRegistry(new FieldRegistry(new StructRegistry())), List.of(), 8, 8, 8);
    }
}
