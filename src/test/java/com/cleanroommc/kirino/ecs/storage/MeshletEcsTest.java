package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.*;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.*;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class MeshletEcsTest {
    private FieldRegistry fields() {
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.registerFieldType("float", float.class, new FieldDef(ScalarType.FLOAT));
        fields.registerFieldType("vec3", Vector3f.class, new FieldDef(ScalarType.VEC3));
        for (Class<?> type : List.of(AABB.class, BlockInfo.class, Block.class)) {
            List<String> names = new ArrayList<>();
            List<FieldDef> defs = new ArrayList<>();
            for (Field field : type.getDeclaredFields()) {
                if (fields.fieldTypeExists(field.getType())) {
                    names.add(field.getName());
                    defs.add(fields.getFieldDef(fields.getFieldTypeName(field.getType())));
                }
            }
            structs.registerStructType(type.getSimpleName(), type, new MemberLayout(names), new StructDef(defs));
            fields.registerFieldType(type.getSimpleName(), type, new FieldDef(type.getSimpleName()));
        }
        return fields;
    }

    private ComponentRegistry components(FieldRegistry fields) {
        ComponentRegistry components = new ComponentRegistry(fields);
        Field[] members = MeshletComponent.class.getDeclaredFields();
        String[] names = Arrays.stream(members).map(Field::getName).toArray(String[]::new);
        String[] types = Arrays.stream(members).map(f -> fields.getFieldTypeName(f.getType())).toArray(String[]::new);
        components.registerComponent("meshlet", MeshletComponent.class, new MemberLayout(names), types);
        return components;
    }

    @Test
    void flattenComponent_nestedMeshlet_matchesFieldOrderAndRoundTrips() {
        FieldRegistry fields = fields();
        ComponentRegistry registry = components(fields);
        MeshletComponent value = new MeshletComponent();
        value.meshletId = 1001;
        value.chunkPosX = -42;
        value.normal.set(1, 2, 3);
        value.aabb.xMin = -12.5f;
        for (int i = 0; i < 32; i++) {
            value.getBlock(i).positionAndFaceMask = 1234 + i;
            value.getBlock(i).blockInfo.xPlusFaceTexCoord0 = 2000 + i;
        }
        // Compare component flattening with the per-field API.
        List<Object> expected = new ArrayList<>();
        for (Field member : MeshletComponent.class.getDeclaredFields()) {
            try {
                Collections.addAll(expected, fields.flattenField(member.get(value)));
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        Object[] flattened = registry.flattenComponent(value);
        assertArrayEquals(expected.toArray(), flattened);
        MeshletComponent copy = (MeshletComponent) registry.newComponent("meshlet", flattened);
        assertArrayEquals(flattened, registry.flattenComponent(copy));
        assertEquals(value.normal, copy.normal);
        assertEquals(2031, copy.block31.blockInfo.xPlusFaceTexCoord0);
    }

    @Test
    void destroyContext_getComponent_returnsStoredMeshletForEntityIdNotEqualToSlot() {
        ComponentRegistry registry = components(fields());
        HeapPool pool = new HeapPool(registry, List.of(MeshletComponent.class), 1, 1, 1);
        MeshletComponent value = new MeshletComponent();
        value.meshletId = 12345;
        value.block31.blockInfo.xPlusFaceTexCoord0 = 654321;
        pool.addEntity(99, List.of(value));
        // Use an entity ID that differs from its pool slot.
        EntityDestroyContext context = new EntityDestroyContext();
        context.setInternal(99, List.of(MeshletComponent.class), pool);
        MeshletComponent first = (MeshletComponent) context.getComponent(MeshletComponent.class);
        assertEquals(12345, first.meshletId);
        assertEquals(654321, first.block31.blockInfo.xPlusFaceTexCoord0);
        pool.removeEntity(99);
        value.meshletId = 9;
        pool.addEntity(100, List.of(value));
        context.setInternal(100, List.of(MeshletComponent.class), pool);
        assertEquals(9, ((MeshletComponent) context.getComponent(MeshletComponent.class)).meshletId);
        assertThrows(IllegalArgumentException.class, () -> context.getComponent(HeapPoolTest.Values.class));
    }
}
