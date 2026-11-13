package com.cleanroommc.kirino.ecs.api.system.exegraph;

import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class SingleFlow<TSys extends CleanSystem> implements SystemExeFlowGraph {
    private final TSys system;
    private final SystemExeFlowGraph execution;

    private SingleFlow(TSys system, SystemExeFlowGraph execution) {
        this.system = Preconditions.checkNotNull(system);
        this.execution = Preconditions.checkNotNull(execution);
    }

    public TSys getSystem() {
        return system;
    }

    @Override
    public void execute() {
        execution.execute();
    }

    @Override
    public CompletableFuture<Void> executeAsync(Executor executor) {
        return execution.executeAsync(executor);
    }

    @Override
    public boolean isExecuting() {
        return execution.isExecuting();
    }

    public static <T extends CleanSystem> Builder<SingleFlow<T>> newBuilder(CleanWorld world, Class<T> clazz) {
        return Preconditions.checkNotNull(world).newSingleFlowBuilder(Preconditions.checkNotNull(clazz));
    }
}
