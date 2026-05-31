package com.cleanroommc.kirino.ui.simpletext.glyph;

import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontHandle;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * It helps to load metrics and stores metrics.
 * <p>Note: {@link GlyphMetricsStore} must be owned by a {@link ST_FontHandle} owner,
 * so <code>glyphIndex</code> therefore makes sense with a given font face.</p>
 */
public class GlyphMetricsStore {

    // key: glyph index
    private final ConcurrentMap<Integer, GlyphMetrics> metricsMap = new ConcurrentHashMap<>();
    private final ST_Config config;

    public GlyphMetricsStore(ST_Config config) {
        this.config = config;
    }

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
    private GlyphMetrics loadMetrics(ST_FontHandle font, ResourceLocation fontRl, int glyphIndex) {
        GlyphMetrics outMetrics = new GlyphMetrics();
        font.loadGlyph(glyphIndex, config.payload(), outMetrics);

        if (outMetrics.isEmpty()) {
            throw new RuntimeException(String.format(
                    "Load glyph metrics failed (fontRl=%s, glyphIndex=%d, backend=%s, impl=%s).",
                    fontRl.toString(), glyphIndex, font.type(), font.getClass().getName()));
        }

        outMetrics.setSdf(config.sdfPadding());

        return outMetrics;
    }

    /**
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     *
     * @param glyphIndex Glyph index must be positive and valid
     */
    @NonNull
    public GlyphMetrics loadMetricsIfAbsent(ST_FontHandle font, ResourceLocation fontRl, int glyphIndex) {
        Preconditions.checkArgument(glyphIndex > 0,
                "Argument \"glyphIndex\"=%s must be positive.", glyphIndex);

        return metricsMap.computeIfAbsent(glyphIndex, k -> loadMetrics(font, fontRl, k));
    }
}
