package com.cleanroommc.kirino.simpletext.sdf;

import com.cleanroommc.kirino.simpletext.ST_Bitmap;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class SDFGeneratorBruteForceImpl implements SDFGenerator {

    private final int padding;
    private final int spread;

    public SDFGeneratorBruteForceImpl(int padding, int spread) {
        Preconditions.checkArgument(padding > 0,
                "Padding=%s must be positive.", padding);
        Preconditions.checkArgument(spread > 0,
                "Spread=%s must be positive.", spread);
        Preconditions.checkArgument(padding >= spread,
                "Padding=%s must be greater or equal to spread=%s.", padding, spread);

        this.padding = padding;
        this.spread = spread;
    }

    @NonNull
    public SDFBitmap compute(@NonNull ST_Bitmap bitmap) {
        Preconditions.checkNotNull(bitmap, "Bitmap must be non-null.");
        ByteBuffer source = Preconditions.checkNotNull(bitmap.byteBuffer(), "Bitmap.byteBuffer must be non-null.");

        int width = bitmap.width();
        int height = bitmap.height();

        int outWidth = width + padding * 2;
        int outHeight = height + padding * 2;
        int size = outWidth * outHeight;

        float[] coverage = new float[size];

        for (int y = 0; y < height; y++) {
            int dy = y + padding;

            for (int x = 0; x < width; x++) {
                int dx = x + padding;
                int v = source.get(y * width + x) & 255;
                coverage[dy * outWidth + dx] = v / 255f;
            }
        }

        boolean[] edge = new boolean[size];
        float[] edgeX = new float[size];
        float[] edgeY = new float[size];

        for (int y = 1; y < outHeight - 1; y++) {
            for (int x = 1; x < outWidth - 1; x++) {
                int i = y * outWidth + x;
                float a = coverage[i];
                if (a <= 0f || a >= 1f) {
                    continue;
                }

                edge[i] = true;

                float gx = coverage[(y - 1) * outWidth + (x + 1)] +
                        2f * coverage[y * outWidth + (x + 1)] +
                        coverage[(y + 1) * outWidth + (x + 1)] -
                        coverage[(y - 1) * outWidth + (x - 1)] -
                        2f * coverage[y * outWidth + (x - 1)] -
                        coverage[(y + 1) * outWidth + (x - 1)];

                float gy = coverage[(y + 1) * outWidth + (x - 1)] +
                        2f * coverage[(y + 1) * outWidth + x] +
                        coverage[(y + 1) * outWidth + (x + 1)] -
                        coverage[(y - 1) * outWidth + (x - 1)] -
                        2f * coverage[(y - 1) * outWidth + x] -
                        coverage[(y - 1) * outWidth + (x + 1)];

                float len = (float) Math.sqrt(gx * gx + gy * gy);

                if (len < 1e-4f) {
                    edgeX[i] = x + 0.5f;
                    edgeY[i] = y + 0.5f;
                    continue;
                }

                gx /= len;
                gy /= len;

                float t = 0.5f - a;
                edgeX[i] = x + 0.5f + gx * t;
                edgeY[i] = y + 0.5f + gy * t;
            }
        }

        ByteBuffer out = MemoryUtil.memAlloc(size);

        float inv = 1f / (2f * spread);

        for (int y = 0; y < outHeight; y++) {
            for (int x = 0; x < outWidth; x++) {
                int i = y * outWidth + x;
                boolean inside = coverage[i] > 0.5f;

                float best = Float.POSITIVE_INFINITY;
                float px = x + 0.5f;
                float py = y + 0.5f;

                for (int yy = 1; yy < outHeight - 1; yy++) {
                    for (int xx = 1; xx < outWidth - 1; xx++) {
                        int j = yy * outWidth + xx;
                        if (!edge[j]) {
                            continue;
                        }

                        float dx = edgeX[j] - px;
                        float dy = edgeY[j] - py;
                        float dist = dx * dx + dy * dy;

                        if (dist < best) {
                            best = dist;
                        }
                    }
                }

                float d = (float) Math.sqrt(best);

                if (!inside) {
                    d = -d;
                }

                d = Math.max(-spread, Math.min(spread, d));

                float v = (d + spread) * inv;
                int iv = (int) (v * 255f + 0.5f);

                out.put(i, (byte) iv);
            }
        }

        return new SDFBitmap(outWidth, outHeight, out);
    }
}
