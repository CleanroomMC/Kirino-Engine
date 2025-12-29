package com.cleanroommc.kirino.engine.render.scene.gpu_meshlet;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public class MeshletGpuWriter {
    private final MeshletGpuRegistry meshletGpuRegistry;

    public MeshletGpuWriter(MeshletGpuRegistry meshletGpuRegistry) {
        this.meshletGpuRegistry = meshletGpuRegistry;
    }

    /**
     * Use one view per thread.
     *
     * @return The view of a persistently mapped buffer
     */
    @NonNull
    public ByteBuffer getNewByteBufferView() {
        ByteBuffer byteBuffer = meshletGpuRegistry.meshletInputBuffer.getWriteTarget().getPersistentMappedBuffer();
        Preconditions.checkState(byteBuffer != null,
                "The persistently mapped buffer is null.");

        return byteBuffer.duplicate();
    }

    /**
     * Provide the correct byte buffer and all the info, and write to gpu.
     *
     * @param byteBuffer The byte buffer that comes from {@link #getNewByteBufferView()}
     */
    public void writeMeshlet(ByteBuffer byteBuffer, int meshletId) {
        int pos = meshletGpuRegistry.meshletBufferSlotAllocator.getSlotForMeshletId(meshletId) * MeshletInputDoubleBuffer.MESHLET_STRIDE_BYTE;

    }
}
