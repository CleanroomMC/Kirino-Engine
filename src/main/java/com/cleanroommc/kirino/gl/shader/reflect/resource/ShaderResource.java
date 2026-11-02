package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import org.jspecify.annotations.NonNull;

public sealed interface ShaderResource permits
        Uniform,
        Variable,
        Block {

    @NonNull String name();
    @NonNull StorageType storageType();
    @NonNull QualifierSet qualifiers();
    @NonNull LayoutQualifiers layout();
}
