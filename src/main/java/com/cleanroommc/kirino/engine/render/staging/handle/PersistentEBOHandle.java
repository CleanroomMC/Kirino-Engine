package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.BufferStorage;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class PersistentEBOHandle extends StagingBufferHandle<PersistentEBOHandle> {
    private final BufferStorage.SlotHandle<EBOView> handle;
    private boolean expired = false;

    public PersistentEBOHandle(StagingBufferManager stagingBufferManager, long generation, int offset, int maxLength, BufferStorage.SlotHandle<EBOView> handle) {
        super(stagingBufferManager, generation, offset, maxLength);
        this.handle = handle;

        this.handle.setReleaseCallback(handle1 -> {
            expired = true;
        });
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(!expired, "This handle is expired.");
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Buffer slice size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        ByteBuffer byteBuffer0 = handle.getView().getPersistentMappedBuffer();
        int oldPos = byteBuffer0.position();
        byteBuffer0.position(this.offset + offset);
        byteBuffer0.put(byteBuffer);
        byteBuffer0.position(oldPos);
    }

    public Runnable getSlotReleaseAction() {
        getterPreconditionsCheck();
        Preconditions.checkState(!expired, "This handle is expired.");

        return handle::release;
    }

    public int getSlotPageIndex() {
        getterPreconditionsCheck();
        Preconditions.checkState(!expired, "This handle is expired.");

        return handle.getPageIndex();
    }

    public int getSlotOffset() {
        getterPreconditionsCheck();
        Preconditions.checkState(!expired, "This handle is expired.");

        return handle.getOffset();
    }

    public int getSlotSize() {
        getterPreconditionsCheck();
        Preconditions.checkState(!expired, "This handle is expired.");

        return handle.getSize();
    }
}
