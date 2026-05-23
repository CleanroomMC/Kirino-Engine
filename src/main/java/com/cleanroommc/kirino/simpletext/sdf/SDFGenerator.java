package com.cleanroommc.kirino.simpletext.sdf;

import com.cleanroommc.kirino.simpletext.ST_Bitmap;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class SDFGenerator {

    private final int padding;
    private final int spread;

    public SDFGenerator(int padding, int spread) {
        Preconditions.checkArgument(padding > 0,
                "Padding=%s must be positive.", padding);
        Preconditions.checkArgument(spread > 0,
                "Spread=%s must be positive.", spread);
        Preconditions.checkArgument(padding >= spread,
                "Padding=%s must be greater or equal to spread=%s.", padding, spread);

        this.padding = padding;
        this.spread = spread;
    }

    /**
     * <p>Note: {@link SDFBitmap} must be freed later.</p>
     * <p>Note: This method will not free the input <code>bitmap</code>.</p>
     */
    @NonNull
    public SDFBitmap compute(@NonNull ST_Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "Bitmap must be non-null.");

        int width = bitmap.width();
        int height = bitmap.height();

        int outWidth = width + padding * 2;
        int outHeight = height + padding * 2;
        int size = outWidth * outHeight;

        boolean[] inside = new boolean[size];

        ByteBuffer source = bitmap.byteBuffer();

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                int index = y * outWidth + x;
                int sourceX = x - padding;
                int sourceY = y - padding;

                if (sourceX < 0 || sourceY < 0 || sourceX >= width || sourceY >= height) {
                    inside[index] = false;
                    continue;
                }

                int v = source.get(sourceY * width + sourceX) & 0xFF;
                inside[index] = v > 127;
            }
        }

        float[] distInside = edt(inside, outWidth, outHeight, true);
        float[] distOutside = edt(inside, outWidth, outHeight, false);

        ByteBuffer out = MemoryUtil.memAlloc(size);

        float inv = 1f / (2f * spread);

        for (int i = 0; i < size; i++) {
            float d = distOutside[i] - distInside[i];
            d = Math.max(-spread, Math.min(spread, d));

            float v = (d + spread) * inv;
            int iv = (int) (v * 255f + 0.5f);

            out.put(i, (byte) (iv & 0xFF));
        }

        return new SDFBitmap(outWidth, outHeight, out);
    }

    private static final float BIG_FLOAT = 1e6f;

    private static float[] edt(boolean[] inside, int width, int height, boolean invert) {
        float[] f = new float[width * height];

        for (int i = 0; i < f.length; i++) {
            boolean v = inside[i];
            if (invert) {
                v = !v;
            }

            f[i] = v ? BIG_FLOAT : 0f;
        }

        float[] tmp = new float[width * height];
        float[] row = new float[width];
        float[] distRow = new float[width];

        for (int y = 0; y < height; y++) {
            int off = y * width;
            System.arraycopy(f, off, row, 0, width);
            edt1d(row, distRow, width);
            System.arraycopy(distRow, 0, tmp, off, width);
        }

        float[] col = new float[height];
        float[] distCol = new float[height];
        float[] out = new float[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                col[y] = tmp[y * width + x];
            }

            edt1d(col, distCol, height);

            for (int y = 0; y < height; y++) {
                out[y * width + x] = (float) Math.sqrt(distCol[y]);
            }
        }

        return out;
    }

    private static void edt1d(float[] f, float[] d, int n) {
        int[] v = new int[n];
        float[] z = new float[n + 1];

        int k = 0;
        v[0] = 0;
        z[0] = Float.NEGATIVE_INFINITY;
        z[1] = Float.POSITIVE_INFINITY;

        for (int q = 1; q < n; q++) {
            float s;
            do {
                int p = v[k];
                s = ((f[q] + q * q) - (f[p] + p * p)) / (2f * (q - p));
                if (s <= z[k]) k--;
            } while (s <= z[k]);

            k++;
            v[k] = q;
            z[k] = s;
            z[k + 1] = Float.POSITIVE_INFINITY;
        }

        k = 0;
        for (int q = 0; q < n; q++) {
            while (z[k + 1] < q) k++;
            int p = v[k];
            float dx = q - p;
            d[q] = dx * dx + f[p];
        }
    }
}
