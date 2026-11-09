package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.CompletionException;

public final class CleanECSRuntime {
    public final EntityManager entityManager;
    public final JobScheduler jobScheduler;

    private CleanECSRuntime(EntityManager entityManager, JobScheduler jobScheduler) {
        this.entityManager = Preconditions.checkNotNull(entityManager);
        this.jobScheduler = Preconditions.checkNotNull(jobScheduler);
    }

    public static CleanECSRuntime create(EventBus eventBus, Logger logger) {
        return MethodHolder.create(Preconditions.checkNotNull(eventBus), Preconditions.checkNotNull(logger));
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            try {
                Class<?> bootstrap = Class.forName("com.cleanroommc.kirino.ecs.RuntimeBootstrap");
                DELEGATE = new Delegate(Preconditions.checkNotNull(ReflectionUtils.getMethod(bootstrap,
                    "create", CleanECSRuntime.class, EventBus.class, Logger.class)));
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

        private record Delegate(MethodHandle create) {
        }
    }
}
