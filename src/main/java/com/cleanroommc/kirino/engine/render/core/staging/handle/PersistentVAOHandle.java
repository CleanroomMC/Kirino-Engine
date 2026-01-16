package com.cleanroommc.kirino.engine.render.core.staging.handle;

import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferHandle;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.gl.vao.VAO;

import java.nio.ByteBuffer;

public class PersistentVAOHandle extends StagingBufferHandle<PersistentVAOHandle> {
    private final VAO vao;

    public PersistentVAOHandle(StagingBufferManager stagingBufferManager, long generation, VAO vao) {
        super(stagingBufferManager, generation, 0, 0);
        this.vao = vao;
    }

    public int getVaoID() {
        getterPreconditionsCheck();

        return vao.vaoID;
    }

    @Override
    protected void writeInternal(int offset, ByteBuffer byteBuffer) {
        throw new RuntimeException("VAO handle doesn't support write.");
    }
}
