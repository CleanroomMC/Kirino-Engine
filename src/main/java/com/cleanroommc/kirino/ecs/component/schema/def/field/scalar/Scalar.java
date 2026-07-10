package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import org.jspecify.annotations.NonNull;

interface Scalar {
    Object newScalar(@NonNull Object @NonNull ... args);
    @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance);
}
