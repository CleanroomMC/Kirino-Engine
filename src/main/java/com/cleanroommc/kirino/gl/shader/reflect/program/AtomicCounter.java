package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record AtomicCounter(@NonNull String name, int arraySize, long offset) {

    public AtomicCounter {
        Preconditions.checkNotNull(name);
    }
}
