package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.backend.DebugTextRenderer;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetricsStore;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

public class SimpleTextContext {

    private final ResourceLocation fontRl;
    private final FT_Face face;
    private final boolean hasKerning;

    public ResourceLocation getFontRl() {
        return fontRl;
    }

    public FT_Face getFontFace() {
        return face;
    }

    public boolean hasFontKerning() {
        return hasKerning;
    }

    private final GlyphMetricsStore metricsStore = new GlyphMetricsStore();
    private final SimpleTextConsumer textConsumer;
    private final SimpleTextProducer textProducer;

    public SimpleTextContext(FreeTypeManager freeTypeManager, ResourceLocation fontRl) {
        this.fontRl = fontRl;
        face = freeTypeManager.load(fontRl, 0, 64);
        hasKerning = FreeType.FT_HAS_KERNING(face);

        textConsumer = new DebugTextRenderer(this);
        textProducer = new SimpleTextProducer(this);
    }

    /**
     * It automatically loads a new metrics if the requested one wasn't loaded.
     *
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     */
    @NonNull
    public GlyphMetrics getMetrics(int glyphIndex) {
        return metricsStore.loadMetricsIfAbsent(face, fontRl, glyphIndex);
    }

    /**
     * It straight up fetches the metrics. Will return <code>null</code> if the requested one wasn't loaded.
     *
     * <p>Note: Guaranteed to be thread safe.</p>
     */
    @Nullable
    public GlyphMetrics getMetricsDirectly(int glyphIndex) {
        return metricsStore.get(glyphIndex);
    }
}
