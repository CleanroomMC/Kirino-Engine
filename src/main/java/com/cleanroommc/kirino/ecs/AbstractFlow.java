package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.ExecutionContainer;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

abstract class AbstractFlow implements SystemExeFlowGraph {
    static final class BarrierNode {
        final String id;
        final List<Transition> outgoing = new ObjectArrayList<>();
        final List<Transition> incoming = new ObjectArrayList<>();
        @Nullable Runnable callback;
        int inDegree;

        BarrierNode(String id, @Nullable Runnable callback) {
            this.id = id;
            this.callback = callback;
        }
    }

    static final class Transition {
        final @Nullable CleanSystem system;
        final BarrierNode from;
        final BarrierNode to;

        Transition(@Nullable CleanSystem system, BarrierNode from, BarrierNode to) {
            this.system = system;
            this.from = from;
            this.to = to;
        }
    }

    private final @Nullable Runnable finishCallback;
    private final @Nullable Consumer<Throwable> failureCallback;
    private final CleanWorld world;
    private final List<BarrierNode> topo;
    private final RuntimeLifecycle lifecycle;
    private final long epoch;
    private final AtomicBoolean executing = new AtomicBoolean();

    AbstractFlow(@NonNull CleanWorld world, @NonNull List<@NonNull BarrierNode> topo,
                 @Nullable Runnable finishCallback, @Nullable Consumer<Throwable> failureCallback) {
        this.finishCallback = finishCallback;
        this.failureCallback = failureCallback;
        this.world = world;
        EntityManager manager = MethodHolder.entityManager(world);
        lifecycle = manager instanceof com.cleanroommc.kirino.ecs.EntityManager owned ? owned.lifecycle : null;
        epoch = lifecycle == null ? 0 : lifecycle.epoch();
        Map<BarrierNode, BarrierNode> copies = new Reference2ObjectOpenHashMap<>();
        List<BarrierNode> snapshot = new ObjectArrayList<>(topo.size());
        for (BarrierNode node : topo) {
            BarrierNode copy = new BarrierNode(node.id, node.callback);
            copies.put(node, copy);
            snapshot.add(copy);
        }
        for (BarrierNode node : topo) {
            for (Transition edge : node.outgoing) {
                BarrierNode from = copies.get(edge.from);
                BarrierNode to = copies.get(edge.to);
                Transition copy = new Transition(edge.system, from, to);
                from.outgoing.add(copy);
                to.incoming.add(copy);
            }
        }
        this.topo = snapshot;
    }

    @Override
    public void execute() {
        Run run = start();
        Throwable failure = null;
        try {
            for (BarrierNode node : topo) {
                run.dependencies(node).join();
                run.visit(node);
            }
        } catch (Throwable executionFailure) {
            failure = ExecutionFailures.unwrap(executionFailure);
        }
        run.drain().handle((unused, drainFailure) -> null).join();
        run.complete(failure);
        try {
            run.completion.join();
        } catch (Throwable completionFailure) {
            ExecutionFailures.rethrow(ExecutionFailures.unwrap(completionFailure));
        }
    }

    @Override
    public CompletableFuture<Void> executeAsync(Executor executor) {
        Preconditions.checkNotNull(executor);
        Run run = start();
        try {
            executor.execute(() -> run.executeAsync(executor));
        } catch (Throwable submissionFailure) {
            executing.set(false);
            run.completion.completeExceptionally(submissionFailure);
            if (run.lease != null) {
                run.lease.prepared(null);
            }
            ExecutionFailures.rethrow(submissionFailure);
        }
        return new ObservedCompletion(run.lease == null ? run.completion : run.lease.completion);
    }

    private Run start() {
        Preconditions.checkState(executing.compareAndSet(false, true),
            "Must not execute while the previous flow execution is in progress.");
        try {
            return new Run();
        } catch (Throwable failure) {
            executing.set(false);
            throw failure;
        }
    }

    @Override
    public boolean isExecuting() {
        return executing.get();
    }

    private final class Run {
        private final Map<CleanSystem, SystemExecution> systems = new Reference2ObjectOpenHashMap<>();
        private final List<SystemExecution> acquired = new ObjectArrayList<>();
        private final CompletableFuture<Void> completion = new CompletableFuture<>();
        private final RuntimeLifecycle.Lease lease;

        Run() {
            lease = lifecycle == null ? null : lifecycle.acquire(epoch);
            if (lease != null) {
                lease.follow(completion);
            }
        }

        CompletableFuture<Void> dependencies(BarrierNode node) {
            List<CompletableFuture<Void>> pending = new ObjectArrayList<>();
            for (Transition edge : node.incoming) {
                if (edge.system != null) {
                    SystemExecution execution = systems.get(edge.system);
                    Preconditions.checkState(execution != null, "The incoming system has not been started.");
                    pending.add(execution.completion());
                }
            }
            return CompletableFuture.allOf(pending.toArray(CompletableFuture[]::new));
        }

        void visit(BarrierNode node) {
            if (lifecycle != null) {
                lifecycle.checkEpoch(epoch);
            }
            if (lease == null) {
                visitOwned(node);
            } else {
                lease.run(() -> visitOwned(node));
            }
        }

        void visitOwned(BarrierNode node) {
            if (node.callback != null) {
                node.callback.run();
            }
            for (Transition edge : node.outgoing) {
                if (edge.system != null) {
                    SystemExecution execution = MethodHolder.execution(edge.system);
                    execution.acquire();
                    acquired.add(execution);
                    systems.put(edge.system, execution);
                    execution.update(edge.system, MethodHolder.entityManager(world), MethodHolder.jobScheduler(world));
                }
            }
        }

        CompletableFuture<Void> drain() {
            CompletableFuture<?>[] pending = new CompletableFuture<?>[acquired.size()];
            for (int i = 0; i < pending.length; i++) {
                pending[i] = acquired.get(i).completion();
            }
            return CompletableFuture.allOf(pending);
        }

        void executeAsync(Executor executor) {
            try {
                CompletableFuture<Void> pipeline = CompletableFuture.completedFuture(null);
                for (BarrierNode node : topo) {
                    pipeline = pipeline.thenCompose(unused -> dependencies(node))
                        .thenRunAsync(() -> visit(node), executor);
                }
                pipeline.whenComplete((unused, failure) -> finishAsync(failure));
            } catch (Throwable failure) {
                finishAsync(failure);
            }
        }

        void finishAsync(Throwable failure) {
            drain().whenComplete((unused, drainFailure) -> complete(ExecutionFailures.unwrap(failure)));
        }

        void complete(Throwable failure) {
            if (lease == null) {
                completeOwned(failure);
            } else {
                lease.run(() -> completeOwned(failure));
                lease.prepared(null);
            }
        }

        void completeOwned(Throwable failure) {
            for (SystemExecution execution : acquired) {
                try {
                    execution.completion().join();
                } catch (Throwable taskFailure) {
                    failure = ExecutionFailures.merge(failure, taskFailure);
                }
            }
            if (failure == null && lifecycle != null) {
                try {
                    lifecycle.checkEpoch(epoch);
                } catch (Throwable stoppingFailure) {
                    failure = stoppingFailure;
                }
            }
            if (failure == null && finishCallback != null) {
                try {
                    finishCallback.run();
                } catch (Throwable callbackFailure) {
                    failure = callbackFailure;
                }
            }
            if (failure != null && failureCallback != null) {
                try {
                    failureCallback.accept(failure);
                } catch (Throwable callbackFailure) {
                    failure = ExecutionFailures.merge(failure, callbackFailure);
                }
            }
            for (SystemExecution execution : acquired) {
                try {
                    execution.release();
                } catch (Throwable releaseFailure) {
                    failure = ExecutionFailures.merge(failure, releaseFailure);
                }
            }
            executing.set(false);
            if (failure == null) {
                completion.complete(null);
            } else {
                completion.completeExceptionally(failure);
            }
        }
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE = new Delegate(
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(CleanSystem.class, "execution", ExecutionContainer.class)),
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(ExecutionContainer.class, "state", Object.class)),
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(CleanWorld.class, "entityManager", EntityManager.class)),
            Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(CleanWorld.class, "jobScheduler", JobScheduler.class)));

        static SystemExecution execution(CleanSystem system) {
            try {
                ExecutionContainer container = (ExecutionContainer) DELEGATE.container.invokeExact(system);
                return (SystemExecution) (Object) DELEGATE.state.invokeExact(container);
            } catch (Throwable failure) {
                ExecutionFailures.rethrow(failure);
                throw new AssertionError();
            }
        }

        static EntityManager entityManager(CleanWorld world) {
            try {
                return (EntityManager) DELEGATE.manager.invokeExact(world);
            } catch (Throwable failure) {
                ExecutionFailures.rethrow(failure);
                throw new AssertionError();
            }
        }

        static JobScheduler jobScheduler(CleanWorld world) {
            try {
                return (JobScheduler) DELEGATE.scheduler.invokeExact(world);
            } catch (Throwable failure) {
                ExecutionFailures.rethrow(failure);
                throw new AssertionError();
            }
        }

        private record Delegate(MethodHandle container, MethodHandle state, MethodHandle manager, MethodHandle scheduler) {
        }
    }
}
