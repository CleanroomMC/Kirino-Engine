package com.cleanroommc.kirino.engine.resource;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;

public final class ResourceStorage {
    private final Int2ObjectMap<Object> storage = new Int2ObjectOpenHashMap<>();

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
