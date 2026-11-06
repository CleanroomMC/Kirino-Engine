package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HeapPoolTest {
    public static class Values implements CleanComponent {
        public byte b;
        public short s;
        public int i;
        public long l;
        public float f;
        public double d;
        public boolean flag;
    }

    private HeapPool pool(int initial, int growth, int shrink) {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        ScalarType[] types = {ScalarType.BYTE, ScalarType.SHORT, ScalarType.INT,
            ScalarType.LONG, ScalarType.FLOAT, ScalarType.DOUBLE, ScalarType.BOOL};
        Class<?>[] classes = {byte.class, short.class, int.class, long.class,
            float.class, double.class, boolean.class};
        String[] names = {"b", "s", "i", "l", "f", "d", "flag"};
        for (int i = 0; i < types.length; i++) {
            fields.registerFieldType(names[i], classes[i], new FieldDef(types[i]));
        }
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("values", Values.class, new MemberLayout(names), names);
        return new HeapPool(registry, List.of(Values.class), initial, growth, shrink);
    }

    private void add(HeapPool pool, int id) {
        Values values = new Values();
        values.b = (byte) id;
        values.s = (short) id;
        values.i = id;
        values.l = 1L << 40 | id;
        values.f = id + 0.5f;
        values.d = id + 0.25;
        values.flag = (id & 1) == 0;
        pool.addEntity(id, List.of(values));
    }

    private void assertValues(HeapPool pool, int id) {
        Values value = (Values) pool.getComponent(id, Values.class);
        assertEquals((byte) id, value.b);
        assertEquals((short) id, value.s);
        assertEquals(id, value.i);
        assertEquals(1L << 40 | id, value.l);
        assertEquals(id + 0.5f, value.f, 0f);
        assertEquals(id + 0.25, value.d, 0d);
        assertEquals((id & 1) == 0, value.flag);
    }

    @Test
    void addEntity_bulkLoad_preservesAllColumns() {
        HeapPool pool = pool(100, 50, 50);
        for (int id = 0; id < 20_000; id++) add(pool, id);
        for (int id = 0; id < 20_000; id++) assertValues(pool, id);
    }

    @Test
    void removeEntity_tailBatches_preservesSurvivorsAcrossRegrowth() {
        HeapPool pool = pool(100, 50, 50);
        for (int id = 0; id < 1_000; id++) add(pool, id);
        for (int cycle = 0; cycle < 10; cycle++) {
            for (int id = 999; id >= 700; id--) pool.removeEntity(id);
            for (int id = 700; id < 1_000; id++) add(pool, id);
        }
        assertValues(pool, 0);
        assertValues(pool, 999);
    }

    @Test
    void removeEntity_sparseTail_preservesSurvivorsAndAcceptsNewEntities() {
        HeapPool pool = pool(100, 50, 50);
        for (int id = 0; id < 1_000; id++) add(pool, id);
        for (int id = 999; id >= 250; id--) pool.removeEntity(id);
        for (int id = 0; id < 250; id++) assertValues(pool, id);
        for (int id = 249; id >= 0; id--) pool.removeEntity(id);
        add(pool, 123);
        assertValues(pool, 123);
    }

    @Test
    void removeEntity_interiorHoles_reusesSlotsWithoutTruncatingHighEntities() {
        HeapPool pool = pool(100, 50, 50);
        for (int id = 0; id < 1_000; id++) add(pool, id);
        for (int id = 0; id < 900; id++) pool.removeEntity(id);
        assertValues(pool, 999);
        for (int id = 1_000; id < 1_900; id++) add(pool, id);
        for (int id = 900; id < 1_900; id++) assertValues(pool, id);
    }
}
