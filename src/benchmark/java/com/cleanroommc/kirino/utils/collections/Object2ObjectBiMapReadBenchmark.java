package com.cleanroommc.kirino.utils.collections;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class Object2ObjectBiMapReadBenchmark {
    @Param({"mutable", "locked", "guava"})
    public String implementation;

    @Param({"64", "4096"})
    public int size;

    private BiMap<Integer, Integer> map;
    private Integer[] keys;
    private Integer[] values;
    private int cursor;
    private long checksum;
    private BiConsumer<Integer, Integer> accumulator;

    @Setup
    public void setup() {
        map = implementation.equals("guava") ? HashBiMap.create(size) : new Object2ObjectBiMap<>(size);
        keys = new Integer[size];
        values = new Integer[size];
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = i + size;
            map.put(keys[i], values[i]);
        }
        if (implementation.equals("locked")) {
            ((Object2ObjectBiMap<Integer, Integer>) map).lock();
        }
        cursor = 0;
        accumulator = (key, value) -> checksum += key + value;
    }

    @Benchmark
    public Integer forwardLookup() {
        return map.get(keys[cursor++ & (size - 1)]);
    }

    @Benchmark
    public Integer inverseLookup() {
        return map.inverse().get(values[cursor++ & (size - 1)]);
    }

    @Benchmark
    public long keyIteration() {
        long sum = 0;
        for (Integer key : map.keySet()) {
            sum += key;
        }
        return sum;
    }

    @Benchmark
    public long valueIteration() {
        long sum = 0;
        for (Integer value : map.values()) {
            sum += value;
        }
        return sum;
    }

    @Benchmark
    public long forEach() {
        checksum = 0;
        map.forEach(accumulator);
        return checksum;
    }
}
