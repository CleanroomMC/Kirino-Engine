package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class GLResourceManager {

    private GLResourceManager() {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino GLResourceManager");
    private static final ThreadLocal<ResourceScope> CURRENT_SCOPE = new ThreadLocal<>();

    public static final class ResourceScope implements AutoCloseable {

        private final PriorityQueue<GLDisposable> resources = new PriorityQueue<>();

        private ResourceScope() {
        }

        @Override
        public void close() {
            Preconditions.checkState(CURRENT_SCOPE.get() == this,
                    "Close on the owning GL thread.");

            try {
                while (!resources.isEmpty()) {
                    GLDisposable resource = resources.poll();
                    try {
                        resource.dispose();
                    } catch (Throwable t) {
                        String str = String.format("Failed to dispose OpenGL resource \"%s\".", resource.getName());
                        LOGGER.error(str, t);
                    }
                }
            } finally {
                CURRENT_SCOPE.remove();
            }
        }
    }

    /**
     * It only guards the main thread resource tracking.
     * Independent scopes don't have such a concept.
     */
    private static boolean active;

    static {
        active = true;
        ShutdownManager.register(() -> {
            if (!active) {
                return;
            }

            active = false;
            disposeAll();
        });
    }

    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    /**
     * Tracks resources for an independent GL context on another thread.
     */
    public static ResourceScope openScope() {
        Preconditions.checkState(CURRENT_SCOPE.get() == null,
                "GL resource scopes cannot be nested.");

        ResourceScope scope = new ResourceScope();
        CURRENT_SCOPE.set(scope);
        return scope;
    }

    private static PriorityQueue<GLDisposable> resources() {
        ResourceScope scope = CURRENT_SCOPE.get();
        return scope == null ? disposables : scope.resources;
    }

    /**
     * Call this method to keep track of GL resources.
     *
     * <p>Note: The main thread queue only accepts resources when <code>{@link #active} == true</code>.</p>
     */
    public static void addDisposable(@NonNull GLDisposable disposable) {
        if (CURRENT_SCOPE.get() == null && !active) {
            return;
        }

        Preconditions.checkNotNull(disposable);

        resources().add(disposable);
    }

    /**
     * Remove the tracked GL resource from the queue and dispose it manually.
     *
     * <p>Note: For the main thread queue, only runs when <code>{@link #active} == true</code>.</p>
     * <p>Note: The resource remains removed if its disposal fails.</p>
     */
    static void disposeEarly(@NonNull GLDisposable disposable) {
        if (CURRENT_SCOPE.get() == null && !active) {
            return;
        }

        Preconditions.checkNotNull(disposable);

        String resourceName = disposable.getName();

        if (resources().remove(disposable)) {
            LOGGER.debug("Early disposing {}", resourceName);
            try {
                disposable.dispose();
            } catch (Throwable t) {
                String str = String.format("Failed to dispose OpenGL resource \"%s\".", resourceName);
                LOGGER.error(str, t);
                throw t;
            }
        } else {
            throw new RuntimeException(String.format("Argument \"disposable\"=%s is not in the disposable queue.", resourceName));
        }
    }

    /**
     * Dispose all GL resources tracked in the main thread queue.
     */
    private static void disposeAll() {
        LOGGER.debug("Starts disposing OpenGL resources.");

        try {
            List<RuntimeException> failures = new ArrayList<>();

            while (!disposables.isEmpty()) {
                GLDisposable disposable = disposables.poll();
                String resourceName = disposable.getName();
                LOGGER.debug("Disposing {}", resourceName);

                try {
                    disposable.dispose();
                } catch (Throwable t) {
                    failures.add(new RuntimeException(
                            String.format("Failed to dispose OpenGL resource \"%s\".", resourceName), t));
                }
            }

            if (!failures.isEmpty()) {
                RuntimeException aggregate = new RuntimeException(
                        String.format("Failed to dispose %d OpenGL resource(s).", failures.size()));
                for (RuntimeException failure : failures) {
                    aggregate.addSuppressed(failure);
                }

                LOGGER.debug("Failed to dispose some OpenGL resource(s).", aggregate);
            }
        } finally {
            LOGGER.debug("Finished disposing OpenGL resources.");
        }
    }
}
