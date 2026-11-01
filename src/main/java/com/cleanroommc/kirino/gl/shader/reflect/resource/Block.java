package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.layout.BufferLayout;
import com.cleanroommc.kirino.gl.shader.reflect.layout.LayoutQualifiers;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record Block(
        @NonNull String name,
        @NonNull StorageType storageType,
        @NonNull QualifierSet qualifiers,
        @NonNull LayoutQualifiers layout,
        @Nullable BlockInstance instance,
        @NonNull ImmutableList<BlockMember> members,
        @NonNull Optional<BufferLayout> computedLayout) implements ShaderResource {

    public Block {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(storageType);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(layout);
        Preconditions.checkNotNull(members);
        Preconditions.checkNotNull(computedLayout);
    }

    @NonNull
    public Optional<BlockMember> member(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return members
                .stream()
                .filter(member -> member.name().equals(name))
                .findFirst();
    }
}
