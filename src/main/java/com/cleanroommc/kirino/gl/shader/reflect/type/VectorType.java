package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record VectorType(@NonNull ScalarType componentType, int components) implements GlslType {

    public VectorType {
        Preconditions.checkNotNull(componentType);
        Preconditions.checkArgument(components >= 2 && components <= 4,
                "Vector component count \"%s\" must be between 2 and 4.", components);
    }
}
