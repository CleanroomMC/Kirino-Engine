package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record UnresolvedType(@NonNull String spelling) implements GlslType {

    public UnresolvedType {
        Preconditions.checkNotNull(spelling);
        Preconditions.checkArgument(!spelling.isBlank(), "An unresolved type spelling must not be blank.");
    }
}
