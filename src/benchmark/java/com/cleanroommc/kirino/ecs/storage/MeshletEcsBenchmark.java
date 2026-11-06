package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.*;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.usage.ecs.struct.*;
import net.minecraftforge.fml.relauncher.Side;
import org.joml.Vector3f;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Measures the existing meshlet paths: flattening a {@link MeshletComponent} on insertion
 * ({@link ComponentRegistry#flattenComponent}) and rebuilding it from a pool on destruction
 * ({@link HeapPool#getComponent}).
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
public class MeshletEcsBenchmark {
    static {
        // JMH skips TestEnvironmentSetup; reflection needs the side set before FML initializes.
        CleanroomEnvironment.setSide(Side.CLIENT);
    }

    private ComponentRegistry registry;
    private MeshletComponent value;
    private HeapPool pool;

    @Setup
    public void setup() {
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
        Field[] members = MeshletComponent.class.getDeclaredFields();
        String[] names = Arrays.stream(members).map(Field::getName).toArray(String[]::new);
        String[] types = Arrays.stream(members).map(f -> fields.getFieldTypeName(f.getType())).toArray(String[]::new);
        registry.registerComponent("meshlet", MeshletComponent.class, new MemberLayout(names), types);
        value = new MeshletComponent();
        value.meshletId = 1001;
        for (int i = 0; i < 32; i++) value.getBlock(i).blockInfo.xPlusFaceTexCoord0 = 1000 + i;
        pool = new HeapPool(registry, List.of(MeshletComponent.class), 100, 50, 50);
        pool.addEntity(42, List.of(value));
    }

    /**
     * @return the meshlet rebuilt from the pool, as the destroy callback does
     */
    @Benchmark
    public Object reconstructComponent() {
        return pool.getComponent(42, MeshletComponent.class);
    }

    /**
     * @return the meshlet flattened into scalar columns, as entity creation does
     */
    @Benchmark
    public Object[] flattenComponent() {
        return registry.flattenComponent(value);
    }
}
