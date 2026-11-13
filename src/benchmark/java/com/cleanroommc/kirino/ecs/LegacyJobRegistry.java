package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

class LegacyJobRegistry {

    private final Map<Class<? extends ParallelJob>, Map<JobDataQuery, JobDataInjector>> parallelJobDataQueryMap = new HashMap<>();
    private final Map<Class<? extends ParallelJob>, Map<String, JobDataInjector>> parallelJobExternalDataQueryMap = new HashMap<>();
    private final Map<Class<? extends ParallelJob>, JobInstantiator> parallelJobInstantiatorMap = new HashMap<>();
    private final Map<Class<? extends ParallelJob>, Map<JobDataQuery, SchemaLayout.Column>> parallelJobColumns = new HashMap<>();

    private final ComponentRegistry componentRegistry;

    public LegacyJobRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @NonNull
    private JobDataInjector genParallelJobDataInjector(@NonNull Class<? extends ParallelJob> clazz, @NonNull String fieldName, @NonNull Class<?> fieldClass) {
        MethodHandle setter = ReflectionUtils.getFieldSetter(clazz, fieldName, fieldClass);
        Preconditions.checkNotNull(setter);

        MethodType setterType = setter.type();
        if (fieldClass.isPrimitive()) {
            setterType = setterType.wrap().changeReturnType(void.class);
        }

        CallSite callSite;
        try {
            callSite = LambdaMetafactory.metafactory(MethodHandles.lookup(), "inject",
                MethodType.methodType(JobDataInjector.class, MethodHandle.class),
                setterType.erase(),
                MethodHandles.exactInvoker(setter.type()),
                setterType);
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        }
        try {
            return (JobDataInjector) callSite.getTarget().invokeExact(setter);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private JobInstantiator genParallelJobInstantiator(@NonNull Class<? extends ParallelJob> clazz) {
        MethodHandle ctor = ReflectionUtils.getConstructor(clazz);
        Preconditions.checkNotNull(ctor);

        MethodType ctorType = ctor.type();
        CallSite callSite;
        try {
            callSite = LambdaMetafactory.metafactory(MethodHandles.lookup(), "instantiate",
                MethodType.methodType(JobInstantiator.class, MethodHandle.class),
                ctorType.erase(),
                MethodHandles.exactInvoker(ctorType),
                ctorType);
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        }
        try {
            return (JobInstantiator) callSite.getTarget().invokeExact(ctor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void registerParallelJob(Class<? extends ParallelJob> clazz) {
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Parallel job class \"" + clazz.getName() + "\" is missing a default constructor with no parameters.", e);
        }

        // TODO: Rework THIS
        JobInstantiator instantiator = genParallelJobInstantiator(clazz);
        Map<JobDataQuery, JobDataInjector> dataQueryMap = new Object2ObjectOpenHashMap<>();
        Map<String, JobDataInjector> externalDataQueryMap = new Object2ObjectOpenHashMap<>();
        Map<JobDataQuery, SchemaLayout.Column> columns = new Object2ObjectOpenHashMap<>();

        String exceptionText = "Parallel job class \"" + clazz.getName() + "\" contains invalid annotation entries.";

        for (Field field : clazz.getDeclaredFields()) {
            // scan JobDataQuery
            if (field.isAnnotationPresent(JobDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                if (PrimitiveArray.class != field.getType()) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("PrimitiveArray must be the type of the JobDataQuery-annotated field \"" + field.getName() + "\"."));
                }

                JobDataQuery jobDataQuery = field.getAnnotation(JobDataQuery.class);

                if (!CleanComponent.class.isAssignableFrom(jobDataQuery.componentClass())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("CleanComponent must be assignable from JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\"."));
                }
                if (jobDataQuery.componentClass() == CleanComponent.class) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\" must not be CleanComponent itself."));
                }
                if (!componentRegistry.componentExists(jobDataQuery.componentClass().asSubclass(CleanComponent.class))) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\" isn't registered in the component registry."));
                }
                String componentName = componentRegistry.getComponentName(jobDataQuery.componentClass().asSubclass(CleanComponent.class));
                try {
                    columns.put(jobDataQuery, componentRegistry.getLayout(componentName).resolve(jobDataQuery.fieldAccessChain()));
                } catch (Throwable e) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#fieldAccessChain() is invalid.", e));
                }

                JobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                dataQueryMap.put(jobDataQuery, jobDataInjector);
            }
            // scan JobExternalDataQuery
            if (field.isAnnotationPresent(JobExternalDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                JobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                externalDataQueryMap.put(field.getName(), jobDataInjector);
            }
        }
        parallelJobColumns.put(clazz, Map.copyOf(columns));
        parallelJobDataQueryMap.put(clazz, Map.copyOf(dataQueryMap));
        parallelJobExternalDataQueryMap.put(clazz, Map.copyOf(externalDataQueryMap));
        parallelJobInstantiatorMap.put(clazz, instantiator);
    }

    @Nullable
    public Map<JobDataQuery, JobDataInjector> getParallelJobDataQueries(Class<? extends ParallelJob> clazz) {
        return parallelJobDataQueryMap.get(clazz);
    }

    public SchemaLayout.Column getParallelJobColumn(Class<? extends ParallelJob> clazz, JobDataQuery query) {
        Map<JobDataQuery, SchemaLayout.Column> columns = parallelJobColumns.get(clazz);
        return columns == null ? null : columns.get(query);
    }

    @Nullable
    public Map<String, JobDataInjector> getParallelJobExternalDataQueries(Class<? extends ParallelJob> clazz) {
        return parallelJobExternalDataQueryMap.get(clazz);
    }

    @Nullable
    public JobInstantiator getParallelJobInstantiator(Class<? extends ParallelJob> clazz) {
        return parallelJobInstantiatorMap.get(clazz);
    }
}
