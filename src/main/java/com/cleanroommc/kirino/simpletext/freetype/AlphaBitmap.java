package com.cleanroommc.kirino.simpletext.freetype;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Format: row-major, width * height, 1-byte-per-pixel
 */
public record AlphaBitmap(int width, int height, ByteBuffer byteBuffer) implements AutoCloseable {

    @Override
    public void close() {
        MemoryUtil.memFree(byteBuffer);
    }
}
