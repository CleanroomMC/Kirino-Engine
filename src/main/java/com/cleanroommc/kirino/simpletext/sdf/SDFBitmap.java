package com.cleanroommc.kirino.simpletext.sdf;

import com.cleanroommc.kirino.simpletext.SimpleTextBitmap;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

/**
 * Format: row-major, width * height, 1-byte-per-pixel
 *
 * <p>Note: Do not initialize it by yourself. Call {@link SDFGenerator#compute()}!</p>
 */
public record SDFBitmap(int width, int height, ByteBuffer byteBuffer) implements SimpleTextBitmap {

    @Override
    public void close() {
        MemoryUtil.memFree(byteBuffer);
    }
}
