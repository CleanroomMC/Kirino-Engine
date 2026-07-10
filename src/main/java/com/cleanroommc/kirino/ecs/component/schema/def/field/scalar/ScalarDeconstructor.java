package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.joml.*;
import org.jspecify.annotations.NonNull;

public final class ScalarDeconstructor {
    private ScalarDeconstructor() {
    }

    public static @NonNull Object @NonNull [] flattenScalar(@NonNull ScalarType scalarType, @NonNull Object scalarInstance) {
        Preconditions.checkNotNull(scalarType);
        Preconditions.checkNotNull(scalarInstance);

        try {
            return scalarType.flattenScalar(scalarInstance);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid scalar type."); // impossible
        }
    }
}
