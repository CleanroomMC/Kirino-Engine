package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class KnowledgeSupervisor {

    private final Map<KnowledgeKey<?>, Entry<?>> knowledge = new HashMap<>();
    private final Set<KnowledgeKey<?>> frozen = new HashSet<>();
    private final Map<KnowledgeKey<?>, Integer> claims = new HashMap<>();

    private final ViolationPolicy violationPolicy;

    public KnowledgeSupervisor(@NonNull ViolationPolicy violationPolicy) {
        Preconditions.checkNotNull(violationPolicy);

        this.violationPolicy = violationPolicy;
    }

    @NonNull
    public KnowledgeRuntime access(@NonNull KnowledgeOwner owner) {
        Preconditions.checkNotNull(owner);

        return new Access(this, owner);
    }

    private void checkCommitConflict(
            @NonNull KnowledgeOwner requester,
            @NonNull KnowledgeKey<?> key,
            @NonNull KnowledgeValue<?> commitValue) {

        Preconditions.checkNotNull(requester);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(commitValue);

        Integer depth = claims.get(key);

        boolean report = false;
        if (depth != null && depth > 0) {
            Entry<?> rawEntry = knowledge.get(key);
            if (rawEntry == null) {
                if (commitValue.isKnown()) {
                    report = true;
                }
            } else {
                if (!rawEntry.value.isKnown() && commitValue.isKnown()) {
                    report = true;
                }
                if (rawEntry.value.isKnown() && !commitValue.isKnown()) {
                    report = true;
                }
                if (rawEntry.value.isKnown() && commitValue.isKnown() && rawEntry.value.value() != commitValue.value()) {
                    report = true;
                }
            }
        }

        if (report) {
            violationPolicy.onViolation(new KnowledgeViolation(
                    ViolationKind.COMMIT_CONFLICT,
                    key,
                    commitValue,
                    requester));
        }
    }

    private void commit(
            @NonNull KnowledgeOwner owner,
            @NonNull Consumer<KnowledgeCheckpoint> writer) {

        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(writer);

        KnowledgeCheckpoint checkpoint = new KnowledgeCheckpoint();

        writer.accept(checkpoint);

        Map<KnowledgeKey<?>, Entry<?>> next = new HashMap<>(knowledge);

        for (String domain : checkpoint.unknownDomains()) {
            for (Map.Entry<KnowledgeKey<?>, Entry<?>> entry : next.entrySet()) {
                if (entry.getKey().domain().equals(domain)) {
                    checkCommitConflict(owner, entry.getKey(), KnowledgeValue.unknown());
                    entry.setValue(Entry.unknown());
                }
            }
        }

        for (Map.Entry<KnowledgeKey<?>, KnowledgeCheckpoint.Change<?>> entry : checkpoint.changes().entrySet()) {
            applyChange(
                    owner,
                    next,
                    entry.getKey(),
                    entry.getValue());
        }

        knowledge.clear();
        knowledge.putAll(next);

        frozen.clear();
        for (Map.Entry<KnowledgeKey<?>, Entry<?>> entry : knowledge.entrySet()) {
            if (entry.getValue().value.isKnown()) {
                frozen.add(entry.getKey());
            }
        }
    }

    private void applyChange(
            @NonNull KnowledgeOwner owner,
            @NonNull Map<KnowledgeKey<?>, Entry<?>> target,
            @NonNull KnowledgeKey<?> key,
            KnowledgeCheckpoint.@NonNull Change<?> change) {

        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(change);

        switch (change.kind()) {
            case KNOWN:
                Object value = change.value();
                Preconditions.checkState(key.type().isInstance(value),
                        "Value %s doesn't much %s for %s.",
                        value,
                        key.type().getName(),
                        key);

                checkCommitConflict(owner, key, KnowledgeValue.known(value));
                target.put(key, Entry.known(value));
                break;

            case UNKNOWN:
                checkCommitConflict(owner, key, KnowledgeValue.unknown());
                target.put(key, Entry.unknown());
                break;

            default:
                throw new IllegalStateException("Invalid change kind.");
        }
    }

    private <T> void require(
            @NonNull KnowledgeOwner requester,
            @NonNull KnowledgeKey<T> key,
            @NonNull Predicate<? super T> predicate) {

        Preconditions.checkNotNull(requester);
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(predicate);

        Entry<?> rawEntry = knowledge.get(key);

        if (rawEntry == null || !rawEntry.value.isKnown()) {
            violationPolicy.onViolation(new KnowledgeViolation(
                    ViolationKind.REQUIRE_UNKNOWN,
                    key,
                    KnowledgeValue.unknown(),
                    requester));
            return;
        }

        Object rawValue = rawEntry.value.value();
        Preconditions.checkState(key.type().isInstance(rawValue));

        T value = key.type().cast(rawValue);

        if (!predicate.test(value)) {
            violationPolicy.onViolation(new KnowledgeViolation(
                    ViolationKind.REQUIRE_MISMATCH,
                    key,
                    KnowledgeValue.known(value),
                    requester));
        }
    }

    private <T> void reportMutation(
            @NonNull KnowledgeOwner reporter,
            @NonNull KnowledgeKey<T> key) {

        Preconditions.checkNotNull(reporter);
        Preconditions.checkNotNull(key);

        Entry<?> existing = knowledge.get(key);

        if (frozen.contains(key)) {
            KnowledgeValue<?> actual = existing == null ? KnowledgeValue.unknown() : existing.value;

            violationPolicy.onViolation(new KnowledgeViolation(
                    ViolationKind.FROZEN_MUTATION,
                    key,
                    actual,
                    reporter));
        }
    }

    private <T> ClaimedScopeHandle claim(@NonNull KnowledgeKey<T> key) {
        Preconditions.checkNotNull(key);

        claims.merge(key, 1, Integer::sum);
        return new ClaimedScopeHandleImpl<>(this, key);
    }

    private ClaimedScopeHandle claim(@NonNull KnowledgeKey<?>[] keys) {
        Preconditions.checkNotNull(keys);

        for (KnowledgeKey<?> key : keys) {
            Preconditions.checkNotNull(key);

            claims.merge(key, 1, Integer::sum);
        }
        return new ClaimedMultiScopeHandleImpl(this, keys);
    }

    void release(@NonNull KnowledgeKey<?> key) {
        Preconditions.checkNotNull(key);

        Integer depth = claims.get(key);

        Preconditions.checkState(depth != null,
                "The key=\"%s\" isn't claimed yet. Must not release it.", key.toString());

        if (depth == 1) {
            claims.remove(key);
        } else {
            claims.put(key, depth - 1);
        }
    }

    private static final class Entry<T> {

        private final KnowledgeValue<T> value;

        private Entry(@NonNull KnowledgeValue<T> value) {
            Preconditions.checkNotNull(value);

            this.value = value;
        }

        static <T> Entry<T> known(@NonNull T value) {
            return new Entry<>(KnowledgeValue.known(value));
        }

        static <T> Entry<T> unknown() {
            return new Entry<>(KnowledgeValue.unknown());
        }
    }

    private static final class Access implements KnowledgeRuntime {

        private final KnowledgeSupervisor supervisor;
        private final KnowledgeOwner owner;

        private Access(@NonNull KnowledgeSupervisor supervisor, @NonNull KnowledgeOwner owner) {
            Preconditions.checkNotNull(supervisor);
            Preconditions.checkNotNull(owner);

            this.supervisor = supervisor;
            this.owner = owner;
        }

        @Override
        public void commit(@NonNull Consumer<KnowledgeCheckpoint> checkpoint) {
            Preconditions.checkNotNull(checkpoint);

            supervisor.commit(owner, checkpoint);
        }

        @Override
        public <T> void require(@NonNull KnowledgeKey<T> key, @NonNull T expected) {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(expected);

            supervisor.require(owner, key, value -> Objects.equals(value, expected));
        }

        @Override
        public <T> void require(@NonNull KnowledgeKey<T> key, @NonNull Predicate<? super T> predicate) {
            Preconditions.checkNotNull(key);
            Preconditions.checkNotNull(predicate);

            supervisor.require(owner, key, predicate);
        }

        @Override
        public void requireKnown(@NonNull KnowledgeKey<?> key) {
            Preconditions.checkNotNull(key);

            requireKnownUnchecked(key);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void requireKnownUnchecked(@NonNull KnowledgeKey key) {
            supervisor.require(owner, key, value -> true);
        }

        @Override
        public <T> void reportMutation(@NonNull KnowledgeKey<T> key) {
            Preconditions.checkNotNull(key);

            supervisor.reportMutation(owner, key);
        }

        @NonNull
        @Override
        public <T> ClaimedScopeHandle claim(@NonNull KnowledgeKey<T> key) {
            Preconditions.checkNotNull(key);

            return supervisor.claim(key);
        }

        @NonNull
        @Override
        public ClaimedScopeHandle claim(@NonNull KnowledgeKey<?> @NonNull ... keys) {
            Preconditions.checkNotNull(keys);
            for (KnowledgeKey<?> key : keys) {
                Preconditions.checkNotNull(key);
            }

            return supervisor.claim(keys);
        }

        @NonNull
        @Override
        public KnowledgeOwner owner() {
            return owner;
        }
    }
}
