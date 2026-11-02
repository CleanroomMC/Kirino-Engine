package com.cleanroommc.kirino.gl.shader.reflect.execution;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public sealed interface ShaderExecution permits
        VertexExecution,
        FragmentExecution,
        GeometryExecution,
        TessControlExecution,
        TessEvaluationExecution,
        ComputeExecution {

    @NonNull
    default <T extends ShaderExecution> Optional<T> as(@NonNull Class<T> type) {
        Preconditions.checkNotNull(type);

        return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
    }
}
