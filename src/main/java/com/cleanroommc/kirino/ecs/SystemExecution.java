package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler.ExecutionHandle;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class SystemExecution {
    private static final ThreadLocal<SystemExecution> CURRENT = new ThreadLocal<>();

    private final List<CompletableFuture<Void>> tasks = new ObjectArrayList<>();
    private boolean active;
    private Thread updateThread;
    private CompletableFuture<Void> completion;

    static Object create() {
        return new SystemExecution();
    }

    static void register(Object state, CompletableFuture<Void>[] futures, ExecutionHandle[] handles) {
        ((SystemExecution) state).register(futures, handles);
    }

    static void track(CompletableFuture<Void> completion) {
        SystemExecution current = CURRENT.get();
        if (current != null) {
            current.add(completion);
        }
    }

    synchronized void acquire() {
        Preconditions.checkState(!active, "The system is already executing in another flow.");
        for (CompletableFuture<Void> task : tasks) {
            Preconditions.checkState(task.isDone(), "The system still has manually registered work in progress.");
        }
        tasks.clear();
        completion = null;
        active = true;
    }

    void update(CleanSystem system, EntityManager manager, JobScheduler scheduler) {
        synchronized (this) {
            updateThread = Thread.currentThread();
        }
        SystemExecution previous = CURRENT.get();
        CURRENT.set(this);
        try {
            system.update(manager, scheduler);
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
            synchronized (this) {
                updateThread = null;
            }
        }
    }

    private synchronized void register(CompletableFuture<Void>[] futures, ExecutionHandle[] handles) {
        Preconditions.checkState(!active || updateThread == Thread.currentThread(),
            "Executions may only be registered during the owning system update.");
        if (futures != null) {
            for (CompletableFuture<Void> future : futures) {
                Preconditions.checkNotNull(future);
            }
        }
        if (handles != null) {
            for (ExecutionHandle handle : handles) {
                Preconditions.checkNotNull(handle);
                Preconditions.checkNotNull(handle.future());
            }
        }
        if (!active) {
            tasks.clear();
            completion = null;
        }
        if (futures != null) {
            for (CompletableFuture<Void> future : futures) {
                add(ObservedCompletion.actual(future));
            }
        }
        if (handles != null) {
            for (ExecutionHandle handle : handles) {
                add(ObservedCompletion.actual(handle.future()));
            }
        }
    }

    private synchronized void add(CompletableFuture<Void> task) {
        for (CompletableFuture<Void> existing : tasks) {
            if (existing == task) {
                return;
            }
        }
        tasks.add(task);
    }

    synchronized CompletableFuture<Void> completion() {
        Preconditions.checkState(updateThread == null, "The system update has not finished registering work.");
        if (completion == null) {
            CompletableFuture<?>[] snapshot = tasks.toArray(CompletableFuture[]::new);
            completion = CompletableFuture.allOf(snapshot).handle((unused, failure) -> {
                Throwable combined = null;
                for (CompletableFuture<?> task : snapshot) {
                    try {
                        task.join();
                    } catch (Throwable taskFailure) {
                        combined = ExecutionFailures.merge(combined, taskFailure);
                    }
                }
                if (combined != null) {
                    ExecutionFailures.rethrow(combined);
                }
                return null;
            });
        }
        return completion;
    }

    synchronized void release() {
        tasks.clear();
        completion = null;
        active = false;
    }
}
