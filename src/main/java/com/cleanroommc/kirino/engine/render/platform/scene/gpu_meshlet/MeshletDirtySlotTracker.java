package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MeshletDirtySlotTracker {

    private final List<Integer> dirtySlots = new ArrayList<>();
    private final Set<Integer> dirtySlotSet = new HashSet<>();

    public void markDirty(int slot) {
        if (dirtySlotSet.add(slot)) {
            dirtySlots.add(slot);
        }
    }

    public List<Integer> snapshot() {
        return List.copyOf(dirtySlots);
    }

    public boolean isEmpty() {
        return dirtySlots.isEmpty();
    }

    public void clear() {
        dirtySlots.clear();
        dirtySlotSet.clear();
    }
}