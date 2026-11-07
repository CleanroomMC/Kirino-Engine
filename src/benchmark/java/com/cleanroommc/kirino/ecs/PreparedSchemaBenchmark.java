package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.*;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import org.joml.Vector3f;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PreparedSchemaBenchmark {
    private StructRegistry prepared;
    private LegacyStructRegistry legacy;
    private Root instance;
    private Object[] values;
    private String[][] paths;
    private int cursor;

    @Setup
    public void setup() {
        com.cleanroommc.common.CleanroomEnvironment.setSide(net.minecraftforge.fml.relauncher.Side.CLIENT);
        prepared = new StructRegistry();
        legacy = new LegacyStructRegistry();
        MemberLayout leafMembers = new MemberLayout("count", "position");
        StructDef leaf = new StructDef(new FieldDef(ScalarType.INT), new FieldDef(ScalarType.VEC3));
        MemberLayout rootMembers = new MemberLayout("id", "first", "second", "mass");
        StructDef root = new StructDef(new FieldDef(ScalarType.INT), new FieldDef("leaf"), new FieldDef("leaf"), new FieldDef(ScalarType.FLOAT));
        prepared.registerStructType("leaf", Leaf.class, leafMembers, leaf);
        prepared.registerStructType("root", Root.class, rootMembers, root);
        legacy.registerStructType("leaf", Leaf.class, leafMembers, leaf);
        legacy.registerStructType("root", Root.class, rootMembers, root);
        prepared.lock(); legacy.lock();
        values = new Object[]{7, 9, 1f, 2f, 3f, 11, 4f, 5f, 6f, 8f};
        instance = (Root) prepared.newStruct("root", values);
        if (!Arrays.equals(legacy.flattenStruct(instance), prepared.flattenStruct(instance))) {
            throw new IllegalStateException("Different conversion order");
        }
        paths = new String[][]{{"first", "position", "x"}, {"second", "position", "z"}};
        for (String[] path : paths) {
            if (legacy.getFieldOrdinal("root", path) != prepared.getFieldOrdinal("root", path)) {
                throw new IllegalStateException("Different field ordinal");
            }
        }
        legacy.newStruct("root", values);
    }

    @Benchmark
    public int legacyPath() { return legacy.getFieldOrdinal("root", paths[cursor++ & 1]); }
    @Benchmark
    public int preparedPath() { return prepared.getFieldOrdinal("root", paths[cursor++ & 1]); }
    @Benchmark
    public Object[] legacyFlatten() { return legacy.flattenStruct(instance); }
    @Benchmark
    public Object[] preparedFlatten() { return prepared.flattenStruct(instance); }
    @Benchmark
    public Object legacyCreate() { return legacy.newStruct("root", values); }
    @Benchmark
    public Object preparedCreate() { return prepared.newStruct("root", values); }

    public static class Leaf { public int count; public Vector3f position; }
    public static class Root { public int id; public Leaf first; public Leaf second; public float mass; }
}
