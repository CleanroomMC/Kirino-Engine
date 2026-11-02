package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record BlockMember(
        @NonNull String name,
        @NonNull GlslType type,
        @NonNull QualifierSet qualifiers,
        @NonNull LayoutQualifiers layout) {

    public BlockMember {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(layout);
    }
}
