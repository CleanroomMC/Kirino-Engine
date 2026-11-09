package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EntityQueryCollectionsBenchmark {
    @Param({"ArrayList", "ObjectArrayList", "ReferenceArrayList", "ReferenceArraySet", "ReferenceOpenHashSet"})
    public String collection;

    @Param({"single", "small", "duplicates", "wide"})
    public String scenario;

    private Supplier<Collection<Class<? extends CleanComponent>>> factory;
    private Class<? extends CleanComponent>[] required;
    private Class<? extends CleanComponent>[] excluded;
    private ArchetypeKey[] archetypes;
    private Conditions prepared;
    private long expectedMatches;

    @Setup
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setup() {
        factory = switch (collection) {
            case "ArrayList" -> ArrayList::new;
            case "ObjectArrayList" -> ObjectArrayList::new;
            case "ReferenceArrayList" -> ReferenceArrayList::new;
            case "ReferenceArraySet" -> ReferenceArraySet::new;
            case "ReferenceOpenHashSet" -> ReferenceOpenHashSet::new;
            default -> throw new IllegalArgumentException(collection);
        };
        int requiredCount = switch (scenario) {
            case "single" -> 1;
            case "small", "duplicates" -> 4;
            case "wide" -> 16;
            default -> throw new IllegalArgumentException(scenario);
        };
        int excludedCount = scenario.equals("single") ? 0 : scenario.equals("wide") ? 4 : 2;
        int repetitions = scenario.equals("duplicates") ? 4 : 1;
        Class<? extends CleanComponent>[] types = new Class[]{
            C00.class, C01.class, C02.class, C03.class, C04.class, C05.class, C06.class, C07.class,
            C08.class, C09.class, C10.class, C11.class, C12.class, C13.class, C14.class, C15.class,
            C16.class, C17.class, C18.class, C19.class, C20.class, C21.class, C22.class, C23.class,
            C24.class, C25.class
        };
        required = new Class[requiredCount * repetitions];
        excluded = new Class[excludedCount * repetitions];
        for (int i = 0; i < required.length; i++) {
            required[i] = types[i % requiredCount];
        }
        for (int i = 0; i < excluded.length; i++) {
            excluded[i] = types[requiredCount + i % excludedCount];
        }
        List<ArchetypeKey> keys = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            List<Class<? extends CleanComponent>> components = new ArrayList<>();
            int group = i & 3;
            for (int j = 0; j < requiredCount; j++) {
                if ((group == 1 && j == 0) || (group == 2 && j == requiredCount - 1)
                        || (group == 3 && excludedCount == 0 && j == 0)) {
                    continue;
                }
                components.add(types[j]);
            }
            if (group == 3 && excludedCount != 0) {
                components.add(types[requiredCount]);
            }
            for (int bit = 0; bit < 6; bit++) {
                if ((i & (1 << bit)) != 0) {
                    components.add(types[20 + bit]);
                }
            }
            keys.add(new ArchetypeKey(components));
        }
        Collections.shuffle(keys, new Random(0x4B4952494E4FL));
        archetypes = keys.toArray(ArchetypeKey[]::new);
        expectedMatches = 0;
        for (int i = 0; i < archetypes.length; i++) {
            boolean matches = true;
            for (Class<? extends CleanComponent> type : required) {
                matches &= archetypes[i].contains(type);
            }
            for (Class<? extends CleanComponent> type : excluded) {
                matches &= !archetypes[i].contains(type);
            }
            if (matches) {
                expectedMatches |= 1L << i;
            }
        }
        if (Long.bitCount(expectedMatches) != 16) {
            throw new IllegalStateException("Expected exactly 16 matching archetypes");
        }
        prepared = build();
        int retainedRepetitions = collection.endsWith("Set") ? 1 : repetitions;
        if (prepared.required.size() != requiredCount * retainedRepetitions
                || prepared.excluded.size() != excludedCount * retainedRepetitions
                || matchPrepared() != expectedMatches || buildAndMatch() != expectedMatches) {
            throw new IllegalStateException("Different query results for " + collection + ": " + scenario);
        }
    }

    @Benchmark
    public Conditions build() {
        Collection<Class<? extends CleanComponent>> mustHave = factory.get();
        Collection<Class<? extends CleanComponent>> mustNotHave = factory.get();
        for (Class<? extends CleanComponent> component : required) {
            mustHave.add(component);
        }
        for (Class<? extends CleanComponent> component : excluded) {
            mustNotHave.add(component);
        }
        return new Conditions(mustHave, mustNotHave);
    }

    @Benchmark
    public long matchPrepared() {
        return match(prepared);
    }

    @Benchmark
    public long buildAndMatch() {
        return match(build());
    }

    private long match(Conditions query) {
        long matches = 0;
        for (int i = 0; i < archetypes.length; i++) {
            ArchetypeKey archetype = archetypes[i];
            boolean mustHave = true;
            for (Class<? extends CleanComponent> component : query.required) {
                if (!archetype.contains(component)) {
                    mustHave = false;
                    break;
                }
            }
            if (!mustHave) {
                continue;
            }
            boolean mustNotHave = true;
            for (Class<? extends CleanComponent> component : query.excluded) {
                if (archetype.contains(component)) {
                    mustNotHave = false;
                    break;
                }
            }
            if (mustNotHave) {
                matches |= 1L << i;
            }
        }
        return matches;
    }

    public record Conditions(Collection<Class<? extends CleanComponent>> required,
                             Collection<Class<? extends CleanComponent>> excluded) {
    }

    public static class C00 implements CleanComponent {}
    public static class C01 implements CleanComponent {}
    public static class C02 implements CleanComponent {}
    public static class C03 implements CleanComponent {}
    public static class C04 implements CleanComponent {}
    public static class C05 implements CleanComponent {}
    public static class C06 implements CleanComponent {}
    public static class C07 implements CleanComponent {}
    public static class C08 implements CleanComponent {}
    public static class C09 implements CleanComponent {}
    public static class C10 implements CleanComponent {}
    public static class C11 implements CleanComponent {}
    public static class C12 implements CleanComponent {}
    public static class C13 implements CleanComponent {}
    public static class C14 implements CleanComponent {}
    public static class C15 implements CleanComponent {}
    public static class C16 implements CleanComponent {}
    public static class C17 implements CleanComponent {}
    public static class C18 implements CleanComponent {}
    public static class C19 implements CleanComponent {}
    public static class C20 implements CleanComponent {}
    public static class C21 implements CleanComponent {}
    public static class C22 implements CleanComponent {}
    public static class C23 implements CleanComponent {}
    public static class C24 implements CleanComponent {}
    public static class C25 implements CleanComponent {}
}
