package com.cleanroommc.kirino.engine.resource;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.jspecify.annotations.NonNull;

public final class ResourceStorage {
    private final Int2ObjectMap<Object> storage = new Int2ObjectOpenHashMap<>();
    private final Int2BooleanMap resourceSealed = new Int2BooleanOpenHashMap();

    private ResourceStorage() {
    }

    private boolean sealed = false;

    public boolean isStorageSealed() {
        return sealed;
    }

    private void seal() {
        sealed = true;
    }

    public <T> boolean isResourceSealed(@NonNull ResourceSlot<T> slot) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkState(has(slot),
                "Resource \"%s\" isn't in the storage yet.", slot.type().getName());

        return isResourceSealedInternal(slot);
    }

    private <T> boolean isResourceSealedInternal(@NonNull ResourceSlot<T> slot) {
        if (sealed) {
            return true;
        }

        Boolean result = resourceSealed.get((Integer) slot.id());
        if (result == null) {
            return false;
        }

        return result;
    }

    public <T> void sealResource(@NonNull ResourceSlot<T> slot) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkState(has(slot),
                "Resource \"%s\" isn't in the storage yet.", slot.type().getName());

        resourceSealed.put(slot.id(), true);
    }

    public <T> boolean has(@NonNull ResourceSlot<T> slot) {
        Preconditions.checkNotNull(slot);

        return storage.containsKey(slot.id());
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <T> T get(@NonNull ResourceSlot<T> slot) {
        Preconditions.checkNotNull(slot);

        T result = (T) storage.get(slot.id());
        Preconditions.checkNotNull(result,
                "Can't resolve resource \"%s\".", slot.type().getName());

        return result;
    }

    public <T> void put(@NonNull ResourceSlot<T> slot, @NonNull T resource) {
        Preconditions.checkState(!sealed,
                "The storage is sealed. You are no longer allowed to make changes.");
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(resource);
        Preconditions.checkState(!isResourceSealedInternal(slot),
                "The resource \"%s\" is sealed. You are no longer allowed to make changes.", slot.type().getName());

        storage.put(slot.id(), resource);
    }

    public void remove(@NonNull ResourceSlot<?> slot) {
        Preconditions.checkState(!sealed,
                "The storage is sealed. You are no longer allowed to make changes.");
        Preconditions.checkNotNull(slot);
        Preconditions.checkState(!isResourceSealedInternal(slot),
                "The resource \"%s\" is sealed. You are no longer allowed to make changes.", slot.type().getName());

        storage.remove(slot.id());
    }
}
