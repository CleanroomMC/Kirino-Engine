package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ExternalDataBindingBenchmark {
    @Param({"1", "8", "16"})
    public int fields;

    private JobDefinition definition;
    private Map<String, Object> input;

    @Setup
    public void setup() {
        CleanroomEnvironment.setSide(Side.CLIENT);
        FieldRegistry types = new FieldRegistry(new StructRegistry());
        types.lock();
        ComponentRegistry components = new ComponentRegistry(types);
        components.lock();
        JobRegistry jobs = new JobRegistry(components);
        Class<? extends ParallelJob> type = switch (fields) {
            case 1 -> One.class;
            case 8 -> Eight.class;
            case 16 -> Sixteen.class;
            default -> throw new IllegalArgumentException("Unsupported field count");
        };
        jobs.registerParallelJob(type);
        definition = jobs.getDefinition(type);
        input = new HashMap<>();
        for (JobDefinition.ExternalBinding binding : definition.externalBindings) {
            input.put(binding.key, binding.valueType == String.class ? "text" : 713);
        }
        if (!Arrays.equals(captureUnchecked(), captureValidated())) {
            throw new IllegalStateException("External captures disagree.");
        }
    }

    @Benchmark
    public Object[] captureUnchecked() {
        Preconditions.checkArgument(input != null,
            "Argument \"externalData\" must not be null since there are %s external data queries.", definition.externalBindings.length);
        Object[] values = new Object[definition.externalBindings.length];
        for (int i = 0; i < values.length; i++) {
            String key = definition.externalBindings[i].key;
            Preconditions.checkArgument(input.containsKey(key), "Missing the entry \"%s\" from \"externalData\".", key);
            values[i] = input.get(key);
        }
        return values;
    }

    @Benchmark
    public Object[] captureValidated() {
        return definition.resolveExternalData(input);
    }

    public static class One implements ParallelJob {
        @JobExternalDataQuery("example.first") private int renamed;
        public void execute(EntityCommands commands, int row, int entity, int ordinal) {}
        public void query(EntityQuery query) {}
        public int estimateWorkload(int row) { return 1; }
    }

    public static class Eight extends One {
        @JobExternalDataQuery private String text1;
        @JobExternalDataQuery private Object object1;
        @JobExternalDataQuery private int number1;
        @JobExternalDataQuery private String text2;
        @JobExternalDataQuery private Object object2;
        @JobExternalDataQuery private int number2;
        @JobExternalDataQuery private int number3;
    }

    public static class Sixteen extends Eight {
        @JobExternalDataQuery private String text3;
        @JobExternalDataQuery private Object object3;
        @JobExternalDataQuery private int number4;
        @JobExternalDataQuery private String text4;
        @JobExternalDataQuery private Object object4;
        @JobExternalDataQuery private int number5;
        @JobExternalDataQuery private Object object5;
        @JobExternalDataQuery private int number6;
    }
}
