package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record StructMember(
        @NonNull String name,
        @NonNull GlslType type,
        @NonNull QualifierSet qualifiers,
        ShaderMeta.@Nullable SourceSpan span) {

    public StructMember {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkArgument(!name.isBlank(), "A struct member name must not be blank.");
        Preconditions.checkArgument(!(type instanceof VoidType), "A struct member must not have type void.");
    }
}
