package com.cleanroommc.kirino.engine.render.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.render.ecs.component.MeshletComponent;

import java.util.concurrent.atomic.AtomicLong;

public class MeshletGpuRegistry {
    private final AtomicLong meshletIdCounter;

    private long nextMeshletID() {
        return meshletIdCounter.getAndIncrement();
    }

    /**
     * Returns low 32 bits.
     */
    private static int splitLongLow(long value) {
        return (int) value;
    }

    /**
     * Returns high 32 bits.
     */
    private static int splitLongHigh(long value) {
        return (int) (value >>> 32);
    }

    public void fillMeshletID(MeshletComponent meshletComponent) {
        long id = nextMeshletID();
        meshletComponent.id0 = splitLongLow(id);
        meshletComponent.id1 = splitLongHigh(id);
    }

    private final MeshletInputDoubleBuffer meshletInput;

    public MeshletGpuRegistry() {
        meshletIdCounter = new AtomicLong();
        meshletInput = new MeshletInputDoubleBuffer();
    }

    public void lateInit() {
        meshletInput.lateInit();
    }
}
