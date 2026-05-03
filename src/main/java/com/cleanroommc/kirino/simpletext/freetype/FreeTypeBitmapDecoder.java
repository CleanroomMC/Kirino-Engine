package com.cleanroommc.kirino.simpletext.freetype;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public final class FreeTypeBitmapDecoder {

    private FreeTypeBitmapDecoder() {
    }

    /**
     * It only implements some of freetype specs.
     *
     * <p>Note: This method removes freetype bitmap memory padding,
     * normalizes grayscale, handles <code>FT_PIXEL_MODE_GRAY</code> and <code>FT_PIXEL_MODE_MONO</code>.</p>
     *
     * <p>Note: {@link AlphaBitmap} must be freed later.</p>
     */
    @NonNull
    public static AlphaBitmap decode(@NonNull FT_Bitmap bitmap) throws UnsupportedOperationException {
        Preconditions.checkNotNull(bitmap);

        int width = bitmap.width();
        int height = bitmap.rows();
        int pitch = bitmap.pitch();
        int pixelMode = bitmap.pixel_mode();
        int numGrays = bitmap.num_grays();
        int absPitch = Math.abs(pitch);

        ByteBuffer source = bitmap.buffer(absPitch * height);
        Preconditions.checkNotNull(source, "FT_Bitmap.buffer is null.");

        if (pixelMode == FreeType.FT_PIXEL_MODE_GRAY) {
            ByteBuffer dest = MemoryUtil.memAlloc(width * height);

            for (int y = 0; y < height; y++) {
                int sourceRow = (pitch > 0) ? y : (height - 1 - y);

                if (numGrays == 256 && absPitch >= width) {
                    MemoryUtil.memCopy(
                            MemoryUtil.memAddress(source) + (long) sourceRow * absPitch,
                            MemoryUtil.memAddress(dest) + (long) y * width,
                            width);
                } else {
                    for (int x = 0; x < width; x++) {
                        int v = source.get(sourceRow * absPitch + x) & 0xFF;
                        v = (v * 255) / (numGrays - 1);
                        dest.put(y * width + x, (byte) v);
                    }
                }
            }

            return new AlphaBitmap(width, height, dest);
        }

        if (pixelMode == FreeType.FT_PIXEL_MODE_MONO) {
            ByteBuffer dest = MemoryUtil.memAlloc(width * height);

            for (int y = 0; y < height; y++) {
                int sourceRow = (pitch > 0) ? y : (height - 1 - y);

                for (int x = 0; x < width; x++) {
                    int byteIndex = sourceRow * absPitch + (x >> 3);
                    int bitIndex = 7 - (x & 7);

                    byte b = source.get(byteIndex);
                    int bit = (b >> bitIndex) & 1;

                    dest.put(y * width + x, (byte) (bit != 0 ? 0xFF : 0x00));
                }
            }

            return new AlphaBitmap(width, height, dest);
        }

        throw new UnsupportedOperationException("Unsupported pixel mode: " + pixelMode);
    }
}
