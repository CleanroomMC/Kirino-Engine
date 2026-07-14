package com.cleanroommc.kirino.engine.semantic;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface KnowledgeRuntime {

    void commit(@NonNull Consumer<KnowledgeCheckpoint> checkpoint);

    <T> void require(@NonNull KnowledgeKey<T> key, @NonNull T expected);

    <T> void require(@NonNull KnowledgeKey<T> key, @NonNull Predicate<? super T> predicate);

    void requireKnown(@NonNull KnowledgeKey<?> key);

    <T> void reportMutation(@NonNull KnowledgeKey<T> key);

    @NonNull
    KnowledgeOwner owner();
}
