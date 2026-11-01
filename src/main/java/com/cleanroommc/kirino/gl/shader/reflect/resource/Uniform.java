package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record Uniform(
        @NonNull String name,
        @NonNull StorageType storageType,
        @NonNull GlslType type,
        @NonNull QualifierSet qualifiers,
        @NonNull LayoutQualifiers layout,
        @Nullable String initializer,
        @NonNull ImmutableList<ShaderMeta.SourceSpan> declarations) implements ShaderResource {

    public Uniform {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(storageType);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(layout);
        Preconditions.checkNotNull(declarations);
    }
}
