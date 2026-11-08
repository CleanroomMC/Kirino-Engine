package com.cleanroommc.kirino.utils.collections;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ImmutableArrayTraversalBenchmark {
    @Param({"1", "8", "64"})
    public int size;

    private List<Value> previous;
    private ImmutableObjectArrayList<Value> current;

    @Setup
    public void setup() {
        Value[] values = new Value[size];
        for (int i = 0; i < size; i++) {
            values[i] = new Value(i + 1000);
        }
        previous = List.of(values);
        current = ImmutableObjectArrayList.copyOf(values);
        if (jdkIterator() != directArray() || jdkIndexed() != directArray()) {
            throw new IllegalStateException("Different traversal result");
        }
    }

    @Benchmark
    public int jdkIterator() {
        int result = 0;
        for (Value value : previous) {
            result += value.value;
        }
        return result;
    }

    @Benchmark
    public int jdkIndexed() {
        int result = 0;
        for (int i = 0, length = previous.size(); i < length; i++) {
            result += previous.get(i).value;
        }
        return result;
    }

    @Benchmark
    public int directArray() {
        int result = 0;
        for (Value value : current.elements()) {
            result += value.value;
        }
        return result;
    }

    private static final class Value {
        private final int value;

        private Value(int value) {
            this.value = value;
        }
    }
}
