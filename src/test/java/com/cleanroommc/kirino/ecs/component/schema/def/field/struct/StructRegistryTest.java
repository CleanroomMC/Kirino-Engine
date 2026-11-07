package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StructRegistryTest {
    @Test
    void registration_bothIndexes_resolveSameMetadata() {
        StructRegistry registry = new StructRegistry();
        MemberLayout layout = new MemberLayout("value");
        StructDef definition = new StructDef(new FieldDef(ScalarType.INT));
        registry.registerStructType("leaf", Leaf.class, layout, definition);

        assertTrue(registry.structTypeExists("leaf"));
        assertTrue(registry.structTypeExists(Leaf.class));
        assertEquals("leaf", registry.getStructTypeName(Leaf.class));
        assertSame(Leaf.class, registry.getStructClass("leaf"));
        assertSame(layout, registry.getClassMemberLayout("leaf"));
        assertSame(definition, registry.getStructDef("leaf"));
        StructRegistry.Data data = registry.getStructDataMap().get("leaf");
        assertEquals("leaf", data.getName());
        assertSame(Leaf.class, data.getStructClass());
        assertSame(layout, data.getMemberLayout());
        assertSame(definition, data.getStructDef());
    }

    @Test
    void registration_duplicateNameOrClass_leavesIndexesUnchanged() {
        StructRegistry registry = new StructRegistry();
        registry.registerStructType("leaf", Leaf.class, new MemberLayout(), new StructDef());
        assertThrows(IllegalStateException.class,
            () -> registry.registerStructType("leaf", Root.class, new MemberLayout(), new StructDef()));
        assertThrows(IllegalArgumentException.class,
            () -> registry.registerStructType("alias", Leaf.class, new MemberLayout(), new StructDef()));
        assertEquals(1, registry.getStructDataMap().size());
        assertFalse(registry.structTypeExists("alias"));
        assertFalse(registry.structTypeExists(Root.class));
        assertEquals("leaf", registry.getStructTypeName(Leaf.class));
        assertSame(Leaf.class, registry.getStructClass("leaf"));
        registry.registerStructType("alias", Root.class, new MemberLayout(), new StructDef());
        assertEquals("alias", registry.getStructTypeName(Root.class));
    }

    @Test
    void lookups_missingType_preserveNullAndExceptionBehavior() {
        StructRegistry registry = new StructRegistry();
        assertFalse(registry.structTypeExists("missing"));
        assertFalse(registry.structTypeExists(Leaf.class));
        assertNull(registry.getStructClass("missing"));
        assertNull(registry.getStructTypeName(Leaf.class));
        assertNull(registry.getStructDef("missing"));
        assertNull(registry.getClassMemberLayout("missing"));
        assertNull(registry.newStruct("missing"));
        assertThrows(IllegalArgumentException.class, () -> registry.flattenedUnitCount("missing"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("missing", "value"));
        assertThrows(IllegalArgumentException.class, () -> registry.flattenStruct(new Leaf()));
    }

    @Test
    void registration_nullArguments_leaveBothIndexesEmpty() {
        StructRegistry registry = new StructRegistry();
        assertThrows(NullPointerException.class,
            () -> registry.registerStructType(null, Leaf.class, new MemberLayout(), new StructDef()));
        assertThrows(NullPointerException.class,
            () -> registry.registerStructType("leaf", null, new MemberLayout(), new StructDef()));
        assertThrows(NullPointerException.class,
            () -> registry.registerStructType("leaf", Leaf.class, null, new StructDef()));
        assertThrows(NullPointerException.class,
            () -> registry.registerStructType("leaf", Leaf.class, new MemberLayout(), null));
        assertTrue(registry.getStructDataMap().isEmpty());
        assertFalse(registry.structTypeExists(Leaf.class));
    }

    @Test
    void lookups_nullArguments_remainRejected() {
        StructRegistry registry = new StructRegistry();
        assertThrows(NullPointerException.class, () -> registry.structTypeExists((String) null));
        assertThrows(NullPointerException.class, () -> registry.structTypeExists((Class<?>) null));
        assertThrows(NullPointerException.class, () -> registry.getStructTypeName(null));
        assertThrows(NullPointerException.class, () -> registry.getStructClass(null));
        assertThrows(NullPointerException.class, () -> registry.getStructDef(null));
        assertThrows(NullPointerException.class, () -> registry.getClassMemberLayout(null));
        assertThrows(NullPointerException.class, () -> registry.flattenedUnitCount(null));
        assertThrows(NullPointerException.class, () -> registry.flattenStruct(null));
        assertThrows(NullPointerException.class, () -> registry.newStruct("missing", (Object[]) null));
        assertThrows(NullPointerException.class, () -> registry.newStruct("missing", new Object[]{null}));
    }

    @Test
    void lock_preparesCountsAndProhibitsRegistration() {
        StructRegistry registry = nestedRegistry();
        registry.lock();
        assertEquals(5, registry.getStructDataMap().get("root").getUnit());
        assertEquals(5, registry.flattenedUnitCount("root"));
        assertEquals(5, registry.flattenedUnitCount("root"));
        assertThrows(IllegalStateException.class, registry::lock);
        assertThrows(IllegalStateException.class,
            () -> registry.registerStructType("empty", Empty.class, new MemberLayout(), new StructDef()));
        assertFalse(registry.structTypeExists(Empty.class));
    }

    @Test
    void flattenedUnitCount_emptyAndNestedStructs_cacheZeroAndNestedCounts() {
        StructRegistry registry = nestedRegistry();
        registry.registerStructType("empty", Empty.class, new MemberLayout(), new StructDef());
        assertEquals(-1, registry.getStructDataMap().get("empty").getUnit());
        assertEquals(0, registry.flattenedUnitCount("empty"));
        assertEquals(0, registry.flattenedUnitCount("empty"));
        assertEquals(0, registry.getStructDataMap().get("empty").getUnit());
        assertEquals(5, registry.flattenedUnitCount("root"));
        assertEquals(1, registry.getStructDataMap().get("leaf").getUnit());
        assertEquals(5, registry.getStructDataMap().get("root").getUnit());
    }

    @Test
    void getFieldOrdinal_nestedAndVectorFields_preservesOffsetsAndValidation() {
        StructRegistry registry = nestedRegistry();
        assertEquals(0, registry.getFieldOrdinal("root", "id"));
        assertEquals(1, registry.getFieldOrdinal("root", "leaf", "value"));
        assertEquals(2, registry.getFieldOrdinal("root", "position", "x"));
        assertEquals(4, registry.getFieldOrdinal("root", "position", "z"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("root"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("root", "missing"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("root", "leaf"));
        assertThrows(IllegalArgumentException.class, () -> registry.getFieldOrdinal("root", "id", "x"));
    }

    @Test
    void construction_nestedStruct_roundTripsBeforeAndAfterLock() {
        StructRegistry registry = nestedRegistry();
        Object[] values = {7, 42, 1.5f, -2.0f, 3.25f};
        Root root = assertInstanceOf(Root.class, registry.newStruct("root", values));
        assertEquals(7, root.id);
        assertEquals(42, root.leaf.value);
        assertEquals(new Vector3f(1.5f, -2.0f, 3.25f), root.position);
        assertArrayEquals(values, registry.flattenStruct(root));
        registry.lock();
        assertArrayEquals(values, registry.flattenStruct(registry.newStruct("root", values)));
    }

    @Test
    void flattenStruct_existingInstance_initializesClassIndexAndHandles() {
        StructRegistry registry = nestedRegistry();
        Root root = new Root();
        root.id = 3;
        root.leaf = new Leaf();
        root.leaf.value = 9;
        root.position = new Vector3f(1, 2, 3);
        assertArrayEquals(new Object[]{3, 9, 1f, 2f, 3f}, registry.flattenStruct(root));
    }

    @Test
    void snapshotAndCopy_preserveIdentityAndIndependentCachedCount() {
        StructRegistry registry = nestedRegistry();
        var snapshot = registry.getStructDataMap();
        StructRegistry.Data data = snapshot.get("root");
        StructRegistry.Data copy = data.copy();
        assertNotSame(data, copy);
        assertEquals(data.getName(), copy.getName());
        assertSame(data.getStructClass(), copy.getStructClass());
        assertSame(data.getStructDef(), copy.getStructDef());
        assertSame(data.getMemberLayout(), copy.getMemberLayout());
        registry.flattenedUnitCount("root");
        assertEquals(5, data.getUnit());
        assertEquals(-1, copy.getUnit());
        registry.registerStructType("empty", Empty.class, new MemberLayout(), new StructDef());
        assertFalse(snapshot.containsKey("empty"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove("root"));
    }

    private static StructRegistry nestedRegistry() {
        StructRegistry registry = new StructRegistry();
        registry.registerStructType("leaf", Leaf.class, new MemberLayout("value"),
            new StructDef(new FieldDef(ScalarType.INT)));
        registry.registerStructType("root", Root.class, new MemberLayout("id", "leaf", "position"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef("leaf"), new FieldDef(ScalarType.VEC3)));
        return registry;
    }

    public static class Empty {
    }

    public static class Leaf {
        public int value;
    }

    public static class Root {
        public int id;
        public Leaf leaf;
        public Vector3f position;
    }
}
