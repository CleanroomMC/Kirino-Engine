package com.cleanroommc.kirino.utils.collections;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Object2ObjectBiMap<TKey, TValue> extends AbstractMap<TKey, TValue> implements BiMap<TKey, TValue> {
    private static final Object ABSENT = new Object();

    private final Object2ObjectOpenHashMap<TKey, TValue> forward;
    private final Object2ObjectOpenHashMap<TValue, TKey> backward;
    private final Object2ObjectBiMap<TValue, TKey> inverse;
    private transient Set<Entry<TKey, TValue>> entries;
    private transient Set<TKey> keys;
    private Snapshot snapshot;
    private Consumer<BiConsumer<? super TKey, ? super TValue>> traversal;

    private record Snapshot(Object[] keys, Object[] values) {
    }

    public void lock() {
        if (isLocked()) {
            return;
        }
        Object[] denseKeys = new Object[size()];
        Object[] denseValues = new Object[size()];
        var iterator = forward.object2ObjectEntrySet().fastIterator();
        int index = 0;
        while (iterator.hasNext()) {
            var entry = iterator.next();
            denseKeys[index] = entry.getKey();
            denseValues[index++] = entry.getValue();
        }
        inverse.snapshot = new Snapshot(denseValues, denseKeys);
        snapshot = new Snapshot(denseKeys, denseValues);
        inverse.traversal = action -> forEachDense(inverse.snapshot, action);
        traversal = action -> forEachDense(snapshot, action);
    }

    public boolean isLocked() {
        return snapshot != null;
    }

    private void checkWritable() {
        if (isLocked()) {
            throw new UnsupportedOperationException("The bimap is locked");
        }
    }

    private abstract class MapView<E> extends AbstractSet<E> {
        @Override
        public boolean addAll(Collection<? extends E> collection) {
            checkWritable();
            return super.addAll(collection);
        }

        @Override
        public int size() {
            return forward.size();
        }

        @Override
        public void clear() {
            Object2ObjectBiMap.this.clear();
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            checkWritable();
            return super.removeAll(collection);
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            checkWritable();
            return super.retainAll(collection);
        }

        @Override
        public boolean removeIf(Predicate<? super E> predicate) {
            checkWritable();
            return super.removeIf(predicate);
        }
    }

    private static <E> Iterator<E> denseIterator(Object[] array) {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                return index < array.length;
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (E) array[index++];
            }
        };
    }

    public Object2ObjectBiMap() {
        this(16);
    }

    public Object2ObjectBiMap(int expectedSize) {
        this(newMap(expectedSize), newMap(expectedSize), null);
    }

    public Object2ObjectBiMap(Map<? extends TKey, ? extends TValue> map) {
        this(map.size());
        putAll(map);
    }

    private Object2ObjectBiMap(Object2ObjectOpenHashMap<TKey, TValue> forward,
                              Object2ObjectOpenHashMap<TValue, TKey> backward,
                              Object2ObjectBiMap<TValue, TKey> inverse) {
        this.forward = forward;
        this.backward = backward;
        this.traversal = forward::forEach;
        this.inverse = inverse == null ? new Object2ObjectBiMap<>(backward, forward, this) : inverse;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Object2ObjectOpenHashMap<K, V> newMap(int expectedSize) {
        Object2ObjectOpenHashMap<K, V> map = new Object2ObjectOpenHashMap<>(expectedSize);
        map.defaultReturnValue((V) ABSENT);
        return map;
    }

    @Override
    public int size() {
        return forward.size();
    }

    @Override
    public boolean isEmpty() {
        return forward.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return forward.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return backward.containsKey(value);
    }

    @Override
    public @Nullable TValue get(Object key) {
        return forward.getOrDefault(key, null);
    }

    @Override
    public TValue getOrDefault(Object key, TValue defaultValue) {
        return forward.getOrDefault(key, defaultValue);
    }

    @Override
    public @Nullable TValue put(TKey key, TValue value) {
        return put(key, value, false);
    }

    @Override
    public @Nullable TValue forcePut(TKey key, TValue value) {
        return put(key, value, true);
    }

    private @Nullable TValue put(TKey key, TValue value, boolean force) {
        checkWritable();
        TValue previous = forward.get(key);
        boolean keyPresent = previous != ABSENT;
        if (keyPresent && Objects.equals(previous, value)) {
            return previous;
        }
        TKey previousKey = backward.putIfAbsent(value, key);
        if (previousKey != ABSENT) {
            if (!force) {
                throw new IllegalArgumentException("Value already present: " + value);
            }
            forward.remove(previousKey);
            backward.put(value, key);
        }
        if (keyPresent) {
            backward.remove(previous);
        }
        forward.put(key, value);
        return keyPresent ? previous : null;
    }

    @Override
    public @Nullable TValue remove(Object key) {
        checkWritable();
        TValue previous = forward.remove(key);
        if (previous == ABSENT) {
            return null;
        }
        backward.remove(previous);
        return previous;
    }

    @Override
    public void putAll(Map<? extends TKey, ? extends TValue> map) {
        checkWritable();
        for (Entry<? extends TKey, ? extends TValue> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super TKey, ? super TValue, ? extends TValue> function) {
        checkWritable();
        Objects.requireNonNull(function);
        if (isEmpty()) {
            return;
        }
        Object2ObjectOpenHashMap<TValue, TKey> replacement = newMap(size());
        Object[] newValues = new Object[size()];
        var iterator = forward.object2ObjectEntrySet().fastIterator();
        int index = 0;
        while (iterator.hasNext()) {
            var entry = iterator.next();
            TValue value = function.apply(entry.getKey(), entry.getValue());
            if (replacement.putIfAbsent(value, entry.getKey()) != ABSENT) {
                throw new IllegalArgumentException("Value already present: " + value);
            }
            newValues[index++] = value;
        }
        checkWritable();
        iterator = forward.object2ObjectEntrySet().fastIterator();
        index = 0;
        while (iterator.hasNext()) {
            @SuppressWarnings("unchecked")
            TValue value = (TValue) newValues[index++];
            iterator.next().setValue(value);
        }
        backward.clear();
        backward.putAll(replacement);
    }

    @Override
    public void forEach(BiConsumer<? super TKey, ? super TValue> action) {
        traversal.accept(Objects.requireNonNull(action));
    }

    private static <K, V> void forEachDense(Snapshot current, BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < current.keys.length; i++) {
            @SuppressWarnings("unchecked")
            K key = (K) current.keys[i];
            @SuppressWarnings("unchecked")
            V value = (V) current.values[i];
            action.accept(key, value);
        }
    }

    @Override
    public TValue putIfAbsent(TKey key, TValue value) {
        checkWritable();
        return super.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        checkWritable();
        return super.remove(key, value);
    }

    @Override
    public TValue replace(TKey key, TValue value) {
        checkWritable();
        return super.replace(key, value);
    }

    @Override
    public boolean replace(TKey key, TValue previous, TValue value) {
        checkWritable();
        return super.replace(key, previous, value);
    }

    @Override
    public TValue computeIfAbsent(TKey key, Function<? super TKey, ? extends TValue> function) {
        checkWritable();
        return super.computeIfAbsent(key, function);
    }

    @Override
    public TValue computeIfPresent(TKey key, BiFunction<? super TKey, ? super TValue, ? extends TValue> function) {
        checkWritable();
        return super.computeIfPresent(key, function);
    }

    @Override
    public TValue compute(TKey key, BiFunction<? super TKey, ? super TValue, ? extends TValue> function) {
        checkWritable();
        return super.compute(key, function);
    }

    @Override
    public TValue merge(TKey key, TValue value, BiFunction<? super TValue, ? super TValue, ? extends TValue> function) {
        checkWritable();
        return super.merge(key, value, function);
    }

    @Override
    public int hashCode() {
        return forward.hashCode();
    }

    @Override
    public void clear() {
        checkWritable();
        forward.clear();
        backward.clear();
    }

    @Override
    public @NonNull Set<TKey> keySet() {
        if (keys == null) {
            keys = new MapView<TKey>() {
                @Override
                public int size() {
                    return forward.size();
                }

                @Override
                public boolean contains(Object key) {
                    return forward.containsKey(key);
                }

                @Override
                public boolean remove(Object key) {
                    checkWritable();
                    TValue value = forward.remove(key);
                    if (value == ABSENT) {
                        return false;
                    }
                    backward.remove(value);
                    return true;
                }

                @Override
                public void clear() {
                    Object2ObjectBiMap.this.clear();
                }

                @Override
                public void forEach(Consumer<? super TKey> action) {
                    Objects.requireNonNull(action);
                    Snapshot current = snapshot;
                    if (current == null) {
                        forward.keySet().forEach(action);
                    } else {
                        for (Object key : current.keys) {
                            @SuppressWarnings("unchecked")
                            TKey typedKey = (TKey) key;
                            action.accept(typedKey);
                        }
                    }
                }

                @Override
                public Iterator<TKey> iterator() {
                    Snapshot current = snapshot;
                    if (current != null) {
                        return denseIterator(current.keys);
                    }
                    Iterator<TKey> iterator = forward.keySet().iterator();
                    return new Iterator<>() {
                        private TKey currentKey;
                        private boolean removable;

                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public TKey next() {
                            currentKey = iterator.next();
                            removable = true;
                            return currentKey;
                        }

                        @Override
                        public void remove() {
                            checkWritable();
                            if (!removable) {
                                throw new IllegalStateException();
                            }
                            TValue value = forward.get(currentKey);
                            iterator.remove();
                            backward.remove(value);
                            removable = false;
                        }
                    };
                }
            };
        }
        return keys;
    }

    @Override
    public @NonNull Set<TValue> values() {
        return inverse.keySet();
    }

    @Override
    public @NonNull Set<Entry<TKey, TValue>> entrySet() {
        if (entries == null) {
            entries = new MapView<>() {
                @Override
                public int size() {
                    return forward.size();
                }

                @Override
                public void clear() {
                    Object2ObjectBiMap.this.clear();
                }

                @Override
                public boolean contains(Object object) {
                    return object instanceof Entry<?, ?> entry
                            && forward.containsKey(entry.getKey())
                            && Objects.equals(forward.get(entry.getKey()), entry.getValue());
                }

                @Override
                public boolean remove(Object object) {
                    checkWritable();
                    if (!contains(object)) {
                        return false;
                    }
                    Object2ObjectBiMap.this.remove(((Entry<?, ?>) object).getKey());
                    return true;
                }

                @Override
                public Iterator<Entry<TKey, TValue>> iterator() {
                    Snapshot current = snapshot;
                    if (current != null) {
                        return new Iterator<>() {
                            private int index;

                            @Override
                            public boolean hasNext() {
                                return index < current.keys.length;
                            }

                            @Override
                            @SuppressWarnings("unchecked")
                            public Entry<TKey, TValue> next() {
                                if (!hasNext()) {
                                    throw new NoSuchElementException();
                                }
                                int position = index++;
                                return new SimpleImmutableEntry<>((TKey) current.keys[position], (TValue) current.values[position]);
                            }
                        };
                    }
                    var iterator = forward.object2ObjectEntrySet().fastIterator();
                    return new Iterator<>() {
                        private TKey currentKey;
                        private boolean removable;

                        @Override
                        public boolean hasNext() {
                            return iterator.hasNext();
                        }

                        @Override
                        public Entry<TKey, TValue> next() {
                            Entry<TKey, TValue> entry = iterator.next();
                            currentKey = entry.getKey();
                            removable = true;
                            return new SimpleEntry<>(entry) {
                                @Override
                                public TValue setValue(TValue value) {
                                    TValue previous = Object2ObjectBiMap.this.put(getKey(), value);
                                    super.setValue(value);
                                    return previous;
                                }
                            };
                        }

                        @Override
                        public void remove() {
                            checkWritable();
                            if (!removable) {
                                throw new IllegalStateException();
                            }
                            TValue value = forward.get(currentKey);
                            iterator.remove();
                            backward.remove(value);
                            removable = false;
                        }
                    };
                }
            };
        }
        return entries;
    }

    @Override
    public @NonNull Object2ObjectBiMap<TValue, TKey> inverse() {
        return inverse;
    }
}
