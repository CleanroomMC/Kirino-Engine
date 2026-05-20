package com.cleanroommc.kirino.simpletext.glyph;

import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapLoader;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * It helps to load metrics and stores metrics.
 * <p>Note: {@link GlyphMetricsStore} must be owned by a {@link org.lwjgl.util.freetype.FT_Face} owner,
 * so <code>glyphIndex</code> therefore makes sense with a given font face.</p>
 */
public class GlyphMetricsStore {

    // key: glyph index
    private final ConcurrentMap<Integer, GlyphMetrics> metricsMap = new ConcurrentHashMap<>();

    public int size() {
        return metricsMap.size();
    }

    public boolean contains(int glyphIndex) {
        return metricsMap.containsKey(glyphIndex);
    }

    /**
     * <p>Note: Guaranteed to be thread safe.</p>
     */
    @Nullable
    public GlyphMetrics get(int glyphIndex) {
        return metricsMap.get(glyphIndex);
    }

    @SuppressWarnings("resource")
    @NonNull
    private GlyphMetrics loadMetrics(FT_Face face, ResourceLocation fontRl, int glyphIndex) {
        GlyphMetrics metrics = new GlyphMetrics();
        FreeTypeBitmapLoader.loadGlyph(
                face,
                glyphIndex,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING,
                metrics);

        if (metrics.isEmpty()) {
            throw new RuntimeException(String.format(
                    "Load glyph metrics failed (fontRl=%s, glyphIndex=%d).",
                    fontRl.toString(), glyphIndex));
        }

        return metrics;
    }

    /**
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     *
     * @param glyphIndex Glyph index must be positive and valid
     */
    @NonNull
    public GlyphMetrics loadMetricsIfAbsent(FT_Face face, ResourceLocation fontRl, int glyphIndex) {
        Preconditions.checkArgument(glyphIndex > 0,
                "Argument \"glyphIndex\"=%s must be positive.", glyphIndex);

        return metricsMap.computeIfAbsent(glyphIndex, k -> loadMetrics(face, fontRl, k));
    }
}
