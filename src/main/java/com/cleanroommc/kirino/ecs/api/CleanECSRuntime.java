package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletableFuture;

public final class CleanECSRuntime implements AutoCloseable {
    public enum State {
        RUNNING,
        QUIESCING,
        QUIESCENT,
        CLOSED
    }

    public final EntityManager entityManager;
    public final JobScheduler jobScheduler;

    private CleanECSRuntime(EntityManager entityManager, JobScheduler jobScheduler) {
        this.entityManager = Preconditions.checkNotNull(entityManager);
        this.jobScheduler = Preconditions.checkNotNull(jobScheduler);
    }

    public static CleanECSRuntime create(EventBus eventBus, Logger logger) {
        return MethodHolder.create(Preconditions.checkNotNull(eventBus), Preconditions.checkNotNull(logger));
    }

    public State state() {
        return MethodHolder.state(entityManager);
    }

    public CompletableFuture<Void> quiesce() {
        return MethodHolder.quiesce(entityManager);
    }

    public void reset() {
        MethodHolder.finish(entityManager, true);
    }

    @Override
    public void close() {
        MethodHolder.finish(entityManager, false);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            try {
                Class<?> bootstrap = Class.forName("com.cleanroommc.kirino.ecs.RuntimeBootstrap");
                DELEGATE = new Delegate(Preconditions.checkNotNull(ReflectionUtils.getMethod(bootstrap, "create", CleanECSRuntime.class, EventBus.class, Logger.class)), Preconditions.checkNotNull(ReflectionUtils.getMethod(bootstrap, "state", State.class, EntityManager.class)), Preconditions.checkNotNull(ReflectionUtils.getMethod(bootstrap, "quiesce", CompletableFuture.class, EntityManager.class)), Preconditions.checkNotNull(ReflectionUtils.getMethod(bootstrap, "finish", void.class, EntityManager.class, boolean.class)));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        private static CleanECSRuntime create(EventBus events, Logger logger) {
            try {
                return (CleanECSRuntime) DELEGATE.create.invokeExact(events, logger);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        static State state(EntityManager manager) {
            try {
                return (State) DELEGATE.state.invokeExact(manager);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        @SuppressWarnings("unchecked")
        static CompletableFuture<Void> quiesce(EntityManager manager) {
            try {
                return (CompletableFuture<Void>) DELEGATE.quiesce.invokeExact(manager);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        static void finish(EntityManager manager, boolean reset) {
            try {
                DELEGATE.finish.invokeExact(manager, reset);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        private record Delegate(MethodHandle create, MethodHandle state, MethodHandle quiesce, MethodHandle finish) {
        }
    }
}
