package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.cleanroommc.kirino.gl.shader.reflect.type.GlslType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record Variable(
        @NonNull String name,
        @NonNull StorageType storageType,
        @NonNull GlslType type,
        @NonNull InterfaceArraying interfaceArraying,
        @NonNull QualifierSet qualifiers,
        @NonNull LayoutQualifiers layout,
        @Nullable String initializer) implements ShaderResource {

    public Variable {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(storageType);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(interfaceArraying);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(layout);
    }
}
