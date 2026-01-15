package com.cleanroommc.kirino.engine.resource;

public final class ResourceLayout {
    private int nextId = 0;

    public <T> ResourceSlot<T> slot(Class<T> type) {
        return new ResourceSlot<>(nextId++, type);
    }
}
