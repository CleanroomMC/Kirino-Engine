package com.cleanroommc.kirino.ecs.system.exegraph;

import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SingleFlow<TSys extends CleanSystem> extends AbstractFlow {

    //<editor-fold desc="builder">
    public static class BuilderImpl<TSys extends CleanSystem> implements Builder<SingleFlow<TSys>> {
        private final CleanWorld world;
        private final Class<TSys> clazz;
        private final Map<String, BarrierNode> nodes = new HashMap<>();
        private final List<Transition> edges = new ArrayList<>();

        private TSys system = null;
        private @Nullable Runnable finishCallback;

        BuilderImpl(CleanWorld world, Class<TSys> clazz) {
            this.world = world;
            this.clazz = clazz;
            nodes.put(START_NODE, new BarrierNode(START_NODE, null));
            nodes.put(END_NODE, new BarrierNode(END_NODE, null));
        }

        @Override
        public @NonNull Builder<SingleFlow<TSys>> addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback) {
            Preconditions.checkNotNull(nodeID);
            Preconditions.checkArgument(!nodeID.equals(START_NODE),
                    "Must not use \"%s\" as the node ID. \"%s\" is a built-in node.", START_NODE, START_NODE);
            Preconditions.checkArgument(!nodeID.equals(END_NODE),
                    "Must not use \"%s\" as the node ID. \"%s\" is a built-in node.", END_NODE, END_NODE);

            nodes.put(nodeID, new BarrierNode(nodeID, callback));
            return this;
        }

        @Override
        public @NonNull Builder<SingleFlow<TSys>> addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID) {
            Preconditions.checkNotNull(fromNodeID);
            Preconditions.checkNotNull(toNodeID);

            return addTransitionInternal(null, fromNodeID, toNodeID);
        }

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

            system = (TSys) task;
            return addTransitionInternal(system, fromNodeID, toNodeID);
        }

        private @NonNull Builder<SingleFlow<TSys>> addTransitionInternal(@Nullable CleanSystem task, String fromNodeID, String toNodeID) {
            BarrierNode from = nodes.get(fromNodeID);
            BarrierNode to = nodes.get(toNodeID);
            Preconditions.checkNotNull(from, "From node not found: \"%s\".", fromNodeID);
            Preconditions.checkNotNull(to, "To node not found: \"%s\".", toNodeID);

            edges.add(new Transition(task, from, to));
            return this;
        }

        @Override
        public @NonNull Builder<SingleFlow<TSys>> setStartNodeCallback(@Nullable Runnable callback) {
            nodes.get(START_NODE).callback = callback;
            return this;
        }

        @Override
        public @NonNull Builder<SingleFlow<TSys>> setEndNodeCallback(@Nullable Runnable callback) {
            nodes.get(END_NODE).callback = callback;
            return this;
        }

        @Override
        public @NonNull Builder<SingleFlow<TSys>> setFinishCallback(@Nullable Runnable callback) {
            finishCallback = callback;
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

            Deque<BarrierNode> queue = new ArrayDeque<>();
            List<BarrierNode> topo = new ArrayList<>();

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

            BarrierNode start = nodes.get(START_NODE);
            BarrierNode end = nodes.get(END_NODE);

            Preconditions.checkState(start.inDegree == 0,
                    "START node must have in degree = 0.");

            Preconditions.checkState(end.outgoing.isEmpty(),
                    "END node must not have outgoing edges.");

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

            return new SingleFlow<>(world, system, topo, finishCallback);
        }
    }
    //</editor-fold>

    private final TSys system;

    public TSys getSystem() {
        return system;
    }

    private SingleFlow(@NonNull CleanWorld world, @NonNull TSys system, @NonNull List<@NonNull BarrierNode> topo, @Nullable Runnable finishCallback) {
        super(world, topo, finishCallback);
        this.system = system;
    }

    public static <T extends CleanSystem> BuilderImpl<T> newBuilder(CleanWorld world, Class<T> clazz) {
        return new BuilderImpl<>(world, clazz);
    }
}
