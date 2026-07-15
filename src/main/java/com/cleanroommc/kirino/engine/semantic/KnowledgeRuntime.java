package com.cleanroommc.kirino.engine.semantic;

import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface KnowledgeRuntime {

    /**
     * Commit the new changes to the knowledge system. It might result in a commit conflict depending on
     * the claimed keys.
     *
     * @see #claim(KnowledgeKey)
     */
    void commit(@NonNull Consumer<KnowledgeCheckpoint> checkpoint);

    /**
     * It's as simple as a precondition check.
     */
    <T> void require(@NonNull KnowledgeKey<T> key, @NonNull T expected);

    /**
     * It's as simple as a precondition check with advanced conditions.
     */
    <T> void require(@NonNull KnowledgeKey<T> key, @NonNull Predicate<? super T> predicate);

    /**
     * It's as simple as a precondition check.
     */
    void requireKnown(@NonNull KnowledgeKey<?> key);

    /**
     * You don't have to report the new value (use {@link #commit(Consumer)} instead).
     * You only report a mutation when you're sure certain knowledge is longer
     * reliable but can't access too much of information.
     */
    <T> void reportMutation(@NonNull KnowledgeKey<T> key);

    /**
     * To claim a certain key so the corresponding value can't be committed until the release call.
     *
     * <p>Explanation: It means that the program is going to rely on certain knowledge for a while.</p>
     */
    @NonNull
    <T> ClaimedScopeHandle<T> claim(@NonNull KnowledgeKey<T> key);

    @NonNull
    KnowledgeOwner owner();
}
