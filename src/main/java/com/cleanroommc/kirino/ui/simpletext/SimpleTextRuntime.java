package com.cleanroommc.kirino.ui.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.ui.simplegui.CmdRectBuilder;
import com.cleanroommc.kirino.ui.simplegui.SimpleGuiRuntime;
import com.cleanroommc.kirino.ui.simpletext.facade.FontRendererFacade;
import com.cleanroommc.kirino.ui.simpletext.facade.SimpleTextFontRendererFacade;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetricsStore;
import com.cleanroommc.kirino.ui.simpletext.text.*;
import com.google.common.base.Preconditions;
import com.ibm.icu.text.BreakIterator;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleTextRuntime implements AutoCloseable {

    //<editor-fold desc="boilerplate">
    private final ResourceLocation fontRl;
    private final ST_FontHandle font;
    private final ST_Config config;
    private final ImmediateShaderAccess shaderAccess;
    private final SimpleGuiRuntime underlineGui;
    private final SimpleGuiRuntime strikethroughGui;

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

    private final ParagraphLineBreaker paragraphLineBreaker;

    @SuppressWarnings("deprecation")
    private final FontRendererFacade fontRendererFacade;

    @Deprecated
    public FontRendererFacade fontRenderer() {
        return fontRendererFacade;
    }

    /**
     * <p>Note: It handles <code>underlineGui</code> and <code>strikethroughGui</code> disposal in {@link #close()},
     * as well as the instances produced by <code>consumerFactory</code> and <code>producerFactory</code>.</p>
     */
    @SuppressWarnings("deprecation")
    public SimpleTextRuntime(
            @NonNull BiFunction<ResourceLocation, ST_Config, ST_FontHandle> fontFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextConsumer> consumerFactory,
            @NonNull Function<SimpleTextRuntime, SimpleTextProducer> producerFactory,
            @NonNull ImmediateShaderAccess shaderAccess,
            @NonNull SimpleGuiRuntime underlineGui,
            @NonNull SimpleGuiRuntime strikethroughGui,
            @NonNull ST_Config config,
            @NonNull ResourceLocation fontRl) {

        Preconditions.checkNotNull(fontFactory);
        Preconditions.checkNotNull(consumerFactory);
        Preconditions.checkNotNull(producerFactory);
        Preconditions.checkNotNull(shaderAccess);
        Preconditions.checkNotNull(underlineGui);
        Preconditions.checkNotNull(strikethroughGui);
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(fontRl);

        this.fontRl = fontRl;
        this.config = config;
        font = fontFactory.apply(fontRl, config);

        Preconditions.checkState(font.type() == config.target(),
                "Backend must match. Font backend type=%s but config backend target=%s.",
                font.type().toString(), config.target().toString());

        this.shaderAccess = shaderAccess;
        this.underlineGui = underlineGui;
        this.strikethroughGui = strikethroughGui;

        metricsStore = new GlyphMetricsStore(config);

        textConsumer = consumerFactory.apply(this);
        textProducer = producerFactory.apply(this);
        dummyTextProducer = producerFactory.apply(this);

        BreakIterator lineBreakIterator = BreakIterator.getLineInstance(Locale.ROOT);
        BreakIterator characterBreakIterator = BreakIterator.getCharacterInstance(Locale.ROOT);
        paragraphLineBreaker = new ParagraphLineBreaker(lineBreakIterator, characterBreakIterator);

        fontRendererFacade = new SimpleTextFontRendererFacade(this, paragraphLineBreaker);
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
        underlineGui.begin();
        strikethroughGui.begin();
        return this;
    }

    @NonNull
    public SimpleTextRuntime endDraw() {
        textProducer.endBatch();
        underlineGui.endDraw();
        textConsumer.consume(textProducer.submit());
        strikethroughGui.endDraw();
        return this;
    }

    //<editor-fold desc="simulate">
    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty(), "Text must not be empty.");

        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        dummyTextProducer.beginBatch();
        dummyTextProducer.append(text, x, y, dummyTextProducer.standardFontSize(), outLineInfo);
        dummyTextProducer.endBatch();
        return outLineInfo;
    }

    public SimpleTextProducer.@NonNull LineInfo simulate(@NonNull String text, float x, float y, float fontSize) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty(), "Text must not be empty.");

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
    private float penX = 0;
    private float penY = 0;

    public float getPenX() {
        return penX;
    }

    public float getPenY() {
        return penY;
    }

    //<editor-fold desc="append lines">
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

    /**
     * @param text If the input is empty, it appends an empty line instead of doing nothing
     */
    @NonNull
    public SimpleTextRuntime appendBelow(@NonNull String text) {
        Preconditions.checkNotNull(text);

        if (text.isEmpty()) {
            return appendEmptyLineBelow();
        } else {
            return append(text, penX, penY);
        }
    }

    /**
     * @param text If the input is empty, it appends an empty line instead of doing nothing
     */
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

    /**
     * @param text Even if the input is empty, it does nothing instead of appending an empty line
     */
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

    /**
     * @param text Even if the input is empty, it does nothing instead of appending an empty line
     */
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
            int hardBreak = ParagraphLineBreaker.findHardBreak(text, start);
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

            start = hardBreak + ParagraphLineBreaker.hardBreakLength(text, hardBreak);
        }

        return this;
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

            if (lineInfo.getCodepointCount() == 0) {
                return;
            }

            WrapBreak wrapBreak = paragraphLineBreaker.findWrapBreak(remaining, lineInfo, maxWidth);

            Preconditions.checkState(wrapBreak.consumeEnd() > 0,
                    "Wrapping must always make progress.");

            if (wrapBreak.renderEnd() > 0) {
                appendParagraphLine(
                        remaining.substring(0, wrapBreak.renderEnd()),
                        x,
                        lineY,
                        fontSize,
                        overrideColor,
                        color);

                lineY = penY;
            }

            lineStart += wrapBreak.consumeEnd();
        }
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
    //</editor-fold>

    //<editor-fold desc="underline/strikethrough utils">

    /**
     * For both underline and strikethrough.
     */
    private static float decorationThickness(float fontSize) {
        return fontSize * 0.1f;
    }

    /**
     * For underline shadow.
     */
    private static int decorationShadowColor(int color) {
        return color & 0xFF000000 | (color & 0x00FCFCFC) >>> 2;
    }

    private static float boundaryX(
            float x,
            SimpleTextProducer.LineInfo lineInfo,
            int boundary) {

        Preconditions.checkPositionIndex(boundary, lineInfo.getCodepointCount());

        if (boundary == 0) {
            return x;
        }

        return x + lineInfo.getProgressiveLengthAt(boundary - 1);
    }

    private void appendUnderline(
            StyledText text,
            float x,
            float y,
            float fontSize,
            SimpleTextProducer.LineInfo lineInfo) {

        appendUnderline(
                text,
                0,
                text.getCodepointCount(),
                x,
                y,
                fontSize,
                lineInfo);
    }

    private void appendUnderline(
            StyledText source,
            int sourceCodepointStart,
            int codepointCount,
            float x,
            float y,
            float fontSize,
            SimpleTextProducer.LineInfo lineInfo) {

        Preconditions.checkPositionIndexes(sourceCodepointStart, sourceCodepointStart + codepointCount, source.getCodepointCount());
        Preconditions.checkArgument(codepointCount == lineInfo.getCodepointCount(),
                "Styled text range codepoint count (%s) must match line info codepoint count (%s).",
                codepointCount,
                lineInfo.getCodepointCount());

        float thickness = decorationThickness(fontSize);
        float baselineY = y + lineInfo.getLineTopToBaseline();

        int start = 0;

        while (start < codepointCount) {
            TextStyle style = source.get(sourceCodepointStart + start);

            if (!style.underlineEnabled()) {
                start++;
                continue;
            }

            int color = style.underlineColor();
            boolean shadow = style.underlineShadowEnabled();

            int end = start + 1;
            while (end < codepointCount) {
                TextStyle next = source.get(sourceCodepointStart + end);
                if (!next.underlineEnabled() || next.underlineColor() != color || next.underlineShadowEnabled() != shadow) {
                    break;
                }

                end++;
            }

            float left = boundaryX(x, lineInfo, start);
            float right = boundaryX(x, lineInfo, end);

            float width = right - left;

            if (width > 0f) {
                underlineGui.append(stream -> {
                    CmdRectBuilder builder = stream.rectEx(left, baselineY, width, thickness, color);

                    if (shadow) {
                        builder.shadow(0f, thickness / 2f, thickness / 2f, decorationShadowColor(color));
                    }

                    builder.emit();
                });
            }

            start = end;
        }
    }

    private void appendStrikethrough(
            StyledText text,
            float x,
            float y,
            float fontSize,
            SimpleTextProducer.LineInfo lineInfo) {

        appendStrikethrough(
                text,
                0,
                text.getCodepointCount(),
                x,
                y,
                fontSize,
                lineInfo);
    }

    private void appendStrikethrough(
            StyledText source,
            int sourceCodepointStart,
            int codepointCount,
            float x,
            float y,
            float fontSize,
            SimpleTextProducer.LineInfo lineInfo) {

        Preconditions.checkPositionIndexes(sourceCodepointStart, sourceCodepointStart + codepointCount, source.getCodepointCount());
        Preconditions.checkArgument(codepointCount == lineInfo.getCodepointCount(),
                "Styled text range codepoint count (%s) must match line info codepoint count (%s).",
                codepointCount,
                lineInfo.getCodepointCount());

        float thickness = decorationThickness(fontSize);
        float baselineY = y + lineInfo.getLineTopToBaseline();
        float strikeY = baselineY - lineInfo.getLineTopToBaseline() * 0.35f - thickness * 0.5f;

        int start = 0;

        while (start < codepointCount) {
            TextStyle style = source.get(sourceCodepointStart + start);

            if (!style.strikethroughEnabled()) {
                start++;
                continue;
            }

            int color = style.strikethroughColor();
            boolean rounded = style.strikethroughRounded();
            boolean outline = style.strikethroughOutlineEnabled();

            int outlineColor = outline ? style.strikethroughOutlineColor() : 0;

            int end = start + 1;
            while (end < codepointCount) {
                TextStyle next = source.get(sourceCodepointStart + end);

                boolean same = next.strikethroughEnabled();
                if (same) {
                    if (next.strikethroughColor() != color || next.strikethroughRounded() != rounded || next.strikethroughOutlineEnabled() != outline) {
                        same = false;
                    }
                }
                if (same) {
                    same = !outline || next.strikethroughOutlineColor() == outlineColor;
                }
                if (!same) {
                    break;
                }

                end++;
            }

            float left = boundaryX(x, lineInfo, start);
            float right = boundaryX(x, lineInfo, end);

            float width = right - left;

            if (width > 0f) {
                strikethroughGui.append(stream -> {
                    CmdRectBuilder builder = stream.rectEx(left, strikeY, width, thickness, color);

                    if (rounded) {
                        builder.radius(Math.min(width, thickness) * 0.5f, 0);
                    }
                    if (outline) {
                        builder.border(thickness * 0.3f, outlineColor);
                    }

                    builder.emit();
                });
            }

            start = end;
        }
    }
    //</editor-fold>

    //<editor-fold desc="append lines (styled text)">
    @NonNull
    public SimpleTextRuntime appendStyled(@NonNull StyledText text, float x, float y) {
        return appendStyled(text, x, y, textProducer.standardFontSize());
    }

    @NonNull
    public SimpleTextRuntime appendStyled(@NonNull StyledText text, float x, float y, float fontSize) {
        Preconditions.checkNotNull(text);

        if (text.getPureText().isEmpty()) {
            return this;
        }

        int cpCount = text.getCodepointCount();
        float[] sizeArr = new float[cpCount];
        int[] colorArr = new int[cpCount];
        int[] hintArr = new int[cpCount];
        for (int i = 0; i < cpCount; i++) {
            TextStyle style = text.get(i);
            sizeArr[i] = style.size();
            colorArr[i] = style.color();
            hintArr[i] = style.hint();
        }

        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();
        textProducer.append(text.getPureText(), x, y, fontSize, sizeArr, colorArr, hintArr, outLineInfo);
        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;

        appendUnderline(text, x, y, fontSize, outLineInfo);
        appendStrikethrough(text, x, y, fontSize, outLineInfo);

        return this;
    }

    /**
     * @param text If the input is empty, it appends an empty line instead of doing nothing
     */
    @NonNull
    public SimpleTextRuntime appendBelowStyled(@NonNull StyledText text) {
        Preconditions.checkNotNull(text);

        if (text.getPureText().isEmpty()) {
            return appendEmptyLineBelow();
        } else {
            return appendStyled(text, penX, penY);
        }
    }

    /**
     * @param text If the input is empty, it appends an empty line instead of doing nothing
     */
    @NonNull
    public SimpleTextRuntime appendBelowStyled(@NonNull StyledText text, float fontSize) {
        Preconditions.checkNotNull(text);

        if (text.getPureText().isEmpty()) {
            return appendEmptyLineBelow(fontSize);
        } else {
            return appendStyled(text, penX, penY, fontSize);
        }
    }
    //</editor-fold>

    //<editor-fold desc="append paragraphs (styled text)">
    @NonNull
    public SimpleTextRuntime appendParagraphStyled(
            @NonNull StyledText text,
            float x,
            float y,
            float maxWidth) {

        return appendParagraphStyled0(
                text,
                x,
                y,
                maxWidth,
                textProducer.standardFontSize());
    }

    @NonNull
    public SimpleTextRuntime appendParagraphStyled(
            @NonNull StyledText text,
            float x,
            float y,
            float maxWidth,
            float fontSize) {

        return appendParagraphStyled0(
                text,
                x,
                y,
                maxWidth,
                fontSize);
    }

    /**
     * @param text Even if the input is empty, it does nothing instead of appending an empty line
     */
    @NonNull
    public SimpleTextRuntime appendParagraphBelowStyled(
            @NonNull StyledText text,
            float maxWidth) {

        return appendParagraphStyled(
                text,
                penX,
                penY,
                maxWidth);
    }

    /**
     * @param text Even if the input is empty, it does nothing instead of appending an empty line
     */
    @NonNull
    public SimpleTextRuntime appendParagraphBelowStyled(
            @NonNull StyledText text,
            float maxWidth,
            float fontSize) {

        return appendParagraphStyled(
                text,
                penX,
                penY,
                maxWidth,
                fontSize);
    }

    @NonNull
    private SimpleTextRuntime appendParagraphStyled0(
            @NonNull StyledText text,
            float x,
            float y,
            float maxWidth,
            float fontSize) {

        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(Float.isFinite(maxWidth) && maxWidth > 0f,
                "Argument \"maxWidth\"=%s must be finite and > 0.",
                maxWidth);

        String pureText = text.getPureText();

        if (pureText.isEmpty()) {
            return this;
        }

        float currentY = y;

        int start = 0; // UTF-16 index for pureText
        int codepointStart = 0; // codepoint index for StyledText

        while (start < pureText.length()) {
            int hardBreak = ParagraphLineBreaker.findHardBreak(pureText, start);
            int end = hardBreak < 0 ? pureText.length() : hardBreak;

            if (start == end) {
                penX = x;
                penY = currentY + textProducer.calcLineHeight("A", fontSize) + LINE_GAP;
            } else {
                appendWrappedSegmentStyled(
                        text,
                        pureText.substring(start, end),
                        codepointStart,
                        x,
                        currentY,
                        maxWidth,
                        fontSize);
            }

            currentY = penY;

            if (hardBreak < 0) {
                break;
            }

            int hardBreakLength = ParagraphLineBreaker.hardBreakLength(pureText, hardBreak);
            int nextStart = hardBreak + hardBreakLength;

            codepointStart += pureText.codePointCount(start, nextStart);
            start = nextStart;
        }

        return this;
    }

    private void appendWrappedSegmentStyled(
            @NonNull StyledText source,
            @NonNull String text,
            int sourceCodepointStart,
            float x,
            float y,
            float maxWidth,
            float fontSize) {

        int lineStart = 0;
        int codepointStart = sourceCodepointStart;

        float lineY = y;

        while (lineStart < text.length()) {
            String remaining = text.substring(lineStart);
            SimpleTextProducer.LineInfo lineInfo = simulate(remaining, 0f, 0f, fontSize);

            if (lineInfo.getCodepointCount() == 0) {
                return;
            }

            WrapBreak wrapBreak = paragraphLineBreaker.findWrapBreak(
                    remaining,
                    lineInfo,
                    maxWidth);

            Preconditions.checkState(wrapBreak.consumeEnd() > 0,
                    "Wrapping must always make progress.");

            if (wrapBreak.renderEnd() > 0) {
                String renderedText = remaining.substring(0, wrapBreak.renderEnd());
                appendParagraphLineStyled(
                        source,
                        codepointStart,
                        renderedText,
                        x,
                        lineY,
                        fontSize);

                lineY = penY;
            }

            int consumedCodepoints = remaining.codePointCount(0, wrapBreak.consumeEnd());

            lineStart += wrapBreak.consumeEnd();
            codepointStart += consumedCodepoints;
        }
    }

    private void appendParagraphLineStyled(
            @NonNull StyledText source,
            int sourceCodepointStart,
            @NonNull String text,
            float x,
            float y,
            float fontSize) {

        int cpCount = CodepointIterator.count(text);

        Preconditions.checkPositionIndexes(sourceCodepointStart, sourceCodepointStart + cpCount, source.getCodepointCount());

        float[] sizeArr = new float[cpCount];
        int[] colorArr = new int[cpCount];
        int[] hintArr = new int[cpCount];

        for (int i = 0; i < cpCount; i++) {
            TextStyle style = source.get(sourceCodepointStart + i);
            sizeArr[i] = style.size();
            colorArr[i] = style.color();
            hintArr[i] = style.hint();
        }

        SimpleTextProducer.LineInfo outLineInfo = new SimpleTextProducer.LineInfo();

        textProducer.append(
                text,
                x,
                y,
                fontSize,
                sizeArr,
                colorArr,
                hintArr,
                outLineInfo);

        penX = x;
        penY = y + outLineInfo.getLineTopToBaseline() + LINE_GAP;

        appendUnderline(
                source,
                sourceCodepointStart,
                cpCount,
                x,
                y,
                fontSize,
                outLineInfo);

        appendStrikethrough(
                source,
                sourceCodepointStart,
                cpCount,
                x,
                y,
                fontSize,
                outLineInfo);
    }
    //</editor-fold>

    @Override
    public void close() throws Exception {
        textConsumer.close();
        textProducer.close();
        dummyTextProducer.close();
        underlineGui.close();
        strikethroughGui.close();
    }
}
