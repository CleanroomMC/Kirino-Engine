package com.cleanroommc.kirino.utils.collections;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

import java.util.Collection;
import java.util.Objects;
import java.util.function.IntFunction;

public final class ImmutableObjectArrayList<TValue> extends ObjectImmutableList<TValue> {
    private static final ImmutableObjectArrayList<?> EMPTY = new ImmutableObjectArrayList<>(new Object[0]);
    private final TValue[] elements;

    private ImmutableObjectArrayList(TValue[] elements) {
        super(elements);
        this.elements = elements;
    }

    @SuppressWarnings("unchecked")
    public static <TValue> ImmutableObjectArrayList<TValue> of() {
        return (ImmutableObjectArrayList<TValue>) EMPTY;
    }

    @SafeVarargs
    public static <TValue> ImmutableObjectArrayList<TValue> of(TValue... values) {
        return copyOf(values);
    }

    @SuppressWarnings("unchecked")
    public static <TValue> ImmutableObjectArrayList<TValue> copyOf(Collection<? extends TValue> collection) {
        Objects.requireNonNull(collection);
        return checked((TValue[]) collection.toArray());
    }

    public static <TValue> ImmutableObjectArrayList<TValue> copyOf(Collection<? extends TValue> collection,
                                                                IntFunction<TValue[]> arrayFactory) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(arrayFactory);
        return checked(collection.toArray(arrayFactory));
    }

    @SafeVarargs
    public static <TValue> ImmutableObjectArrayList<TValue> copyOf(TValue... values) {
        Objects.requireNonNull(values);
        return checked(values.clone());
    }

    private static <TValue> ImmutableObjectArrayList<TValue> checked(TValue[] values) {
        for (TValue value : values) {
            Objects.requireNonNull(value);
        }
        return new ImmutableObjectArrayList<>(values);
    }

    public TValue[] elements() {
        return elements;
    }

    @Override
    public ImmutableObjectArrayList<TValue> clone() {
        return copyOf(elements);
    }
}
