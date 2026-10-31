package com.cleanroommc.kirino.gl.shader.reflect.execution;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record TessEvaluationExecution(
        @Nullable TessellationPrimitive primitive,
        @NonNull Spacing spacing,
        @NonNull Winding winding,
        boolean pointMode) implements ShaderExecution {

    public TessEvaluationExecution {
        Preconditions.checkNotNull(spacing);
        Preconditions.checkNotNull(winding);
    }
}
