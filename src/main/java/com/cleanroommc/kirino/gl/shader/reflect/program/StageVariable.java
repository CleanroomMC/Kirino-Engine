package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record StageVariable(
        @NonNull String name,
        @NonNull GlslType type,
        int location,
        int component,
        int index,
        boolean perPatch) {

    public StageVariable {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
    }
}
