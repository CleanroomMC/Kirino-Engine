package com.cleanroommc.kirino.engine.render.scene.gpu_meshlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshletBufferSlotAllocator {
    private final MeshletInputDoubleBuffer buffer;

    // key: meshlet id, value: buffer slot ordinal
    private final Map<Integer, Integer> meshletId2BufSlotMapping = new HashMap<>();

    public MeshletBufferSlotAllocator(MeshletInputDoubleBuffer buffer) {
        this.buffer = buffer;
    }

    public void syncMeshletIdAddition(List<Integer> ids) {

    }

    public void syncMeshletIdRemoval(List<Integer> ids) {

    }
}
