package com.cleanroommc.kirino.schemata.collections;

import it.unimi.dsi.fastutil.objects.AbstractObjectList;

import java.util.Collection;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.IntFunction;

public final class ObjectArrayView<TValue> extends AbstractObjectList<TValue> implements RandomAccess, Cloneable {
    private static final ObjectArrayView<?> EMPTY = new ObjectArrayView<>(new Object[0]);
    private final TValue[] elements;

    private ObjectArrayView(TValue[] elements) {
        this.elements = elements;
    }

    @SuppressWarnings("unchecked")
    public static <TValue> ObjectArrayView<TValue> of() {
        return (ObjectArrayView<TValue>) EMPTY;
    }

    @SafeVarargs
    public static <TValue> ObjectArrayView<TValue> of(TValue... values) {
        return copyOf(values);
    }

    @SuppressWarnings("unchecked")
    public static <TValue> ObjectArrayView<TValue> copyOf(Collection<? extends TValue> collection) {
        Objects.requireNonNull(collection);
        return checked((TValue[]) collection.toArray());
    }

    public static <TValue> ObjectArrayView<TValue> copyOf(Collection<? extends TValue> collection,
                                                        IntFunction<TValue[]> arrayFactory) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(arrayFactory);
        return checked(collection.toArray(arrayFactory));
    }

    @SafeVarargs
    public static <TValue> ObjectArrayView<TValue> copyOf(TValue... values) {
        Objects.requireNonNull(values);
        return checked(values.clone());
    }

    private static <TValue> ObjectArrayView<TValue> checked(TValue[] values) {
        for (TValue value : values) {
            Objects.requireNonNull(value);
        }
        return new ObjectArrayView<>(values);
    }

    public TValue[] elements() {
        return elements;
    }

    @Override
    public TValue get(int index) {
        return elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public ObjectArrayView<TValue> clone() {
        return copyOf(elements);
    }
}
