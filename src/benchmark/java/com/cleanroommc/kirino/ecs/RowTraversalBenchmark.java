package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class RowTraversalBenchmark {
    @Param({"65536"})
    public int rowCount;
    @Param({"0", "50", "90"})
    public int removedPercent;
    private HeapPool candidate;
    private LegacyRowsHeapPool legacy;
    private long expected;

    @Setup
    public void setup() {
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        candidate = new HeapPool(registry, List.of(), rowCount);
        legacy = new LegacyRowsHeapPool(registry, List.of(), rowCount);
        CleanComponent[] empty = new CleanComponent[0];
        for (int row = 0; row < rowCount; row++) {
            candidate.addEntity(entityID(row), empty);
            legacy.addEntity(entityID(row), empty);
        }
        for (int row = 0; row < rowCount; row++) {
            if (removed(row)) {
                candidate.removeEntity(entityID(row));
                legacy.removeEntity(entityID(row));
            } else {
                expected += entityID(row);
            }
        }
        verify();
    }

    @Benchmark
    public Object legacyPrepare() { return legacy.getArrayRange(); }
    @Benchmark
    public Object candidatePrepare() { return candidate.getRows(); }

    @Benchmark
    public long legacyPrepareAndTraverse() {
        ArrayRange range = legacy.getArrayRange();
        long checksum = 0;
        for (int row = range.start; row < range.end; row++) {
            if (!range.deprecatedIndexes.contains(row)) checksum += legacy.getEntityID(row);
        }
        return checksum;
    }

    @Benchmark
    public long candidatePrepareAndTraverse() {
        PoolRows range = candidate.getRows();
        long checksum = 0;
        for (int row = 0; row < range.limit; row++) {
            if (range.nextFree[row] == 0) checksum += candidate.getEntityID(row);
        }
        return checksum;
    }

    @TearDown
    public void verify() {
        if (candidatePrepareAndTraverse() != expected || legacyPrepareAndTraverse() != expected) {
            throw new IllegalStateException("Live row traversal differs.");
        }
    }

    private boolean removed(int row) { return row % 10 < removedPercent / 10; }

    private static int entityID(int row) {
        return switch (row) {
            case 0 -> Integer.MIN_VALUE;
            case 1 -> Integer.MAX_VALUE;
            case 2 -> 0;
            default -> row;
        };
    }
}
