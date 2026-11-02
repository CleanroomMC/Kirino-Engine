package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record Subroutine(@NonNull String name, int index) {

    public Subroutine {
        Preconditions.checkNotNull(name);
    }
}
