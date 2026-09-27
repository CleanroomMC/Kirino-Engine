package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

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
     */
    static void disposeEarly(@NonNull GLDisposable disposable) {
        if (!active) {
            return;
        }

        Preconditions.checkNotNull(disposable);

        if (disposables.remove(disposable)) {
            disposable.dispose();
        } else {
            throw new RuntimeException("Argument \"disposable\" is not in the disposable queue.");
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
        while (!disposables.isEmpty()) {
            GLDisposable disposable = disposables.poll();
            LOGGER.debug("Disposing " + disposable.getName());
            disposable.dispose();
        }
        LOGGER.debug("Finished.");
    }
}
