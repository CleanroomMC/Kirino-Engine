package com.cleanroommc.kirino.ecs.component.schema.def.field.scalar;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

interface Scalar {

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>Input must be non-null</li>
     * </ul>
     *
     * @implNote No manual preconditions check required
     */
    @Nullable Object newScalar(@NonNull Object @NonNull ... args);

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>Input must be non-null</li>
     * </ul>
     *
     * @implNote No manual preconditions check required
     */
    @NonNull Object @NonNull [] flattenScalar(@NonNull Object scalarInstance);
}
