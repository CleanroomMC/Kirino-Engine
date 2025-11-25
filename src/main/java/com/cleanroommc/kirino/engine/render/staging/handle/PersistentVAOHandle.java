package com.cleanroommc.kirino.engine.render.staging.handle;

import com.cleanroommc.kirino.engine.render.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;

import java.nio.ByteBuffer;

public class PersistentVAOHandle extends StagingBufferHandle<PersistentVAOHandle> {
    public final long generation;

    public PersistentVAOHandle(StagingBufferManager stagingBufferManager, long generation) {
        super(stagingBufferManager, 0, 0);
        this.generation = generation;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        throw new RuntimeException("VAO handle doesn't support write.");
    }
}
