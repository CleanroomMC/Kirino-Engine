package com.cleanroommc.kirino.engine.resource;

import org.jspecify.annotations.NonNull;

public final class ResourceSlot<T> {
    private final int id;
    private final Class<T> type;

    ResourceSlot(int id, @NonNull Class<T> type) {
        this.id = id;
        this.type = type;
    }

    public int id() {
        return id;
    }

    public Class<T> type() {
        return type;
    }
}