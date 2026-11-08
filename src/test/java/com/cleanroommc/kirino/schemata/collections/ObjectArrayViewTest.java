package com.cleanroommc.kirino.schemata.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObjectArrayViewTest {
    @Test
    void copyOf_array_preservesTypeAndCopiesInput() {
        String[] input = {"first", "second"};
        var list = ObjectArrayView.copyOf(input);
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
            var list = ObjectArrayView.copyOf(input, String[]::new);
            String[] elements = list.elements();
            assertEquals(String[].class, elements.getClass());
            assertEquals(size, elements.length);
            assertEquals(input, list);
            input.clear();
            assertEquals(size, list.size());
        }
        Object[] genericElements = ObjectArrayView.copyOf(List.of("value")).elements();
        assertArrayEquals(new Object[]{"value"}, genericElements);
        String[] emptyElements = ObjectArrayView.<String>copyOf(new String[0]).elements();
        assertEquals(String[].class, emptyElements.getClass());
    }

    @Test
    void mutationMethods_iteratorsAndViews_rejectWrites() {
        var list = ObjectArrayView.of("a", "b", "c");
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
        assertThrows(NullPointerException.class, () -> ObjectArrayView.copyOf((String[]) null));
        assertThrows(NullPointerException.class, () -> ObjectArrayView.of("a", null));
        assertThrows(NullPointerException.class, () -> ObjectArrayView.copyOf(Arrays.asList("a", null)));
        assertThrows(NullPointerException.class, () -> ObjectArrayView.copyOf(Arrays.asList("a", null), String[]::new));
        var original = ObjectArrayView.of("a", "b");
        var clone = original.clone();
        assertNotSame(original.elements(), clone.elements());
        clone.elements()[0] = "changed";
        assertEquals("a", original.getFirst());
        assertEquals(0, ObjectArrayView.of().elements().length);
    }

    @Test
    void backingArrayWrites_areVisibleThroughDerivedViewsAndCopiesRemainIndependent() {
        var view = ObjectArrayView.of("first", "second", "third");
        var slice = view.subList(1, 3);
        var reversed = view.reversed();
        var iterator = view.listIterator(1);
        Object[] snapshot = view.toArray();
        var copy = view.clone();

        view.elements()[1] = "updated";

        assertEquals("updated", slice.getFirst());
        assertEquals("updated", reversed.get(1));
        assertEquals("updated", iterator.next());
        assertEquals(List.of("first", "updated", "third"), view);
        assertEquals(List.of("first", "updated", "third").hashCode(), view.hashCode());
        assertArrayEquals(new String[]{"first", "second", "third"}, snapshot);
        assertEquals(List.of("first", "second", "third"), copy);
    }

    @Test
    void get_rejectsIndicesOutsideTheFixedArray() {
        var view = ObjectArrayView.of("value");

        assertThrows(IndexOutOfBoundsException.class, () -> view.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> view.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> ObjectArrayView.of().get(0));
        assertEquals(1, view.size());
    }
}
