package com.cleanroommc.kirino.schemata.immutable;

import com.google.common.base.Preconditions;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Immutable
public final class KirinoImmutableMap<K,V> implements Map<K, V>, Serializable {

    private final transient Class<K> keyClass;
    private final transient Class<V> valueClass;

    /**
     * @implSpec No duplicates. That's what indices is for. Has to be sorted. This will be binary searched.
     */
    private final V[] values;

    /**
     * Index of a value assigned to the equivalent key in the values variable
     *
     * @implSpec Each value index should have the same index in the array as the key value it represents (eg. value of <code>keys[2]</code> is <code>values[indices[2]]</code>)
     * @apiNote Using shorts for indices because who in the right of their mind will store over 30 thousand entries in an immutable map.
     */
    private final short[] indices;

    /**
     * @implSpec Has to be sorted. This will be binary searched.
     */
    private final K[] keys;

    KirinoImmutableMap(Class<K> keyClass, K[] keys, short[] indices, V[] values, Class<V> valueClass) {
        this.keys = keys;
        this.keyClass = keyClass;
        this.indices = indices;
        this.valueClass = valueClass;
        this.values = values;
    }

    @Override
    public int size() {
        return indices.length;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(@NonNull Object key) {
        Preconditions.checkNotNull(key);

        if (!keyClass.isInstance(key)) {
            return false;
        }
        if (key instanceof Comparable) {
            return Arrays.binarySearch(this.keys, key) >= 0;
        }
        else {
            return bruteForceSearch(this.keys, key) >= 0;
        }
    }

    private static int bruteForceSearch(Object[] arr, Object val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsValue(@NonNull Object value) {
        Preconditions.checkNotNull(value);

        if (!valueClass.isInstance(value)) {
            return false;
        }
        if (value instanceof Comparable) {
            return Arrays.binarySearch(this.values, value) >= 0;
        }
        else {
            return bruteForceSearch(this.values, value) >= 0;
        }
    }

    @Override
    public V get(@NonNull Object key) {
        Preconditions.checkNotNull(key);

        if (!keyClass.isInstance(key)) {
            return null;
        }
        try {
            return values[indices[Arrays.binarySearch(this.keys, key)]];
        } catch (ArrayIndexOutOfBoundsException _) {
            return null;
        }
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("Entries can not be put into an immutable data structure.");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Entries can not be put removed from an immutable data structure.");
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("Entries can not be put into an immutable data structure.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("An immutable data structure can not be cleaned.");
    }

    @Override
    public @NonNull Set<K> keySet() {
        return Set.of(this.keys);
    }

    @Override
    public @NonNull Collection<V> values() {
        List<V> list = new ArrayList<>();
        for (int idx : this.indices) {
            list.add(values[idx]);
        }
        return list;
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entries = new ObjectArraySet<>();
        for (int i = 0; i < this.indices.length; i++) {
            entries.add(Map.entry(keys[i], values[indices[i]]));
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Map<?, ?>)) {
            return false;
        }
        return this.entrySet().equals(((Map<K, V>) o).entrySet());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.keys) + (Arrays.hashCode(this.values) ^ 0x9e3779b9) + (Arrays.hashCode(this.indices) ^ 0xc2b2ae35);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < this.indices.length; i++) {
            action.accept(keys[i], values[indices[i]]);
        }
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("Entries can not be put removed from an immutable data structure.");
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("Entries can not be replaced in an immutable data structure.");
    }

    @Override
    public @Nullable V replace(K key, V value) {
        throw new UnsupportedOperationException("Entries can not be replaced in an immutable data structure.");
    }

    @Override
    public V computeIfAbsent(K key, @NonNull Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("Entries can not be put in an immutable data structure.");
    }

    @Override
    public V computeIfPresent(K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("Entries can not be replaced in an immutable data structure.");
    }

    @Override
    public V compute(K key, @NonNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("Entries can not be replaced in an immutable data structure.");
    }

    @Override
    public V merge(K key, @NonNull V value, @NonNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("Entries can not be put in an immutable data structure.");
    }

    public static class Builder<K, V> {
        private final Class<K> keyClass;
        private final Class<V> valueClass;

        private final Set<K> keys = new HashSet<>();
        private final Set<V> values = new HashSet<>();

        private final Set<Map.Entry<K, V>> mappings = new HashSet<>();

        public Builder(@NonNull final Class<K> keyClass, @NonNull final Class<V> valueClass) {
            Preconditions.checkNotNull(keyClass);
            Preconditions.checkNotNull(valueClass);

            this.keyClass = keyClass;
            this.valueClass = valueClass;
        }

        public void insert(@NonNull K key, @NonNull V value) {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(value);

            if (keys.contains(key)) {
                throw new IllegalArgumentException("Duplicate key.");
            }

            keys.add(key);
            mappings.add(Map.entry(key, value));
            values.add(value);
        }

        @SuppressWarnings("unchecked")
        public KirinoImmutableMap<K, V> build() {
            K[] ks = keys.toArray((K[]) Array.newInstance(keyClass, 0));
            short[] indices = new short[keys.size()];
            V[] vs = values.toArray((V[]) Array.newInstance(valueClass, 0));

            Arrays.sort(ks);
            Arrays.sort(vs);

            for (var entry : this.mappings) {
                indices[(ks[0] instanceof Comparable) ? Arrays.binarySearch(ks, entry.getKey()) : bruteForceSearch(ks, entry.getKey())]
                        = (short) ((vs[0] instanceof Comparable) ? Arrays.binarySearch(vs, entry.getValue()) : bruteForceSearch(vs, entry.getValue()));
            }

            return new KirinoImmutableMap<K, V>(this.keyClass, ks, indices, vs, this.valueClass);
        }
    }
}
