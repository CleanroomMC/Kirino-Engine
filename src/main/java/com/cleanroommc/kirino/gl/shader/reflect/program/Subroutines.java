package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record Subroutines(
        @NonNull ImmutableList<Subroutine> functions,
        @NonNull ImmutableList<SubroutineUniform> uniforms) {

    public Subroutines {
        Preconditions.checkNotNull(functions);
        Preconditions.checkNotNull(uniforms);
    }

    @NonNull
    public Optional<Subroutine> function(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return functions
                .stream()
                .filter(function -> function.name().equals(name))
                .findFirst();
    }

    @NonNull
    public Optional<SubroutineUniform> uniform(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return uniforms
                .stream()
                .filter(uniform -> uniform.name().equals(name) || uniform.name().equals(name + "[0]"))
                .findFirst();
    }
}
