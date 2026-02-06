package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer.MeshletInputDoubleBuffer;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;

/**
 * Only valid during the {@link MeshletGpuRegistry#beginWriting()} to {@link MeshletGpuRegistry#finishWriting()} period.
 */
public class MeshletGpuWriterContext {
    private final ResourceStorage storage;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

    public MeshletGpuWriterContext(ResourceStorage storage, ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {
        this.storage = storage;
        this.meshletGpuRegistry = meshletGpuRegistry;
    }

    /**
     * Use one view per thread.
     *
     * @return The view of a persistently mapped buffer
     */
    @NonNull
    public ByteBuffer getNewByteBufferView() {
        Optional<ByteBuffer> optional = storage.get(meshletGpuRegistry).meshletInputBuffer.getWriteTarget().getPersistentMappedBuffer();
        Preconditions.checkState(optional.isPresent()); // impossible to throw

        ByteBuffer byteBuffer = optional.get();
        ByteOrder oldOrder = byteBuffer.order();
        return byteBuffer.duplicate().order(oldOrder);
    }

    public int getByteBufferPosition(int meshletId) {
        return storage.get(meshletGpuRegistry).meshletBufferSlotAllocator.getSlotForMeshletId(meshletId) * MeshletInputDoubleBuffer.MESHLET_STRIDE_BYTE;
    }
}
