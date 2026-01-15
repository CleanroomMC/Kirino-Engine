package com.cleanroommc.kirino.engine.resource;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class ResourceStorage {
    private final Map<Integer, Object> storage = new HashMap<>();

    public <T> boolean has(ResourceSlot<T> slot) {
        return storage.containsKey(slot.id());
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull ResourceSlot<T> slot) {
        T result = (T) storage.get(slot.id());
        Preconditions.checkNotNull(result,
                "Can't resolve resource \"%s\".", slot.type().getName());

        return result;
    }

    public <T> void put(@NonNull ResourceSlot<T> slot, @NonNull T resource) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(resource);

        storage.put(slot.id(), resource);
    }

    public void remove(@NonNull ResourceSlot<?> slot) {
        Preconditions.checkNotNull(slot);

        storage.remove(slot.id());
    }
}
