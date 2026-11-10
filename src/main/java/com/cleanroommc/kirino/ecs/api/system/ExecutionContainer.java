package com.cleanroommc.kirino.ecs.api.system;

import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * It helps {@link SystemExeFlowGraph} to guide the execution flow by providing the future handles.
 *
 */
public final class ExecutionContainer {
    ExecutionContainer() {
    }

    private final Object state = MethodHolder.create();

    public void noExecutions() {
        updateExecutions(null, (JobScheduler.ExecutionHandle[]) null);
    }

    public void updateExecutions(JobScheduler.ExecutionHandle @NonNull ... handles) {
        updateExecutions(null, handles);
    }

    public void updateExecutions(@NonNull CompletableFuture<Void> @Nullable [] futures, JobScheduler.ExecutionHandle @Nullable ... handles) {
        MethodHolder.register(state, futures, handles);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            try {
                Class<?> implementation = Class.forName("com.cleanroommc.kirino.ecs.SystemExecution");
                DELEGATE = new Delegate(
                    Preconditions.checkNotNull(ReflectionUtils.getMethod(implementation, "create", Object.class)),
                    Preconditions.checkNotNull(ReflectionUtils.getMethod(implementation, "register", void.class,
                        Object.class, CompletableFuture[].class, JobScheduler.ExecutionHandle[].class)));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        static Object create() {
            try {
                return (Object) DELEGATE.create.invokeExact();
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        static void register(Object state, CompletableFuture<Void>[] futures, JobScheduler.ExecutionHandle[] handles) {
            try {
                DELEGATE.register.invokeExact(state, (CompletableFuture[]) futures, handles);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle create, MethodHandle register) {
        }
    }
}
