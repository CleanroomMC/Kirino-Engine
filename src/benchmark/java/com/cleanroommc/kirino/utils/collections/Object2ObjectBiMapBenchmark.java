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
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class Object2ObjectBiMapBenchmark {
    @Param({"fastutil", "previous", "guava"})
    public String implementation;

    @Param({"64", "4096"})
    public int size;

    private BiMap<Integer, Integer> map;
    private Integer[] keys;
    private Integer[] values;
    private Integer[] replacements;
    private int cursor;
    private long checksum;
    private BiConsumer<Integer, Integer> accumulator;
    private BiFunction<Integer, Integer, Integer> valueReplacement;

    @Setup
    public void setup() {
        map = switch (implementation) {
            case "fastutil" -> new Object2ObjectBiMap<>(size);
            case "previous" -> new PreviousObject2ObjectBiMap<>(size);
            case "guava" -> HashBiMap.create(size);
            default -> throw new IllegalArgumentException(implementation);
        };
        keys = new Integer[size];
        values = new Integer[size];
        replacements = new Integer[size];
        cursor = 0;
        accumulator = (key, value) -> checksum += key + value;
        valueReplacement = (key, value) -> value.equals(values[key]) ? replacements[key] : values[key];
        for (int i = 0; i < size; i++) {
            keys[i] = i;
            values[i] = i + size;
            replacements[i] = i + size * 2;
            map.put(keys[i], values[i]);
        }
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
    public Integer replaceValue() {
        int index = cursor++ & (size - 1);
        Integer previous = map.put(keys[index], replacements[index]);
        replacements[index] = previous;
        return previous;
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
    public void entryIteration(Blackhole blackhole) {
        for (var entry : map.entrySet()) {
            blackhole.consume(entry);
        }
    }

    @Benchmark
    public long forEach() {
        checksum = 0;
        map.forEach(accumulator);
        return checksum;
    }

    @Benchmark
    public Integer replaceAllValues() {
        map.replaceAll(valueReplacement);
        return map.get(keys[cursor++ & (size - 1)]);
    }
}
