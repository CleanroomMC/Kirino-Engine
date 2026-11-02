package com.cleanroommc.kirino.gl.shader.reflect.program;

import com.cleanroommc.kirino.gl.shader.reflect.resource.StorageType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public record ProgramBlock(
        @NonNull String name,
        @NonNull StorageType storageType,
        int index,
        int binding,
        long dataSize,
        @NonNull ImmutableList<ProgramBlockMember> members) {

    public ProgramBlock {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(storageType);
        Preconditions.checkNotNull(members);
    }

    @NonNull
    public Optional<ProgramBlockMember> member(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return members
                .stream()
                .filter(member -> member.name().equals(name)
                        || member.name().endsWith("[0]") && member.name().substring(0, member.name().length() - 3).equals(name))
                .findFirst();
    }
}
