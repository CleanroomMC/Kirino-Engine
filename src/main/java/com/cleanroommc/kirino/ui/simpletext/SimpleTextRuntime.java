package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetricsStore;
import com.cleanroommc.kirino.ui.simpletext.text.CodepointIterator;
import com.google.common.base.Preconditions;
import com.ibm.icu.text.BreakIterator;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleTextRuntime {

    //<editor-fold desc="boilerplate">
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
    private final SimpleTextProducer dummyTextProducer;

    private final BreakIterator lineBreakIterator = BreakIterator.getLineInstance(Locale.ROOT);
    private final BreakIterator characterBreakIterator = BreakIterator.getCharacterInstance(Locale.ROOT);

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
        dummyTextProducer = producerFactory.apply(this);

//        int[] outParallelism = new int[1];
//        ForkJoinPool workerPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoSimpleTextSDF", outParallelism);
//        ShutdownManager.registerAsync(() -> ForkJoinPoolUtils.shutdownPool(workerPool, 5));
//        SDFGeneratorPool generatorPool = new SDFGeneratorPool(outParallelism[0], () ->
//                new SDFGenerator(SimpleTextConstants.SDF_PADDING, SimpleTextConstants.SDF_SPREAD));
    }
    //</editor-fold>

    //<editor-fold desc="glyph metrics">
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
    //</editor-fold>

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

    //<editor-fold desc="simulate">
    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y) {
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        dummyTextProducer.beginBatch();
        dummyTextProducer.append(text, x, y, dummyTextProducer.standardFontSize(), outLineInfo);
        dummyTextProducer.endBatch();
        return outLineInfo;
    }

    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y, float fontSize) {
        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        dummyTextProducer.beginBatch();
        dummyTextProducer.append(text, x, y, fontSize, outLineInfo);
        dummyTextProducer.endBatch();
        return outLineInfo;
    }
    //</editor-fold>

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private static final float LINE_GAP = 1f;

    //<editor-fold desc="append lines">
    private float penX = 0;
    private float penY = 0;

    @NonNull
    public SimpleTextRuntime append(@NonNull String text, float x, float y) {
        Preconditions.checkNotNull(text);

        if (text.isEmpty()) {
            return this;
        }

        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.append(text, x, y, textProducer.standardFontSize(), outLineInfo);
        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;
        return this;
    }

    @NonNull
    public SimpleTextRuntime append(@NonNull String text, float x, float y, float fontSize, int color) {
        Preconditions.checkNotNull(text);

        if (text.isEmpty()) {
            return this;
        }

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
    public SimpleTextRuntime appendEmptyLineBelow(float fontSize) {
        penY += textProducer.calcLineHeight("A", fontSize) + LINE_GAP;
        return this;
    }

    @NonNull
    public SimpleTextRuntime appendEmptyLineBelow() {
        penY += textProducer.calcLineHeight("A", textProducer.standardFontSize()) + LINE_GAP;
        return this;
    }

    @NonNull
    public SimpleTextRuntime appendBelow(@NonNull String text) {
        Preconditions.checkNotNull(text);

        if (text.isEmpty()) {
            return appendEmptyLineBelow();
        } else {
            return append(text, penX, penY);
        }
    }

    @NonNull
    public SimpleTextRuntime appendBelow(@NonNull String text, float fontSize, int color) {
        Preconditions.checkNotNull(text);

        if (text.isEmpty()) {
            return appendEmptyLineBelow(fontSize);
        } else {
            return append(text, penX, penY, fontSize, color);
        }
    }
    //</editor-fold>

    //<editor-fold desc="append paragraphs">
    @NonNull
    public SimpleTextRuntime appendParagraph(
            @NonNull String text,
            float x,
            float y,
            float maxWidth) {

        return appendParagraph0(
                text,
                x,
                y,
                maxWidth,
                textProducer.standardFontSize(),
                false,
                0);
    }

    @NonNull
    public SimpleTextRuntime appendParagraph(
            @NonNull String text,
            float x,
            float y,
            float maxWidth,
            float fontSize,
            int color) {

        return appendParagraph0(
                text,
                x,
                y,
                maxWidth,
                fontSize,
                true,
                color);
    }

    @NonNull
    public SimpleTextRuntime appendParagraphBelow(
            @NonNull String text,
            float maxWidth) {

        return appendParagraph(
                text,
                penX,
                penY,
                maxWidth);
    }

    @NonNull
    public SimpleTextRuntime appendParagraphBelow(
            @NonNull String text,
            float maxWidth,
            float fontSize,
            int color) {

        return appendParagraph(
                text,
                penX,
                penY,
                maxWidth,
                fontSize,
                color);
    }

    @NonNull
    private SimpleTextRuntime appendParagraph0(
            @NonNull String text,
            float x,
            float y,
            float maxWidth,
            float fontSize,
            boolean overrideColor,
            int color) {

        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(Float.isFinite(maxWidth) && maxWidth > 0f,
                "Argument \"maxWidth\"=%s must be finite and > 0.",
                maxWidth);

        if (text.isEmpty()) {
            return this;
        }

        float currentY = y;
        int start = 0;

        while (start < text.length()) {
            int hardBreak = findHardBreak(text, start);
            int end = hardBreak < 0 ? text.length() : hardBreak;

            if (start == end) {
                penX = x;
                penY = currentY + textProducer.calcLineHeight("A", fontSize) + LINE_GAP;
            } else {
                appendWrappedSegment(
                        text.substring(start, end),
                        x,
                        currentY,
                        maxWidth,
                        fontSize,
                        overrideColor,
                        color);
            }

            currentY = penY;

            if (hardBreak < 0) {
                break;
            }

            start = hardBreak + hardBreakLength(text, hardBreak);
        }

        return this;
    }

    private static final class WrapBreak {

        private final int consumeEnd;
        private final int renderEnd;

        private WrapBreak(int consumeEnd, int renderEnd) {
            this.consumeEnd = consumeEnd;
            this.renderEnd = renderEnd;
        }
    }

    private void appendWrappedSegment(
            @NonNull String text,
            float x,
            float y,
            float maxWidth,
            float fontSize,
            boolean overrideColor,
            int color) {

        int lineStart = 0;
        float lineY = y;

        while (lineStart < text.length()) {
            String remaining = text.substring(lineStart);
            SimpleTextProducer.LineInfo lineInfo = simulate(remaining, 0f, 0f, fontSize);

            int cpCount = lineInfo.getCodepointCount();
            if (cpCount == 0) {
                return;
            }

            int fittingCpCount = findFittingCodepointCount(lineInfo, maxWidth);
            if (fittingCpCount == cpCount) {
                appendParagraphLine(
                        remaining,
                        x,
                        lineY,
                        fontSize,
                        overrideColor,
                        color);
                return;
            }

            int maxUtf16Offset = remaining.offsetByCodePoints(0, fittingCpCount);

            WrapBreak wrapBreak = findPreferredLineBreak(remaining, maxUtf16Offset);
            if (wrapBreak == null) {
                wrapBreak = findEmergencyBreak(remaining, maxUtf16Offset);
            }

            Preconditions.checkState(wrapBreak.consumeEnd > 0,
                    "Wrapping must always make progress.");

            if (wrapBreak.renderEnd > 0) {
                appendParagraphLine(
                        remaining.substring(0, wrapBreak.renderEnd),
                        x,
                        lineY,
                        fontSize,
                        overrideColor,
                        color);

                lineY = penY;
            }

            lineStart += wrapBreak.consumeEnd;
        }
    }

    /**
     * It returns how many codepoints can fit within <code>maxWidth</code>.
     */
    private static int findFittingCodepointCount(
            SimpleTextProducer.@NonNull LineInfo lineInfo,
            float maxWidth) {

        int cpCount = lineInfo.getCodepointCount();

        for (int i = 0; i < cpCount; i++) {
            if (lineInfo.getProgressiveLengthAt(i) > maxWidth) {
                return i;
            }
        }

        return cpCount;
    }

    /**
     * It finds the last ICU legal line-break position that can fit inside
     * <code>maxUtf16Offset</code>.
     */
    @Nullable
    private WrapBreak findPreferredLineBreak(@NonNull String text, int maxUtf16Offset) {
        lineBreakIterator.setText(text);

        WrapBreak best = null;

        lineBreakIterator.first();

        for (int boundary = lineBreakIterator.next();
             boundary != BreakIterator.DONE;
             boundary = lineBreakIterator.next()) {

            int trimmedBoundary = trimTrailingWrapWhitespace(text, boundary);
            if (trimmedBoundary > maxUtf16Offset) {
                break;
            }

            if (boundary <= maxUtf16Offset) {
                best = new WrapBreak(boundary, boundary);
            } else if (trimmedBoundary > 0) {
                best = new WrapBreak(boundary, trimmedBoundary);
            }
        }

        return best;
    }

    /**
     * It's used when there is no proper Unicode line-break opportunity
     * like a single extremely long English word.
     */
    @NonNull
    private WrapBreak findEmergencyBreak(@NonNull String text, int maxUtf16Offset) {
        characterBreakIterator.setText(text);

        int boundary = 0;
        if (maxUtf16Offset > 0) {
            if (characterBreakIterator.isBoundary(maxUtf16Offset)) {
                boundary = maxUtf16Offset;
            } else {
                boundary = characterBreakIterator.preceding(maxUtf16Offset);
            }
        }

        if (boundary <= 0) {
            boundary = characterBreakIterator.following(0);
            if (boundary == BreakIterator.DONE) {
                boundary = Character.charCount(text.codePointAt(0));
            }
        }

        return new WrapBreak(boundary, boundary);
    }

    private void appendParagraphLine(
            @NonNull String text,
            float x,
            float y,
            float fontSize,
            boolean overrideColor,
            int color) {

        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();

        if (!overrideColor) {
            textProducer.append(
                    text,
                    x,
                    y,
                    fontSize,
                    outLineInfo);
        } else {
            int cpCount = CodepointIterator.count(text);

            float[] sizeArr = new float[cpCount];
            int[] colorArr = new int[cpCount];
            int[] hintArr = new int[cpCount];

            Arrays.fill(sizeArr, 1f);
            Arrays.fill(colorArr, color);

            textProducer.append(
                    text,
                    x,
                    y,
                    fontSize,
                    sizeArr,
                    colorArr,
                    hintArr,
                    outLineInfo);
        }

        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;
    }

    private static int trimTrailingWrapWhitespace(@NonNull String text, int end) {
        while (end > 0) {
            int cp = text.codePointBefore(end);
            if (!Character.isWhitespace(cp)) {
                break;
            }

            end -= Character.charCount(cp);
        }
        return end;
    }

    private static int findHardBreak(@NonNull String text, int fromIndex) {
        for (int i = fromIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n' || c == '\r') {
                return i;
            }
        }
        return -1;
    }

    private static int hardBreakLength(@NonNull String text, int index) {
        if (text.charAt(index) == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
            return 2;
        }
        return 1;
    }
    //</editor-fold>
}
