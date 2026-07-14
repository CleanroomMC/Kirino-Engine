package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class KnowledgeCheckpoint {

    private final Map<KnowledgeKey<?>, Change<?>> changes = new LinkedHashMap<>();
    private final Set<String> unknownDomains = new LinkedHashSet<>();

    public <T> KnowledgeCheckpoint know(
            @NonNull KnowledgeKey<T> key,
            @NonNull T value) {

        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        changes.put(key, Change.known(value));
        return this;
    }

    @NonNull
    public KnowledgeCheckpoint unknown(@NonNull KnowledgeKey<?> key) {
        Preconditions.checkNotNull(key);

        changes.put(key, Change.unknown());
        return this;
    }

    @NonNull
    public KnowledgeCheckpoint unknownDomain(@NonNull String domain) {
        Preconditions.checkNotNull(domain);

        unknownDomains.add(domain);
        return this;
    }

    Map<KnowledgeKey<?>, Change<?>> changes() {
        return changes;
    }

    Set<String> unknownDomains() {
        return unknownDomains;
    }

    static final class Change<T> {

        enum Kind {
            KNOWN,
            UNKNOWN
        }

        private final Kind kind;
        private final T value;

        private Change(@NonNull Kind kind, @Nullable T value) {
            Preconditions.checkNotNull(kind);

            this.kind = kind;
            this.value = value;
        }

        @NonNull
        static <T> Change<T> known(@NonNull T value) {
            Preconditions.checkNotNull(value);

            return new Change<>(Kind.KNOWN, value);
        }

        @NonNull
        static <T> Change<T> unknown() {
            return new Change<>(Kind.UNKNOWN, null);
        }

        @NonNull
        Kind kind() {
            return kind;
        }

        @NonNull
        T value() {
            Preconditions.checkState(kind == Kind.KNOWN, "Unknown change has no value.");

            return value;
        }
    }
}