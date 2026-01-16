package com.cleanroommc.kirino.engine.render.core.staging.handle;

import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

public class TemporaryVBOHandle extends StagingBufferHandle<TemporaryVBOHandle> {
    protected final VBOView vboView; // turn off validation; handle preconditions manually here

    public TemporaryVBOHandle(StagingBufferManager stagingBufferManager, long generation, int maxLength, VBOView vboView) {
        super(stagingBufferManager, generation, 0, maxLength);
        this.vboView = vboView;
    }

    public int getVboID() {
        getterPreconditionsCheck();

        return vboView.bufferID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(offset + byteBuffer.remaining() <= maxLength,
                "Allocated buffer size must be greater than or equal to \"offset + byteBuffer.remaining()\".");

        vboView.bind();
        vboView.uploadBySubData(offset, byteBuffer);
        vboView.bind(0);
    }
}
