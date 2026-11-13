package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodType;
import java.util.Map;

final class JobDefinition {
    private static final PrimitiveArray[] NO_COLUMNS = new PrimitiveArray[0];
    private static final Object[] NO_EXTERNAL_DATA = new Object[0];

    final JobInstantiator instantiator;
    final DataBinding[] dataBindings;
    final ExternalBinding[] externalBindings;

    JobDefinition(JobInstantiator instantiator,
                  DataBinding[] dataBindings, ExternalBinding[] externalBindings) {
        this.instantiator = instantiator;
        this.dataBindings = dataBindings;
        this.externalBindings = externalBindings;
    }

    PrimitiveArray[] resolveColumns(ArchetypeDataPool pool) {
        if (dataBindings.length == 0) {
            return NO_COLUMNS;
        }

        PrimitiveArray[] columns = new PrimitiveArray[dataBindings.length];
        for (int i = 0; i < dataBindings.length; i++) {
            DataBinding binding = dataBindings[i];
            columns[i] = pool.getArray(binding.component, binding.column);
        }
        return columns;
    }

    Object[] resolveExternalData(@Nullable Map<String, Object> externalData) {
        if (externalBindings.length == 0) {
            return NO_EXTERNAL_DATA;
        }

        Preconditions.checkArgument(externalData != null,
            "Argument \"externalData\" must not be null since there are %s external data queries.",
            externalBindings.length);

        Object[] values = new Object[externalBindings.length];
        for (int i = 0; i < externalBindings.length; i++) {
            ExternalBinding binding = externalBindings[i];
            String key = binding.key;
            Preconditions.checkArgument(externalData.containsKey(key),
                "Missing the entry \"%s\" from \"externalData\".", key);
            Object value = externalData.get(key);
            Preconditions.checkArgument(value == null ? !binding.primitive : binding.valueType.isInstance(value),
                "External data \"%s\" for %s requires %s.", key, binding.target, binding.valueType.getName());
            values[i] = value;
        }
        return values;
    }

    ParallelJob instantiate(PrimitiveArray[] columns, Object[] externalData) {
        ParallelJob job = (ParallelJob) instantiator.instantiate();
        for (int i = 0; i < dataBindings.length; i++) {
            dataBindings[i].injector.inject(job, columns[i]);
        }
        for (int i = 0; i < externalBindings.length; i++) {
            externalBindings[i].injector.inject(job, externalData[i]);
        }
        return job;
    }

    static final class DataBinding {
        final Class<? extends CleanComponent> component;
        final SchemaLayout.Column column;
        final JobDataInjector injector;

        DataBinding(Class<? extends CleanComponent> component, SchemaLayout.Column column,
                    JobDataInjector injector) {
            this.component = component;
            this.column = column;
            this.injector = injector;
        }
    }

    static final class ExternalBinding {
        final String key;
        final Class<?> valueType;
        final boolean primitive;
        final String target;
        final JobDataInjector injector;

        ExternalBinding(String key, Class<?> fieldType, String target, JobDataInjector injector) {
            this.key = key;
            this.valueType = MethodType.methodType(fieldType).wrap().returnType();
            this.primitive = fieldType.isPrimitive();
            this.target = target;
            this.injector = injector;
        }
    }
}
