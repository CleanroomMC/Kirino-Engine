package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class JobRegistry {
    private final Map<Class<? extends ParallelJob>, JobDefinition> definitions = new Reference2ObjectOpenHashMap<>();

    private final ComponentRegistry componentRegistry;

    public JobRegistry(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    @NonNull
    private JobDataInjector genParallelJobDataInjector(@NonNull Class<?> clazz, @NonNull String fieldName, @NonNull Class<?> fieldClass) {
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
        Preconditions.checkNotNull(clazz);
        Preconditions.checkArgument(!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()),
            "Parallel job class \"%s\" must be concrete.", clazz.getName());

        try {
            clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Parallel job class \"" + clazz.getName() + "\" is missing a default constructor with no parameters.", e);
        }

        JobInstantiator instantiator = genParallelJobInstantiator(clazz);
        List<JobDefinition.DataBinding> dataBindings = new ArrayList<>();
        List<JobDefinition.ExternalBinding> externalBindings = new ArrayList<>();

        String exceptionText = "Parallel job class \"" + clazz.getName() + "\" contains invalid annotation entries.";

        for (Class<?> owner = clazz; owner != Object.class; owner = owner.getSuperclass()) {
            for (Field field : owner.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                JobDataQuery jobDataQuery = field.getAnnotation(JobDataQuery.class);
                boolean external = field.isAnnotationPresent(JobExternalDataQuery.class);
                if (jobDataQuery == null && !external) {
                    continue;
                }

                Preconditions.checkArgument(jobDataQuery == null || !external,
                    "Field \"%s\" in \"%s\" must not declare both job data annotations.",
                    field.getName(), owner.getName());
                Preconditions.checkArgument(!Modifier.isFinal(field.getModifiers()),
                    "Job data field \"%s\" in \"%s\" must not be final.", field.getName(), owner.getName());

                JobDataInjector injector = genParallelJobDataInjector(owner, field.getName(), field.getType());
                if (external) {
                    externalBindings.add(new JobDefinition.ExternalBinding(field.getName(), injector));
                    continue;
                }

                if (PrimitiveArray.class != field.getType()) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("PrimitiveArray must be the type of the JobDataQuery-annotated field \"" + field.getName() + "\"."));
                }

                if (!CleanComponent.class.isAssignableFrom(jobDataQuery.componentClass())) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("CleanComponent must be assignable from JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\"."));
                }
                if (jobDataQuery.componentClass() == CleanComponent.class) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\" must not be CleanComponent itself."));
                }
                if (!componentRegistry.componentExists(jobDataQuery.componentClass().asSubclass(CleanComponent.class))) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#componentClass() \"" + jobDataQuery.componentClass().getName() + "\" isn't registered in the component registry."));
                }
                Class<? extends CleanComponent> component = jobDataQuery.componentClass().asSubclass(CleanComponent.class);
                SchemaLayout.Column column;
                try {
                    column = componentRegistry.getLayout(component).resolve(jobDataQuery.fieldAccessChain());
                } catch (RuntimeException e) {
                    throw new RuntimeException(exceptionText, new IllegalStateException("JobDataQuery#fieldAccessChain() is invalid.", e));
                }

                dataBindings.add(new JobDefinition.DataBinding(component, column, injector));
            }
        }
        definitions.put(clazz, new JobDefinition(instantiator,
            dataBindings.toArray(JobDefinition.DataBinding[]::new),
            externalBindings.toArray(JobDefinition.ExternalBinding[]::new)));
    }

    @Nullable
    public JobDefinition getDefinition(Class<? extends ParallelJob> clazz) {
        return definitions.get(clazz);
    }
}
