package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetricsStore;
import com.cleanroommc.kirino.ui.simpletext.text.CodepointIterator;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleTextRuntime {

    private final ResourceLocation fontRl;
    private final ST_FontHandle font;
    private final ST_Config config;
    private final ImmediateShaderAccess shaderAccess;

    public ResourceLocation getFontRl() {
        return fontRl;
    }

    public ST_FontHandle getFont() {
        return font;
    }

    public ST_Config getConfig() {
        return config;
    }

    public ImmediateShaderAccess getShaderAccess() {
        return shaderAccess;
    }

    private final GlyphMetricsStore metricsStore;
    private final SimpleTextConsumer textConsumer;
    private final SimpleTextProducer textProducer;

    public SimpleTextRuntime(
            @NonNull BiFunction<ResourceLocation, ST_Config, ST_FontHandle> fontFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextConsumer> consumerFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextProducer> producerFactory,
            @NonNull ImmediateShaderAccess shaderAccess,
            @NonNull ST_Config config,
            @NonNull ResourceLocation fontRl) {

        Preconditions.checkNotNull(fontFactory);
        Preconditions.checkNotNull(consumerFactory);
        Preconditions.checkNotNull(producerFactory);
        Preconditions.checkNotNull(shaderAccess);
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(fontRl);

        this.fontRl = fontRl;
        this.config = config;
        font = fontFactory.apply(fontRl, config);

        Preconditions.checkState(font.type() == config.target(),
                "Backend must match. Font backend type=%s but config backend target=%s.",
                font.type().toString(), config.target().toString());

        this.shaderAccess = shaderAccess;

        metricsStore = new GlyphMetricsStore(config);

        textConsumer = consumerFactory.apply(this);
        textProducer = producerFactory.apply(this);

//        int[] outParallelism = new int[1];
//        ForkJoinPool workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
//        ShutdownManager.registerAsync(() -> ForkJoinPoolUtils.shutdownPool(workerPool, 5));
//        SDFGeneratorPool generatorPool = new SDFGeneratorPool(outParallelism[0], () ->
//                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD));
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

    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y) {
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.beginBatch();
        textProducer.append(text, x, y, textProducer.standardFontSize(), outLineInfo);
        textProducer.endBatch();
        return outLineInfo;
    }

    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y, float fontSize) {
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.beginBatch();
        textProducer.append(text, x, y, fontSize, outLineInfo);
        textProducer.endBatch();
        return outLineInfo;
    }

    private static final float LINE_GAP = 1f;

    private float penX = 0;
    private float penY = 0;

    @NonNull
    public SimpleTextRuntime append(@NonNull String text, float x, float y) {
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.append(text, x, y, textProducer.standardFontSize(), outLineInfo);
        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;
        return this;
    }

    @NonNull
    public SimpleTextRuntime append(@NonNull String text, float x, float y, float fontSize, int color) {
        int cpCount = CodepointIterator.count(text);
        float[] sizeArr = new float[cpCount];
        int[] colorArr = new int[cpCount];
        int[] hintArr = new int[cpCount];
        Arrays.fill(sizeArr, 1f);
        Arrays.fill(colorArr, color);
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.append(text, x, y, fontSize, sizeArr, colorArr, hintArr, outLineInfo);
        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;
        return this;
    }

    @NonNull
    public SimpleTextRuntime appendBelow(@NonNull String text) {
        return append(text, penX, penY);
    }

    @NonNull
    public SimpleTextRuntime appendBelow(@NonNull String text, float fontSize, int color) {
        return append(text, penX, penY, fontSize, color);
    }
}
