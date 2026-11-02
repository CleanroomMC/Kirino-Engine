package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

public record SubroutineType(
        @NonNull String name,
        @NonNull GlslType returnType,
        @NonNull QualifierSet returnQualifiers,
        @NonNull ImmutableList<Parameter> parameters) implements GlslType {

    public SubroutineType {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(returnType);
        Preconditions.checkNotNull(returnQualifiers);
        Preconditions.checkNotNull(parameters);
        Preconditions.checkArgument(!name.isBlank(), "A subroutine type name must not be blank.");
    }
}
