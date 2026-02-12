package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class JobRegistry {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final Map<Class<? extends ParallelJob>, Map<JobDataQuery, JobDataInjector>> parallelJobDataQueryMap = new HashMap<>();
    private final Map<Class<? extends ParallelJob>, Map<String, JobDataInjector>> parallelJobExternalDataQueryMap = new HashMap<>();
    private final Map<Class<? extends ParallelJob>, JobInstantiator> parallelJobInstantiatorMap = new HashMap<>();

    private final ComponentRegistry componentRegistry;

    public JobRegistry(ComponentRegistry componentRegistry) {
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
            callSite = LambdaMetafactory.metafactory(LOOKUP, "inject",
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
            callSite = LambdaMetafactory.metafactory(LOOKUP, "instantiate",
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
            throw new RuntimeException("Parallel job class " + clazz.getName() + " is missing a default constructor with no parameters.", e);
        }

        parallelJobInstantiatorMap.computeIfAbsent(clazz, this::genParallelJobInstantiator);

        Map<JobDataQuery, JobDataInjector> dataQueryMap = parallelJobDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());
        Map<String, JobDataInjector> externalDataQueryMap = parallelJobExternalDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());

        String exceptionText = "Parallel job class " + clazz.getName() + " contains invalid annotation entries.";

        for (Field field : clazz.getDeclaredFields()) {
            // scan JobDataQuery
            if (field.isAnnotationPresent(JobDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                if (PrimitiveArray.class != field.getType()) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("PrimitiveArray must be the type of the JobDataQuery-annotated field " + field.getName() + "."));
                }

                JobDataQuery jobDataQuery = field.getAnnotation(JobDataQuery.class);

                if (!CleanComponent.class.isAssignableFrom(jobDataQuery.componentClass())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("CleanComponent must be assignable from JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + "."));
                }
                if (jobDataQuery.componentClass() == CleanComponent.class) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " must not be CleanComponent itself."));
                }
                if (!componentRegistry.componentExists(jobDataQuery.componentClass().asSubclass(CleanComponent.class))) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " isn't registered in the component registry."));
                }
                String componentName = componentRegistry.getComponentName(jobDataQuery.componentClass().asSubclass(CleanComponent.class));
                try {
                    componentRegistry.getFieldOrdinal(componentName, jobDataQuery.fieldAccessChain());
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
    }

    @Nullable
    public Map<JobDataQuery, JobDataInjector> getParallelJobDataQueries(Class<? extends ParallelJob> clazz) {
        return parallelJobDataQueryMap.get(clazz);
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
