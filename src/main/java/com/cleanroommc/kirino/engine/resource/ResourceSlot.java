package com.cleanroommc.kirino.engine.resource;

public final class ResourceSlot<T> {
    private final int id;
    private final Class<T> type;

    ResourceSlot(int id, Class<T> type) {
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