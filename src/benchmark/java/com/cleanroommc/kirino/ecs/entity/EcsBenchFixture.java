package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.AABB;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.Block;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.BlockInfo;
import net.minecraftforge.fml.relauncher.Side;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Terrain schema shared by the flush benchmarks. Keep in sync with EcsFixture.
 */
public final class EcsBenchFixture {
    static {
        // JMH skips TestEnvironmentSetup; reflection needs the side set before FML initializes.
        CleanroomEnvironment.setSide(Side.CLIENT);
    }

    public static class Tiny implements CleanComponent {
        public int a;
        public float b;
    }

    public static class Tag implements CleanComponent {
        public int tag;
    }

    public static class T0 implements CleanComponent {
        public int v;
    }

    public static class T1 implements CleanComponent {
        public int v;
    }

    public static class T2 implements CleanComponent {
        public int v;
    }

    public static class T3 implements CleanComponent {
        public int v;
    }

    public static class T4 implements CleanComponent {
        public int v;
    }

    public static class T5 implements CleanComponent {
        public int v;
    }

    public static class T6 implements CleanComponent {
        public int v;
    }

    public static class T7 implements CleanComponent {
        public int v;
    }

    static final List<Class<? extends CleanComponent>> ARCHETYPE_PARTS =
        List.of(T0.class, T1.class, T2.class, T3.class, T4.class, T5.class, T6.class, T7.class);

    /**
     * Must be a power of two: template() wraps the index with a mask.
     */
    static final int TEMPLATES = 256;

    final ComponentRegistry registry;

    private final MeshletComponent[] templates = new MeshletComponent[TEMPLATES];

    EcsBenchFixture() {
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
        registry = new ComponentRegistry(fields);
        register(fields, "meshlet", MeshletComponent.class);
        register(fields, "tiny", Tiny.class);
        register(fields, "tag", Tag.class);
        for (int i = 0; i < ARCHETYPE_PARTS.size(); i++) {
            register(fields, "t" + i, ARCHETYPE_PARTS.get(i));
        }
        for (int i = 0; i < TEMPLATES; i++) {
            templates[i] = buildMeshlet(i);
        }
    }

    private void register(FieldRegistry fields, String name, Class<? extends CleanComponent> type) {
        Field[] members = type.getDeclaredFields();
        String[] names = Arrays.stream(members).map(Field::getName).toArray(String[]::new);
        String[] types = Arrays.stream(members).map(f -> fields.getFieldTypeName(f.getType())).toArray(String[]::new);
        registry.registerComponent(name, type, new MemberLayout(names), types);
    }

    EntityManager newManager() {
        return new EntityManager(registry);
    }

    /**
     * Reuse templates to keep large batches from allocating a meshlet object graph per entity.
     * The create callback changes meshletId, which flush copies into storage before the next callback.
     */
    MeshletComponent template(int ordinal) {
        return templates[ordinal & (TEMPLATES - 1)];
    }

    static MeshletComponent buildMeshlet(int variant) {
        MeshletComponent m = new MeshletComponent();
        m.aabb = new AABB(variant, variant + 1, variant + 2, variant + 3, variant + 4, variant + 5);
        m.normal.set(variant, -variant, 0.5f);
        m.pass = variant % 3;
        m.chunkPosX = variant;
        m.chunkPosY = variant * 2 - 5;
        m.chunkPosZ = -variant;
        m.blockCount = 32;
        for (int i = 0; i < 32; i++) {
            Block block = m.getBlock(i);
            block.faceMask = 0b101010;
            block.positionAndFaceMask = Block.compress(i & 15, (i >> 1) & 15, i & 7, block.faceMask);
            // Stay outside the Integer cache so the benchmark includes boxing allocations.
            block.blockInfo.xPlusFaceTexCoord0 = 1_000 + variant * 100 + i;
        }
        return m;
    }

    /**
     * GPU registry stub with locking and set bookkeeping.
     */
    static final class GpuRegistryStub {
        private final ArrayDeque<Integer> freeIds = new ArrayDeque<>();
        private final Set<Integer> added = new HashSet<>();
        private final Set<Integer> removed = new HashSet<>();
        private int counter;

        synchronized void allocate(MeshletComponent meshlet) {
            meshlet.meshletId = freeIds.isEmpty() ? counter++ : freeIds.removeLast();
            added.add(meshlet.meshletId);
        }

        synchronized void dispose(int id) {
            removed.add(id);
        }
    }

    static EntityCreateCallback createCallback(GpuRegistryStub gpu) {
        return ctx -> gpu.allocate((MeshletComponent) ctx.getComponent(MeshletComponent.class));
    }

    static EntityDestroyCallback destroyCallback(GpuRegistryStub gpu) {
        return ctx -> gpu.dispose(((MeshletComponent) ctx.getComponent(MeshletComponent.class)).meshletId);
    }

    /**
     * MeshletDestroyJob visits entities in FORWARD order.
     */
    public enum Order {
        FORWARD, REVERSE, SHUFFLED, EVERY_OTHER_THEN_REST;

        int[] indices(int count) {
            int[] ids = new int[count];
            for (int i = 0; i < count; i++) ids[i] = i;
            switch (this) {
                case FORWARD -> {
                }
                case REVERSE -> {
                    for (int i = 0; i < count; i++) ids[i] = count - 1 - i;
                }
                case SHUFFLED -> {
                    Random random = new Random(0xEC5L);
                    for (int i = count - 1; i > 0; i--) {
                        int j = random.nextInt(i + 1);
                        int t = ids[i];
                        ids[i] = ids[j];
                        ids[j] = t;
                    }
                }
                case EVERY_OTHER_THEN_REST -> {
                    int k = 0;
                    for (int i = 0; i < count; i += 2) ids[k++] = i;
                    for (int i = 1; i < count; i += 2) ids[k++] = i;
                }
            }
            return ids;
        }
    }
}
