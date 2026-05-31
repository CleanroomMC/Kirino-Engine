package com.cleanroommc.kirino.ui.simpletext;

import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

/**
 * It is a specialized bitmap representation for SimpleText,
 * and it represents any <code>row-major, width * height, 1-byte-per-pixel</code> bitmap.
 */
public interface ST_Bitmap extends AutoCloseable {
    int width();
    int height();
    @Nullable ByteBuffer byteBuffer();

    ST_Bitmap EMPTY = new ST_Bitmap() {

        @Override
        public int width() { return 0; }

        @Override
        public int height() { return 0; }

        @Override
        public @Nullable ByteBuffer byteBuffer() { return null; }

        @Override
        public void close() { }
    };
}
