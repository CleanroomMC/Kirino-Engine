package com.cleanroommc.kirino.ecs.component.schema.def.field.struct;

import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class StructRegistryBenchmark {
    private final StructRegistry registry = new StructRegistry();
    private final BiMap<String, Class<?>> previousTypes = HashBiMap.create();
    private final Map<String, StructRegistry.Data> previousData = new Object2ObjectOpenHashMap<>();
    private String[] names;
    private Class<?>[] classes;
    private int cursor;

    @Setup
    public void setup() {
        classes = new Class<?>[]{Fixture0.class, Fixture1.class, Fixture2.class, Fixture3.class, Fixture4.class, Fixture5.class, Fixture6.class, Fixture7.class, Fixture8.class, Fixture9.class, Fixture10.class, Fixture11.class, Fixture12.class, Fixture13.class, Fixture14.class, Fixture15.class};
        names = new String[classes.length];
        MemberLayout layout = new MemberLayout("value");
        StructDef definition = new StructDef(new FieldDef(ScalarType.INT));
        for (int i = 0; i < classes.length; i++) {
            names[i] = "Struct" + i;
            registry.registerStructType(names[i], classes[i], layout, definition);
            registry.flattenedUnitCount(names[i]);
            previousTypes.put(names[i], classes[i]);
            previousData.put(names[i], registry.getStructDataMap().get(names[i]).copy());
        }
        registry.lock();
    }

    @Benchmark
    public String previousClassToName() {
        Class<?> type = classes[cursor++ & 15];
        Preconditions.checkNotNull(type);
        return previousTypes.inverse().get(type);
    }

    @Benchmark
    public String indexedClassToName() {
        return registry.getStructTypeName(classes[cursor++ & 15]);
    }

    @Benchmark
    public int previousCachedUnitCount() {
        String name = names[cursor++ & 15];
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(previousTypes.containsKey(name), "Struct type %s doesn't exist.", name);
        return previousData.get(name).getUnit();
    }

    @Benchmark
    public int indexedCachedUnitCount() {
        return registry.flattenedUnitCount(names[cursor++ & 15]);
    }
    public static class Fixture { public int value; }
    public static class Fixture0 extends Fixture {}
    public static class Fixture1 extends Fixture {}
    public static class Fixture2 extends Fixture {}
    public static class Fixture3 extends Fixture {}
    public static class Fixture4 extends Fixture {}
    public static class Fixture5 extends Fixture {}
    public static class Fixture6 extends Fixture {}
    public static class Fixture7 extends Fixture {}
    public static class Fixture8 extends Fixture {}
    public static class Fixture9 extends Fixture {}
    public static class Fixture10 extends Fixture {}
    public static class Fixture11 extends Fixture {}
    public static class Fixture12 extends Fixture {}
    public static class Fixture13 extends Fixture {}
    public static class Fixture14 extends Fixture {}
    public static class Fixture15 extends Fixture {}
}
