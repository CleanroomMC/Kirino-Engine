package com.cleanroommc.kirino.utils.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableObjectArrayListTest {
    @Test
    void copyOf_array_preservesTypeAndCopiesInput() {
        String[] input = {"first", "second"};
        var list = ImmutableObjectArrayList.copyOf(input);
        String[] array = list.elements();
        assertNotSame(input, array);
        assertSame(array, list.elements());
        input[0] = "changed";
        assertArrayEquals(new String[]{"first", "second"}, array);
        array[1] = "raw";
        assertEquals("raw", list.get(1));
        assertEquals(List.of("first", "raw"), list);
    }

    @Test
    void copyOf_collectionWithFactory_hasExactLengthAndRuntimeArrayType() {
        for (int size : new int[]{0, 1, 2, 3, 16, 128}) {
            List<String> input = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                input.add("value" + i);
            }
            var list = ImmutableObjectArrayList.copyOf(input, String[]::new);
            String[] elements = list.elements();
            assertEquals(String[].class, elements.getClass());
            assertEquals(size, elements.length);
            assertEquals(input, list);
            input.clear();
            assertEquals(size, list.size());
        }
        Object[] genericElements = ImmutableObjectArrayList.copyOf(List.of("value")).elements();
        assertArrayEquals(new Object[]{"value"}, genericElements);
        String[] emptyElements = ImmutableObjectArrayList.<String>copyOf(new String[0]).elements();
        assertEquals(String[].class, emptyElements.getClass());
    }

    @Test
    void mutationMethods_iteratorsAndViews_rejectWrites() {
        var list = ImmutableObjectArrayList.of("a", "b", "c");
        assertThrows(UnsupportedOperationException.class, () -> list.set(0, "x"));
        assertThrows(UnsupportedOperationException.class, () -> list.add("x"));
        assertThrows(UnsupportedOperationException.class, () -> list.addAll(1, List.of("x")));
        assertThrows(UnsupportedOperationException.class, () -> list.size(5));
        assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        assertThrows(UnsupportedOperationException.class, () -> list.removeFirst());
        assertThrows(UnsupportedOperationException.class, () -> list.removeLast());
        assertThrows(UnsupportedOperationException.class, list::clear);
        assertThrows(UnsupportedOperationException.class, () -> list.removeIf(value -> true));
        assertThrows(UnsupportedOperationException.class, () -> list.retainAll(List.of("a")));
        assertThrows(UnsupportedOperationException.class, () -> list.replaceAll(String::toUpperCase));
        assertThrows(UnsupportedOperationException.class, () -> list.sort(null));
        assertThrows(UnsupportedOperationException.class, () -> list.unstableSort(null));
        assertThrows(UnsupportedOperationException.class, () -> list.addElements(0, new String[]{"x"}));
        assertThrows(UnsupportedOperationException.class, () -> list.setElements(new String[]{"x"}));
        assertThrows(UnsupportedOperationException.class, () -> list.removeElements(0, 1));
        var iterator = list.listIterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertThrows(UnsupportedOperationException.class, () -> iterator.set("x"));
        assertThrows(UnsupportedOperationException.class, () -> iterator.add("x"));
        assertThrows(UnsupportedOperationException.class, () -> list.subList(0, 2).set(0, "x"));
        assertThrows(UnsupportedOperationException.class, () -> list.reversed().set(0, "x"));
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void factories_nullValuesAreRejectedAndCopiesDoNotShareBackingArrays() {
        assertThrows(NullPointerException.class, () -> ImmutableObjectArrayList.copyOf((String[]) null));
        assertThrows(NullPointerException.class, () -> ImmutableObjectArrayList.of("a", null));
        assertThrows(NullPointerException.class, () -> ImmutableObjectArrayList.copyOf(Arrays.asList("a", null)));
        assertThrows(NullPointerException.class, () -> ImmutableObjectArrayList.copyOf(Arrays.asList("a", null), String[]::new));
        var original = ImmutableObjectArrayList.of("a", "b");
        var clone = original.clone();
        assertNotSame(original.elements(), clone.elements());
        clone.elements()[0] = "changed";
        assertEquals("a", original.getFirst());
        assertEquals(0, ImmutableObjectArrayList.of().elements().length);
    }
}
