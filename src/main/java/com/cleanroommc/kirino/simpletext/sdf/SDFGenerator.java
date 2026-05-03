package com.cleanroommc.kirino.simpletext.sdf;

import com.cleanroommc.kirino.simpletext.freetype.AlphaBitmap;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapDecoder;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapLoader;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

public class SDFGenerator {

    private final FT_Face face;
    private final int padding;
    private final int spread;

    public SDFGenerator(@NonNull FT_Face face, int padding, int spread) {
        Preconditions.checkNotNull(face);
        Preconditions.checkArgument(padding > 0,
                "Padding=%s must be positive.", padding);
        Preconditions.checkArgument(spread > 0,
                "Spread=%s must be positive.", spread);
        Preconditions.checkArgument(padding >= spread,
                "Padding=%s must be greater or equal to spread=%s.", padding, spread);

        this.face = face;
        this.padding = padding;
        this.spread = spread;
    }

    private AlphaBitmap currentBitmap = null;

    /**
     * Call {@link #compute()} right after if it returns <code>true</code>.
     */
    public boolean tryLoadBitmap(char c) {
        FT_Bitmap bitmap = FreeTypeBitmapLoader.load(face, c, FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

        if (bitmap == null) {
            return false;
        }

        try {
            currentBitmap = FreeTypeBitmapDecoder.decode(bitmap);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Call <code>compute</code> right after a successful {@link #tryLoadBitmap(char)}, and the
     * loaded underlying bitmap will be freed too.
     *
     * <p>Note: {@link SDFBitmap} must be freed later.</p>
     */
    public SDFBitmap compute() {
        Preconditions.checkNotNull(currentBitmap, "Bitmap must be non-null.");

        int width = currentBitmap.width();
        int height = currentBitmap.height();

        int outWidth = width + padding * 2;
        int outHeight = height + padding * 2;
        int size = outWidth * outHeight;

        boolean[] inside = new boolean[size];

        ByteBuffer source = currentBitmap.byteBuffer();

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
                inside[index] = v > 0;
            }
        }

        float[] distInside = edt(inside, outWidth, outHeight, true);
        float[] distOutside = edt(inside, outWidth, outHeight, false);

        ByteBuffer out = MemoryUtil.memAlloc(size);

        float inv = 1f / (2f * spread);
        float bias = bias(size, spread, distInside, distOutside);

        for (int i = 0; i < size; i++) {
            float d = distOutside[i] - distInside[i];
            d -= bias;
            d = Math.max(-spread, Math.min(spread, d));

            float v = (d + spread) * inv;
            int iv = (int) (v * 255f + 0.5f);

            out.put(i, (byte) (iv & 0xFF));
        }

        currentBitmap.close();
        currentBitmap = null;

        return new SDFBitmap(outWidth, outHeight, out);
    }

    private static float bias(int size, int spread, float[] distInside, float[] distOutside) {
        float sum = 0;
        int count = 0;

        for (int i = 0; i < size; i++) {
            float d = distOutside[i] - distInside[i];

            if (Math.abs(d) < spread) {
                sum += d;
                count++;
            }
        }

        return (count > 0) ? (sum / count) : 0f;
    }

    private static final float BIG_FLOAT = 1e20f;

    private static float[] edt(boolean[] inside, int width, int height, boolean invert) {
        float[] f = new float[width * height];

        for (int i = 0; i < f.length; i++) {
            boolean v = inside[i];
            if (invert) v = !v;

            f[i] = v ? 0f : BIG_FLOAT;
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
