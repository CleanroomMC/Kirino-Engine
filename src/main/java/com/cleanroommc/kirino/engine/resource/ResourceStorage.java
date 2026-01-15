package com.cleanroommc.kirino.engine.resource;

import java.util.HashMap;
import java.util.Map;

public final class ResourceStorage {
    private final Map<Integer, Object> storage = new HashMap<>();

    public <T> boolean has(ResourceSlot<T> slot) {
        return storage.containsKey(slot.id());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ResourceSlot<T> slot) {
        return (T) storage.get(slot.id());
    }

    public <T> void put(ResourceSlot<T> slot, T resource) {
        storage.put(slot.id(), resource);
    }

    public void remove(ResourceSlot<?> slot) {
        storage.remove(slot.id());
    }
}
