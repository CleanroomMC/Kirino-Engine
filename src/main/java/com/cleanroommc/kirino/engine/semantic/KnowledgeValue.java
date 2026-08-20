package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class KnowledgeValue<T> {

    private static final KnowledgeValue<?> UNKNOWN = new KnowledgeValue<>(false, null);

    private final boolean known;
    private final T value;

    private KnowledgeValue(boolean known, @Nullable T value) {
        this.known = known;
        this.value = value;
    }

    @NonNull
    public static <T> KnowledgeValue<T> known(@NonNull T value) {
        Preconditions.checkNotNull(value);

        return new KnowledgeValue<>(true, value);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> KnowledgeValue<T> unknown() {
        return (KnowledgeValue<T>) UNKNOWN;
    }

    public boolean isKnown() {
        return known;
    }

    @NonNull
    public T value() {
        Preconditions.checkState(known, "Knowledge value is unknown.");
        Preconditions.checkNotNull(value);

        return value;
    }

    @Override
    public String toString() {
        return known ? "Known(" + value + ")" : "Unknown";
    }
}
