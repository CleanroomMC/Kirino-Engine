package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.engine.ShutdownManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.PriorityQueue;

/**
 * It activates itself (<code>active = true</code>) and registers the shutdown hook when the class is loaded.
 */
public final class GLResourceManager {

    private static final Logger LOGGER = LogManager.getLogger("Kirino GLResourceManager");

    private static boolean active;

    static {
        active = true;
        ShutdownManager.register(GLResourceManager::disposeAll);
    }

    public static boolean isActive() {
        return active;
    }

    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    /**
     * Call this method to keep track of GL resources.
     * The GL resource will only be added to the tracking queue when <code>{@link #isActive()} == true</code>.
     */
    public static void addDisposable(GLDisposable disposable) {
        if (!active) {
            return;
        }

        disposables.add(disposable);
    }

    /**
     * Remove the tracked GL resource from the queue and dispose it manually.
     *
     * <p>Only runs when <code>{@link #isActive()} == true</code>.</p>
     */
    public static void disposeEarly(GLDisposable disposable) {
        if (!active) {
            return;
        }

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
