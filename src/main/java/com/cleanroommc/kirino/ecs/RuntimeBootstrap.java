package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;

final class RuntimeBootstrap {

    @SuppressWarnings("FieldCanBeLocal")
    private final StructRegistry structRegistry;

    @SuppressWarnings("FieldCanBeLocal")
    private final FieldRegistry fieldRegistry;

    @SuppressWarnings("FieldCanBeLocal")
    private final ComponentRegistry componentRegistry;

    private final EntityManager entityManager;

    @SuppressWarnings("FieldCanBeLocal")
    private final JobRegistry jobRegistry;

    private final JobScheduler jobScheduler;

    @SuppressWarnings({"DataFlowIssue", "LoggingSimilarMessage"})
    private RuntimeBootstrap(EventBus eventBus, Logger logger) {
        structRegistry = new StructRegistry();
        fieldRegistry = new FieldRegistry(structRegistry);

        // hard coded fields
        fieldRegistry.registerFieldType("byte", ScalarType.BYTE.clazz, new FieldDef(ScalarType.BYTE));
        fieldRegistry.registerFieldType("short", ScalarType.SHORT.clazz, new FieldDef(ScalarType.SHORT));
        fieldRegistry.registerFieldType("int", ScalarType.INT.clazz, new FieldDef(ScalarType.INT));
        fieldRegistry.registerFieldType("long", ScalarType.LONG.clazz, new FieldDef(ScalarType.LONG));
        fieldRegistry.registerFieldType("float", ScalarType.FLOAT.clazz, new FieldDef(ScalarType.FLOAT));
        fieldRegistry.registerFieldType("double", ScalarType.DOUBLE.clazz, new FieldDef(ScalarType.DOUBLE));
        fieldRegistry.registerFieldType("bool", ScalarType.BOOL.clazz, new FieldDef(ScalarType.BOOL));
        fieldRegistry.registerFieldType("vec2", ScalarType.VEC2.clazz, new FieldDef(ScalarType.VEC2));
        fieldRegistry.registerFieldType("vec3", ScalarType.VEC3.clazz, new FieldDef(ScalarType.VEC3));
        fieldRegistry.registerFieldType("vec4", ScalarType.VEC4.clazz, new FieldDef(ScalarType.VEC4));
        fieldRegistry.registerFieldType("mat3", ScalarType.MAT3.clazz, new FieldDef(ScalarType.MAT3));
        fieldRegistry.registerFieldType("mat4", ScalarType.MAT4.clazz, new FieldDef(ScalarType.MAT4));

        StructScanningEvent structScanningEvent = new StructScanningEvent();
        eventBus.post(structScanningEvent);
        for (StructRegisterPlan plan : StructScanningHelper.scanStructClasses(structScanningEvent, fieldRegistry)) {
            // struct class loading
            Class<?> structClass;
            try {
                structClass = Class.forName(plan.structClass(), false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) { // impossible
                throw new RuntimeException("Unexpected class not found.", e);
            }

            try {
                structClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("CleanStruct " + structClass.getName() + " is missing a default constructor with no parameters.", e);
            }

            structRegistry.registerStructType(
                plan.structName(),
                structClass,
                plan.memberLayout(),
                plan.structDef());
            fieldRegistry.registerFieldType(
                plan.structName(),
                structClass,
                new FieldDef(plan.structName()));

            logger.debug("Registered struct \"{}\". Loaded \"{}\".", plan.structName(), plan.structClass());
        }

        structRegistry.lock();
        fieldRegistry.lock();

        logger.debug("Struct defs are as follows:{}", structRegistry.getStructDataMap().isEmpty() ? " (Empty)" : "");
        for (Map.Entry<String, StructRegistry.Data> entry : structRegistry.getStructDataMap().entrySet()) {
            logger.debug("  - {}: {}", entry.getKey(), entry.getValue().getStructDef().toString(structRegistry));
        }

        componentRegistry = new ComponentRegistry(fieldRegistry);

        ComponentScanningEvent componentScanningEvent = new ComponentScanningEvent();
        eventBus.post(componentScanningEvent);
        for (ComponentRegisterPlan plan : ComponentScanningHelper.scanComponentClasses(componentScanningEvent, fieldRegistry)) {
            // component class loading
            Class<? extends CleanComponent> componentClass;
            try {
                componentClass = Class.forName(plan.componentClass(), false, Thread.currentThread().getContextClassLoader()).asSubclass(CleanComponent.class);
            } catch (ClassNotFoundException e) { // impossible
                throw new RuntimeException("Unexpected class not found.", e);
            }

            try {
                componentClass.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("CleanComponent " + componentClass.getName() + " is missing a default constructor with no parameters.", e);
            }

            componentRegistry.registerComponent(
                plan.componentName(),
                componentClass,
                plan.memberLayout(),
                plan.fieldTypeNames());

            logger.debug("Registered component \"{}\". Loaded \"{}\".", plan.componentName(), plan.componentClass());
        }

        componentRegistry.lock();

        logger.debug("Component descs are as follows:{}", componentRegistry.getComponentDataMap().isEmpty() ? " (Empty)" : "");

        for (Map.Entry<String, ComponentRegistry.Data> entry : componentRegistry.getComponentDataMap().entrySet()) {
            ComponentRegistry.Data data = entry.getValue();

            logger.debug("  - {}: {}", entry.getKey(), data.componentDesc.toString(structRegistry));
            logger.debug("  - {}: {}", entry.getKey(), data.layout.columns());
        }

        entityManager = new EntityManager(componentRegistry);

        jobRegistry = new JobRegistry(componentRegistry);
        jobScheduler = MethodHolder.newJobScheduler(new JobExecutor(jobRegistry, entityManager));

        JobRegistrationEvent jobRegistrationEvent = new JobRegistrationEvent();
        eventBus.post(jobRegistrationEvent);
        List<Class<? extends ParallelJob>> parallelJobs = MethodHolder.getParallelJobs(jobRegistrationEvent);
        for (Class<? extends ParallelJob> clazz : parallelJobs) {
            jobRegistry.registerParallelJob(clazz);
            JobDefinition definition = jobRegistry.getDefinition(clazz);
            logger.debug("Parallel job \"{}\" registered. Data queries are as follows:{}", clazz.getName(),
                definition.dataBindings.length == 0 && definition.externalBindings.length == 0 ? " (Empty)" : "");

            List<String> arrayQueries = new ArrayList<>();
            for (JobDefinition.DataBinding binding : definition.dataBindings) {
                arrayQueries.add(componentRegistry.getComponentName(binding.component) + "; "
                    + String.join(".", binding.column.path()));
            }
            List<String> externalQueries = new ArrayList<>();
            for (JobDefinition.ExternalBinding binding : definition.externalBindings) {
                externalQueries.add(binding.key);
            }
            arrayQueries = arrayQueries.stream().sorted().toList();
            externalQueries = externalQueries.stream().sorted().toList();

            for (String query : arrayQueries) {
                logger.debug("  - Array query: {}", query);
            }
            for (String query : externalQueries) {
                logger.debug("  - External query: {}", query);
            }
        }
    }

    static CleanECSRuntime create(EventBus eventBus, Logger logger) {
        RuntimeBootstrap bootstrap = new RuntimeBootstrap(eventBus, logger);
        return MethodHolder.newRuntime(bootstrap.entityManager, bootstrap.jobScheduler);
    }

    static JobScheduler newJobScheduler(JobRegistry registry) {
        return MethodHolder.newJobScheduler(new JobExecutor(registry));
    }

    static CleanECSRuntime.State state(com.cleanroommc.kirino.ecs.api.EntityManager manager) {
        return ((EntityManager) manager).lifecycle.state();
    }

    static java.util.concurrent.CompletableFuture<Void> quiesce(com.cleanroommc.kirino.ecs.api.EntityManager manager) {
        return ((EntityManager) manager).lifecycle.quiesce();
    }

    static void finish(com.cleanroommc.kirino.ecs.api.EntityManager manager, boolean reset) {
        ((EntityManager) manager).lifecycle.finish(reset);
    }

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getFieldGetter(JobRegistrationEvent.class, "parallelJobClasses", List.class),
                Preconditions.checkNotNull(ReflectionUtils.getConstructor(JobScheduler.class, Object.class)),
                Preconditions.checkNotNull(ReflectionUtils.getConstructor(CleanECSRuntime.class,
                    com.cleanroommc.kirino.ecs.api.EntityManager.class, JobScheduler.class)));

            Preconditions.checkNotNull(DELEGATE.parallelJobsGetter);
        }

        @SuppressWarnings("unchecked")
        static List<Class<? extends ParallelJob>> getParallelJobs(JobRegistrationEvent jobRegistrationEvent) {
            try {
                return (List<Class<? extends ParallelJob>>) DELEGATE.parallelJobsGetter.invokeExact(jobRegistrationEvent);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        static JobScheduler newJobScheduler(Object executor) {
            try {
                return (JobScheduler) DELEGATE.schedulerConstructor.invokeExact(executor);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        static CleanECSRuntime newRuntime(
            com.cleanroommc.kirino.ecs.api.EntityManager entities, JobScheduler scheduler) {
            try {
                return (CleanECSRuntime) DELEGATE.runtimeConstructor.invokeExact(entities, scheduler);
            } catch (Throwable failure) {
                Throwables.throwIfUnchecked(failure);
                throw new CompletionException(failure);
            }
        }

        record Delegate(MethodHandle parallelJobsGetter, MethodHandle schedulerConstructor, MethodHandle runtimeConstructor) {
        }
    }
}
