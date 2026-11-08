package com.cleanroommc.kirino.schemata.collections;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

final class PreviousObject2ObjectBiMap<TKey, TValue> extends AbstractMap<TKey, TValue> implements BiMap<TKey, TValue> {
    private final Object2ObjectOpenHashMap<TKey, TValue> forward;
    private final Object2ObjectOpenHashMap<TValue, TKey> backward;
    private final PreviousObject2ObjectBiMap<TValue, TKey> inverse;
    private transient Set<Entry<TKey, TValue>> entries;

    public PreviousObject2ObjectBiMap() {
        this(16);
    }

    public PreviousObject2ObjectBiMap(int expectedSize) {
        this(new Object2ObjectOpenHashMap<>(expectedSize), new Object2ObjectOpenHashMap<>(expectedSize), null);
    }

    public PreviousObject2ObjectBiMap(Map<? extends TKey, ? extends TValue> map) {
        this(map.size());
        putAll(map);
    }

    private PreviousObject2ObjectBiMap(Object2ObjectOpenHashMap<TKey, TValue> forward,
                              Object2ObjectOpenHashMap<TValue, TKey> backward,
                              PreviousObject2ObjectBiMap<TValue, TKey> inverse) {
        this.forward = forward;
        this.backward = backward;
        this.inverse = inverse == null ? new PreviousObject2ObjectBiMap<>(backward, forward, this) : inverse;
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
        return forward.get(key);
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
        boolean keyPresent = forward.containsKey(key);
        TValue previous = forward.get(key);
        if (keyPresent && Objects.equals(previous, value)) {
            return previous;
        }
        if (backward.containsKey(value)) {
            if (!force) {
                throw new IllegalArgumentException("Value already present: " + value);
            }
            forward.remove(backward.remove(value));
        }
        if (keyPresent) {
            backward.remove(previous);
        }
        forward.put(key, value);
        backward.put(value, key);
        return previous;
    }

    @Override
    public @Nullable TValue remove(Object key) {
        if (!forward.containsKey(key)) {
            return null;
        }
        TValue previous = forward.remove(key);
        backward.remove(previous);
        return previous;
    }

    @Override
    public void putAll(Map<? extends TKey, ? extends TValue> map) {
        for (Entry<? extends TKey, ? extends TValue> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void replaceAll(BiFunction<? super TKey, ? super TValue, ? extends TValue> function) {
        Objects.requireNonNull(function);
        PreviousObject2ObjectBiMap<TKey, TValue> replacement = new PreviousObject2ObjectBiMap<>(size());
        for (Entry<TKey, TValue> entry : forward.entrySet()) {
            replacement.put(entry.getKey(), function.apply(entry.getKey(), entry.getValue()));
        }
        clear();
        putAll(replacement);
    }

    @Override
    public void clear() {
        forward.clear();
        backward.clear();
    }

    @Override
    public @NonNull Set<TKey> keySet() {
        return super.keySet();
    }

    @Override
    public @NonNull Set<TValue> values() {
        return inverse.keySet();
    }

    @Override
    public @NonNull Set<Entry<TKey, TValue>> entrySet() {
        if (entries == null) {
            entries = new AbstractSet<>() {
                @Override
                public int size() {
                    return forward.size();
                }

                @Override
                public void clear() {
                    PreviousObject2ObjectBiMap.this.clear();
                }

                @Override
                public boolean contains(Object object) {
                    return object instanceof Entry<?, ?> entry
                            && forward.containsKey(entry.getKey())
                            && Objects.equals(forward.get(entry.getKey()), entry.getValue());
                }

                @Override
                public boolean remove(Object object) {
                    if (!contains(object)) {
                        return false;
                    }
                    PreviousObject2ObjectBiMap.this.remove(((Entry<?, ?>) object).getKey());
                    return true;
                }

                @Override
                public Iterator<Entry<TKey, TValue>> iterator() {
                    Iterator<Entry<TKey, TValue>> iterator = forward.entrySet().iterator();
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
                                    TValue previous = PreviousObject2ObjectBiMap.this.put(getKey(), value);
                                    super.setValue(value);
                                    return previous;
                                }
                            };
                        }

                        @Override
                        public void remove() {
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
    public @NonNull PreviousObject2ObjectBiMap<TValue, TKey> inverse() {
        return inverse;
    }
}

