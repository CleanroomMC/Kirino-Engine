package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.render.staging.buffer.BufferStorage;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class PersistentEBOHandle extends StagingBufferHandle<PersistentEBOHandle> {
    public final long generation;
    private final BufferStorage.SlotHandle<EBOView> handle;
    private boolean expired = false;

    public PersistentEBOHandle(StagingBufferManager stagingBufferManager, long generation, int offset, int maxLength, BufferStorage.SlotHandle<EBOView> handle) {
        super(stagingBufferManager, offset, maxLength);
        this.generation = generation;
        this.handle = handle;

        this.handle.setReleaseCallback(handle1 -> {
            expired = true;
        });
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(!expired, "This temporary handle is expired.");
        Preconditions.checkState(generation == stagingBufferManager.getHandleGeneration(), "This temporary handle is expired.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Buffer slice size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        ByteBuffer byteBuffer0 = handle.getView().getPersistentMappedBuffer();
        int oldPos = byteBuffer0.position();
        byteBuffer0.position(this.offset + offset);
        byteBuffer0.put(byteBuffer);
        byteBuffer0.position(oldPos);
    }

    public Runnable getReleaseSlotAction() {
        Preconditions.checkState(!expired, "This temporary handle is expired.");
        Preconditions.checkState(generation == stagingBufferManager.getHandleGeneration(), "This temporary handle is expired.");

        return handle::release;
    }
}
