package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ForkJoinPoolUtils {

    private ForkJoinPoolUtils() {
    }

    /**
     * To create a new pre-configured working stealing pool.
     */
    public static ForkJoinPool newWorkStealingPool(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return newWorkStealingPool(name, -1, true);
    }

    /**
     * To create a new working stealing pool.
     *
     * @param name The pool name. Worker thread name follows the <code>XXX-worker-*</code> pattern
     * @param parallelismOffset The initial parallelism is the number of cores.
     *                          The offsetted parallelism will be adjusted so it's in <code>[1, cores]</code>
     * @param asyncMode The <code>asyncMode</code> parameter inside {@link ForkJoinPool#ForkJoinPool(int, ForkJoinPool.ForkJoinWorkerThreadFactory, Thread.UncaughtExceptionHandler, boolean)}
     * @return ForkJoinPool
     */
    @NonNull
    public static ForkJoinPool newWorkStealingPool(@NonNull String name, int parallelismOffset, boolean asyncMode) {
        Preconditions.checkNotNull(name);

        int cores = Runtime.getRuntime().availableProcessors();
        int parallelism = Math.max(1, Math.min(cores, cores + parallelismOffset));

        AtomicInteger counter = new AtomicInteger();

        return new ForkJoinPool(
                parallelism,
                pool -> {
                    ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    t.setName(name + "-worker-" + counter.getAndIncrement());
                    return t;
                },
                null,
                asyncMode);
    }

    public static void shutdownPool(@NonNull ForkJoinPool pool) {
        Preconditions.checkNotNull(pool);

        pool.shutdown();

        try {
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
