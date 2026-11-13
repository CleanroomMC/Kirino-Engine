package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class RuntimeLifecycle {
    private static final ThreadLocal<Lease> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<List<Lease>> ANCESTORS = new ThreadLocal<>();
    private final EntityManager manager;
    private final List<Lease> active = new ObjectArrayList<>();
    private CleanECSRuntime.State state = CleanECSRuntime.State.RUNNING;
    private long epoch;
    private Stop stop;
    private Runnable clearScratch = () -> {
    };

    RuntimeLifecycle(EntityManager manager) {
        this.manager = manager;
    }

    void setClearScratch(Runnable action) {
        synchronized (manager) {
            clearScratch = action;
        }
    }

    long epoch() {
        synchronized (manager) {
            return epoch;
        }
    }

    CleanECSRuntime.State state() {
        synchronized (manager) {
            return state;
        }
    }

    void requireRunning() {
        Preconditions.checkState(state == CleanECSRuntime.State.RUNNING, "The ECS runtime is not running.");
    }

    Lease acquire(long expectedEpoch) {
        synchronized (manager) {
            requireRunning();
            Preconditions.checkState(expectedEpoch == epoch, "This execution belongs to an expired runtime epoch.");
            Lease lease = new Lease();
            active.add(lease);
            return lease;
        }
    }

    void checkEpoch(long expectedEpoch) {
        synchronized (manager) {
            requireRunning();
            Preconditions.checkState(expectedEpoch == epoch, "This execution belongs to an expired runtime epoch.");
        }
    }

    CompletableFuture<Void> quiesce() {
        return new ObservedCompletion(beginStop().completion);
    }

    private Stop beginStop() {
        Stop result;
        boolean complete;
        synchronized (manager) {
            manager.requireLifecycleAccess();
            if (stop != null) {
                return stop;
            }
            state = CleanECSRuntime.State.QUIESCING;
            stop = result = new Stop();
            complete = active.isEmpty();
            if (complete) {
                state = CleanECSRuntime.State.QUIESCENT;
            }
        }
        if (complete) {
            result.completion.complete(null);
        }
        return result;
    }

    void finish(boolean reset) {
        Lease current = CURRENT.get();
        Preconditions.checkState(current == null || current.owner() != this,
            "Cannot wait for runtime shutdown from its own execution.");
        List<Lease> ancestors = ANCESTORS.get();
        if (ancestors != null) {
            for (Lease ancestor : ancestors) {
                Preconditions.checkState(ancestor.owner() != this,
                    "Cannot wait for runtime shutdown from a nested execution.");
            }
        }
        Stop expected = beginStop();
        expected.completion.handle((unused, failure) -> null).join();
        Throwable failure;
        synchronized (manager) {
            manager.requireLifecycleAccess();
            Preconditions.checkState(stop == expected, "The runtime lifecycle changed while waiting for shutdown.");
            if (!expected.cleaned) {
                boolean canReset = reset && epoch != Long.MAX_VALUE;
                Throwable cleanupFailure = manager.clearEntities(canReset);
                if (reset && !canReset) {
                    cleanupFailure = ExecutionFailures.merge(cleanupFailure,
                        new IllegalStateException("The runtime epoch is exhausted."));
                }
                try {
                    clearScratch.run();
                } catch (Throwable scratchFailure) {
                    cleanupFailure = ExecutionFailures.merge(cleanupFailure, scratchFailure);
                }
                expected.failure = ExecutionFailures.merge(expected.failure, cleanupFailure);
                expected.cleaned = true;
                state = CleanECSRuntime.State.CLOSED;
                if (canReset && cleanupFailure == null) {
                    epoch++;
                    stop = null;
                    state = CleanECSRuntime.State.RUNNING;
                }
            } else {
                Preconditions.checkState(!reset, "A closed runtime cannot be reset.");
            }
            failure = expected.failure;
        }
        if (failure != null) {
            ExecutionFailures.rethrow(failure);
        }
    }

    private void retired(Lease lease) {
        Stop completed = null;
        synchronized (manager) {
            active.remove(lease);
            if (stop != null && !stop.cleaned) {
                stop.failure = ExecutionFailures.mergeEnclosing(stop.failure, lease.failure);
                if (active.isEmpty()) {
                    state = CleanECSRuntime.State.QUIESCENT;
                    completed = stop;
                }
            }
        }
        if (completed != null) {
            if (completed.failure == null) {
                completed.completion.complete(null);
            } else {
                completed.completion.completeExceptionally(completed.failure);
            }
        }
    }

    final class Lease {
        final CompletableFuture<Void> completion = new CompletableFuture<>();
        private boolean prepared;
        private boolean finished;
        private boolean followed;
        private boolean retired;
        private Throwable failure;

        RuntimeLifecycle owner() {
            return RuntimeLifecycle.this;
        }

        Lease enter() {
            Lease previous = CURRENT.get();
            if (previous != null) {
                List<Lease> ancestors = ANCESTORS.get();
                if (ancestors == null) {
                    ancestors = new ArrayList<>();
                    ANCESTORS.set(ancestors);
                }
                ancestors.add(previous);
            }
            CURRENT.set(this);
            return previous;
        }

        void leave(Lease previous) {
            if (previous == null) {
                CURRENT.remove();
            } else {
                List<Lease> ancestors = ANCESTORS.get();
                ancestors.removeLast();
                if (ancestors.isEmpty()) {
                    ANCESTORS.remove();
                }
                CURRENT.set(previous);
            }
        }

        void run(Runnable action) {
            Lease previous = enter();
            try {
                action.run();
            } finally {
                leave(previous);
            }
        }

        void follow(CompletableFuture<Void> actual) {
            synchronized (manager) {
                followed = true;
            }
            actual.whenComplete((unused, error) -> {
                synchronized (manager) {
                    failure = ExecutionFailures.merge(failure, error);
                    finished = true;
                }
                retireIfReady();
            });
        }

        void prepared(Throwable error) {
            synchronized (manager) {
                prepared = true;
                failure = ExecutionFailures.merge(failure, error);
                if (!followed) {
                    finished = true;
                }
            }
            retireIfReady();
        }

        private void retireIfReady() {
            synchronized (manager) {
                if (!prepared || !finished || retired) {
                    return;
                }
                retired = true;
            }
            RuntimeLifecycle.this.retired(this);
            if (failure == null) {
                completion.complete(null);
            } else {
                completion.completeExceptionally(failure);
            }
        }
    }

    private static final class Stop {
        final CompletableFuture<Void> completion = new CompletableFuture<>();
        Throwable failure;
        boolean cleaned;
    }
}
