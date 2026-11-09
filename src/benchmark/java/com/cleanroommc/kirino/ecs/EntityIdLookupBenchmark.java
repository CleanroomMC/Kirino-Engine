package com.cleanroommc.kirino.ecs;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EntityIdLookupBenchmark {
    @Param({"present", "missing"})
    public String lookup;
    private Int2IntOpenHashMap previousRows;
    private int baseRow;
    private int cursor;

    @Setup
    public void setup() {
        previousRows = new Int2IntOpenHashMap();
        for (int row = 0; row < 64; row++) {
            previousRows.put(row, 1000 + row);
        }
        baseRow = lookup.equals("present") ? 0 : 64;
    }

    @Benchmark
    public Optional<Integer> boxedResult() {
        int row = baseRow + (cursor++ & 63);
        return previousRows.containsKey(row) ? Optional.of(previousRows.get(row)) : Optional.empty();
    }

    @Benchmark
    public OptionalInt primitiveResult() {
        int row = baseRow + (cursor++ & 63);
        return previousRows.containsKey(row) ? OptionalInt.of(previousRows.get(row)) : OptionalInt.empty();
    }
}
