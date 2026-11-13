package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ArchetypeKeyBenchmark {
    @Param({"0", "1", "4", "16", "64"}) public int components;
    private List<Class<? extends CleanComponent>> types;
    private final Object2ObjectOpenHashMap<LegacyKey, Object> legacy = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectOpenHashMap<ArchetypeKey, Object> current = new Object2ObjectOpenHashMap<>();

    @Setup
    public void setup() throws Exception {
        byte[] bytecode;
        try (var input = Value.class.getResourceAsStream("ArchetypeKeyBenchmark$Value.class")) {
            bytecode = Objects.requireNonNull(input).readAllBytes();
        }
        class Loader extends ClassLoader {
            Loader() { super(Value.class.getClassLoader()); }
            Class<? extends CleanComponent> copy() {
                return defineClass(Value.class.getName(), bytecode, 0, bytecode.length).asSubclass(CleanComponent.class);
            }
        }
        types = new ArrayList<>();
        for (int i = 0; i < components; i++) {
            types.add(new Loader().copy());
        }
        Object marker = new Object();
        legacy.put(new LegacyKey(types), marker);
        current.put(new ArchetypeKey(types), marker);
        Collections.reverse(types);
        if (legacyLookup() != marker || currentLookup() != marker) {
            throw new IllegalStateException("Key equality changed");
        }
    }

    @Benchmark public Object legacyConstruction() { return new LegacyKey(types); }
    @Benchmark public Object currentConstruction() { return new ArchetypeKey(types); }
    @Benchmark public Object legacyLookup() { return legacy.get(new LegacyKey(types)); }
    @Benchmark public Object currentLookup() { return current.get(new ArchetypeKey(types)); }

    private static final class LegacyKey {
        private final ReferenceOpenHashSet<Class<? extends CleanComponent>> components;
        private final int hash;
        public final String key;

        LegacyKey(List<Class<? extends CleanComponent>> components) {
            Objects.requireNonNull(components);
            this.components = new ReferenceOpenHashSet<>();
            int hash = 0;
            for (Class<? extends CleanComponent> component : components) {
                Objects.requireNonNull(component);
                if (!this.components.add(component)) {
                    throw new IllegalArgumentException("Duplicate component: " + component.getName());
                }
                hash += System.identityHashCode(component);
            }
            this.hash = hash;
            key = String.join(",", components.stream().map(Class::getName).sorted().toList());
        }

        @Override public int hashCode() { return hash; }
        @Override public boolean equals(Object other) {
            return this == other || other instanceof LegacyKey that
                && components.size() == that.components.size() && components.containsAll(that.components);
        }
    }

    public static class Value implements CleanComponent {
    }
}
