package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.*;
import com.google.common.base.Preconditions;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EcsValidationBenchmark {
    private ComponentRegistry components;
    private String[][] paths;
    private int cursor;

    @Setup
    public void setup() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("integer", int.class, new FieldDef(ScalarType.INT));
        components = new ComponentRegistry(fields);
        components.registerComponent("test", TestComponent.class, new MemberLayout("first", "second"), "integer", "integer");
        components.lock();
        paths = new String[][]{{"first"}, {"second"}};
    }

    @Benchmark
    public int ordinalWithEagerDiagnostics() {
        String[] path = paths[cursor++ & 1];
        Preconditions.checkArgument(path.length != 0,
            "The given \"fieldAccessChain\"=%s must not be empty.", formatFieldAccessChain(path));
        Preconditions.checkArgument(path[0].equals("first") || path[0].equals("second"),
            "Can't find a field (from component \"%s\") that matches the \"fieldAccessChain\"=%s.",
            "test", formatFieldAccessChain(path));
        return components.getFieldOrdinal("test", path);
    }

    @Benchmark
    public int ordinalWithLazyDiagnostics() {
        return components.getFieldOrdinal("test", paths[cursor++ & 1]);
    }

    private static String formatFieldAccessChain(String... fieldAccessChain) {
        if (fieldAccessChain.length == 0) {
            return "\"\"";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("\".");
        for (int i = 0; i < fieldAccessChain.length; i++) {
            builder.append(fieldAccessChain[i]);
            if (i != fieldAccessChain.length - 1) {
                builder.append(".");
            }
        }
        builder.append("\"");
        return builder.toString();
    }

    public static class TestComponent implements CleanComponent {
        public int first;
        public int second;
    }
}
