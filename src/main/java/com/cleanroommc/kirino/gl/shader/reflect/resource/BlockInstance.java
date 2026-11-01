package com.cleanroommc.kirino.gl.shader.reflect.resource;

import com.cleanroommc.kirino.gl.shader.reflect.type.ArrayExtent;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

public record BlockInstance(
        @NonNull String name,
        @NonNull ImmutableList<ArrayExtent> dimensions,
        @NonNull InterfaceArraying interfaceArraying) {

    public BlockInstance {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(dimensions);
        Preconditions.checkNotNull(interfaceArraying);
    }
}
