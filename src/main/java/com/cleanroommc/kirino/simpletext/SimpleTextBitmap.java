package com.cleanroommc.kirino.simpletext;

import java.nio.ByteBuffer;

/**
 * It is a specialized bitmap representation for SimpleText,
 * and it represents any <code>row-major, width * height, 1-byte-per-pixel</code> bitmap.
 */
public interface SimpleTextBitmap extends AutoCloseable {
    int width();
    int height();
    ByteBuffer byteBuffer();
}
