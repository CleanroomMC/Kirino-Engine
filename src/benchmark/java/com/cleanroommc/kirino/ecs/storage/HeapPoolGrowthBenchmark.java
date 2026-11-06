package com.cleanroommc.kirino.ecs.storage;

import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Compare fixed-step and geometric growth on 64 integer columns.
 * Measures array allocation and copying without component flattening or ECS bookkeeping.
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
public class HeapPoolGrowthBenchmark {
    @Param({"4096", "16384"})
    public int entities;

    @Benchmark
    public int[][] fixedStep() {
        return load(false);
    }

    @Benchmark
    public int[][] geometric() {
        return load(true);
    }

    private int[][] load(boolean geometric) {
        int capacity = 100;
        int[][] columns = new int[64][capacity];
        for (int entity = 0; entity < entities; entity++) {
            if (entity == capacity) {
                capacity += geometric ? Math.max(50, capacity / 2) : 50;
                for (int column = 0; column < columns.length; column++) {
                    columns[column] = Arrays.copyOf(columns[column], capacity);
                }
            }
            columns[0][entity] = entity;
        }
        return columns;
    }
}
