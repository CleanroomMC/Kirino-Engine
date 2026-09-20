package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Optional;

/**
 * <p>This API performs <i><b>no validation or protection against illegal OpenGL or
 * driver-level behavior</b></i> unless validation is explicitly enabled. Validation only
 * covers Java-side preconditions and range checks; it does not replace OpenGL
 * validation or synchronization.</p>
 *
 * <p>In target-bound mode, call {@link #bind()} before using the view and keep the binding unchanged.
 * In DSA mode, operations with a named-buffer equivalent act on {@link #bufferID} without
 * reading or modifying the target binding.</p>
 */
public abstract class BufferView {

    private boolean validation = false;

    /**
     * Enables basic argument and range validation.
     *
     * <p>Some range checks issue synchronous OpenGL queries. Enable validation while developing,
     * not on hot paths.</p>
     */
    public final void turnOnValidation() {
        validation = true;
    }

    /**
     * Disables basic argument and range validation.
     */
    public final void turnOffValidation() {
        validation = false;
    }

    public final boolean isValidationOn() {
        return validation;
    }

    public final GLBuffer buffer;
    public final int bufferID;

    public final boolean dsa;

    /**
     * Creates a target-bound view.
     *
     * <p>This preserves the original behavior. Call {@link #bind()} before operations that act on
     * {@link #target()}.</p>
     */
    public BufferView(@NonNull GLBuffer buffer) {
        this(buffer, false);
    }

    /**
     * <p>When <code>dsa</code> is true, the buffer must be an existing object suitable for
     * named-buffer calls. (i.e. instantiated buffer)</p>
     *
     * @param buffer The underlying buffer
     * @param dsa Whether to use OpenGL 4.5+ named-buffer entry points
     *
     * @see GLBuffer
     */
    public BufferView(@NonNull GLBuffer buffer, boolean dsa) {
        Preconditions.checkNotNull(buffer);

        this.buffer = buffer;
        this.bufferID = buffer.bufferID;
        this.dsa = dsa;
    }

    public final boolean isDsaView() {
        return dsa;
    }

    public abstract int target();
    public abstract int bindingTarget();

    /**
     * Binds an arbitrary buffer name to this view's target.
     *
     * <p>This always modifies OpenGL binding state, including in DSA mode.</p>
     */
    public void bind(int bufferID) {
        GL15.glBindBuffer(target(), bufferID);
    }

    /**
     * Binds this view's buffer to this view's target.
     *
     * <p>This always modifies OpenGL binding state, including in DSA mode.</p>
     */
    public void bind() {
        bind(bufferID);
    }

    private static int combineAccessBits(@NonNull MapBufferAccessBit @NonNull ... accessBits) {
        int access = 0;
        for (MapBufferAccessBit bit : accessBits) {
            access |= bit.glValue;
        }
        return access;
    }

    private static void validateAccessBits(@NonNull MapBufferAccessBit @NonNull ... accessBits) {
        Preconditions.checkNotNull(accessBits);
        for (MapBufferAccessBit bit : accessBits) {
            Preconditions.checkNotNull(bit);
        }
    }

    private static void validateRange(long offset, long size, long bufferSize) {
        Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
        Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
        Preconditions.checkArgument(offset <= bufferSize && size <= bufferSize - offset,
                "Allocated buffer size must be greater than or equal to \"offset + size\". (bufferSize=%s, offset=%s, size=%s)",
                bufferSize, offset, size);
    }

    //<editor-fold desc="OpenGL buffer queries">
    /**
     * Returns the buffer currently bound to this view's target.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public int fetchCurrentBoundBufferID() {
        return GL11.glGetInteger(bindingTarget());
    }

    /**
     * Returns an arbitrary integer buffer parameter.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     * <p>Note: This is the generic query method. It queries {@link #bufferID} in DSA
     * mode and the buffer currently bound to {@link #target()} otherwise.</p>
     */
    public int fetchBufferParameter(int parameter) {
        return dsa
                ? GL45C.glGetNamedBufferParameteri(bufferID, parameter)
                : GL15.glGetBufferParameteri(target(), parameter);
    }

    /**
     * Returns an arbitrary 64-bit buffer parameter.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     * <p>Note: This is the 64-bit generic query method.</p>
     */
    public long fetchBufferParameter64(int parameter) {
        return dsa
                ? GL45C.glGetNamedBufferParameteri64(bufferID, parameter)
                : GL32C.glGetBufferParameteri64(target(), parameter);
    }

    /**
     * Returns the buffer size, narrowed to 32-bit.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public int fetchBufferSize() {
        return fetchBufferParameter(GL15.GL_BUFFER_SIZE);
    }

    /**
     * Returns the buffer size without narrowing it.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public long fetchBufferSize64() {
        return fetchBufferParameter64(GL15.GL_BUFFER_SIZE);
    }

    /**
     * Returns the buffer's current usage hint.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     * <p>Note: The result is the hint supplied at the last mutable-storage re-specification. It does not
     * describe actual driver placement.</p>
     */
    @NonNull
    public BufferUploadHint fetchBufferUploadHint() {
        int usage = fetchBufferParameter(GL15.GL_BUFFER_USAGE);
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
     * Returns the access flags of the current mapping.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public @NonNull MapBufferAccessBit @NonNull [] fetchMapBufferAccessBits() {
        int flags = fetchBufferParameter(GL30.GL_BUFFER_ACCESS_FLAGS);
        return Arrays.stream(MapBufferAccessBit.values())
                .filter(bit -> (flags & bit.glValue) != 0)
                .toArray(MapBufferAccessBit[]::new);
    }

    /**
     * Returns whether the buffer is currently mapped.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public boolean fetchIsBufferMapped() {
        return fetchBufferParameter(GL15.GL_BUFFER_MAPPED) == GL11.GL_TRUE;
    }

    /** Returns the current mapped offset, narrowed to 32 bits.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public int fetchMapBufferOffset() {
        return fetchBufferParameter(GL30.GL_BUFFER_MAP_OFFSET);
    }

    /**
     * Returns the current mapped offset without narrowing it.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public long fetchMapBufferOffset64() {
        return fetchBufferParameter64(GL30.GL_BUFFER_MAP_OFFSET);
    }

    /**
     * Returns the current mapped length, narrowed to 32 bits.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public int fetchMapBufferLength() {
        return fetchBufferParameter(GL30.GL_BUFFER_MAP_LENGTH);
    }

    /**
     * Returns the current mapped length without narrowing it.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public long fetchMapBufferLength64() {
        return fetchBufferParameter64(GL30.GL_BUFFER_MAP_LENGTH);
    }

    /**
     * Returns whether the buffer has immutable storage.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public boolean fetchIsImmutableStorage() {
        return fetchBufferParameter(GL44C.GL_BUFFER_IMMUTABLE_STORAGE) == GL11.GL_TRUE;
    }

    /**
     * Returns the flags used to create immutable storage.
     *
     * <p>Note: This is a synchronous OpenGL state query and should not be used on hot paths.</p>
     */
    public int fetchBufferStorageFlags() {
        return fetchBufferParameter(GL44C.GL_BUFFER_STORAGE_FLAGS);
    }
    //</editor-fold>

    //<editor-fold desc="mutable storage and data transfer">
    /**
     * Allocates or re-specifies mutable buffer storage without uploading data.
     *
     * <p>This calls <code>glBufferData(...NULL...)</code> or <code>glNamedBufferData(...NULL...)</code>.
     * Previous storage and data are discarded and the new contents are undefined.</p>
     *
     * <p>Note: Existing mappings prevent re-specification.</p>
     * <p>Note: The driver may orphan old storage, but that is not guaranteed.</p>
     * <p>Note: Re-specification may synchronize if storage still used by the GPU cannot be orphaned.</p>
     * <p>Note: Immutable storage created by <code>allocImmutable</code> cannot be re-specified.</p>
     *
     * <p><b>Suggestion</b>: After this call, use <code>uploadBySubData</code> for ordinary uploads or a
     * mapping path for streaming. Do not immediately call <code>uploadDirectly</code>:
     * that re-specifies storage again and makes this allocation redundant.</p>
     */
    public void alloc(long size, @NonNull BufferUploadHint hint) {
        if (validation) {
            Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
            Preconditions.checkNotNull(hint);
        }

        if (dsa) {
            GL45C.glNamedBufferData(bufferID, size, hint.glValue);
        } else {
            GL15.glBufferData(target(), size, hint.glValue);
        }
    }

    /**
     * @see #alloc(long, BufferUploadHint)
     */
    public void alloc(int size, @NonNull BufferUploadHint hint) {
        alloc((long) size, hint);
    }

    /**
     * Re-specifies mutable buffer storage and uploads <code>byteBuffer.remaining()</code> bytes using
     * {@link BufferUploadHint#STATIC_DRAW}.
     *
     * <p>Note: This is a complete storage re-specification, not an update of storage created by <code>alloc</code>.</p>
     * <p>Note: The driver may orphan old storage, but that is not guaranteed.</p>
     * <p>Note: Re-specification may synchronize if storage still used by the GPU cannot be orphaned.</p>
     * <p>Note: Immutable storage created by <code>allocImmutable</code> cannot be re-specified.</p>
     *
     * <p><b>Suggestion</b>: Prefer it for one-call initialization. Use
     * <code>uploadBySubData</code> to update existing storage.</p>
     */
    public void uploadDirectly(@NonNull ByteBuffer byteBuffer) {
        uploadDirectly(byteBuffer, BufferUploadHint.STATIC_DRAW);
    }

    /**
     * Re-specifies mutable buffer storage and uploads <code>byteBuffer.remaining()</code> bytes.
     *
     * <p>This is the generic one-call initialization path. It replaces both previous storage and
     * its usage hint, and cannot be used with immutable storage.</p>
     *
     * @see #uploadDirectly(ByteBuffer)
     */
    public void uploadDirectly(@NonNull ByteBuffer byteBuffer, @NonNull BufferUploadHint hint) {
        if (validation) {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(hint);
        }

        if (dsa) {
            GL45C.glNamedBufferData(bufferID, byteBuffer, hint.glValue);
        } else {
            GL15.glBufferData(target(), byteBuffer, hint.glValue);
        }
    }

    /**
     * Uploads <code>byteBuffer.remaining()</code> bytes into existing storage.
     * This does not change buffer size or usage hint.
     *
     * <p>Note: The destination range must exist.</p>
     * <p>Note: Immutable storage requires <code>GL_DYNAMIC_STORAGE_BIT</code>.</p>
     * <p>Note: Command ordering makes the data visible to later GL commands,
     * but the driver may synchronize if the range is still in use.</p>
     */
    public void uploadBySubData(long offset, @NonNull ByteBuffer byteBuffer) {
        if (validation) {
            Preconditions.checkNotNull(byteBuffer);
            validateRange(offset, byteBuffer.remaining(), fetchBufferSize64());
        }

        if (dsa) {
            GL45C.glNamedBufferSubData(bufferID, offset, byteBuffer);
        } else {
            GL15.glBufferSubData(target(), offset, byteBuffer);
        }
    }

    /**
     * @see #uploadBySubData(long, ByteBuffer)
     */
    public void uploadBySubData(int offset, @NonNull ByteBuffer byteBuffer) {
        uploadBySubData((long) offset, byteBuffer);
    }

    /**
     * Reads data into <code>byteBuffer.remaining()</code> bytes of destination space.
     *
     * <p>Note: Readback commonly waits for pending GPU writes to the requested range and should
     * not be used on a hot path.</p>
     */
    public void downloadBySubData(long offset, @NonNull ByteBuffer byteBuffer) {
        if (validation) {
            Preconditions.checkNotNull(byteBuffer);
            validateRange(offset, byteBuffer.remaining(), fetchBufferSize64());
        }

        if (dsa) {
            GL45C.glGetNamedBufferSubData(bufferID, offset, byteBuffer);
        } else {
            GL15.glGetBufferSubData(target(), offset, byteBuffer);
        }
    }

    /**
     * @see #downloadBySubData(long, ByteBuffer)
     */
    public void downloadBySubData(int offset, @NonNull ByteBuffer byteBuffer) {
        downloadBySubData((long) offset, byteBuffer);
    }

    /**
     * Copies a byte range from this buffer into another buffer.
     *
     * <p>DSA mode does not change bindings. Target-bound mode uses <code>GL_COPY_READ_BUFFER</code> and
     * <code>GL_COPY_WRITE_BUFFER</code>; those bindings are intentionally left changed.</p>
     *
     * <p>Note: This is a GPU command. Take care of explicit synchronization when needed.</p>
     */
    public void copyTo(@NonNull BufferView destination, long readOffset, long writeOffset, long size) {
        if (validation) {
            Preconditions.checkNotNull(destination);
            Preconditions.checkArgument(size >= 0, "Buffer copy size must be no smaller than zero.");
            if (bufferID == destination.bufferID) {
                Preconditions.checkArgument(readOffset + size <= writeOffset || writeOffset + size <= readOffset,
                        "Source and destination ranges must not overlap when copying within the same buffer.");
            }
        }

        if (dsa) {
            GL45C.glCopyNamedBufferSubData(bufferID, destination.bufferID, readOffset, writeOffset, size);
        } else {
            GL15.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, bufferID);
            GL15.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, destination.bufferID);
            GL31C.glCopyBufferSubData(GL31C.GL_COPY_READ_BUFFER, GL31C.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
        }
    }

    /**
     * @see #copyTo(BufferView, long, long, long)
     */
    public void copyFrom(@NonNull BufferView source, long readOffset, long writeOffset, long size) {
        if (validation) {
            Preconditions.checkNotNull(source);
            Preconditions.checkArgument(size >= 0, "Buffer copy size must be no smaller than zero.");
            if (source.bufferID == bufferID) {
                Preconditions.checkArgument(readOffset + size <= writeOffset || writeOffset + size <= readOffset,
                        "Source and destination ranges must not overlap when copying within the same buffer.");
            }
        }

        if (dsa) {
            GL45C.glCopyNamedBufferSubData(source.bufferID, bufferID, readOffset, writeOffset, size);
        } else {
            GL15.glBindBuffer(GL31C.GL_COPY_READ_BUFFER, source.bufferID);
            GL15.glBindBuffer(GL31C.GL_COPY_WRITE_BUFFER, bufferID);
            GL31C.glCopyBufferSubData(GL31C.GL_COPY_READ_BUFFER, GL31C.GL_COPY_WRITE_BUFFER, readOffset, writeOffset, size);
        }
    }
    //</editor-fold>

    //<editor-fold desc="transient mapping">
    /**
     * Maps a range and returns a native-order view of the mapped buffer storage.
     *
     * <p>Note: Mapping does not synchronize later CPU writes
     * with GPU use. Select <code>access</code>, <code>invalidation</code>, <code>unsynchronized</code>,
     * <code>explicit-flush</code>, <code>persistent</code>, and <code>coherent</code> flags according to the OpenGL mapping contract.</p>
     */
    @NonNull
    public ByteBuffer mapRange(long offset, int length, int access) {
        if (validation) {
            Preconditions.checkArgument(length > 0, "Buffer mapping length must be greater than zero.");
            validateRange(offset, length, fetchBufferSize64());
        }

        ByteBuffer mappedBuffer = dsa
                ? GL45C.glMapNamedBufferRange(bufferID, offset, length, access, null)
                : GL30.glMapBufferRange(target(), offset, length, access, null);

        if (mappedBuffer == null) {
            throw new RuntimeException("Failed to map buffer.");
        }

        // key assumption, valid for supported LWJGL/OpenGL desktop platforms:
        // CPU and GPU use the same native endianness (little endian in practice)
        return mappedBuffer.order(ByteOrder.nativeOrder());
    }

    /**
     * Maps a range using typed access bits.
     *
     * @see #mapRange(long, int, int)
     */
    @NonNull
    public ByteBuffer mapRange(
            long offset,
            int length,
            @NonNull MapBufferAccessBit @NonNull ... accessBits) {

        if (validation) {
            validateAccessBits(accessBits);
        }

        return mapRange(offset, length, combineAccessBits(accessBits));
    }

    /**
     * Maps a range, copies <code>byteBuffer.remaining()</code> bytes into it, then unmaps it.
     *
     * <p>Note: <code>offset</code> is relative to the mapped range, not the buffer.</p>
     * <p>Note: Unmapping makes ordinary non-persistent writes available to later OpenGL commands.
     * It guarantees visibility but does not eliminate reuse hazards in short.</p>
     */
    public void uploadByMapBuffer(
            int mappingOffset,
            int mappingSize,
            int offset,
            @NonNull ByteBuffer byteBuffer,
            @NonNull MapBufferAccessBit @NonNull ... accessBits) {

        if (validation) {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkArgument(offset >= 0, "Cannot have a negative offset.");
            Preconditions.checkArgument((long) offset + byteBuffer.remaining() <= mappingSize,
                    "Argument \"mappingSize\"=%s must be greater than or equal to \"offset + byteBuffer.remaining()\"=%s.",
                    mappingSize, (long) offset + byteBuffer.remaining());
            validateAccessBits(accessBits);
        }

        ByteBuffer mappedBuffer = mapRange(mappingOffset, mappingSize, combineAccessBits(accessBits));
        mappedBuffer.position(offset);
        mappedBuffer.put(byteBuffer);

        if (!unmap()) {
            throw new RuntimeException("Buffer unmap failed, data may be corrupted.");
        }
    }

    /**
     * Flushes a range of a mapping created with <code>GL_MAP_FLUSH_EXPLICIT_BIT</code>.
     *
     * <p>Note: Offset and length are relative to the mapped buffer and must lie inside the mapped range.</p>
     * <p>Note: This makes CPU writes visible to OpenGL.</p>
     */
    public void flushMappedRange(long offset, long length) {
        if (validation) {
            Preconditions.checkArgument(offset >= 0, "Cannot have a negative buffer offset.");
            Preconditions.checkArgument(length >= 0, "Cannot have a negative buffer length.");
            validateRange(offset, length, fetchMapBufferLength64());
        }

        if (dsa) {
            GL45C.glFlushMappedNamedBufferRange(bufferID, offset, length);
        } else {
            GL30C.glFlushMappedBufferRange(target(), offset, length);
        }
    }

    /**
     * Returns <code>false</code> if OpenGL detected corruption of the mapped storage.
     *
     * <p>Note: Must only call when the buffer is mapped.</p>
     */
    public boolean unmap() {
        return dsa
                ? GL45C.glUnmapNamedBuffer(bufferID)
                : GL15.glUnmapBuffer(target());
    }
    //</editor-fold>

    //<editor-fold desc="GPU clear and invalidation">
    /**
     * Fills the complete buffer with a clear value.
     *
     * <p>Note: <code>data == null</code> supplies zero.</p>
     * <p>Note: The internal format must be a valid sized buffer-clear format.</p>
     * <p>Note: This enqueues a GPU-side clear command and does not wait.
     * CPU access through persistent mappings, in-flight range reuse, etc. require explicit synchronization.</p>
     */
    public void clear(int internalFormat, int format, int type, @Nullable ByteBuffer data) {
        if (dsa) {
            GL45C.glClearNamedBufferData(bufferID, internalFormat, format, type, data);
        } else {
            GL43.glClearBufferData(target(), internalFormat, format, type, data);
        }
    }

    /**
     * Fills a byte range with a clear value.
     *
     * <p>Note: Offset and size must be aligned to the byte size of the internal format.</p>
     * <p>Note: <code>data == null</code> supplies zero.</p>
     * <p>Note: Synchronization requirements are the same as for {@link #clear(int, int, int, ByteBuffer)}.</p>
     */
    public void clearSubData(
            int internalFormat,
            long offset,
            long size,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        if (validation) {
            validateRange(offset, size, fetchBufferSize64());
        }

        if (dsa) {
            GL45C.glClearNamedBufferSubData(bufferID, internalFormat, offset, size, format, type, data);
        } else {
            GL43.glClearBufferSubData(target(), internalFormat, offset, size, format, type, data);
        }
    }

    /**
     * Clears the complete buffer to unsigned integer zero using <code>R32UI</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearUint0() {
        clear(GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, null);
    }

    /**
     * Clears the complete buffer to signed integer zero using <code>R32I</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearInt0() {
        clear(GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT, null);
    }

    /**
     * Clears the complete buffer to floating-point zero using <code>R32F</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearFloat0() {
        clear(GL30.GL_R32F, GL11.GL_RED, GL11.GL_FLOAT, null);
    }

    /**
     * Fills the complete buffer with an unsigned 32-bit value using <code>R32UI</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearUint(int value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clear(GL30.GL_R32UI, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, stack.malloc(Integer.BYTES).putInt(0, value));
        }
    }

    /**
     * Fills the complete buffer with a signed 32-bit value using <code>R32I</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearInt(int value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clear(GL30.GL_R32I, GL30.GL_RED_INTEGER, GL11.GL_INT, stack.malloc(Integer.BYTES).putInt(0, value));
        }
    }

    /**
     * Fills the complete buffer with a 32-bit floating-point value using <code>R32F</code>.
     *
     * @see #clear(int, int, int, ByteBuffer)
     */
    public void clearFloat(float value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clear(GL30.GL_R32F, GL11.GL_RED, GL11.GL_FLOAT, stack.malloc(Float.BYTES).putFloat(0, value));
        }
    }

    /**
     * Clears a byte range to unsigned integer zero using <code>R32UI</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearUint0(long offset, long size) {
        clearSubData(GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, null);
    }

    /**
     * Clears a byte range to signed integer zero using <code>R32I</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearInt0(long offset, long size) {
        clearSubData(GL30.GL_R32I, offset, size, GL30.GL_RED_INTEGER, GL11.GL_INT, null);
    }

    /**
     * Clears a byte range to floating-point zero using <code>R32F</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearFloat0(long offset, long size) {
        clearSubData(GL30.GL_R32F, offset, size, GL11.GL_RED, GL11.GL_FLOAT, null);
    }

    /**
     * Fills a byte range with an unsigned 32-bit value using <code>R32UI</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearUint(long offset, long size, int value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clearSubData(GL30.GL_R32UI, offset, size, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_INT, stack.malloc(Integer.BYTES).putInt(0, value));
        }
    }

    /**
     * Fills a byte range with a signed 32-bit value using <code>R32I</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearInt(long offset, long size, int value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clearSubData(GL30.GL_R32I, offset, size, GL30.GL_RED_INTEGER, GL11.GL_INT, stack.malloc(Integer.BYTES).putInt(0, value));
        }
    }

    /**
     * Fills a byte range with a 32-bit floating-point value using <code>R32F</code>.
     *
     * @see #clearSubData(int, long, long, int, int, ByteBuffer)
     */
    public void clearFloat(long offset, long size, float value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            clearSubData(GL30.GL_R32F, offset, size, GL11.GL_RED, GL11.GL_FLOAT, stack.malloc(Float.BYTES).putFloat(0, value));
        }
    }

    /**
     * Invalidates the complete buffer storage.
     *
     * <p>Note: This does not clear, re-specify, allocate, or synchronize.
     * Contents become undefined and OpenGL may ignore the hint depending on the driver implementation.</p>
     */
    public void invalidate() {
        GL43C.glInvalidateBufferData(bufferID);
    }

    /**
     * Invalidates a byte range.
     *
     * <p>Note: This does not clear, re-specify, allocate, or synchronize.
     * Contents become undefined and OpenGL may ignore the hint depending on the driver implementation.</p>
     */
    public void invalidate(long offset, long size) {
        if (validation) {
            validateRange(offset, size, fetchBufferSize64());
        }

        GL43C.glInvalidateBufferSubData(bufferID, offset, size);
    }
    //</editor-fold>

    //<editor-fold desc="immutable and persistent storage">
    protected ByteBuffer persistentMappedBuffer = null;

    /**
     * Returns the locally tracked persistent mapping, if present.
     *
     * <p>Note: This does not query OpenGL.</p>
     * <p><b>Suggestion</b>: Cache the returned {@link ByteBuffer} on hot paths
     * rather than repeatedly allocating an {@link Optional}.</p>
     */
    @NonNull
    public final Optional<ByteBuffer> getPersistentMappedBuffer() {
        return Optional.ofNullable(persistentMappedBuffer);
    }

    /**
     * Allocates immutable storage.
     *
     * <p>Note: Storage can be specified only once and cannot later be replaced by
     * <code>alloc</code> or <code>uploadDirectly</code>.</p>
     */
    public void allocImmutable(long size, int storageFlags) {
        if (validation) {
            Preconditions.checkArgument(size >= 0, "Cannot have a negative buffer size.");
        }

        if (dsa) {
            GL45C.glNamedBufferStorage(bufferID, size, storageFlags);
        } else {
            GL44C.glBufferStorage(target(), size, storageFlags);
        }
    }

    /**
     * Allocates immutable storage using typed flags.
     *
     * @see #allocImmutable(long, int)
     */
    public void allocImmutable(long size, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        if (validation) {
            validateAccessBits(accessBits);
        }

        allocImmutable(size, combineAccessBits(accessBits));
    }

    /**
     * Allocates immutable storage for a persistent-mapping usage pattern.
     *
     * <p><b>Suggestion</b>: The caller most likely <i>want to/<b>even must</b></i> include
     * <code>GL_MAP_PERSISTENT_BIT</code> and required read/write flags. Map once with
     * {@link #mapPersistent(int, int, MapBufferAccessBit...)} and retain the result.
     * Non-coherent mappings require the visibility operations.</p>
     */
    public void allocPersistent(int size, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        allocImmutable(size, accessBits);
    }

    /**
     * Maps and locally tracks a persistent range.
     *
     * <p>Note: Persistent mapping doesn't need the map/unmap usage pattern. It does not make CPU/GPU reuse safe.</p>
     */
    public void mapPersistent(int offset, int length, @NonNull MapBufferAccessBit @NonNull ... accessBits) {
        if (validation) {
            Preconditions.checkState(persistentMappedBuffer == null, "Buffer already mapped persistently.");
            validateAccessBits(accessBits);
        }

        persistentMappedBuffer = mapRange(offset, length, combineAccessBits(accessBits));
    }

    /**
     * Unmaps the locally tracked persistent mapping.
     *
     * <p>Note: Normally call this during buffer teardown, not once per frame.</p>
     */
    public void unmapPersistent() {
        if (validation) {
            Preconditions.checkState(persistentMappedBuffer != null, "Buffer not persistently mapped.");
        }

        boolean success = unmap();
        persistentMappedBuffer = null;

        if (!success) {
            throw new RuntimeException("Persistent buffer unmap failed, data may be corrupted.");
        }
    }
    //</editor-fold>
}
