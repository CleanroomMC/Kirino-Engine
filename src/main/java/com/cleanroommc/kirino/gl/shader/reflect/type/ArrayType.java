package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record ArrayType(@NonNull GlslType elementType, @NonNull ArrayExtent extent) implements GlslType {

    public ArrayType {
        Preconditions.checkNotNull(elementType);
        Preconditions.checkNotNull(extent);
        Preconditions.checkArgument(!(elementType instanceof VoidType),
                "An array element must not have type void.");
    }
}
