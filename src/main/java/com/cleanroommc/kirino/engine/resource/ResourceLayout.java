package com.cleanroommc.kirino.engine.resource;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class ResourceLayout {

    private int nextId = 0;

    private ResourceLayout() {
    }

    public <T> ResourceSlot<T> slot(@NonNull Class<T> type) {
        Preconditions.checkNotNull(type);

        return new ResourceSlot<>(nextId++, type, "");
    }

    public <T> ResourceSlot<T> slot(@NonNull Class<T> type, @NonNull String name) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(name);

        return new ResourceSlot<>(nextId++, type, name);
    }
}
