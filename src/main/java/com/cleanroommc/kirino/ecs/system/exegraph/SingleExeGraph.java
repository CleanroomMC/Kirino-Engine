package com.cleanroommc.kirino.ecs.system.exegraph;

import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SingleExeGraph<TSys extends CleanSystem> implements ISystemExeGraph {
    private final TSys system;

    public TSys getSystem() {
        return system;
    }

    private SingleExeGraph(TSys system) {
        this.system = system;
    }

    public static <T extends CleanSystem> Builder<T> newBuilder(Class<T> clazz) {
        return new Builder<>(clazz);
    }

    public static class Builder<TSys extends CleanSystem> implements IBuilder {
        private final Class<TSys> clazz;
        private final Map<String, Runnable> nodes = new HashMap<>();
        private TSys system = null;

        Builder(Class<TSys> clazz) {
            this.clazz = clazz;
        }

        @Override
        public @NonNull IBuilder addBarrierNode(@NonNull String nodeID, @Nullable Runnable callback) {
            Preconditions.checkNotNull(nodeID);
            Preconditions.checkArgument(!nodeID.equals("START"),
                    "Must not use \"START\" as the node ID. \"START\" is a built-in node.");
            Preconditions.checkArgument(!nodeID.equals("END"),
                    "Must not use \"END\" as the node ID. \"END\" is a built-in node.");

            nodes.put(nodeID, callback);
            return this;
        }

        @Override
        public @NonNull IBuilder addDummyTransition(@NonNull String fromNodeID, @NonNull String toNodeID) {
            Preconditions.checkNotNull(fromNodeID);
            Preconditions.checkNotNull(toNodeID);

            return this;
        }

        @Override
        public @NonNull IBuilder addTransition(@NonNull CleanSystem task, @NonNull String fromNodeID, @NonNull String toNodeID) {
            Preconditions.checkNotNull(task);
            Preconditions.checkNotNull(fromNodeID);
            Preconditions.checkNotNull(toNodeID);
            Preconditions.checkState(system == null,
                    "SingleExeGraph must only contain one ecs-system instance.");
            Preconditions.checkArgument(task.getClass() == clazz,
                    "The type of the argument \"task\" must match %s.", clazz.getName());

            return this;
        }

        @Override
        public @NonNull IBuilder setStartCallback(@Nullable Runnable callback) {

            return this;
        }

        @Override
        public @NonNull IBuilder setEndCallback(@Nullable Runnable callback) {

            return this;
        }

        @Override
        public @NonNull ISystemExeGraph build() {
            Preconditions.checkState(system != null,
                    "Must call \"addTransition\" to set a system first.");

            return new SingleExeGraph<>(system);
        }
    }

    @Override
    public void execute() {

    }

    @Override
    public boolean isExecuting() {
        return false;
    }
}
