package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public record StructType(
        @Nullable String name,
        @NonNull ImmutableList<StructMember> members) implements GlslType {

    public StructType {
        Preconditions.checkNotNull(members);
        Preconditions.checkArgument(!members.isEmpty(), "A struct must contain at least one member.");
        Preconditions.checkArgument(name == null || !name.isBlank(), "A struct name must not be blank.");

        Set<String> names = new HashSet<>();
        for (StructMember member : members) {
            Preconditions.checkArgument(names.add(member.name()), "Duplicate struct member \"%s\".", member.name());
        }
    }

    @NonNull
    public Optional<StructMember> member(@NonNull String name) {
        Preconditions.checkNotNull(name);

        for (StructMember member : members) {
            if (member.name().equals(name)) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }
}
