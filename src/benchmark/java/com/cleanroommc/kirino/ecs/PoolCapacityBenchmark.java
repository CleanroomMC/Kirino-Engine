package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import net.minecraftforge.fml.relauncher.Side;
import org.joml.Matrix4f;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PoolCapacityBenchmark {
    @Param({"1000", "16000"})
    public int rows;
    @Param({"scalars", "wide"})
    public String schema;

    private ComponentRegistry registry;
    private List<Class<? extends CleanComponent>> types;
    private CleanComponent[] payload;
    private HeapPool candidate;
    private FixedStepHeapPool fixed;
    private PrimitiveArray candidateIDs;
    private PrimitiveArray fixedIDs;

    @Setup
    public void setup() {
        CleanroomEnvironment.setSide(Side.CLIENT);
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        for (ScalarType type : ScalarType.values()) {
            fields.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        }
        fields.lock();
        registry = new ComponentRegistry(fields);
        registry.registerComponent("scalars", Scalars.class,
            new MemberLayout("b", "s", "id", "l", "f", "d", "flag"),
            "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BOOL");
        registry.registerComponent("wide", Wide.class,
            new MemberLayout("b", "s", "id", "l", "f", "d", "flag", "a", "c", "e", "g"),
            "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BOOL", "MAT4", "MAT4", "MAT4", "MAT4");
        registry.lock();
        Scalars value = schema.equals("wide") ? new Wide() : new Scalars();
        value.b = 3; value.s = 5; value.id = 12345; value.l = 9876543210L;
        value.f = 2.5f; value.d = 7.25; value.flag = true;
        types = List.of(value.getClass());
        payload = new CleanComponent[]{value};
        candidate = newCandidate();
        fixed = newFixed();
        fill(candidate, 0, rows);
        fill(fixed, 0, rows);
        candidateIDs = candidate.getArray(types.getFirst(), "id");
        fixedIDs = fixed.getArray(types.getFirst(), "id");
        verify(candidate); verify(fixed);
        if (!Arrays.equals(registry.flattenComponent(candidate.getComponent(rows - 1, types.getFirst())),
            registry.flattenComponent(fixed.getComponent(rows - 1, types.getFirst())))) {
            throw new IllegalStateException("Different component values.");
        }
        candidateDrainAndRefill(); fixedDrainAndRefill();
        candidateBoundaryChurn(); fixedBoundaryChurn();
        verify(candidate); verify(fixed);
        if (candidateIDs != candidate.getArray(types.getFirst(), "id")
            || fixedIDs != fixed.getArray(types.getFirst(), "id")) {
            throw new IllegalStateException("Column identity changed.");
        }
    }

    @Benchmark
    public ArchetypeDataPool candidateGrowth() {
        HeapPool pool = newCandidate();
        fill(pool, 0, rows);
        return pool;
    }

    @Benchmark
    public ArchetypeDataPool fixedGrowth() {
        FixedStepHeapPool pool = newFixed();
        fill(pool, 0, rows);
        return pool;
    }

    @Benchmark
    public int candidateDrainAndRefill() { return drainAndRefill(candidate); }
    @Benchmark
    public int fixedDrainAndRefill() { return drainAndRefill(fixed); }
    @Benchmark
    public int candidateBoundaryChurn() { return boundaryChurn(candidate, candidateIDs); }
    @Benchmark
    public int fixedBoundaryChurn() { return boundaryChurn(fixed, fixedIDs); }

    private int drainAndRefill(ArchetypeDataPool pool) {
        int remaining = rows / 8;
        for (int i = rows - 1; i >= remaining; i--) pool.removeEntity(i);
        pool.trimCapacity();
        fill(pool, remaining, rows);
        return pool.getEntityID(rows - 1);
    }

    private int boundaryChurn(ArchetypeDataPool pool, PrimitiveArray column) {
        int checksum = 0;
        for (int i = 0; i < 128; i++) {
            pool.addEntity(rows, payload);
            checksum += column.getInt(rows);
            pool.removeEntity(rows);
            pool.trimCapacity();
        }
        return checksum;
    }

    private void fill(ArchetypeDataPool pool, int from, int end) {
        for (int i = from; i < end; i++) pool.addEntity(i, payload);
    }

    private HeapPool newCandidate() { return new HeapPool(registry, types, 100); }
    private FixedStepHeapPool newFixed() { return new FixedStepHeapPool(registry, types, 100, 50, 50); }

    @TearDown
    public void verify() {
        verify(candidate); verify(fixed);
        if (candidateIDs != candidate.getArray(types.getFirst(), "id")
            || fixedIDs != fixed.getArray(types.getFirst(), "id")) {
            throw new IllegalStateException("Column identity changed.");
        }
    }

    private void verify(ArchetypeDataPool pool) {
        if (pool.getRows().limit != rows || pool.getCurrentSize() < rows) {
            throw new IllegalStateException("Invalid capacity or row count.");
        }
        PrimitiveArray ids = pool.getArray(types.getFirst(), "id");
        if (!Arrays.equals(registry.flattenComponent(payload[0]),
            registry.flattenComponent(pool.getComponent(rows - 1, types.getFirst())))) {
            throw new IllegalStateException("Component values changed.");
        }
        for (int i = 0; i < rows; i++) {
            if (pool.getEntityID(i) != i || ids.getInt(i) != 12345) {
                throw new IllegalStateException("Invalid row values.");
            }
        }
    }

    public static class Scalars implements CleanComponent {
        public byte b; public short s; public int id; public long l;
        public float f; public double d; public boolean flag;
    }
    public static class Wide extends Scalars {
        public Matrix4f a = new Matrix4f().translation(1, 2, 3);
        public Matrix4f c = new Matrix4f().translation(4, 5, 6);
        public Matrix4f e = new Matrix4f().translation(7, 8, 9);
        public Matrix4f g = new Matrix4f().translation(10, 11, 12);
    }
}
