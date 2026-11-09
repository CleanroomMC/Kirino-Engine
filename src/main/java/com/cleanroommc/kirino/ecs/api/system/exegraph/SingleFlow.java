package com.cleanroommc.kirino.ecs.api.system.exegraph;

import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
        return MethodHolder.newBuilder(Preconditions.checkNotNull(world), Preconditions.checkNotNull(clazz));
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            try {
                Class<?> implementation = Class.forName("com.cleanroommc.kirino.ecs.SingleFlowExecutor");
                DELEGATE = new Delegate(Preconditions.checkNotNull(ReflectionUtils.getMethod(implementation,
                    "newBuilder", Builder.class, CleanWorld.class, Class.class)));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T extends CleanSystem> Builder<SingleFlow<T>> newBuilder(CleanWorld world, Class<T> type) {
            try {
                return (Builder<SingleFlow<T>>) DELEGATE.factory.invokeExact(world, (Class) type);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle factory) {
        }
    }
}
