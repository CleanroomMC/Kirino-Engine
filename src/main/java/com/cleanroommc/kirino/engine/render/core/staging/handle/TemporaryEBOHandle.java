package com.cleanroommc.kirino.engine.render.core.staging.handle;

import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class TemporaryEBOHandle extends StagingBufferHandle<TemporaryEBOHandle> {
    protected final EBOView eboView; // turn off validation; handle preconditions manually here

    public TemporaryEBOHandle(StagingBufferManager stagingBufferManager, long generation, int maxLength, EBOView eboView) {
        super(stagingBufferManager, generation, 0, maxLength);
        this.eboView = eboView;
    }

    public int getEboID() {
        getterPreconditionsCheck();

        return eboView.bufferID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Allocated buffer size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        eboView.bind();
        eboView.uploadBySubData(offset, byteBuffer);
        eboView.bind(0);
    }
}
