package com.cleanroommc.kirino.engine.render.scene.gpu_meshlet;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeshletGpuRegistry {

    private final static int MESHLET_STRIDE_BYTE = 3600;

    private int meshletIdCounter;
    private final List<Integer> freeMeshletIds = new ArrayList<>();

    // key: meshletId, value: buffer slot ordinal
    private final Map<Integer, Integer> meshletId2BufSlotMapping = new HashMap<>();
    private final MeshletInputDoubleBuffer meshletInputBuffer;

    public MeshletGpuRegistry() {
        meshletInputBuffer = new MeshletInputDoubleBuffer();
    }

    public void lateInit() {
        meshletInputBuffer.lateInit();
    }

    private int getMaxMeshletInputCount() {
        return Math.ceilDivExact(meshletInputBuffer.getSize(), MESHLET_STRIDE_BYTE);
    }

    private synchronized int newMeshletID() {
        int meshletId;
        if (freeMeshletIds.isEmpty()) {
            meshletId = meshletIdCounter++;
        } else {
            meshletId = freeMeshletIds.removeLast();
        }

        KirinoCore.LOGGER.info("new meshlet ID");

        return meshletId;
    }

    public synchronized void disposeMeshletID(int meshletId) {
        freeMeshletIds.add(meshletId);

        KirinoCore.LOGGER.info("dispose meshlet ID");
    }

    public void fillMeshletID(MeshletComponent meshletComponent) {
        meshletComponent.gpuId = newMeshletID();
    }
}
