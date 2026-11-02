package com.cleanroommc.kirino.gl.shader.reflect.layout;

import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record BufferLayoutMember(
        @NonNull String name,
        @NonNull GlslType type,
        long offset,
        @NonNull BufferLayout layout) {

    public BufferLayoutMember {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(layout);
    }
}
