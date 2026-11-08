package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.*;
import com.cleanroommc.kirino.ecs.component.schema.meta.*;
import org.joml.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DirectStorageTest {
    @Test
    void retainedColumns_growthShrinkAndReuse_keepIdentityAndCurrentBackingArrays() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(All.class), 1, 2, 1);
        PrimitiveArray[] columns = registry.getLayout(All.class).columns().stream()
            .map(column -> pool.getArray(All.class, column)).toArray(PrimitiveArray[]::new);
        Object[] first = values(1);
        pool.addEntity(1000, List.of(registry.newComponent("all", first)));
        pool.addEntity(1001, List.of(registry.newComponent("all", values(2))));
        pool.addEntity(1002, List.of(registry.newComponent("all", values(3))));
        assertEquals(3, columns[0].length());
        for (int i = 0; i < columns.length; i++) {
            assertSame(columns[i], pool.getArray(All.class, registry.getLayout(All.class).columns().get(i)));
        }
        assertEquals(1003, columns[2].getInt(2));
        columns[2].setInt(0, 99);
        assertEquals(99, ((All) pool.getComponent(1000, All.class)).i);
        pool.removeEntity(1002);
        assertEquals(2, columns[0].length());
        assertThrows(IndexOutOfBoundsException.class, () -> columns[2].getInt(2));
        pool.removeEntity(1001);
        assertEquals(1, columns[0].length());
        pool.addEntity(2000, List.of(registry.newComponent("all", values(4))));
        assertEquals(3, columns[0].length());
        assertEquals(1004, columns[2].getInt(1));
        assertEquals(99, columns[2].getInt(0));
        first[2] = 99;
        assertArrayEquals(first, registry.flattenComponent(pool.getComponent(1000, All.class)));
        assertArrayEquals(values(4), registry.flattenComponent(pool.getComponent(2000, All.class)));
    }

    @Test
    void directTransfer_allScalarTypesAndNonzeroRows_matchesBoundaryRepresentation() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(All.class), 2, 2, 1);
        for (int i = 0; i < 2; i++) {
            Object[] input = values(i + 1);
            CleanComponent value = registry.newComponent("all", input);
            pool.addEntity(1000 + i, List.of(value));
            CleanComponent output = pool.getComponent(1000 + i, All.class);
            assertArrayEquals(input, registry.flattenComponent(output));
            All original = (All) value;
            All copy = (All) output;
            assertNotSame(original.v3, copy.v3);
            assertNotSame(original.m4, copy.m4);
            assertEquals(original.m4.m12(), copy.m4.m12(), 0f);
        }
        Object[] special = values(3);
        special[4] = -0.0f; special[5] = Double.POSITIVE_INFINITY;
        special[9] = Float.NaN; special[10] = Float.NEGATIVE_INFINITY;
        pool.setComponent(1001, registry.newComponent("all", special));
        assertArrayEquals(special, registry.flattenComponent(pool.getComponent(1001, All.class)));
        assertThrows(IllegalStateException.class, () -> pool.getArray(All.class, "i").getFloat(0));
    }

    @Test
    void nestedComponentsAndMultipleTypes_writeDirectlyWithoutAliasing() {
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("child", Child.class, new MemberLayout("id", "position"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef(ScalarType.VEC3)));
        structs.lock();
        FieldRegistry fields = new FieldRegistry(structs);
        fields.registerFieldType("child", Child.class, new FieldDef("child"));
        fields.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("pair", Pair.class, new MemberLayout("a", "b"), "child", "child");
        registry.registerComponent("other", Other.class, new MemberLayout("value"), "float");
        registry.lock();
        HeapPool pool = new HeapPool(registry, List.of(Pair.class, Other.class), 1, 1, 1);
        Pair pair = (Pair) registry.newComponent("pair", 1000, 1f, 2f, 3f, 2000, 4f, 5f, 6f);
        Other other = new Other(); other.value = 99;
        pool.addEntity(1, List.of(other, pair));
        Pair output = (Pair) pool.getComponent(1, Pair.class);
        assertNotSame(output.a, output.b);
        assertEquals(1000, output.a.id); assertEquals(2000, output.b.id);
        assertEquals(99f, pool.getArray(Other.class, "value").getFloat(0), 0f);
        Object[] before = registry.flattenComponent(output);
        pair.b = null;
        assertThrows(IllegalArgumentException.class, () -> pool.setComponent(1, pair));
        assertArrayEquals(before, registry.flattenComponent(pool.getComponent(1, Pair.class)));
        assertThrows(IllegalArgumentException.class, () -> pool.addEntity(2, List.of(other, pair)));
        assertFalse(pool.containsEntity(2));
        assertEquals(1, pool.getArrayRange().end);
        pair.b = new Child(); pair.b.position = new Vector3f();
        pool.addEntity(2, List.of(pair, other));
        assertEquals(2, pool.getArrayRange().end);
    }

    @Test
    void rowLifecycle_holesAndTrailingRemovals_preserveAllLiveRows() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(All.class), 0, 2, 1);
        for (int i = 0; i < 6; i++) { pool.addEntity(i, List.of(registry.newComponent("all", values(i)))); }
        pool.removeEntity(1); pool.removeEntity(3); pool.removeEntity(5); pool.removeEntity(4);
        assertEquals(3, pool.getArrayRange().end);
        assertEquals(Set.of(1), pool.getArrayRange().deprecatedIndexes);
        assertTrue(pool.getCurrentSize() >= 3);
        assertEquals(1002, ((All) pool.getComponent(2, All.class)).i);
        pool.addEntity(9, List.of(registry.newComponent("all", values(9))));
        assertEquals(9, pool.getEntityID(1));
        assertTrue(pool.getArrayRange().deprecatedIndexes.isEmpty());
        assertThrows(IllegalArgumentException.class, () -> pool.removeEntity(3));
        assertThrows(IllegalArgumentException.class, () -> pool.getComponent(3, All.class));
        assertThrows(IllegalArgumentException.class, () -> pool.addEntity(9, List.of(registry.newComponent("all", values(1)))));
    }

    @Test
    void invalidInsertion_preservesEntityAndRowState() {
        ComponentRegistry registry = registry();
        HeapPool pool = new HeapPool(registry, List.of(All.class), 1, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> pool.addEntity(1, List.of()));
        assertThrows(IllegalArgumentException.class, () -> pool.addEntity(1, List.of(new Other())));
        assertFalse(pool.containsEntity(1));
        pool.addEntity(1, List.of(registry.newComponent("all", values(1))));
        assertEquals(0, pool.getArrayRange().start);
        assertEquals(1, pool.getArrayRange().end);
    }

    @Test
    void primitiveWrapperFields_allowNullValidationAndTypedTransfer() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT)); fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("boxed", Boxed.class, new MemberLayout("value"), "int"); registry.lock();
        HeapPool pool = new HeapPool(registry, List.of(Boxed.class), 1, 1, 1);
        Boxed boxed = new Boxed(); boxed.value = 1000;
        pool.addEntity(1, List.of(boxed));
        assertEquals(1000, ((Boxed) pool.getComponent(1, Boxed.class)).value);
        boxed.value = null;
        assertThrows(IllegalArgumentException.class, () -> pool.setComponent(1, boxed));
        assertEquals(1000, pool.getArray(Boxed.class, "value").getInt(0));
    }

    @Test
    void scalarType_boundaryCreation_preservesWrongInputAndFieldOrderingBehavior() {
        assertNull(ScalarType.INT.newScalar(1L));
        assertNull(ScalarType.VEC3.newScalar(1f, 2f));
        assertNull(ScalarType.VEC3.newScalar(1f, "wrong", 3f));
        Vector4f vector = (Vector4f) ScalarType.VEC4.newScalar(1f, 2f, 3f, 4f);
        assertEquals(1f, vector.x, 0f); assertEquals(4f, vector.w, 0f);
        assertEquals(3, ScalarType.VEC4.ordinalOffsetOfField("w"));
    }

    private static ComponentRegistry registry() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        ScalarType[] scalars = ScalarType.values();
        String[] names = new String[scalars.length];
        for (int i = 0; i < scalars.length; i++) {
            names[i] = scalars[i].name(); fields.registerFieldType(names[i], scalars[i].clazz, new FieldDef(scalars[i]));
        }
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("all", All.class,
            new MemberLayout("b", "s", "i", "l", "f", "d", "flag", "v2", "v3", "v4", "m3", "m4"), names);
        registry.lock(); return registry;
    }

    private static Object[] values(int seed) {
        Object[] values = new Object[41];
        values[0] = (byte) seed; values[1] = (short) (seed + 100); values[2] = seed + 1000;
        values[3] = 100000L + seed; values[4] = seed + 0.25f; values[5] = seed + 0.125d; values[6] = seed % 2 == 0;
        for (int i = 7; i < values.length; i++) { values[i] = seed * 100f + i; }
        return values;
    }

    public static class All implements CleanComponent {
        public byte b; public short s; public int i; public long l; public float f; public double d; public boolean flag;
        public Vector2f v2; public Vector3f v3; public Vector4f v4; public Matrix3f m3; public Matrix4f m4;
    }
    public static class Child { public int id; public Vector3f position; }
    public static class Pair implements CleanComponent { public Child a; public Child b; }
    public static class Other implements CleanComponent { public float value; }
    public static class Boxed implements CleanComponent { public Integer value; }
}
