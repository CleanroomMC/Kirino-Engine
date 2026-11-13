package com.cleanroommc.kirino.ecs.api.system;

import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * It's recommended to utilize the implementations of {@link SystemExeFlowGraph} to guide the execution of systems in {@link CleanWorld}.
 *
 * @see CleanWorld
 */
public abstract class CleanSystem {
    protected final ExecutionContainer execution = new ExecutionContainer();

    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);

    public static <T extends CleanSystem> SystemExeFlowGraph.Builder<SingleFlow<T>> newSingleFlowBuilder(
        EntityManager entityManager, JobScheduler jobScheduler, Class<T> type) {
        return MethodHolder.newBuilder(entityManager, jobScheduler, Preconditions.checkNotNull(type));
    }

    private static final class MethodHolder {
        private static final Function<CleanSystem, Object> STATE = system -> system.execution.state();
        private static final MethodHandle FACTORY;

        static {
            try {
                Class<?> implementation = Class.forName("com.cleanroommc.kirino.ecs.SingleFlowExecutor");
                FACTORY = Preconditions.checkNotNull(ReflectionUtils.getMethod(implementation, "newBuilder",
                    SystemExeFlowGraph.Builder.class, EntityManager.class, JobScheduler.class, Class.class, Function.class));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        @SuppressWarnings("unchecked")
        static <T extends CleanSystem> SystemExeFlowGraph.Builder<SingleFlow<T>> newBuilder(
            EntityManager entities, JobScheduler scheduler, Class<T> type) {
            try {
                return (SystemExeFlowGraph.Builder<SingleFlow<T>>) FACTORY.invokeExact(entities, scheduler, (Class) type, STATE);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }
    }
}
