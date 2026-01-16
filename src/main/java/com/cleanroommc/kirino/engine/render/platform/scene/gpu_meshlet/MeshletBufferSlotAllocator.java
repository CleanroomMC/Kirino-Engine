package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MeshletBufferSlotAllocator {
    private final MeshletInputDoubleBuffer buffer;

    // key: meshlet id, value: buffer slot ordinal
    private final Map<Integer, Integer> meshletId2BufSlotMapping = new HashMap<>();

    MeshletBufferSlotAllocator(MeshletInputDoubleBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Assign buffer slots to newly added meshlet ids.
     * Slots are allocated densely from <code>0</code> to <code>{@link #getMeshletCount()} - 1</code>.
     * <br>
     * Must only be called at a synchronization point (beginWriting).
     */
    public void syncMeshletIdAddition(List<Integer> meshletIds) {
        for (int meshletId : meshletIds) {
            Preconditions.checkArgument(!meshletId2BufSlotMapping.containsKey(meshletId),
                    "Meshlet ID=%s already has a buffer slot.", meshletId);

            int slot = meshletId2BufSlotMapping.size();
            meshletId2BufSlotMapping.put(meshletId, slot);
        }
    }

    /**
     * Remove buffer slots for disposed meshlet ids.
     * Slots are allocated densely from <code>0</code> to <code>{@link #getMeshletCount()} - 1</code>.
     * <br>
     * Must only be called at a synchronization point (beginWriting).
     */
    public void syncMeshletIdRemoval(List<Integer> meshletIds) {
        for (int meshletId : meshletIds) {
            Integer removedSlot = meshletId2BufSlotMapping.remove(meshletId);
            Preconditions.checkArgument(removedSlot != null,
                    "Meshlet ID=%s does not have a buffer slot.", meshletId);

            // fill the gap if didn't remove the last entry
            int lastSlot = meshletId2BufSlotMapping.size();
            if (removedSlot != lastSlot) {
                Optional<Integer> lastMeshletId = findMeshletIdBySlot(lastSlot);
                Preconditions.checkState(lastMeshletId.isPresent()); // impossible to throw

                // update the last entry to fill the gap
                meshletId2BufSlotMapping.put(lastMeshletId.get(), removedSlot);
            }
        }
    }

    public void growBufferIfNeeded() {
        buffer.growToMatchSize();
        while (meshletId2BufSlotMapping.size() > buffer.getMaxMeshletInputCount()) {
            if (!buffer.grow()) {
                throw new RuntimeException("Failed to grow the write target buffer.");
            }
        }
    }

    @NonNull
    public Optional<Integer> findMeshletIdBySlot(int slot) {
        for (Map.Entry<Integer, Integer> entry : meshletId2BufSlotMapping.entrySet()) {
            if (entry.getValue() == slot) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
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
