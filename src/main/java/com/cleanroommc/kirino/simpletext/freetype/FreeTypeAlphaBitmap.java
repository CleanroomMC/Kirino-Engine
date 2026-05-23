package com.cleanroommc.kirino.simpletext.freetype;

import com.cleanroommc.kirino.simpletext.ST_Bitmap;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;

import java.nio.ByteBuffer;

/**
 * Format: row-major, width * height, 1-byte-per-pixel
 *
 * <p>Note: Do not initialize it by yourself. Call {@link FreeTypeBitmapDecoder#decode(FT_Bitmap)}!</p>
 */
public record FreeTypeAlphaBitmap(int width, int height, ByteBuffer byteBuffer) implements ST_Bitmap {

    @Override
    public void close() {
        MemoryUtil.memFree(byteBuffer);
    }
}
