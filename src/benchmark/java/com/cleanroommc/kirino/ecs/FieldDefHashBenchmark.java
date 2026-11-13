package com.cleanroommc.kirino.ecs;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class FieldDefHashBenchmark {
    @Param({"scalar", "struct"})
    public String kind;

    private FieldDef[] fields;
    private int cursor;

    @Setup
    public void setup() {
        fields = new FieldDef[64];
        ScalarType[] types = ScalarType.values();
        for (int i = 0; i < fields.length; i++) {
            fields[i] = kind.equals("scalar") ? new FieldDef(types[i % types.length]) : new FieldDef("Struct" + i);
            fields[i].hashCode();
        }
        cursor = 0;
    }

    @Benchmark
    public int objectsHash() {
        FieldDef field = fields[cursor++ & 63];
        return Objects.hash(field.fieldKind, field.scalarType, field.structTypeName);
    }

    @Benchmark
    public int directHash() {
        return fields[cursor++ & 63].hashCode();
    }
}
