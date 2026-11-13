package com.cleanroommc.kirino.ecs;

import com.google.common.base.Throwables;

import java.util.concurrent.CompletionException;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;

final class ExecutionFailures {
    private ExecutionFailures() {
    }

    static Throwable merge(Throwable primary, Throwable secondary) {
        secondary = unwrap(secondary);
        if (primary == null) {
            return secondary;
        }
        if (secondary == null || primary == secondary) {
            return primary;
        }
        if (contains(primary, secondary)) {
            return primary;
        }
        if (contains(secondary, primary)) {
            return primary;
        }
        primary.addSuppressed(secondary);
        return primary;
    }

    static Throwable mergeEnclosing(Throwable primary, Throwable secondary) {
        secondary = unwrap(secondary);
        if (primary != null && secondary != null && contains(secondary, primary)) {
            return secondary;
        }
        return merge(primary, secondary);
    }

    private static boolean contains(Throwable root, Throwable target) {
        ArrayDeque<Throwable> pending = new ArrayDeque<>();
        IdentityHashMap<Throwable, Boolean> visited = new IdentityHashMap<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            Throwable next = pending.removeLast();
            if (next == target) {
                return true;
            }
            if (visited.put(next, Boolean.TRUE) != null) {
                continue;
            }
            if (next.getCause() != null) {
                pending.add(next.getCause());
            }
            for (Throwable suppressed : next.getSuppressed()) {
                pending.add(suppressed);
            }
        }
        return false;
    }

    static Throwable unwrap(Throwable failure) {
        while (failure instanceof CompletionException && failure.getCause() != null) {
            failure = failure.getCause();
        }
        return failure;
    }

    static void rethrow(Throwable failure) {
        Throwables.throwIfUnchecked(failure);
        throw new CompletionException(failure);
    }
}
