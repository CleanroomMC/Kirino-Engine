package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record Parameter(
        @Nullable String name,
        @NonNull GlslType type,
        @NonNull ParameterDirection direction,
        boolean constant,
        @NonNull QualifierSet qualifiers) {

    public Parameter {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(direction);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkArgument(name == null || !name.isBlank(), "A parameter name must not be blank.");
        Preconditions.checkArgument(!(type instanceof VoidType), "A parameter must not have type void.");
    }
}
