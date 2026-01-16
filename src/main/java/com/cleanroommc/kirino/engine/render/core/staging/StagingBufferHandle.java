package com.cleanroommc.kirino.engine.render.core.staging;

import com.google.common.base.Preconditions;

import java.nio.ByteBuffer;

/**
 * This is a handle of a buffer slice.
 */
public abstract class StagingBufferHandle<T extends StagingBufferHandle<T>> {
    protected final StagingBufferManager stagingBufferManager;
    protected final int offset;
    protected final int maxLength;
    public final long generation;

    protected StagingBufferHandle(StagingBufferManager stagingBufferManager, long generation, int offset, int maxLength) {
        this.stagingBufferManager = stagingBufferManager;
        this.offset = offset;
        this.maxLength = maxLength;
        this.generation = generation;
    }

    protected final void getterPreconditionsCheck() {
        Preconditions.checkState(stagingBufferManager.active, "Must not access handles from StagingBufferManager when the manager is inactive.");
        Preconditions.checkState(generation == stagingBufferManager.getHandleGeneration(), "This handle is expired.");
    }

    @SuppressWarnings("unchecked")
    public final T write(int offset, ByteBuffer byteBuffer) {
        Preconditions.checkState(stagingBufferManager.active, "Must not access buffers from StagingBufferManager when the manager is inactive.");
        Preconditions.checkState(generation == stagingBufferManager.getHandleGeneration(), "This handle is expired.");

        writeInternal(offset, byteBuffer);
        return (T) this;
    }

    protected abstract void writeInternal(int offset, ByteBuffer byteBuffer);
}
