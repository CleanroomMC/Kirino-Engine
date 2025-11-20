package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
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

    private final Map<Class<? extends IParallelJob>, Map<JobDataQuery, IJobDataInjector>> parallelJobDataQueryMap = new HashMap<>();
    private final Map<Class<? extends IParallelJob>, Map<String, IJobDataInjector>> parallelJobExternalDataQueryMap = new HashMap<>();
    private final Map<Class<? extends IParallelJob>, IJobInstantiator> parallelJobInstantiatorMap = new HashMap<>();

    private final ComponentRegistry componentRegistry;

    public JobRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @NonNull
    private IJobDataInjector genParallelJobDataInjector(@NonNull Class<? extends IParallelJob> clazz, @NonNull String fieldName, @NonNull Class<?> fieldClass) {
        MethodHandle setter = ReflectionUtils.getFieldSetter(clazz, fieldName, fieldClass);
        Preconditions.checkNotNull(setter);

        MethodType setterType = setter.type();
        CallSite callSite;
        try {
            callSite = LambdaMetafactory.metafactory(LOOKUP, "inject",
                    MethodType.methodType(IJobDataInjector.class, MethodHandle.class),
                    setterType.erase(),
                    MethodHandles.exactInvoker(setterType),
                    setterType);
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        }
        try {
            return (IJobDataInjector) callSite.getTarget().invokeExact(setter);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    private IJobInstantiator genParallelJobInstantiator(@NonNull Class<? extends IParallelJob> clazz) {
        MethodHandle ctor = ReflectionUtils.getConstructor(clazz);
        Preconditions.checkNotNull(ctor);

        MethodType ctorType = ctor.type();
        CallSite callSite;
        try {
            callSite = LambdaMetafactory.metafactory(LOOKUP, "instantiate",
                    MethodType.methodType(IJobInstantiator.class, MethodHandle.class),
                    ctorType.erase(),
                    MethodHandles.exactInvoker(ctorType),
                    ctorType);
        } catch (LambdaConversionException e) {
            throw new RuntimeException(e);
        }
        try {
            return (IJobInstantiator) callSite.getTarget().invokeExact(ctor);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void registerParallelJob(Class<? extends IParallelJob> clazz) {
        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Parallel job class " + clazz.getName() + " is missing a default constructor with no parameters.", e);
        }

        parallelJobInstantiatorMap.computeIfAbsent(clazz, this::genParallelJobInstantiator);

        Map<JobDataQuery, IJobDataInjector> dataQueryMap = parallelJobDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());
        Map<String, IJobDataInjector> externalDataQueryMap = parallelJobExternalDataQueryMap.computeIfAbsent(clazz, k -> new HashMap<>());

        String exceptionText = "Parallel job class " + clazz.getName() + " contains invalid annotation entries.";

        for (Field field : clazz.getDeclaredFields()) {
            // scan JobDataQuery
            if (field.isAnnotationPresent(JobDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                if (IPrimitiveArray.class != field.getType()) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("IPrimitiveArray must be the type of the JobDataQuery-annotated field " + field.getName() + "."));
                }

                JobDataQuery jobDataQuery = field.getAnnotation(JobDataQuery.class);

                if (!ICleanComponent.class.isAssignableFrom(jobDataQuery.componentClass())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("ICleanComponent must be assignable from JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + "."));
                }
                if (jobDataQuery.componentClass() == ICleanComponent.class) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " must not be ICleanComponent itself."));
                }
                if (!componentRegistry.componentExists(jobDataQuery.componentClass().asSubclass(ICleanComponent.class))) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() " + jobDataQuery.componentClass().getName() + " isn't registered in the component registry."));
                }
                String componentName = componentRegistry.getComponentName(jobDataQuery.componentClass().asSubclass(ICleanComponent.class));
                try {
                    componentRegistry.getFieldOrdinal(componentName, jobDataQuery.fieldAccessChain());
                } catch (Throwable e) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#fieldAccessChain() is invalid.", e));
                }

                IJobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                dataQueryMap.put(jobDataQuery, jobDataInjector);
            }
            // scan JobExternalDataQuery
            if (field.isAnnotationPresent(JobExternalDataQuery.class) && !Modifier.isStatic(field.getModifiers())) {
                IJobDataInjector jobDataInjector = genParallelJobDataInjector(clazz, field.getName(), field.getType());
                externalDataQueryMap.put(field.getName(), jobDataInjector);
            }
        }
    }

    @Nullable
    public Map<JobDataQuery, IJobDataInjector> getParallelJobDataQueries(Class<? extends IParallelJob> clazz) {
        return parallelJobDataQueryMap.get(clazz);
    }

    @Nullable
    public Map<String, IJobDataInjector> getParallelJobExternalDataQueries(Class<? extends IParallelJob> clazz) {
        return parallelJobExternalDataQueryMap.get(clazz);
    }

    @Nullable
    public IJobInstantiator getParallelJobInstantiator(Class<? extends IParallelJob> clazz) {
        return parallelJobInstantiatorMap.get(clazz);
    }
}
