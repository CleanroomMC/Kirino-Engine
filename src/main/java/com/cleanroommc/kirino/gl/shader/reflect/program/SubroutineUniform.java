package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

public record SubroutineUniform(
        @NonNull String name,
        int location,
        int arraySize,
        @NonNull ImmutableList<Subroutine> compatibleFunctions) {

    public SubroutineUniform {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(compatibleFunctions);
    }
}
