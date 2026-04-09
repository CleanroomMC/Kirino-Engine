package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class MeshletBufferSlotAllocator {

    public record MeshletAdditionResult(int meshletId, int slot) {
    }

    public record MeshletRemovalResult(int meshletId, int removedSlot, @Nullable Integer swappedMeshletId) {
    }

    private final MeshletInputDoubleBuffer buffer;

    // key: meshlet id, value: buffer slot ordinal
    private final BiMap<Integer, Integer> meshletId2BufSlotMapping = HashBiMap.create();

    public MeshletBufferSlotAllocator(MeshletInputDoubleBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Assign buffer slots to newly added meshlet ids.
     * Slots are allocated densely from <code>0</code> to <code>{@link #getMeshletCount()} - 1</code>.
     * <br>
     * Must only be called at a synchronization point (<code>beginWriting</code>).
     */
    public @NonNull List<@NonNull MeshletAdditionResult> syncMeshletIdAddition(@NonNull List<@NonNull Integer> meshletIds) {
        Preconditions.checkNotNull(meshletIds);

        List<MeshletAdditionResult> results = new ArrayList<>(meshletIds.size());

        for (Integer meshletId : meshletIds) {
            Preconditions.checkNotNull(meshletId);
            Preconditions.checkArgument(!meshletId2BufSlotMapping.containsKey(meshletId),
                    "Meshlet ID=%s already has a buffer slot.", meshletId);

            int slot = meshletId2BufSlotMapping.size();
            meshletId2BufSlotMapping.put(meshletId, slot);

            results.add(new MeshletAdditionResult(meshletId, slot));
        }

        return results;
    }

    /**
     * Remove buffer slots for disposed meshlet ids.
     * Slots are allocated densely from <code>0</code> to <code>{@link #getMeshletCount()} - 1</code>.
     * <br>
     * Must only be called at a synchronization point (<code>beginWriting</code>).
     */
    public @NonNull List<@NonNull MeshletRemovalResult> syncMeshletIdRemoval(@NonNull List<@NonNull Integer> meshletIds) {
        Preconditions.checkNotNull(meshletIds);

        List<MeshletRemovalResult> results = new ArrayList<>(meshletIds.size());

        for (Integer meshletId : meshletIds) {
            Preconditions.checkNotNull(meshletId);

            Integer removedSlot = meshletId2BufSlotMapping.remove(meshletId);
            Preconditions.checkArgument(removedSlot != null,
                    "Meshlet ID=%s does not have a buffer slot.", meshletId);

            int lastSlot = meshletId2BufSlotMapping.size();
            Integer swappedMeshletId = null;

            // fill the gap if didn't remove the last entry
            if (removedSlot != lastSlot) {
                // definitely non null
                swappedMeshletId = meshletId2BufSlotMapping.inverse().get(lastSlot);

                // update the last entry to fill the gap
                meshletId2BufSlotMapping.put(swappedMeshletId, removedSlot);
            }

            results.add(new MeshletRemovalResult(meshletId, removedSlot, swappedMeshletId));
        }

        return results;
    }

    public void growBufferIfNeeded() {
        buffer.growToMatchSize();
        while (meshletId2BufSlotMapping.size() > buffer.getMaxMeshletInputCount()) {
            if (!buffer.grow()) {
                throw new RuntimeException("Failed to grow the write target buffer.");
            }
        }
    }

    public int getSlotForMeshletId(int meshletId) {
        Integer slot = meshletId2BufSlotMapping.get(meshletId);
        Preconditions.checkArgument(slot != null,
                "Invalid argument \"meshletId\"=%s. Can't find its corresponding buffer slot.", meshletId);

        return slot;
    }

    public int getMeshletCount() {
        return meshletId2BufSlotMapping.size();
    }
}
