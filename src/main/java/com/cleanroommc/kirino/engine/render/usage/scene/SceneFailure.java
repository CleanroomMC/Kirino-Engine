package com.cleanroommc.kirino.engine.render.usage.scene;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

final class SceneFailure {
    private final AtomicReference<Throwable> first = new AtomicReference<>();

    void record(Throwable cause) {
        Preconditions.checkNotNull(cause);
        while (cause instanceof CompletionException && cause.getCause() != null) {
            cause = cause.getCause();
        }
        first.compareAndSet(null, cause);
    }

    void check() {
        Throwable cause = first.get();
        if (cause != null) {
            Throwables.throwIfUnchecked(cause);
            throw new CompletionException(cause);
        }
    }

    RuntimeException fail(Throwable cause) {
        record(cause);
        check();
        throw new AssertionError();
    }
}
