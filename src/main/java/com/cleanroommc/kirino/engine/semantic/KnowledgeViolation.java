package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class KnowledgeViolation {

    private final ViolationKind kind;
    private final KnowledgeKey<?> key;
    private final KnowledgeValue<?> actual;
    private final KnowledgeOwner owner;

    public KnowledgeViolation(
            @NonNull ViolationKind kind,
            @NonNull KnowledgeKey<?> key,
            @NonNull KnowledgeValue<?> actual,
            @NonNull KnowledgeOwner owner) {

        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(actual);
        Preconditions.checkNotNull(owner);

        this.kind = kind;
        this.key = key;
        this.actual = actual;
        this.owner = owner;
    }

    @NonNull
    public ViolationKind kind() {
        return kind;
    }

    @NonNull
    public KnowledgeKey<?> key() {
        return key;
    }

    @NonNull
    public KnowledgeValue<?> actual() {
        return actual;
    }

    @NonNull
    public KnowledgeOwner owner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("KnowledgeViolation{ kind=%s, key=%s, actual=%s, owner=%s }",
                kind.toString(),
                key.toString(),
                actual.toString(),
                owner.toString());
    }
}
