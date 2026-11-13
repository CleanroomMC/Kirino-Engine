package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ArchetypeKeyTest {
    @Test
    void equality_differentOrderAndMutableInput_preservesExactComposition() {
        List<Class<? extends CleanComponent>> types = new ArrayList<>(List.of(Value.class, ValueExtra.class));
        ArchetypeKey key = new ArchetypeKey(types);
        ArchetypeKey reordered = new ArchetypeKey(List.of(ValueExtra.class, Value.class));
        types.clear();
        assertEquals(key, reordered);
        assertEquals(key.hashCode(), reordered.hashCode());
        assertTrue(key.contains(Value.class));
        assertNotEquals(key, new ArchetypeKey(List.of(Value.class)));
        assertFalse(new ArchetypeKey(List.of(ValueExtra.class)).contains(Value.class));
        assertThrows(IllegalArgumentException.class, () -> new ArchetypeKey(List.of(Value.class, Value.class)));
    }

    @Test
    void equality_sameClassNameFromDifferentLoaders_usesClassIdentity() throws Exception {
        byte[] bytes;
        try (var input = Value.class.getResourceAsStream("ArchetypeKeyTest$Value.class")) {
            bytes = input.readAllBytes();
        }
        class Loader extends ClassLoader {
            Loader() { super(Value.class.getClassLoader()); }
            Class<? extends CleanComponent> loadCopy() {
                return defineClass(Value.class.getName(), bytes, 0, bytes.length).asSubclass(CleanComponent.class);
            }
        }
        Class<? extends CleanComponent> first = new Loader().loadCopy();
        Class<? extends CleanComponent> second = new Loader().loadCopy();
        ArchetypeKey key = new ArchetypeKey(List.of(first));
        assertEquals(first.getName(), second.getName());
        assertNotEquals(key, new ArchetypeKey(List.of(second)));
        assertTrue(key.contains(first));
        assertFalse(key.contains(second));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 4, 8, 9, 16, 64})
    void equality_variedCompositionSizes_preservesMapLookupAndRejectsDuplicates(int count) throws Exception {
        byte[] bytes;
        try (var input = Value.class.getResourceAsStream("ArchetypeKeyTest$Value.class")) {
            bytes = input.readAllBytes();
        }
        class Loader extends ClassLoader {
            Loader() { super(Value.class.getClassLoader()); }
            Class<? extends CleanComponent> copy() {
                return defineClass(Value.class.getName(), bytes, 0, bytes.length).asSubclass(CleanComponent.class);
            }
        }
        List<Class<? extends CleanComponent>> types = new ArrayList<>();
        for (int i = 0; i < count; i++) types.add(new Loader().copy());
        ArchetypeKey key = new ArchetypeKey(types);
        var map = new Object2ObjectOpenHashMap<ArchetypeKey, Object>();
        Object value = new Object();
        map.put(key, value);
        Collections.shuffle(types, new Random(713));
        ArchetypeKey reordered = new ArchetypeKey(types);
        assertEquals(key, reordered);
        assertEquals(reordered, key);
        assertEquals(key.hashCode(), reordered.hashCode());
        assertSame(value, map.get(reordered));
        for (Class<? extends CleanComponent> type : types) assertTrue(key.contains(type));
        assertFalse(key.contains(new Loader().copy()));
        if (!types.isEmpty()) {
            types.add(types.getFirst());
            assertThrows(IllegalArgumentException.class, () -> new ArchetypeKey(types));
            types.removeLast();
            types.set(0, new Loader().copy());
            assertNotEquals(key, new ArchetypeKey(types));
        }
        types.clear();
        assertEquals(key, reordered);
        assertSame(value, map.get(reordered));
    }

    @Test
    void validation_nullInputsAndElements_areRejected() {
        assertThrows(NullPointerException.class, () -> new ArchetypeKey(null));
        assertThrows(NullPointerException.class, () -> new ArchetypeKey(Arrays.asList(Value.class, null)));
        assertThrows(NullPointerException.class, () -> new ArchetypeKey(List.of(Value.class)).contains(null));
    }

    @Test
    void diagnostics_reorderedTypesAndMutatedInput_keepSortedNamesAndEmptyFormat() {
        var types = new ArrayList<Class<? extends CleanComponent>>(List.of(ValueExtra.class, Value.class));
        ArchetypeKey key = new ArchetypeKey(types);
        types.clear();
        assertEquals("ArchetypeKey{" + Value.class.getName() + "," + ValueExtra.class.getName() + "}", key.toString());
        assertEquals(key.toString(), new ArchetypeKey(List.of(Value.class, ValueExtra.class)).toString());
        assertEquals("ArchetypeKey{}", new ArchetypeKey(List.of()).toString());
    }

    public static class Value implements CleanComponent {}
    public static class ValueExtra implements CleanComponent {}
}
