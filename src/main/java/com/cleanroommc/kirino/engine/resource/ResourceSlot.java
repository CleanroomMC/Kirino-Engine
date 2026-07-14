package com.cleanroommc.kirino.engine.resource;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class ResourceSlot<T> {

    private final int id;
    private final Class<T> type;
    private final String name;

    ResourceSlot(int id, @NonNull Class<T> type, @NonNull String name) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(name);

        this.id = id;
        this.type = type;
        this.name = name;
    }

    public int id() {
        return id;
    }

    @NonNull
    public Class<T> type() {
        return type;
    }

    @NonNull
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("ResourceSlot{ id=%d, type=%s, name=%s}",
                id,
                type.toString(),
                name);
    }
}