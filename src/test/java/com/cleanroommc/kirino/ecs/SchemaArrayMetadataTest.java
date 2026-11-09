package com.cleanroommc.kirino.ecs;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaArrayMetadataTest {
    @Test
    void descriptors_listAndArrayInputs_preserveTypedIndependentStorage() {
        FieldDef definition = new FieldDef(ScalarType.INT);
        List<FieldDef> definitions = new ArrayList<>(List.of(definition));
        List<String> names = new ArrayList<>(List.of("value"));
        StructDef struct = new StructDef(definitions);
        MemberLayout members = new MemberLayout(names);
        ComponentDesc component = new ComponentDesc("component", definitions, names);
        definitions.clear();
        names.clear();
        FieldDef[] structElements = struct.fields.elements();
        FieldDef[] componentElements = component.fields.elements();
        String[] memberElements = members.fieldNames.elements();
        String[] typeNames = component.fieldTypeNames.elements();
        assertArrayEquals(new FieldDef[]{definition}, structElements);
        assertArrayEquals(new FieldDef[]{definition}, componentElements);
        assertArrayEquals(new String[]{"value"}, memberElements);
        assertArrayEquals(new String[]{"value"}, typeNames);
        assertSame(structElements, struct.fields.elements());
        assertNotSame(structElements, componentElements);
        FieldDef[] input = {definition};
        String[] inputNames = {"value"};
        StructDef fromArray = new StructDef(input);
        MemberLayout namesFromArray = new MemberLayout(inputNames);
        ComponentDesc descFromArray = new ComponentDesc("component", List.of(definition), inputNames);
        input[0] = new FieldDef(ScalarType.FLOAT);
        inputNames[0] = "changed";
        assertSame(definition, fromArray.fields.elements()[0]);
        assertEquals("value", namesFromArray.fieldNames.elements()[0]);
        assertEquals("value", descFromArray.fieldTypeNames.elements()[0]);
        assertThrows(UnsupportedOperationException.class, () -> struct.fields.set(0, definition));
        assertThrows(UnsupportedOperationException.class, () -> component.fieldTypeNames.clear());
        assertThrows(NullPointerException.class, () -> new StructDef(Arrays.asList((FieldDef) null)));
        assertThrows(NullPointerException.class, () -> new MemberLayout(Arrays.asList((String) null)));
    }

    @Test
    void layout_nestedColumnsAndMembers_exposesTypedArraysWithResolvedIdentity() {
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("leaf", Leaf.class, new MemberLayout("value"),
            new StructDef(new FieldDef(ScalarType.INT)));
        structs.registerStructType("root", Root.class, new MemberLayout("leaf", "position"),
            new StructDef(new FieldDef("leaf"), new FieldDef(ScalarType.VEC3)));
        structs.lock();
        SchemaLayout layout = structs.getLayout("root");
        SchemaLayout.Member[] members = layout.members().elements();
        SchemaLayout.Column[] columns = layout.columns().elements();
        assertEquals(2, members.length);
        assertEquals(4, columns.length);
        assertSame(members, layout.members().elements());
        assertSame(columns, layout.columns().elements());
        assertArrayEquals(new String[]{"leaf", "value"}, columns[0].path().elements());
        assertArrayEquals(new String[]{"position", "z"}, columns[3].path().elements());
        assertSame(columns[0], layout.resolve("leaf", "value"));
        assertSame(columns[3], layout.resolve("position", "z"));
        Object[] values = {42, 1f, 2f, 3f};
        assertArrayEquals(values, structs.flattenStruct(structs.newStruct("root", values)));
        for (ScalarType type : ScalarType.values()) {
            FlattenedScalarType[] types = type.flattenedTypes.elements();
            assertEquals(type.unitCount, types.length);
            for (FlattenedScalarType primitive : types) {
                assertSame(type.primitiveType, primitive);
            }
        }
    }

    @Test
    void emptyMetadata_allArraysHaveCorrectRuntimeTypeAndNoCapacityTail() {
        FieldDef[] fields = new StructDef().fields.elements();
        String[] names = new MemberLayout().fieldNames.elements();
        ComponentDesc descriptor = new ComponentDesc("empty", List.of(), List.of());
        FieldDef[] descriptorFields = descriptor.fields.elements();
        String[] descriptorNames = descriptor.fieldTypeNames.elements();
        assertEquals(0, fields.length);
        assertEquals(0, names.length);
        assertEquals(0, descriptorFields.length);
        assertEquals(0, descriptorNames.length);
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("empty", Empty.class, new MemberLayout(), new StructDef());
        structs.lock();
        SchemaLayout layout = structs.getLayout("empty");
        SchemaLayout.Member[] members = layout.members().elements();
        SchemaLayout.Column[] columns = layout.columns().elements();
        assertEquals(0, members.length);
        assertEquals(0, columns.length);
        assertArrayEquals(new Object[0], structs.flattenStruct(structs.newStruct("empty")));
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(structs));
        HeapPool pool = new HeapPool(registry, List.of(), 1);
        Class<?>[] components = pool.components.elements();
        assertEquals(0, components.length);
    }

    public static class Leaf { public int value; }
    public static class Root { public Leaf leaf; public org.joml.Vector3f position; }
    public static class Empty {}
}
