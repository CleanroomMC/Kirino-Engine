package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.component.ComponentDesc;
import com.cleanroommc.kirino.ecs.component.ComponentDescFlattened;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.ComponentRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.StructRegisterPlan;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.helper.ComponentScanningHelper;
import com.cleanroommc.kirino.ecs.component.scan.helper.StructScanningHelper;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobRegistry;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.joml.*;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CleanECSRuntime {

    @SuppressWarnings("FieldCanBeLocal")
    private final StructRegistry structRegistry;

    @SuppressWarnings("FieldCanBeLocal")
    private final FieldRegistry fieldRegistry;

    @SuppressWarnings("FieldCanBeLocal")
    private final ComponentRegistry componentRegistry;

    public final EntityManager entityManager;

    @SuppressWarnings("FieldCanBeLocal")
    private final JobRegistry jobRegistry;

    public final JobScheduler jobScheduler;

    @SuppressWarnings({"DataFlowIssue", "LoggingSimilarMessage"})
    private CleanECSRuntime(EventBus eventBus, Logger logger) {
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

        logger.debug("Struct defs are as follows:{}", structRegistry.getStructDefMap().isEmpty() ? " (Empty)" : "");
        for (Map.Entry<String, StructDef> entry : structRegistry.getStructDefMap().entrySet()) {
            logger.debug("  - {}: {}", entry.getKey(), entry.getValue().toString(structRegistry));
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

        logger.debug("Component descs are as follows:{}", componentRegistry.getComponentDescMap().isEmpty() ? " (Empty)" : "");
        ImmutableMap<String, ComponentDescFlattened> componentDescFlattenedMap = componentRegistry.getComponentDescFlattenedMap();
        for (Map.Entry<String, ComponentDesc> entry : componentRegistry.getComponentDescMap().entrySet()) {
            ComponentDescFlattened componentDescFlattened = componentDescFlattenedMap.get(entry.getKey());
            logger.debug("  - {}: {}", entry.getKey(), entry.getValue().toString(structRegistry));
            logger.debug("  - {}: {}", entry.getKey(), componentDescFlattened.toString());
        }

        entityManager = new EntityManager(componentRegistry);

        jobRegistry = new JobRegistry(componentRegistry);
        jobScheduler = new JobScheduler(jobRegistry);

        JobRegistrationEvent jobRegistrationEvent = new JobRegistrationEvent();
        eventBus.post(jobRegistrationEvent);
        List<Class<? extends ParallelJob>> parallelJobs = MethodHolder.getParallelJobs(jobRegistrationEvent);
        for (Class<? extends ParallelJob> clazz : parallelJobs) {
            jobRegistry.registerParallelJob(clazz);
            logger.debug("Parallel job \"{}\" registered. Data queries are as follows:{}", clazz.getName(), jobRegistry.getParallelJobDataQueries(clazz).isEmpty() && jobRegistry.getParallelJobExternalDataQueries(clazz).isEmpty() ? " (Empty)" : "");

            List<String> arrayQueries = new ArrayList<>();
            for (JobDataQuery jobDataQuery : jobRegistry.getParallelJobDataQueries(clazz).keySet()) {
                arrayQueries.add(componentRegistry.getComponentName(jobDataQuery.componentClass().asSubclass(CleanComponent.class)) + "; " + String.join(".", jobDataQuery.fieldAccessChain()));
            }
            List<String> externalQueries = new ArrayList<>(jobRegistry.getParallelJobExternalDataQueries(clazz).keySet());
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

    private static final class MethodHolder {
        private static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getFieldGetter(JobRegistrationEvent.class, "parallelJobClasses", List.class));

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

        record Delegate(MethodHandle parallelJobsGetter) {
        }
    }
}
