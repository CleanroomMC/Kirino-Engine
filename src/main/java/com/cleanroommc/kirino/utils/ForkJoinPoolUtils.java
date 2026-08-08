package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class ForkJoinPoolUtils {

    private final static Logger LOGGER = LogManager.getLogger();

    private ForkJoinPoolUtils() {
    }

    /**
     * To create a new pre-configured work stealing pool (parallelism = <code>cores - 1</code>; FIFO; errorLogger: <code>on</code>).
     *
     * @param name The pool name. Worker thread name follows the <code>XXX-worker-*</code> pattern
     */
    public static ForkJoinPool newWorkStealingPool(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return newWorkStealingPool(
                name,
                Runtime.getRuntime().availableProcessors(),
                1f,
                -1,
                true,
                null,
                LOGGER);
    }

    /**
     * To create a new pre-configured work stealing pool (parallelism = <code>cores - 1</code>; FIFO; errorLogger: <code>on</code>)
     * and receive the parallelism.
     *
     * @param name The pool name. Worker thread name follows the <code>XXX-worker-*</code> pattern
     * @param outParallelism If the input was non-null and the array length equals <code>1</code>, then the calculated parallelism will be outputted
     */
    public static ForkJoinPool newWorkStealingPool(@NonNull String name, int @Nullable [] outParallelism) {
        Preconditions.checkNotNull(name);

        return newWorkStealingPool(
                name,
                Runtime.getRuntime().availableProcessors(),
                1f,
                -1,
                true,
                outParallelism,
                LOGGER);
    }

    /**
     * To create a new work stealing pool.
     * <p>Final parallelism = clamp( ⌊<code>cores</code> * <code>parallelismMultiplier</code>⌋ + parallelismOffset )</p>
     *
     * @param name The pool name. Worker thread name follows the <code>XXX-worker-*</code> pattern
     * @param maxParallelism Used to clamp the final parallelism (final parallelism will be adjusted to <code>[1, maxParallelism]</code>)
     * @param parallelismMultiplier The parallelism multiplier (refer to the parallelism formula)
     * @param parallelismOffset The parallelism offset (refer to the parallelism formula)
     * @param asyncMode Which is the <code>asyncMode</code> parameter inside {@link ForkJoinPool#ForkJoinPool(int, ForkJoinPool.ForkJoinWorkerThreadFactory, Thread.UncaughtExceptionHandler, boolean)}
     * @param outParallelism If the input was non-null and the array length equals <code>1</code>, then the calculated parallelism will be outputted
     * @param errorLogger The optional logger used in the {@link Thread.UncaughtExceptionHandler}
     * @return ForkJoinPool
     */
    @NonNull
    public static ForkJoinPool newWorkStealingPool(
            @NonNull String name,
            int maxParallelism,
            float parallelismMultiplier,
            int parallelismOffset,
            boolean asyncMode,
            int @Nullable [] outParallelism,
            @Nullable Logger errorLogger) {

        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(maxParallelism >= 1,
                "Argument \"maxParallelism\" must be no smaller than 1.");
        if (outParallelism != null) {
            Preconditions.checkArgument(outParallelism.length == 1,
                    "Argument \"outParallelism\"'s length must be 1 when it's non-null.");
        }

        int cores = Runtime.getRuntime().availableProcessors();
        int parallelism = Math.max(1, Math.min(maxParallelism, (int)(cores * parallelismMultiplier) + parallelismOffset));

        if (outParallelism != null) {
            outParallelism[0] = parallelism;
        }

        AtomicInteger counter = new AtomicInteger(1);

        return new ForkJoinPool(
                parallelism,
                pool -> {
                    ForkJoinWorkerThread t = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    t.setName(name + "-worker-" + counter.getAndIncrement());
                    return t;
                },
                errorLogger == null ? null : (thread, throwable) -> {
                    errorLogger.error("[" + name + "] Unhandled exception in " + thread.getName(), throwable);
                },
                asyncMode);
    }

    /**
     * @param timeout In second
     */
    public static void shutdownPool(@NonNull ForkJoinPool pool, long timeout) {
        Preconditions.checkNotNull(pool);

        pool.shutdown();

        try {
            if (!pool.awaitTermination(timeout, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return If the current thread is a {@link ForkJoinWorkerThread}
     */
    public static boolean isForkJoinWorkerThread() {
        return Thread.currentThread() instanceof ForkJoinWorkerThread;
    }

    /**
     * It only performs {@link ForkJoinPool#managedBlock(ForkJoinPool.ManagedBlocker)} for ForkJoin workers.
     * If the current thread isn't a ForkJoin worker,
     * then <code>queue.take()</code> will be returned directly.
     */
    @NonNull
    public static <T> T managedTake(@NonNull BlockingQueue<T> queue) {
        Preconditions.checkNotNull(queue);

        if (!isForkJoinWorkerThread()) {
            try {
                return queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for blocking queue.", e);
            }
        }

        class TakeBlocker implements ForkJoinPool.ManagedBlocker {

            private T value;

            @Override
            public boolean isReleasable() {
                return value != null || (value = queue.poll()) != null;
            }

            @Override
            public boolean block() throws InterruptedException {
                if (value == null) {
                    value = queue.take();
                }
                return true;
            }
        }

        TakeBlocker blocker = new TakeBlocker();

        try {
            ForkJoinPool.managedBlock(blocker);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for blocking queue.", e);
        }

        return blocker.value;
    }
}
