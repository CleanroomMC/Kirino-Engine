package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.atlas.Tex2DGlyphAtlas;
import com.cleanroommc.kirino.simpletext.backend.DebugTextRenderer;
import com.cleanroommc.kirino.simpletext.backend.FreeTypeTextProducer;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetricsStore;
import com.cleanroommc.kirino.simpletext.sdf.SDFGenerator;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;

public class SimpleTextRuntime {

    private final ResourceLocation fontRl;
    private final ST_FontObject font;
    private final ST_Config config;

    public ResourceLocation getFontRl() {
        return fontRl;
    }

    public ST_FontObject getFont() {
        return font;
    }

    public ST_Config getConfig() {
        return config;
    }

    private final GlyphMetricsStore metricsStore;
    private final SimpleTextConsumer textConsumer;
    private final SimpleTextProducer textProducer;

    public SimpleTextRuntime(
            BiFunction<ResourceLocation, ST_Config, ST_FontObject> fontFactory,
            ImmediateShaderAccess shaderAccess,
            ST_Config config,
            ResourceLocation fontRl) {

        this.fontRl = fontRl;
        this.config = config;
        font = fontFactory.apply(fontRl, config);

        Preconditions.checkState(font.type() == config.target(),
                "Backend must match. Font backend type=%s but config backend target=%s.",
                font.type().toString(), config.target().toString());

        metricsStore = new GlyphMetricsStore(config);

//        int[] outParallelism = new int[1];
//        ForkJoinPool workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
//        ShutdownManager.registerAsync(() -> ForkJoinPoolUtils.shutdownPool(workerPool, 5));
//        SDFGeneratorPool generatorPool = new SDFGeneratorPool(outParallelism[0], () ->
//                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD));

        textConsumer = new DebugTextRenderer(
                this,
                new SDFGenerator(config.sdfPadding(), config.sdfSpread()),
                new Tex2DGlyphAtlas(1024, 1024),
                shaderAccess);
        textProducer = new FreeTypeTextProducer(this, config.pixelSize());
    }

    /**
     * It automatically loads a new metrics if the requested one wasn't loaded.
     *
     * <p>Note: <b>Not</b> guaranteed to be thread safe.</p>
     */
    @NonNull
    public GlyphMetrics getGlyphMetrics(int glyphIndex) {
        return metricsStore.loadMetricsIfAbsent(font, fontRl, glyphIndex);
    }

    /**
     * It straight up fetches the metrics. Will return <code>null</code> if the requested one wasn't loaded.
     *
     * <p>Note: Guaranteed to be thread safe.</p>
     */
    @Nullable
    public GlyphMetrics getGlyphMetricsDirectly(int glyphIndex) {
        return metricsStore.get(glyphIndex);
    }

    @NonNull
    public SimpleTextRuntime begin() {
        textProducer.beginBatch();
        return this;
    }

    @NonNull
    public SimpleTextRuntime endDraw() {
        textProducer.endBatch();
        textConsumer.consume(textProducer.submit());
        return this;
    }

    /**
     * <p>Debug Utility</p>
     * It's a simulated version of {@link #endDraw()}, which won't draw anything.
     */
    @NonNull
    public TextCommandList endPseudo() {
        textProducer.endBatch();
        return textProducer.submit().copy();
    }

    @NonNull
    public SimpleTextRuntime append(String text, float x, float y) {
        textProducer.append(text, x, y);
        return this;
    }
}
