package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record TransformFeedbackVarying(
        @NonNull String name,
        @NonNull GlslType type,
        int arraySize,
        long offset) {

    public TransformFeedbackVarying {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkArgument(arraySize > 0, "The captured element count must be positive.");
    }
}
