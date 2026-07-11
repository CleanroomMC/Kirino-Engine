package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import com.google.common.base.Preconditions;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ScalarConstructor {
    private ScalarConstructor() {
    }

    @Nullable
    public static Object newScalar(@NonNull ScalarType scalarType, @NonNull Object @NonNull ... args) {
        Preconditions.checkNotNull(scalarType);
        Preconditions.checkNotNull(args);
        for (Object arg : args) {
            Preconditions.checkNotNull(arg);
        }

        return scalarType.newScalar(args);
    }
}
