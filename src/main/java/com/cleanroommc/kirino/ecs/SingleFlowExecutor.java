package com.cleanroommc.kirino.ecs;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph.Builder;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph.*;

final class SingleFlowExecutor<TSys extends CleanSystem> extends AbstractFlow {

    //<editor-fold desc="builder">
    private static final class BuilderImpl<TSys extends CleanSystem> implements Builder<SingleFlow<TSys>> {
        private final EntityManager entityManager;
        private final JobScheduler jobScheduler;
        private final Function<CleanSystem, Object> executionState;
        private final Class<TSys> clazz;
        private final Map<String, BarrierNode> nodes = new Object2ObjectOpenHashMap<>();
        private final List<Transition> edges = new ObjectArrayList<>();

        private TSys system = null;
        private @Nullable Runnable finishCallback;
        private @Nullable Consumer<Throwable> failureCallback;

        BuilderImpl(EntityManager entityManager, JobScheduler jobScheduler, Class<TSys> clazz,
                    Function<CleanSystem, Object> executionState) {
            this.entityManager = entityManager;
            this.jobScheduler = jobScheduler;
            this.executionState = executionState;
            this.clazz = clazz;
            nodes.put(START_NODE, new BarrierNode(START_NODE, null));
            nodes.put(END_NODE, new BarrierNode(END_NODE, null));
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback) {
            Preconditions.checkNotNull(nodeID);
            Preconditions.checkArgument(!nodeID.equals(START_NODE),
                "Must not use \"%s\" as the node ID. \"%s\" is a built-in node.", START_NODE, START_NODE);
            Preconditions.checkArgument(!nodeID.equals(END_NODE),
                "Must not use \"%s\" as the node ID. \"%s\" is a built-in node.", END_NODE, END_NODE);

            BarrierNode existing = nodes.get(nodeID);
            if (existing == null) {
                nodes.put(nodeID, new BarrierNode(nodeID, callback));
            } else {
                existing.callback = callback;
            }
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID) {
            Preconditions.checkNotNull(fromNodeID);
            Preconditions.checkNotNull(toNodeID);

            return addTransitionInternal(null, fromNodeID, toNodeID);
        }

        @CanIgnoreReturnValue
        @SuppressWarnings("unchecked")
        @Override
        public @NonNull Builder<SingleFlow<TSys>> addTransition(@NonNull CleanSystem task, @NonNull String fromNodeID, @NonNull String toNodeID) {
            Preconditions.checkNotNull(task);
            Preconditions.checkNotNull(fromNodeID);
            Preconditions.checkNotNull(toNodeID);
            Preconditions.checkState(system == null,
                "SingleFlow must only contain one ecs-system instance.");
            Preconditions.checkArgument(task.getClass() == clazz,
                "The type of the argument \"task\" must match %s.", clazz.getName());

            addTransitionInternal(task, fromNodeID, toNodeID);
            system = (TSys) task;
            return this;
        }

        @CanIgnoreReturnValue
        private @NonNull Builder<SingleFlow<TSys>> addTransitionInternal(@Nullable CleanSystem task, String fromNodeID, String toNodeID) {
            BarrierNode from = nodes.get(fromNodeID);
            BarrierNode to = nodes.get(toNodeID);
            Preconditions.checkNotNull(from, "From node not found: \"%s\".", fromNodeID);
            Preconditions.checkNotNull(to, "To node not found: \"%s\".", toNodeID);

            SystemExecution execution = task == null ? null : (SystemExecution) executionState.apply(task);
            edges.add(new Transition(task, execution, from, to));
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> setStartNodeCallback(@Nullable Runnable callback) {
            nodes.get(START_NODE).callback = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> setEndNodeCallback(@Nullable Runnable callback) {
            nodes.get(END_NODE).callback = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> setFinishCallback(@Nullable Runnable callback) {
            finishCallback = callback;
            return this;
        }

        @CanIgnoreReturnValue
        @Override
        public @NonNull Builder<SingleFlow<TSys>> setFailureCallback(@Nullable Consumer<Throwable> callback) {
            failureCallback = callback;
            return this;
        }

        @Override
        public @NonNull SingleFlow<TSys> build() {
            Preconditions.checkState(system != null,
                "SingleFlow must contain exactly one ecs-system instance. Call \"addTransition\" to set a system first.");

            for (BarrierNode node : nodes.values()) {
                node.inDegree = 0;
                node.outgoing.clear();
                node.incoming.clear();
            }
            for (Transition edge : edges) {
                edge.from.outgoing.add(edge);
                edge.to.incoming.add(edge);
                edge.to.inDegree++;
            }

            BarrierNode start = nodes.get(START_NODE);
            BarrierNode end = nodes.get(END_NODE);
            Preconditions.checkState(start.inDegree == 0,
                "START node must have in degree = 0.");
            Preconditions.checkState(end.outgoing.isEmpty(),
                "END node must not have outgoing edges.");

            Deque<BarrierNode> queue = new ArrayDeque<>();
            List<BarrierNode> topo = new ObjectArrayList<>();

            for (BarrierNode node : nodes.values()) {
                if (node.inDegree == 0) {
                    queue.add(node);
                }
            }
            while (!queue.isEmpty()) {
                BarrierNode node = queue.poll();
                topo.add(node);
                for (Transition edge : node.outgoing) {
                    if (--edge.to.inDegree == 0) {
                        queue.add(edge.to);
                    }
                }
            }

            Preconditions.checkState(topo.size() == nodes.size(),
                "SingleFlow graph is not a DAG.");

            Set<BarrierNode> reachable = new HashSet<>();
            Deque<BarrierNode> stack = new ArrayDeque<>();
            stack.push(start);

            while (!stack.isEmpty()) {
                BarrierNode node = stack.pop();
                if (!reachable.add(node)) {
                    continue;
                }

                for (Transition edge : node.outgoing) {
                    stack.push(edge.to);
                }
            }

            Preconditions.checkState(reachable.size() == nodes.size(),
                "Some nodes are not reachable from the START node.");

            reachable.clear();
            stack.push(end);
            while (!stack.isEmpty()) {
                BarrierNode node = stack.pop();
                if (!reachable.add(node)) {
                    continue;
                }
                for (Transition edge : node.incoming) {
                    stack.push(edge.from);
                }
            }
            Preconditions.checkState(reachable.size() == nodes.size(),
                "Some nodes cannot reach the END node.");

            return MethodHolder.newFlow(system,
                new SingleFlowExecutor<>(entityManager, jobScheduler, topo, finishCallback, failureCallback));
        }
    }
    //</editor-fold>

    private SingleFlowExecutor(EntityManager entityManager, JobScheduler jobScheduler, @NonNull List<@NonNull BarrierNode> topo,
                               @Nullable Runnable finishCallback, @Nullable Consumer<Throwable> failureCallback) {
        super(entityManager, jobScheduler, topo, finishCallback, failureCallback);
    }

    static <T extends CleanSystem> Builder<SingleFlow<T>> newBuilder(EntityManager entityManager, JobScheduler jobScheduler,
                                                                     Class<T> clazz, Function<CleanSystem, Object> executionState) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(executionState);
        return new BuilderImpl<>(entityManager, jobScheduler, clazz, executionState);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE = new Delegate(Preconditions.checkNotNull(
            ReflectionUtils.getConstructor(SingleFlow.class, CleanSystem.class,
                SystemExeFlowGraph.class)));

        @SuppressWarnings("unchecked")
        private static <T extends CleanSystem> SingleFlow<T> newFlow(T system, AbstractFlow execution) {
            try {
                return (SingleFlow<T>) DELEGATE.constructor.invokeExact((CleanSystem) system,
                    (SystemExeFlowGraph) execution);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle constructor) {
        }
    }
}
