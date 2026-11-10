package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

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
            String key = externalBindings[i].key;
            Preconditions.checkArgument(externalData.containsKey(key),
                "Missing the entry \"%s\" from \"externalData\".", key);
            values[i] = externalData.get(key);
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
        final JobDataInjector injector;

        ExternalBinding(String key, JobDataInjector injector) {
            this.key = key;
            this.injector = injector;
        }
    }
}
