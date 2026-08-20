package com.cleanroommc.kirino.engine;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownManager {

    private static final Logger LOGGER = LogManager.getLogger("Kirino ShutdownManager");

    private static final List<Runnable> mainThreadHooks = new CopyOnWriteArrayList<>();
    private static final List<Runnable> workerThreadHooks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean installed = new AtomicBoolean(false);

    private static void onDemandInstall() {
        if (installed.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(ShutdownManager::runWorkerHooks, "ShutdownHook"));
        }
    }

    /**
     * Register on the main thread that has the GL context.
     */
    public static void register(@NonNull Runnable hook) {
        Preconditions.checkNotNull(hook);

        mainThreadHooks.add(hook);
    }

    /**
     * Register on a worker thread.
     */
    public static void registerAsync(@NonNull Runnable hook) {
        Preconditions.checkNotNull(hook);

        workerThreadHooks.add(hook);
        onDemandInstall();
    }

    /**
     * <p><b>Note</b>: Must not be called by clients!</p>
     */
    public static void runMainHooks() {
        List<Runnable> reversed = new ArrayList<>(mainThreadHooks);
        Collections.reverse(reversed);

        LOGGER.info("Started running main shutdown hooks.");

        for (Runnable hook : reversed) {
            try {
                hook.run();
            } catch (Throwable t) {
                LOGGER.throwing(t);
            }
        }

        LOGGER.info("Finished running main shutdown hooks.");
    }

    private static void runWorkerHooks() {
        List<Runnable> reversed = new ArrayList<>(workerThreadHooks);
        Collections.reverse(reversed);

        LOGGER.info("Started running worker shutdown hooks.");

        for (Runnable hook : reversed) {
            try {
                hook.run();
            } catch (Throwable t) {
                LOGGER.throwing(t);
            }
        }

        LOGGER.info("Finished running worker shutdown hooks.");
    }
}
