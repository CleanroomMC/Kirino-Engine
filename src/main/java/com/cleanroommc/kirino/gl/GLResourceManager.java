package com.cleanroommc.kirino.gl;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.KirinoCommonCore;

import java.util.PriorityQueue;

public final class GLResourceManager {

    private static boolean active = false;

    public static void turnOn() {
        active = true;
    }

    public static boolean isActive() {
        return active;
    }

    private static final PriorityQueue<GLDisposable> disposables = new PriorityQueue<>();

    /**
     * Call this method to keep track of GL resources.
     * The GL resource will only be added to the tracking queue when <code>{@link #isActive()} == true</code>.
     * You can expect the <code>active</code> toggle to be turned on at an early phase.
     * (see {@link KirinoClientCore#init()})
     */
    public static void addDisposable(GLDisposable disposable) {
        if (active) {
            disposables.add(disposable);
        }
    }

    /**
     * Remove the tracked GL resource from the queue and dispose it manually.
     */
    public static void disposeEarly(GLDisposable disposable) {
        if (disposables.remove(disposable)) {
            disposable.dispose();
        } else {
            throw new RuntimeException("Argument \"disposable\" is not in the disposable queue.");
        }
    }

    /**
     * Turn off the service and dispose all tracked GL resources.
     */
    public static void disposeAll() {
        active = false;
        KirinoCommonCore.LOGGER.info("Starts disposing OpenGL resources.");
        while (!disposables.isEmpty()) {
            GLDisposable disposable = disposables.poll();
            KirinoCommonCore.LOGGER.info("Disposing " + disposable.getName());
            disposable.dispose();
        }
        KirinoCommonCore.LOGGER.info("Finished.");
    }
}
