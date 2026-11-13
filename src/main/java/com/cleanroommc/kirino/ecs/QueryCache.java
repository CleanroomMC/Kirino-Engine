package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

final class QueryCache {
    static final int MIN_POOLS = 64;
    static final int CAPACITY = 64;
    static final int MAX_CONDITIONS = 64;
    static final int MAX_POOLS_PER_ENTRY = 256;
    static final int PROBE_LIMIT = 4;

    private final Entry[] entries = new Entry[CAPACITY];

    static int hash(ReferenceSet<Class<? extends CleanComponent>> required,
                    ReferenceSet<Class<? extends CleanComponent>> excluded) {
        int includeHash = 0;
        for (Class<?> type : required) {
            includeHash += System.identityHashCode(type);
        }
        int excludeHash = 0;
        for (Class<?> type : excluded) {
            excludeHash += System.identityHashCode(type);
        }
        int hash = includeHash + Integer.rotateLeft(excludeHash, 16)
            + 31 * required.size() + excluded.size();
        hash ^= hash >>> 16;
        hash *= 0x7feb352d;
        return hash ^ (hash >>> 15);
    }

    @Nullable
    ArchetypeDataPool[] find(int hash, ReferenceSet<Class<? extends CleanComponent>> required,
                             ReferenceSet<Class<? extends CleanComponent>> excluded) {
        for (int offset = 0; offset < PROBE_LIMIT; offset++) {
            Entry entry = entries[(hash + offset) & (CAPACITY - 1)];
            if (entry == null) {
                return null;
            }
            if (entry.hash == hash && matches(entry.required, required) && matches(entry.excluded, excluded)) {
                return entry.pools;
            }
        }
        return null;
    }

    void store(int hash, ReferenceSet<Class<? extends CleanComponent>> required,
               ReferenceSet<Class<? extends CleanComponent>> excluded, ArchetypeDataPool[] pools) {
        if ((long) required.size() + excluded.size() > MAX_CONDITIONS || pools.length > MAX_POOLS_PER_ENTRY) {
            return;
        }

        int slot = hash & (CAPACITY - 1);
        for (int offset = 0; offset < PROBE_LIMIT; offset++) {
            int candidate = (hash + offset) & (CAPACITY - 1);
            if (entries[candidate] == null) {
                slot = candidate;
                break;
            }
        }
        entries[slot] = new Entry(hash, required.toArray(Class<?>[]::new), excluded.toArray(Class<?>[]::new), pools);
    }

    void clear() {
        Arrays.fill(entries, null);
    }

    private static boolean matches(Class<?>[] snapshot, ReferenceSet<Class<? extends CleanComponent>> conditions) {
        if (snapshot.length != conditions.size()) {
            return false;
        }
        for (Class<?> type : snapshot) {
            if (!conditions.contains(type)) {
                return false;
            }
        }
        return true;
    }

    private record Entry(int hash, Class<?>[] required, Class<?>[] excluded, ArchetypeDataPool[] pools) {
    }
}
