package com.cleanroommc.kirino.engine.render.usage;

import com.google.common.base.Throwables;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

final class WorldSceneLifecycle<W> {
    interface Backend<W> {
        void attach(W world);
        void detach();
        CompletableFuture<Void> quiesce();
        void beginGpuDrain();
        boolean gpuReady();
        void awaitGpu();
        void reset();
        void close();
        void update();
    }

    private final Backend<W> backend;
    private W current;
    private CompletableFuture<Void> draining;
    private boolean closed;
    private Throwable failure;
    private Throwable updateFailure;

    WorldSceneLifecycle(Backend<W> backend) {
        this.backend = backend;
    }

    void update(W requested) {
        check();
        if (closed) {
            return;
        }
        if (updateFailure != null && current == requested) {
            rethrow(updateFailure);
        }
        try {
            if (draining == null && current != requested && current != null) {
                backend.detach();
                updateFailure = null;
                current = null;
                draining = backend.quiesce();
                backend.beginGpuDrain();
            }
            if (draining != null) {
                if (!draining.isDone() || !backend.gpuReady()) {
                    return;
                }
                backend.reset();
                draining = null;
            }
            if (current != requested) {
                backend.attach(requested);
                current = requested;
            }
        } catch (Throwable cause) {
            failure = cause;
            check();
        }
        if (current != null) {
            try {
                backend.update();
            } catch (Throwable cause) {
                updateFailure = cause;
                rethrow(cause);
            }
        }
    }

    void close() {
        if (closed) {
            return;
        }
        backend.detach();
        current = null;
        if (draining == null) {
            draining = backend.quiesce();
            backend.beginGpuDrain();
        }
        draining.handle((unused, cause) -> null).join();
        backend.awaitGpu();
        backend.close();
        draining = null;
        closed = true;
    }

    private void check() {
        if (failure != null) {
            rethrow(failure);
        }
    }

    private static void rethrow(Throwable cause) {
        Throwables.throwIfUnchecked(cause);
        throw new CompletionException(cause);
    }
}
