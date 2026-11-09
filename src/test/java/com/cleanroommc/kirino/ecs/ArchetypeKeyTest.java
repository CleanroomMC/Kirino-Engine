package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

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

    public static class Value implements CleanComponent {}
    public static class ValueExtra implements CleanComponent {}
}
