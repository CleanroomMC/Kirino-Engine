package com.cleanroommc.kirino.ecs;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldRegistryTest {
    @Test
    void lookups_beforeAndAfterLock_returnSameDefinitionByNameAndClass() {
        FieldRegistry registry = new FieldRegistry(new StructRegistry());
        for (ScalarType type : ScalarType.values()) {
            registry.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        }
        verifyLookups(registry);
        registry.lock();
        verifyLookups(registry);
        assertThrows(IllegalStateException.class, registry::lock);
        assertThrows(IllegalStateException.class,
            () -> registry.registerFieldType("extra", String.class, new FieldDef(ScalarType.INT)));
    }

    @Test
    void registration_duplicateNameOrClass_preservesBothIndexes() {
        FieldRegistry registry = new FieldRegistry(new StructRegistry());
        FieldDef original = new FieldDef(ScalarType.INT);
        registry.registerFieldType("int", int.class, original);
        assertThrows(IllegalStateException.class,
            () -> registry.registerFieldType("int", float.class, new FieldDef(ScalarType.FLOAT)));
        assertThrows(IllegalArgumentException.class,
            () -> registry.registerFieldType("alias", int.class, new FieldDef(ScalarType.INT)));
        assertThrows(NullPointerException.class,
            () -> registry.registerFieldType("bad", float.class, null));
        assertSame(original, registry.getFieldDef("int"));
        assertSame(original, registry.getFieldDef(int.class));
        assertEquals("int", registry.getFieldTypeName(int.class));
        assertNull(registry.getFieldClass("alias"));
        assertNull(registry.getFieldDef("bad"));
        assertFalse(registry.fieldTypeExists(float.class));
        registry.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        registry.lock();
    }

    @Test
    void flattenField_wrappedPrimitives_usesPrimitiveRegistrations() {
        FieldRegistry registry = new FieldRegistry(new StructRegistry());
        ScalarType[] types = {ScalarType.BYTE, ScalarType.SHORT, ScalarType.INT, ScalarType.LONG,
            ScalarType.FLOAT, ScalarType.DOUBLE, ScalarType.BOOL};
        Object[] values = {(byte) 2, (short) 3, 1000, 2000L, 1.25f, 2.5d, true};
        for (ScalarType type : types) {
            registry.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        }
        registry.lock();
        for (int i = 0; i < types.length; i++) {
            assertArrayEquals(new Object[]{values[i]}, registry.flattenField(values[i]));
            assertNull(registry.getFieldDef(values[i].getClass()));
            assertEquals(values[i], registry.newField(types[i].name(), values[i]));
        }
        assertThrows(IllegalArgumentException.class, () -> registry.flattenField('a'));
        assertThrows(IllegalArgumentException.class, () -> registry.flattenField("unknown"));
    }

    @Test
    void flattenField_vectorAndForwardStruct_preservesConversionsAndLockValidation() {
        StructRegistry structs = new StructRegistry();
        FieldRegistry registry = new FieldRegistry(structs);
        FieldDef definition = new FieldDef("point");
        registry.registerFieldType("pointField", Point.class, definition);
        assertThrows(IllegalArgumentException.class, registry::lock);
        assertSame(definition, registry.getFieldDef(Point.class));
        structs.registerStructType("point", Point.class, new MemberLayout("value"),
            new StructDef(new FieldDef(ScalarType.INT)));
        structs.lock();
        registry.registerFieldType("vector", Vector3f.class, new FieldDef(ScalarType.VEC3));
        registry.lock();
        Object point = registry.newField("pointField", 1234);
        assertArrayEquals(new Object[]{1234}, registry.flattenField(point));
        assertArrayEquals(new Object[]{1f, 2f, 3f}, registry.flattenField(new Vector3f(1, 2, 3)));
        assertNull(registry.newField("unknown", 1));
    }

    private static void verifyLookups(FieldRegistry registry) {
        for (ScalarType type : ScalarType.values()) {
            assertSame(registry.getFieldDef(type.name()), registry.getFieldDef(type.clazz));
            assertEquals(type.name(), registry.getFieldTypeName(type.clazz));
            assertEquals(type.name(), registry.getFieldTypeName_ClassName(type.clazz.getName()));
            assertSame(type.clazz, registry.getFieldClass(type.name()));
            assertTrue(registry.fieldTypeExists(type.clazz));
            assertTrue(registry.fieldTypeExists(type.name()));
            assertTrue(registry.fieldTypeExists_ClassName(type.clazz.getName()));
        }
        assertNull(registry.getFieldDef(String.class));
        assertNull(registry.getFieldDef("unknown"));
        assertNull(registry.getFieldClass("unknown"));
        assertNull(registry.getFieldTypeName(String.class));
        assertNull(registry.getFieldTypeName_ClassName(String.class.getName()));
        assertFalse(registry.fieldTypeExists(String.class));
        assertFalse(registry.fieldTypeExists("unknown"));
        assertFalse(registry.fieldTypeExists_ClassName(String.class.getName()));
        assertThrows(NullPointerException.class, () -> registry.getFieldDef((Class<?>) null));
        assertThrows(NullPointerException.class, () -> registry.getFieldDef((String) null));
    }

    public static class Point {
        public int value;
    }
}
