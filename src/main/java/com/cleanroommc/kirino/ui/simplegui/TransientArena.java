package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

final class TransientArena {

    private static final float MEM_GROW_FACTOR = 2f;
    private static final int MAX_SIZE = 1024 * 1024 * 16; // 16 MB

    private int capacity;
    private ByteBuffer buffer;

    @NonNull
    ByteBuffer view() {
        ByteBuffer dup = buffer.duplicate();
        dup.order(buffer.order()).flip();
        return dup;
    }

    void reset() {
        buffer.clear();
    }

    private void grow(int target) {
        ByteBuffer oldBuffer = view();
        int newSize = (int) Math.max(target * MEM_GROW_FACTOR, oldBuffer.capacity() * MEM_GROW_FACTOR);

        if (newSize > MAX_SIZE) {
            throw new RuntimeException(String.format(
                    "TransientArena failed to grow: want=%d, max=%d",
                    newSize, MAX_SIZE));
        }

        ByteBuffer newBuffer = MemoryUtil.memAlloc(newSize);
        newBuffer.put(oldBuffer);

        MemoryUtil.memFree(buffer);
        buffer = newBuffer;
        capacity = newSize;
    }

    TransientArena(int initCapacity) {
        capacity = initCapacity;
        buffer = MemoryUtil.memAlloc(initCapacity);

        ShutdownManager.registerAsync(() -> {
            MemoryUtil.memFree(buffer);
        });
    }

    /**
     * <p>Note: It assumes no arithmetic overflow regarding alignment calculation, and doesn't handle such situation.
     * (i.e. don't input crazily big <code>align</code>)</p>
     *
     * @param align Must be power of 2 and >= 2
     */
    public int alloc(int size, int align) {
        Preconditions.checkArgument(Integer.bitCount(align) == 1 && align >= 2,
                "Argument \"align\"=%s must be power of 2 and >= 2.", align);

        int p = buffer.position();
        int aligned = (p + align - 1) & ~(align - 1);
        int target = aligned + size;

        while (target > capacity) {
            grow(target);
        }

        buffer.position(target);
        return aligned;
    }
}
