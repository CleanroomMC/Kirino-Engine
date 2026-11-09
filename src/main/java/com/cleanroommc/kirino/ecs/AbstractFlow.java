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
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph.*;

abstract class AbstractFlow implements SystemExeFlowGraph {

    static final class BarrierNode {
        final String id;
        final List<Transition> outgoing = new ObjectArrayList<>();
        final List<Transition> incoming = new ObjectArrayList<>();

        @Nullable Runnable callback;
        int inDegree = 0;

        BarrierNode(String id, @Nullable Runnable callback) {
            this.id = id;
            this.callback = callback;
        }
    }

    static final class Transition {
        final @Nullable CleanSystem system; // null = dummy
        final BarrierNode from;
        final BarrierNode to;

        Transition(@Nullable CleanSystem system, @NonNull BarrierNode from, @NonNull BarrierNode to) {
            this.system = system;
            this.from = from;
            this.to = to;
        }
    }

    private final @Nullable Runnable finishCallback;
    private final CleanWorld world;
    private final List<BarrierNode> topo;
    private final AtomicBoolean executing = new AtomicBoolean(false);

    AbstractFlow(@NonNull CleanWorld world, @NonNull List<@NonNull BarrierNode> topo, @Nullable Runnable finishCallback) {
        this.finishCallback = finishCallback;
        this.world = world;
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
        if (!executing.compareAndSet(false, true)) {
            throw new IllegalStateException("Must not execute while executing!");
        }

        try {
            for (BarrierNode node : topo) {
                for (Transition edge : node.incoming) {
                    if (edge.system != null) {
                        joinSystem(edge.system);
                    }
                }

                if (node.callback != null) {
                    node.callback.run();
                }

                for (Transition edge : node.outgoing) {
                    if (edge.system != null) {
                        executeSystem(world, edge.system);
                    }
                }
            }
        } finally {
            try {
                if (finishCallback != null) {
                    finishCallback.run();
                }
            } finally {
                executing.set(false);
            }
        }
    }

    @NonNull
    @Override
    public CompletableFuture<Void> executeAsync(Executor executor) {
        if (!executing.compareAndSet(false, true)) {
            throw new IllegalStateException("Must not execute while executing!");
        }

        try {
            return CompletableFuture.runAsync(() -> {
                try {
                    for (BarrierNode node : topo) {
                        for (Transition edge : node.incoming) {
                            if (edge.system != null) {
                                joinSystem(edge.system);
                            }
                        }

                        if (node.callback != null) {
                            node.callback.run();
                        }

                        for (Transition edge : node.outgoing) {
                            if (edge.system != null) {
                                executeSystem(world, edge.system);
                            }
                        }
                    }
                } finally {
                    try {
                        if (finishCallback != null) {
                            finishCallback.run();
                        }
                    } finally {
                        executing.set(false);
                    }
                }
            }, executor);
        } catch (Throwable t) {
            executing.set(false);
            throw t;
        }
    }

    @Override
    public boolean isExecuting() {
        return executing.get();
    }

    private static final class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                ReflectionUtils.getFieldGetter(CleanSystem.class, "execution", ExecutionContainer.class),
                ReflectionUtils.getFieldGetter(ExecutionContainer.class, "handles", List.class),
                ReflectionUtils.getFieldGetter(ExecutionContainer.class, "futures", List.class),
                ReflectionUtils.getFieldGetter(CleanWorld.class, "entityManager", EntityManager.class),
                ReflectionUtils.getFieldGetter(CleanWorld.class, "jobScheduler", JobScheduler.class));

            Preconditions.checkNotNull(DELEGATE.executionContainerGetter);
            Preconditions.checkNotNull(DELEGATE.handlesGetter);
            Preconditions.checkNotNull(DELEGATE.futuresGetter);
            Preconditions.checkNotNull(DELEGATE.entityManagerGetter);
            Preconditions.checkNotNull(DELEGATE.jobSchedulerGetter);
        }

        static ExecutionContainer getExecutionContainer(CleanSystem cleanSystem) {
            ExecutionContainer result;
            try {
                result = (ExecutionContainer) DELEGATE.executionContainerGetter.invokeExact(cleanSystem);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        static List<JobScheduler.ExecutionHandle> getHandles(ExecutionContainer executionContainer) {
            List<JobScheduler.ExecutionHandle> result;
            try {
                result = (List<JobScheduler.ExecutionHandle>) DELEGATE.handlesGetter.invokeExact(executionContainer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        static List<CompletableFuture<Void>> getFutures(ExecutionContainer executionContainer) {
            List<CompletableFuture<Void>> result;
            try {
                result = (List<CompletableFuture<Void>>) DELEGATE.futuresGetter.invokeExact(executionContainer);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static EntityManager getEntityManager(CleanWorld cleanWorld) {
            EntityManager result;
            try {
                result = (EntityManager) DELEGATE.entityManagerGetter.invokeExact(cleanWorld);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static JobScheduler getJobScheduler(CleanWorld cleanWorld) {
            JobScheduler result;
            try {
                result = (JobScheduler) DELEGATE.jobSchedulerGetter.invokeExact(cleanWorld);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        record Delegate(
            MethodHandle executionContainerGetter,
            MethodHandle handlesGetter,
            MethodHandle futuresGetter,
            MethodHandle entityManagerGetter,
            MethodHandle jobSchedulerGetter) {
        }
    }

    private static void joinSystem(CleanSystem system) {
        ExecutionContainer executionContainer = MethodHolder.getExecutionContainer(system);
        List<JobScheduler.ExecutionHandle> handles = MethodHolder.getHandles(executionContainer);
        List<CompletableFuture<Void>> futures = MethodHolder.getFutures(executionContainer);

        List<CompletableFuture<Void>> allFutures = new ObjectArrayList<>();
        for (JobScheduler.ExecutionHandle handle : handles) {
            if (handle.async()) {
                allFutures.add(handle.future());
            }
        }
        allFutures.addAll(futures);

        CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0])).join();
    }

    private static void executeSystem(CleanWorld world, CleanSystem system) {
        EntityManager entityManager = MethodHolder.getEntityManager(world);
        JobScheduler jobScheduler = MethodHolder.getJobScheduler(world);
        ExecutionContainer executionContainer = MethodHolder.getExecutionContainer(system);

        executionContainer.noExecutions();
        system.update(entityManager, jobScheduler);
    }
}
