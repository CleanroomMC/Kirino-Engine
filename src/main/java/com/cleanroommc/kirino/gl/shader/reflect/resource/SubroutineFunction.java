package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.cleanroommc.kirino.gl.shader.reflect.type.Parameter;
import com.cleanroommc.kirino.gl.shader.reflect.type.SubroutineType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

public record SubroutineFunction(
        @NonNull String name,
        @NonNull GlslType returnType,
        @NonNull QualifierSet returnQualifiers,
        @NonNull ImmutableList<Parameter> parameters,
        @NonNull ImmutableList<SubroutineType> compatibleTypes,
        @NonNull LayoutQualifiers layout) {

    public SubroutineFunction {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(returnType);
        Preconditions.checkNotNull(returnQualifiers);
        Preconditions.checkNotNull(parameters);
        Preconditions.checkNotNull(compatibleTypes);
        Preconditions.checkNotNull(layout);
    }
}
