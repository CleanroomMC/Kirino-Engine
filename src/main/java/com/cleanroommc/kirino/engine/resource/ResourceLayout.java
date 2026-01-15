package com.cleanroommc.kirino.engine.resource;

import org.jspecify.annotations.NonNull;

public final class ResourceLayout {
    private int nextId = 0;

    public <T> ResourceSlot<T> slot(@NonNull Class<T> type) {
        return new ResourceSlot<>(nextId++, type);
    }
}
