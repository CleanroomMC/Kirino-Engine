package com.cleanroommc.kirino.ecs.component.schema.def.field;

import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.utils.collections.Object2ObjectBiMap;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class FieldRegistryLookupBenchmark {
    @Param({"present", "mixed99", "missing"})
    public String lookup;

    private FieldRegistry registry;
    private Object2ObjectBiMap<String, Class<?>> previousTypes;
    private Map<String, FieldDef> previousDefinitions;
    private Class<?>[] queries;
    private int cursor;

    @Setup
    public void setup() {
        registry = new FieldRegistry(new StructRegistry());
        previousTypes = new Object2ObjectBiMap<>();
        previousDefinitions = new Object2ObjectOpenHashMap<>();
        ScalarType[] types = ScalarType.values();
        for (int i = 0; i < types.length; i++) {
            ScalarType type = types[i];
            FieldDef definition = new FieldDef(type);
            registry.registerFieldType(type.name(), type.clazz, definition);
            previousTypes.put(type.name(), type.clazz);
            previousDefinitions.put(type.name(), definition);
        }
        registry.lock();
        previousTypes.lock();
        Class<?>[] missingTypes = {String.class, Object.class, Number.class, CharSequence.class,
            Runnable.class, Thread.class, Class.class, ClassLoader.class, Exception.class,
            IllegalArgumentException.class, int[].class, float[].class};
        int hitPercent = switch (lookup) {
            case "present" -> 100;
            case "mixed99" -> 99;
            case "missing" -> 0;
            default -> throw new IllegalArgumentException("Unknown lookup scenario: " + lookup);
        };
        queries = new Class<?>[4096];
        FieldDef[] expected = new FieldDef[queries.length];
        int hits = queries.length * hitPercent / 100;
        for (int i = 0; i < queries.length; i++) {
            if (i < hits) {
                ScalarType type = types[i % types.length];
                queries[i] = type.clazz;
                expected[i] = previousDefinitions.get(type.name());
            } else {
                queries[i] = missingTypes[(i - hits) % missingTypes.length];
            }
        }
        Random random = new Random(0x4B4952494E4FL);
        for (int i = queries.length - 1; i > 0; i--) {
            int other = random.nextInt(i + 1);
            Class<?> query = queries[i];
            queries[i] = queries[other];
            queries[other] = query;
            FieldDef definition = expected[i];
            expected[i] = expected[other];
            expected[other] = definition;
        }
        for (int i = 0; i < queries.length; i++) {
            if (previousLookup(queries[i]) != expected[i] || registry.getFieldDef(queries[i]) != expected[i]) {
                throw new IllegalStateException("Different field definition for " + queries[i].getName());
            }
        }
    }

    @Benchmark
    public FieldDef previousClassToNameToDefinition() {
        return previousLookup(nextQuery());
    }

    @Benchmark
    public FieldDef indexedClassToDefinition() {
        return registry.getFieldDef(nextQuery());
    }

    private Class<?> nextQuery() {
        return queries[cursor++ & (queries.length - 1)];
    }

    private FieldDef previousLookup(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);
        String name = previousTypes.inverse().get(clazz);
        return name == null ? null : previousDefinitions.get(name);
    }
}
