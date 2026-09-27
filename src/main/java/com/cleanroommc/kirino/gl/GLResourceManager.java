package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * It activates itself (<code>active = true</code>) and registers the shutdown hook when the class is loaded.
 *
 * <p>Note: As a common user, you don't need to access this class 99% of times.</p>
 */
public final class GLResourceManager {

    private GLResourceManager() {
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino GLResourceManager");

    private static boolean active;

    static {
        active = true;
        ShutdownManager.register(GLResourceManager::disposeAll);
    }

    /**
     * It's always active before the program ends.
     * It's simply a safety guard. No need to check it before operations.
     */
    public static boolean isActive() {
        return active;
    }

    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    /**
     * Call this method to keep track of GL resources.
     * The GL resource will only be added to the tracking queue when <code>{@link #isActive()} == true</code>.
     */
    public static void addDisposable(@NonNull GLDisposable disposable) {
        if (!active) {
            return;
        }

        Preconditions.checkNotNull(disposable);

        disposables.add(disposable);
    }

    /**
     * Remove the tracked GL resource from the queue and dispose it manually.
     *
     * <p>Only runs when <code>{@link #isActive()} == true</code>.</p>
     * <p>Note: The resource remains removed if its disposal fails.</p>
     */
    static void disposeEarly(@NonNull GLDisposable disposable) {
        if (!active) {
            return;
        }

        Preconditions.checkNotNull(disposable);

        String resourceName = disposable.getName();

        if (disposables.remove(disposable)) {
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
     * Turn off the service and dispose all tracked GL resources.
     *
     * <p>Only runs when <code>{@link #isActive()} == true</code>.</p>
     */
    private static void disposeAll() {
        if (!active) {
            return;
        }

        active = false;
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
