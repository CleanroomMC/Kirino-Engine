package com.cleanroommc.kirino.ecs.api.job;

import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public final class JobScheduler {
    private final Object executor;

    private JobScheduler(Object executor) {
        this.executor = Preconditions.checkNotNull(executor);
    }

    public record ExecutionHandle(@NonNull CompletableFuture<Void> future, int totalThreadCount, boolean async) {
    }

    public ExecutionHandle executeParallelJob(EntityManager entityManager, Class<? extends ParallelJob> job,
                                              @Nullable Map<String, Object> externalData, Executor workers) {
        return MethodHolder.execute(executor, entityManager, job, externalData, workers);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            try {
                Class<?> implementation = Class.forName("com.cleanroommc.kirino.ecs.JobExecutor");
                MethodHandle execute = Preconditions.checkNotNull(ReflectionUtils.getMethod(implementation,
                    "executeParallelJob", ExecutionHandle.class, EntityManager.class, Class.class, Map.class, Executor.class));
                DELEGATE = new Delegate(execute.asType(MethodType.methodType(ExecutionHandle.class,
                    Object.class, EntityManager.class, Class.class, Map.class, Executor.class)));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        private static ExecutionHandle execute(Object executor, EntityManager entities, Class<? extends ParallelJob> job,
                                               Map<String, Object> externalData, Executor workers) {
            try {
                return (ExecutionHandle) DELEGATE.execute.invokeExact(executor, entities, job, externalData, workers);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle execute) {
        }
    }
}
