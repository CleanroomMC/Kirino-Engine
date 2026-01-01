package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;

/**
 * Note: this API performs <i><b>no validation or protection against illegal
 * OpenGL and driver-level behavior</b></i>. Clients are fully responsible for ensuring correctness.
 */
public abstract class BufferView {
    private boolean validation = false;

    /**
     * Enables validation for basic argument precondition checks.
     *
     * <p>Note: you should not turn on validation for hot paths.</p>
     */
    public final void turnOnValidation() {
        validation = true;
    }

    /**
     * Disables validation for basic argument precondition checks.
     *
     * <p>Note: you should not turn on validation for hot paths.</p>
     */
    public final void turnOffValidation() {
        validation = false;
    }

    public final boolean isValidationOn() {
        return validation;
    }

    public final GLBuffer buffer;
    public final int bufferID;

    public BufferView(GLBuffer buffer) {
        this.buffer = buffer;
        this.bufferID = buffer.bufferID;
    }

    public abstract int target();
    public abstract int bindingTarget();

    public void bind(int bufferID) {
        GL15.glBindBuffer(target(), bufferID);
    }

    public void bind() {
        bind(bufferID);
    }

    //<editor-fold desc="OpenGL buffer queries">
    /**
     * Note: this is a OpenGL query.
     */
    public int fetchCurrentBoundBufferID() {
        return GL11.glGetInteger(bindingTarget());
    }

    /**
     * Note: this is a OpenGL query.
     */
    public int fetchBufferSize() {
        return GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_SIZE);
    }

    /**
     * Note: this is a OpenGL query.
     */
    @NonNull
    public BufferUploadHint fetchBufferUploadHint() {
        int usage = GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_USAGE);
        if (usage == BufferUploadHint.STATIC_DRAW.glValue) {
            return BufferUploadHint.STATIC_DRAW;
        } else if (usage == BufferUploadHint.DYNAMIC_DRAW.glValue) {
            return BufferUploadHint.DYNAMIC_DRAW;
        } else if (usage == BufferUploadHint.STREAM_DRAW.glValue) {
            return BufferUploadHint.STREAM_DRAW;
        }
        throw new RuntimeException("Unknown GL_BUFFER_USAGE fetched.");
    }

    /**
     * Note: this is a OpenGL query.
     */
    public @NonNull MapBufferAccessBit @NonNull [] fetchMapBufferAccessBits() {
        int flags = GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_ACCESS_FLAGS);
        return Arrays.stream(MapBufferAccessBit.values())
                .filter(bit -> (flags & bit.glValue) != 0)
                .toArray(MapBufferAccessBit[]::new);
    }

    /**
     * Note: this is a OpenGL query.
     */
    public boolean fetchIsBufferMapped() {
        return GL15.glGetBufferParameteri(target(), GL15.GL_BUFFER_MAPPED) == GL11.GL_TRUE;
    }

    /**
     * Note: this is a OpenGL query.
     */
    public int fetchMapBufferOffset() {
        return GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_MAP_OFFSET);
    }

    /**
     * Note: this is a OpenGL query.
     */
    public int fetchMapBufferLength() {
        return GL15.glGetBufferParameteri(target(), GL30.GL_BUFFER_MAP_LENGTH);
    }
    //</editor-fold>

    /**
     * Allocates or re-specifies the storage and upload hint of this buffer without uploading any data.
     *
     * <p>This method call defines the buffer's size and upload hint, but leaves the buffer
     * contents undefined. Any previous data is discarded, and the buffer storage is guaranteed to be visible
     * to subsequent GPU commands issued after this call.</p>
     *
     * <p>Note: depending on driver behavior, this method call may cause implicit synchronization if the buffer is currently in use by the GPU.</p>
     *
     * @param size The byte size of the buffer
     * @param hint The upload hint
     */
    public void alloc(int size, @NonNull BufferUploadHint hint) {
        if (validation) {
            Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
            Preconditions.checkNotNull(hint);
        }

        GL15.glBufferData(target(), size, hint.glValue);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded using the {@link BufferUploadHint#STATIC_DRAW} hint.
     *
     * <p>This method call re-specifies the buffer storage, resets and current upload hint to {@link BufferUploadHint#STATIC_DRAW},
     * and copies the provided data into driver-managed memory.
     * The uploaded data is guaranteed to be visible to subsequent GPU commands issued after this call.</p>
     *
     * <p>Note: depending on driver behavior, this method call may cause implicit synchronization if the buffer is currently in use by the GPU.</p>
     *
     * @param byteBuffer The data
     */
    public void uploadDirectly(@NonNull ByteBuffer byteBuffer) {
        if (validation) {
            Preconditions.checkNotNull(byteBuffer);
        }

        GL15.glBufferData(target(), byteBuffer, GL15.GL_STATIC_DRAW);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded with the current upload hint and offset.
     *
     * <p>The data is written starting at the specified byte offset. The buffer
     * storage must have been previously allocated and the specified range must
     * lie entirely within the allocated size, and the new data is guaranteed to be visible
     * to subsequent GPU commands issued after this call.</p>
     *
     * <p>Note: depending on driver behavior, this method call may cause implicit synchronization if the buffer is currently in use by the GPU.</p>
     *
     * @param offset The byte offset in the target buffer where the data will be uploaded
     * @param byteBuffer The data
     */
    public void uploadBySubData(int offset, @NonNull ByteBuffer byteBuffer) {
        if (validation) {
            Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
            Preconditions.checkArgument(offset + byteBuffer.remaining() <= fetchBufferSize(),
                    "Allocated buffer size must be greater than or equal to \"offset + byteBuffer.remaining()\".");
            Preconditions.checkNotNull(byteBuffer);
        }

        GL15.glBufferSubData(target(), offset, byteBuffer);
    }

    /**
     * The data that corresponds to <code>byteBuffer.remaining()</code> will be uploaded.
     *
     * @param mappingOffset The starting byte offset in the target buffer to map
     * @param mappingSize The byte size of the target buffer region to map
     * @param offset The byte offset in the mapped buffer where the data will be uploaded
     * @param byteBuffer The data
     * @param accessBits The access bits
     */
    public void uploadByMapBuffer(int mappingOffset, int mappingSize, int offset, @NonNull ByteBuffer byteBuffer, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        if (validation) {
            Preconditions.checkArgument(mappingSize >= 0, "Cannot have a negative buffer size.");
            Preconditions.checkArgument(!(mappingOffset < 0 || offset < 0), "Cannot have a negative offset.");
            Preconditions.checkArgument(mappingOffset + mappingSize <= fetchBufferSize(),
                    "Allocated buffer size must be greater than or equal to \"mappingOffset + mappingSize\".");
            Preconditions.checkArgument(offset + byteBuffer.remaining() <= mappingSize,
                    "Argument \"mappingSize\" must be greater than or equal to \"offset + byteBuffer.remaining()\".");
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(accessBits);
            for (MapBufferAccessBit bit : accessBits) {
                Preconditions.checkNotNull(bit);
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        ByteBuffer mappedBuffer = GL30.glMapBufferRange(
                target(),
                mappingOffset,
                mappingSize,
                access,
                null);

        if (mappedBuffer != null) {
            // key assumption (valid for all supported LWJGL/OpenGL desktop platforms):
            // CPU and GPU use the same native endianness (little endian in practice)
            mappedBuffer.order(ByteOrder.nativeOrder());

            mappedBuffer.position(offset);
            mappedBuffer.put(byteBuffer);
            boolean success = GL15.glUnmapBuffer(target());
            if (!success) {
                throw new RuntimeException("Buffer unmap failed, data may be corrupted.");
            }
        } else {
            throw new RuntimeException("Failed to map buffer.");
        }
    }

    protected ByteBuffer persistentMappedBuffer = null;

    /**
     * This method is not suitable for hot paths.
     * The result should be validated once and then cached if needed.
     *
     * @return The persistently mapped buffer
     */
    @NonNull
    public final Optional<ByteBuffer> getPersistentMappedBuffer() {
        return Optional.ofNullable(persistentMappedBuffer);
    }

    public void allocPersistent(int size, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        if (validation) {
            Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
            Preconditions.checkNotNull(accessBits);
            for (MapBufferAccessBit bit : accessBits) {
                Preconditions.checkNotNull(bit);
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        GL44C.glBufferStorage(target(), size, access);
    }

    public void mapPersistent(int offset, int length, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        if (validation) {
            Preconditions.checkState(persistentMappedBuffer == null, "Buffer already mapped persistently.");
            Preconditions.checkArgument(offset >= 0, "Cannot have a negative offset.");
            Preconditions.checkArgument(offset + length <= fetchBufferSize(),
                    "Allocated buffer size must be greater than or equal to \"offset + length\".");
            Preconditions.checkNotNull(accessBits);
            for (MapBufferAccessBit bit : accessBits) {
                Preconditions.checkNotNull(bit);
            }
        }

        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }

        persistentMappedBuffer = GL44C.glMapBufferRange(target(), offset, length, access, persistentMappedBuffer);

        if (persistentMappedBuffer == null) {
            throw new RuntimeException("Failed to map persistent buffer.");
        }

        // key assumption (valid for all supported LWJGL/OpenGL desktop platforms):
        // CPU and GPU use the same native endianness (little endian in practice)
        persistentMappedBuffer.order(ByteOrder.nativeOrder());
    }

    public void unmapPersistent() {
        if (validation) {
            Preconditions.checkState(persistentMappedBuffer != null, "Buffer not persistently mapped.");
        }

        boolean success = GL15.glUnmapBuffer(target());
        if (!success) {
            throw new RuntimeException("Persistent buffer unmap failed, data may be corrupted.");
        }

        persistentMappedBuffer = null;
    }
}
