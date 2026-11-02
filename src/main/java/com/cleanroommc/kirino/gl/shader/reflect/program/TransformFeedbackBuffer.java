package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record TransformFeedbackBuffer(
        int index,
        int binding,
        long stride,
        @NonNull ImmutableList<TransformFeedbackVarying> varyings) {

    public TransformFeedbackBuffer {
        Preconditions.checkNotNull(varyings);
    }

    @NonNull
    public Optional<TransformFeedbackVarying> varying(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return varyings
                .stream()
                .filter(varying -> varying.name().equals(name) || varying.name().equals(name + "[0]"))
                .findFirst();
    }
}
