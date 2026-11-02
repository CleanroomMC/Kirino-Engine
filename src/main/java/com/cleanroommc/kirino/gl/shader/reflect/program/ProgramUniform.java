package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

public record ProgramUniform(
        @NonNull String name,
        @NonNull GlslType type,
        int location,
        @NonNull ImmutableList<Integer> boundUnits) {

    public ProgramUniform {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(boundUnits);
    }
}
