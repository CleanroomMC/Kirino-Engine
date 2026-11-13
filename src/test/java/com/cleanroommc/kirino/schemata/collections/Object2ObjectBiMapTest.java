package com.cleanroommc.kirino.schemata.collections;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class Object2ObjectBiMapTest {
    @Test
    void lock_throughInverse_freezesBothDirectionsAndPreservesNulls() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put(null, 1);
        map.put("a", null);
        map.put("b", 2);
        Map<String, Integer> expected = new HashMap<>(map);
        assertFalse(map.isLocked());
        assertFalse(map.inverse().isLocked());
        map.inverse().lock();
        map.lock();
        map.inverse().lock();
        assertTrue(map.isLocked());
        assertTrue(map.inverse().isLocked());
        assertSame(map, map.inverse().inverse());
        assertEquals(expected, map);
        assertEquals(HashBiMap.create(expected).inverse(), map.inverse());
        Map<String, Integer> visited = new HashMap<>();
        map.forEach(visited::put);
        assertEquals(expected, visited);
        Map<Integer, String> reverseVisited = new HashMap<>();
        map.inverse().forEach(reverseVisited::put);
        assertEquals(map.inverse(), reverseVisited);
        Set<String> keys = new HashSet<>();
        map.keySet().forEach(keys::add);
        assertEquals(expected.keySet(), keys);
        Set<Integer> values = new HashSet<>();
        map.values().forEach(values::add);
        assertEquals(new HashSet<>(expected.values()), values);
        assertNull(map.get("a"));
        assertEquals(9, map.getOrDefault("missing", 9));
    }

    @Test
    void lock_allMapMutators_rejectIncludingNoOpsAndCallbacks() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1));
        map.lock();
        assertLockedMutators(map, "a", 1, "missing", 2);
        assertLockedMutators(map.inverse(), 1, "a", 2, "missing");
        assertEquals(Map.of("a", 1), map);
    }

    private static <K, V> void assertLockedMutators(Object2ObjectBiMap<K, V> map, K key, V value, K missingKey, V missingValue) {
        List<Runnable> mutations = List.of(
                () -> map.put(key, value),
                () -> map.forcePut(key, value),
                () -> map.putAll(Map.of()),
                () -> map.remove(missingKey),
                () -> map.remove(missingKey, missingValue),
                () -> map.putIfAbsent(key, value),
                () -> map.replace(missingKey, value),
                () -> map.replace(missingKey, missingValue, value),
                () -> map.computeIfAbsent(key, ignored -> fail("Locked map must not call a function")),
                () -> map.computeIfPresent(missingKey, (k, v) -> fail("Locked map must not call a function")),
                () -> map.compute(key, (k, v) -> fail("Locked map must not call a function")),
                () -> map.merge(missingKey, missingValue, (a, b) -> fail("Locked map must not call a function")),
                () -> map.replaceAll((k, v) -> fail("Locked map must not call a function")),
                map::clear);
        for (Runnable mutation : mutations) {
            assertThrows(UnsupportedOperationException.class, mutation::run);
        }
    }

    @Test
    void lock_previouslyObtainedViewsAndEntries_cannotModifyMap() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1, "b", 2));
        Set<String> keys = map.keySet();
        Set<Integer> values = map.values();
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        Iterator<String> keyIterator = keys.iterator();
        Iterator<Integer> valueIterator = values.iterator();
        Iterator<Map.Entry<String, Integer>> entryIterator = entries.iterator();
        keyIterator.next();
        valueIterator.next();
        Map.Entry<String, Integer> oldEntry = entryIterator.next();
        Iterator<Map.Entry<Integer, String>> reverseIterator = map.inverse().entrySet().iterator();
        Map.Entry<Integer, String> reverseEntry = reverseIterator.next();
        map.lock();
        assertThrows(UnsupportedOperationException.class, keyIterator::remove);
        assertThrows(UnsupportedOperationException.class, valueIterator::remove);
        assertThrows(UnsupportedOperationException.class, entryIterator::remove);
        assertThrows(UnsupportedOperationException.class, reverseIterator::remove);
        assertThrows(UnsupportedOperationException.class, () -> oldEntry.setValue(3));
        assertThrows(UnsupportedOperationException.class, () -> reverseEntry.setValue("c"));
        assertThrows(UnsupportedOperationException.class, () -> keys.remove("missing"));
        assertThrows(UnsupportedOperationException.class, () -> values.remove(99));
        assertThrows(UnsupportedOperationException.class, () -> entries.remove(Map.entry("missing", 99)));
        for (Set<?> view : List.of(keys, values, entries, map.inverse().entrySet())) {
            assertThrows(UnsupportedOperationException.class, () -> view.addAll(List.of()));
            assertThrows(UnsupportedOperationException.class, view::clear);
            assertThrows(UnsupportedOperationException.class, () -> view.removeIf(value -> false));
            assertThrows(UnsupportedOperationException.class, () -> view.removeAll(Set.of()));
            assertThrows(UnsupportedOperationException.class, () -> view.retainAll(view));
        }
        assertEquals(Set.of("a", "b"), keys);
        assertEquals(Set.of(1, 2), values);
        assertEquals(Map.of("a", 1, "b", 2), map);
    }

    @Test
    void lock_denseIterators_preserveEntriesAndRejectMutation() {
        Object2ObjectBiMap<Integer, Integer> map = new Object2ObjectBiMap<>(0);
        for (int i = 0; i < 256; i++) {
            map.put(i, i + 256);
        }
        map.keySet().removeIf(key -> key % 2 == 0);
        Map<Integer, Integer> expected = new HashMap<>(map);
        map.lock();
        assertEquals(expected.keySet(), new HashSet<>(map.keySet()));
        assertEquals(new HashSet<>(expected.values()), new HashSet<>(map.values()));
        assertEquals(expected.entrySet(), new HashSet<>(map.entrySet()));
        Iterator<Map.Entry<Integer, Integer>> iterator = map.entrySet().iterator();
        Map.Entry<Integer, Integer> first = iterator.next();
        Map.Entry<Integer, Integer> copy = Map.entry(first.getKey(), first.getValue());
        iterator.next();
        assertEquals(copy, first);
        assertThrows(UnsupportedOperationException.class, () -> first.setValue(999));
        for (Iterator<?> current : List.of(map.keySet().iterator(), map.values().iterator(), iterator)) {
            while (current.hasNext()) {
                current.next();
            }
            assertThrows(NoSuchElementException.class, current::next);
            assertThrows(UnsupportedOperationException.class, current::remove);
        }
    }

    @Test
    void lock_emptyMap_isImmutableAndHasEmptyTraversals() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(0);
        Set<String> keys = map.keySet();
        map.lock();
        assertTrue(map.isEmpty());
        assertTrue(keys.isEmpty());
        assertFalse(keys.iterator().hasNext());
        assertFalse(map.values().iterator().hasNext());
        assertFalse(map.entrySet().iterator().hasNext());
        map.forEach((key, value) -> fail("Empty map must not visit elements"));
        assertThrows(UnsupportedOperationException.class, () -> map.put(null, null));
        assertThrows(UnsupportedOperationException.class, () -> keys.removeIf(key -> false));
        assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((key, value) -> null));
        assertThrows(NullPointerException.class, () -> map.forEach(null));
    }

    @Test
    void replaceAll_callbackLocksMap_cannotModifySnapshot() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1));
        assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((key, value) -> {
            map.lock();
            return value + 1;
        }));
        assertTrue(map.isLocked());
        assertEquals(Map.of("a", 1), map);
        Map<String, Integer> visited = new HashMap<>();
        map.forEach(visited::put);
        assertEquals(map, visited);
    }

    @Test
    void constructors_emptyAndCopied_mapsHaveExpectedContents() {
        Object2ObjectBiMap<String, Integer> empty = new Object2ObjectBiMap<>();
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertNull(empty.get("missing"));
        assertNull(empty.remove("missing"));
        assertFalse(empty.containsKey("missing"));
        assertFalse(empty.containsValue(1));
        assertThrows(IllegalArgumentException.class, () -> new Object2ObjectBiMap<>(-1));
        assertThrows(IllegalArgumentException.class, () -> new Object2ObjectBiMap<>(Map.of("a", 1, "b", 1)));
        assertEquals(Map.of("a", 1), new Object2ObjectBiMap<>(Map.of("a", 1)));
    }

    @Test
    void put_replacingKey_updatesReverseMapping() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(0);
        assertNull(map.put("a", 1));
        assertEquals(1, map.put("a", 1));
        assertEquals(1, map.put("a", 2));
        assertFalse(map.containsValue(1));
        assertEquals("a", map.inverse().get(2));
        assertEquals(1, map.size());
        assertSame(map, map.inverse().inverse());
        assertSame(map.inverse(), map.inverse());
        assertEquals(Map.of("a", 2).hashCode(), map.hashCode());
    }

    @Test
    void put_duplicateValue_rejectsWithoutChangingEitherDirection() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1, "b", 2));
        assertThrows(IllegalArgumentException.class, () -> map.put("c", 1));
        assertThrows(IllegalArgumentException.class, () -> map.put("b", 1));
        assertEquals(Map.of("a", 1, "b", 2), map);
        assertEquals(Map.of(1, "a", 2, "b"), map.inverse());
    }

    @Test
    void forcePut_existingKeyAndValue_removesDisplacedMapping() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1, "b", 2));
        assertEquals(1, map.forcePut("a", 2));
        assertEquals(Map.of("a", 2), map);
        assertEquals(Map.of(2, "a"), map.inverse());
        assertNull(map.forcePut("c", 2));
        assertEquals(Map.of("c", 2), map);
        assertEquals(2, map.forcePut("c", 2));
    }

    @Test
    void nullKeysAndValues_preserveUniquenessAndRemoval() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put(null, 1);
        map.put("a", null);
        assertTrue(map.inverse().containsKey(null));
        assertEquals("a", map.inverse().get(null));
        assertThrows(IllegalArgumentException.class, () -> map.put("b", null));
        assertEquals(1, map.forcePut(null, null));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertNull(map.remove(null));
        assertTrue(map.isEmpty());
        assertTrue(map.inverse().isEmpty());
        map.put(null, 2);
        assertNull(map.forcePut("b", 2));
        assertFalse(map.containsKey(null));
        assertEquals(Map.of("b", 2), map);
    }

    @Test
    void inverse_mutationsUpdateOriginal() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        BiMap<Integer, String> inverse = map.inverse();
        inverse.put(1, "a");
        inverse.put(2, "b");
        assertThrows(IllegalArgumentException.class, () -> inverse.put(3, "a"));
        assertEquals("b", inverse.forcePut(2, "a"));
        assertEquals(Map.of("a", 2), map);
        assertEquals("a", inverse.remove(2));
        assertTrue(map.isEmpty());
    }

    @Test
    void views_obtainedBeforeChanges_remainLive() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        Set<String> keys = map.keySet();
        Set<Integer> values = map.values();
        Set<Map.Entry<String, Integer>> entries = map.entrySet();
        map.put("a", 1);
        assertEquals(Set.of("a"), keys);
        assertEquals(Set.of(1), values);
        assertEquals(Set.of(Map.entry("a", 1)), entries);
        assertThrows(UnsupportedOperationException.class, () -> keys.add("b"));
        assertThrows(UnsupportedOperationException.class, () -> values.add(2));
        assertThrows(UnsupportedOperationException.class, () -> entries.add(Map.entry("b", 2)));
        map.inverse().clear();
        assertTrue(keys.isEmpty());
        assertTrue(values.isEmpty());
        assertTrue(entries.isEmpty());
    }

    @Test
    void viewRemoval_nullMappings_updatesBothDirections() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put("a", null);
        assertTrue(map.keySet().remove("a"));
        assertTrue(map.inverse().isEmpty());
        map.put(null, 1);
        assertTrue(map.values().remove(1));
        assertTrue(map.isEmpty());
        map.put(null, null);
        Map.Entry<String, Integer> entry = new AbstractMap.SimpleEntry<>(null, null);
        assertTrue(map.entrySet().contains(entry));
        assertFalse(map.entrySet().contains(new AbstractMap.SimpleEntry<>("missing", null)));
        assertFalse(map.entrySet().remove(Map.entry("missing", 2)));
        assertFalse(map.entrySet().remove("invalid"));
        assertTrue(map.entrySet().remove(entry));
        assertTrue(map.isEmpty());
        assertTrue(map.inverse().isEmpty());
    }

    @Test
    void iteratorRemoval_allViews_removeEveryMappingWithoutSkipping() {
        for (int view = 0; view < 3; view++) {
            Object2ObjectBiMap<Integer, Integer> map = new Object2ObjectBiMap<>(0);
            for (int i = 0; i < 256; i++) {
                map.put(i, i + 256);
            }
            Iterator<?> iterator = switch (view) {
                case 0 -> map.entrySet().iterator();
                case 1 -> map.keySet().iterator();
                default -> map.values().iterator();
            };
            assertThrows(IllegalStateException.class, iterator::remove);
            int count = 0;
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
                assertThrows(IllegalStateException.class, iterator::remove);
                assertEquals(map.size(), map.inverse().size());
                count++;
            }
            assertEquals(256, count);
            assertTrue(map.isEmpty());
            assertTrue(map.inverse().isEmpty());
        }
    }

    @Test
    void entries_retainedAcrossIteratorSteps_keepTheirIdentity() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1, "b", 2));
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        Map.Entry<String, Integer> first = iterator.next();
        String firstKey = first.getKey();
        Integer oldValue = first.getValue();
        Map.Entry<String, Integer> second = iterator.next();
        assertEquals(firstKey, first.getKey());
        assertEquals(oldValue, first.setValue(3));
        assertEquals(3, map.get(firstKey));
        assertFalse(map.containsValue(oldValue));
        assertThrows(IllegalArgumentException.class, () -> first.setValue(second.getValue()));
        assertEquals(Map.entry(firstKey, 3), first);
        assertEquals(Map.entry(firstKey, 3).hashCode(), first.hashCode());
    }

    @Test
    void entrySetValue_thenIteratorRemove_removesUpdatedReverseEntry() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1));
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        iterator.next().setValue(2);
        iterator.remove();
        assertTrue(map.isEmpty());
        assertTrue(map.inverse().isEmpty());
    }

    @Test
    void bulkViewOperations_keepReverseMappingConsistent() {
        Object2ObjectBiMap<Integer, Integer> map = new Object2ObjectBiMap<>();
        for (int i = 0; i < 100; i++) {
            map.put(i, i + 100);
        }
        map.values().removeIf(value -> value % 2 == 0);
        map.keySet().retainAll(Set.of(1, 3, 5));
        map.entrySet().removeAll(Set.of(Map.entry(1, 101)));
        assertEquals(Map.of(3, 103, 5, 105), map);
        assertEquals(Map.of(103, 3, 105, 5), map.inverse());
        map.values().clear();
        assertTrue(map.isEmpty());
    }

    @Test
    void defaultMapOperations_preserveBijection() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.putIfAbsent("a", 1);
        map.computeIfAbsent("b", key -> 2);
        assertThrows(IllegalArgumentException.class, () -> map.compute("b", (key, value) -> 1));
        assertTrue(map.replace("a", 1, 3));
        assertEquals(3, map.replace("a", 4));
        assertEquals(5, map.merge("a", 1, Integer::sum));
        assertNull(map.computeIfPresent("b", (key, value) -> null));
        assertFalse(map.containsValue(2));
        assertFalse(map.remove("a", 4));
        assertTrue(map.remove("a", 5));
        assertTrue(map.inverse().isEmpty());
    }

    @Test
    void replaceAll_swapsValuesAndRejectsDuplicates() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>(Map.of("a", 1, "b", 2));
        map.replaceAll((key, value) -> 3 - value);
        assertEquals(Map.of("a", 2, "b", 1), map);
        assertEquals(Map.of(2, "a", 1, "b"), map.inverse());
        assertThrows(IllegalArgumentException.class, () -> map.replaceAll((key, value) -> 0));
        assertEquals(Map.of("a", 2, "b", 1), map);
        assertThrows(NullPointerException.class, () -> map.replaceAll(null));
        map.inverse().replaceAll((value, key) -> key + "!");
        assertEquals(Map.of("a!", 2, "b!", 1), map);
    }

    @Test
    void nullMappings_noOpAndConflictingWrites_preserveBothDirections() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put(null, null);
        assertNull(map.put(null, null));
        assertNull(map.forcePut(null, null));
        assertThrows(IllegalArgumentException.class, () -> map.put("a", null));
        assertThrows(IllegalArgumentException.class, () -> map.inverse().put(1, null));
        assertEquals(1, map.size());
        assertEquals(7, map.getOrDefault("missing", 7));
        assertNull(map.getOrDefault(null, 7));
        assertNull(map.remove("missing"));
        assertTrue(map.containsValue(null));
        map.put(null, 1);
        assertFalse(map.inverse().containsKey(null));
        assertTrue(map.inverse().containsKey(1));
        assertNull(map.inverse().get(1));
        map.forcePut("a", 1);
        assertFalse(map.containsKey(null));
        assertEquals(Map.of("a", 1), map);
    }

    @Test
    void forEach_withNulls_visitsEveryMappingInBothDirections() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put(null, 1);
        map.put("a", null);
        map.put("b", 2);
        Map<String, Integer> seen = new HashMap<>();
        map.forEach(seen::put);
        assertEquals(map, seen);
        Map<Integer, String> inverseSeen = new HashMap<>();
        map.inverse().forEach(inverseSeen::put);
        assertEquals(map.inverse(), inverseSeen);
        Set<String> keys = new HashSet<>();
        map.keySet().forEach(keys::add);
        assertEquals(map.keySet(), keys);
        assertThrows(NullPointerException.class, () -> map.forEach(null));
        assertThrows(NullPointerException.class, () -> map.keySet().forEach(null));
    }

    @Test
    void replaceAll_nullsExceptionsAndLiveViews_preserveContract() {
        Object2ObjectBiMap<String, Integer> map = new Object2ObjectBiMap<>();
        map.put(null, 1);
        map.put("a", null);
        Set<Integer> values = map.values();
        Set<String> inverseValues = map.inverse().values();
        map.replaceAll((key, value) -> value == null ? 1 : null);
        assertNull(map.get(null));
        assertEquals(1, map.get("a"));
        assertTrue(values.contains(null));
        assertTrue(values.contains(1));
        assertTrue(inverseValues.contains(null));
        assertThrows(IllegalArgumentException.class, () -> map.replaceAll((key, value) -> null));
        assertEquals(2, map.size());
        assertNull(map.get(null));
        assertEquals(1, map.get("a"));
        assertThrows(IllegalStateException.class, () -> map.replaceAll((key, value) -> {
            if (key != null) {
                throw new IllegalStateException();
            }
            return 99;
        }));
        assertNull(map.get(null));
        assertEquals(1, map.get("a"));
        assertEquals("a", map.inverse().get(1));
        Object2ObjectBiMap<String, Integer> empty = new Object2ObjectBiMap<>();
        empty.replaceAll((key, value) -> fail("Empty map must not call the function"));
        assertThrows(NullPointerException.class, () -> empty.replaceAll(null));
    }

    @Test
    void collisions_growthAndIteratorRemoval_preserveAllMappings() {
        Object2ObjectBiMap<CollisionKey, CollisionKey> map = new Object2ObjectBiMap<>(0);
        for (int i = 0; i < 256; i++) {
            map.put(new CollisionKey(i), new CollisionKey(i + 256));
        }
        map.keySet().removeIf(key -> key.id() % 2 == 0);
        for (int i = 0; i < 256; i++) {
            assertEquals(i % 2 != 0, map.containsKey(new CollisionKey(i)));
            assertEquals(i % 2 != 0, map.inverse().containsKey(new CollisionKey(i + 256)));
        }
        map.inverse().keySet().removeIf(value -> value.id() % 4 == 1);
        assertEquals(64, map.size());
        map.replaceAll((key, value) -> new CollisionKey(value.id() + 256));
        for (var entry : map.entrySet()) {
            assertEquals(entry.getKey(), map.inverse().get(entry.getValue()));
        }
    }

    private record CollisionKey(int id) {
        @Override
        public int hashCode() {
            return 1;
        }
    }

    @Test
    void randomizedMutations_matchGuavaBiMap() {
        BiMap<Integer, Integer> expected = HashBiMap.create();
        BiMap<Integer, Integer> actual = new Object2ObjectBiMap<>();
        Random random = new Random(458712);
        for (int step = 0; step < 10_000; step++) {
            BiMap<Integer, Integer> expectedSide = random.nextBoolean() ? expected : expected.inverse();
            BiMap<Integer, Integer> actualSide = expectedSide == expected ? actual : actual.inverse();
            Integer key = random.nextInt(20) == 0 ? null : random.nextInt(64);
            Integer value = random.nextInt(20) == 0 ? null : random.nextInt(64);
            int operation = random.nextInt(8);
            Object result = null;
            boolean rejected = false;
            try {
                result = mutate(expectedSide, operation, key, value);
            } catch (IllegalArgumentException exception) {
                rejected = true;
            }
            if (rejected) {
                assertThrows(IllegalArgumentException.class, () -> mutate(actualSide, operation, key, value));
            } else {
                assertEquals(result, mutate(actualSide, operation, key, value));
            }
            assertEquals(expected, actual);
            assertEquals(expected.inverse(), actual.inverse());
            assertEquals(expected.hashCode(), actual.hashCode());
        }
    }

    private static Object mutate(BiMap<Integer, Integer> map, int operation, Integer key, Integer value) {
        return switch (operation) {
            case 0, 1 -> map.put(key, value);
            case 2 -> map.forcePut(key, value);
            case 3 -> map.remove(key);
            case 4 -> map.values().remove(value);
            case 5 -> map.keySet().remove(key);
            case 6 -> map.entrySet().remove(new AbstractMap.SimpleEntry<>(key, value));
            default -> map.putIfAbsent(key, value);
        };
    }
}
