package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.*;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.*;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import org.joml.*;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class DirectStorageBenchmark {
    private HeapPool direct;
    private BoxedHeapPool boxed;
    private Value value;
    private PrimitiveArray directID;
    private PrimitiveArray boxedID;

    @Setup
    public void setup() {
        com.cleanroommc.common.CleanroomEnvironment.setSide(net.minecraftforge.fml.relauncher.Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("child", Child.class, new MemberLayout("seed", "flag"),
            new StructDef(new FieldDef(ScalarType.LONG), new FieldDef(ScalarType.BOOL)));
        structs.lock();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType type : ScalarType.values()) {
            fields.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        }
        fields.registerFieldType("child", Child.class, new FieldDef("child")); fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("id", "mass", "position", "child", "matrix"),
            "INT", "FLOAT", "VEC3", "child", "MAT4");
        registry.lock();
        direct = new HeapPool(registry, List.of(Value.class), 16, 16, 16);
        boxed = new BoxedHeapPool(registry, List.of(Value.class), 16, 16, 16);
        value = new Value(); value.id = 1000; value.mass = 7.25f;
        value.position = new Vector3f(1.5f, 2.5f, 3.5f);
        value.child = new Child(); value.child.seed = 1000000L; value.child.flag = true;
        value.matrix = new Matrix4f().translation(4, 5, 6);
        direct.addEntity(1000, List.of(value)); boxed.addEntity(1000, List.of(value));
        directID = direct.getArray(Value.class, "id"); boxedID = boxed.getArray(Value.class, "id");
        if (!Arrays.equals(registry.flattenComponent(direct.getComponent(1000, Value.class)),
            registry.flattenComponent(boxed.getComponent(1000, Value.class)))) {
            throw new IllegalStateException("Different component values");
        }
    }

    @Benchmark
    public int directWrite() { value.id++; direct.setComponent(1000, value); return directID.getInt(0); }
    @Benchmark
    public int boxedWrite() { value.id++; boxed.setComponent(1000, value); return boxedID.getInt(0); }
    @Benchmark
    public CleanComponent directRead() { return direct.getComponent(1000, Value.class); }
    @Benchmark
    public CleanComponent boxedRead() { return boxed.getComponent(1000, Value.class); }
    @Benchmark
    public PrimitiveArray directBinding() { return direct.getArray(Value.class, "position", "x"); }
    @Benchmark
    public PrimitiveArray boxedBinding() { return boxed.getArray(Value.class, "position", "x"); }

    public static class Child { public long seed; public boolean flag; }
    public static class Value implements CleanComponent {
        public int id; public float mass; public Vector3f position; public Child child; public Matrix4f matrix;
    }
}
