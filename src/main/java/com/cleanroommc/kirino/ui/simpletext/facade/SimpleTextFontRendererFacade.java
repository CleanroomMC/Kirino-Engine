package com.cleanroommc.kirino.ui.simpletext.facade;

import com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.text.ParagraphLineBreaker;
import com.cleanroommc.kirino.ui.simpletext.text.StyledText;
import com.cleanroommc.kirino.ui.simpletext.text.TextStyle;
import com.cleanroommc.kirino.ui.simpletext.text.WrapBreak;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class SimpleTextFontRendererFacade implements FontRendererFacade {

    private static final int[] MINECRAFT_COLORS = {
            0x000000,
            0x0000AA,
            0x00AA00,
            0x00AAAA,
            0xAA0000,
            0xAA00AA,
            0xFFAA00,
            0xAAAAAA,
            0x555555,
            0x5555FF,
            0x55FF55,
            0x55FFFF,
            0xFF5555,
            0xFF55FF,
            0xFFFF55,
            0xFFFFFF};

    private final SimpleTextRuntime runtime;
    private final ParagraphLineBreaker lineBreaker;

    public SimpleTextFontRendererFacade(
            @NonNull SimpleTextRuntime runtime,
            @NonNull ParagraphLineBreaker lineBreaker) {

        Preconditions.checkNotNull(runtime);
        Preconditions.checkNotNull(lineBreaker);

        this.runtime = runtime;
        this.lineBreaker = lineBreaker;
    }

    @Override
    public int fontHeight() {
        SimpleTextProducer.LineInfo lineInfo = runtime.simulate("A", 0f, 0f);

        // SimpleTextRuntime advances lines by lineTopToBaseline + 1
        return Math.round(lineInfo.getLineTopToBaseline() + 1f);
    }

    @Override
    public int drawStringWithShadow(
            @NonNull String text,
            float x,
            float y,
            int color) {

        return drawString(text, x, y, color, true);
    }

    @Override
    public int drawString(
            @NonNull String text,
            int x,
            int y,
            int color) {

        return drawString(text, (float) x, (float) y, color, false);
    }

    @Override
    public int drawString(
            @NonNull String text,
            float x,
            float y,
            int color,
            boolean dropShadow) {

        Preconditions.checkNotNull(text);

        StyledText styledText = styled(text, color, dropShadow);
        runtime.appendStyled(styledText, x, y);

        float width = measure(styledText.getPureText());
        return (int) (x + width);
    }

    @Override
    public void drawSplitString(
            @NonNull String text,
            int x,
            int y,
            int wrapWidth,
            int color) {

        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(wrapWidth > 0,
                "Argument \"wrapWidth\" must be > 0.");

        StyledText styledText = styled(text, color, false);

        runtime.appendParagraphStyled(styledText, x, y, wrapWidth);
    }

    @Override
    public int getStringWidth(@NonNull String text) {
        Preconditions.checkNotNull(text);

        String pureText = minecraftPureText(text);
        return Math.round(measure(pureText));
    }

    @Override
    public int getCharWidth(char character) {
        if (character == '§') {
            return -1;
        }

        return Math.round(measure(String.valueOf(character)));
    }

    @Override
    public int getWordWrappedHeight(@NonNull String text, int maxWidth) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(maxWidth > 0,
                "Argument \"maxWidth\" must be > 0.");

        return listFormattedStringToWidth(text, maxWidth).size() * fontHeight();
    }

    @Override
    public @NonNull String trimStringToWidth(
            @NonNull String text,
            int width) {

        return trimStringToWidth(text, width, false);
    }

    @Override
    public @NonNull String trimStringToWidth(
            @NonNull String text,
            int width,
            boolean reverse) {

        Preconditions.checkNotNull(text);

        if (width <= 0 || text.isEmpty()) {
            return "";
        }

        String pureText = minecraftPureText(text);
        if (pureText.isEmpty()) {
            return "";
        }

        if (!reverse) {
            return trimForward(pureText, width);
        } else {
            return trimReverse(pureText, width);
        }
    }

    @Override
    public @NonNull List<@NonNull String> listFormattedStringToWidth(
            @NonNull String text,
            int wrapWidth) {

        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(wrapWidth > 0,
                "Argument \"wrapWidth\" must be > 0.");

        String pureText = minecraftPureText(text);
        if (pureText.isEmpty()) {
            return Collections.singletonList("");
        }

        List<String> lines = new ArrayList<>();

        int start = 0;
        while (start < pureText.length()) {
            int hardBreak = ParagraphLineBreaker.findHardBreak(pureText, start);
            int end = hardBreak >= 0 ? hardBreak : pureText.length();

            if (start == end) {
                lines.add("");
            } else {
                appendWrappedSegment(pureText.substring(start, end), wrapWidth, lines);
            }

            if (hardBreak < 0) {
                break;
            }

            start = hardBreak + ParagraphLineBreaker.hardBreakLength(pureText, hardBreak);
        }

        if (lines.isEmpty()) {
            lines.add("");
        }

        return lines;
    }

    @Override
    public int getColorCode(char character) {
        int index = vanillaColorIndex(character);
        if (index < 0) {
            return -1;
        }

        return MINECRAFT_COLORS[index];
    }

    private @NonNull StyledText styled(
            @NonNull String text,
            int color,
            boolean shadow) {

        StyledText.Syntax syntax = shadow ? StyledText.Syntax.MINECRAFT_SHADOW_ON : StyledText.Syntax.MINECRAFT;

        return new StyledText(
                text,
                syntax,
                TextStyle.DEFAULT.withColor(normalizeMinecraftColor(color)));
    }

    private static int normalizeMinecraftColor(int color) {
        // FontRenderer#renderString behavior:
        // colors without an explicit alpha component are treated as opaque
        //
        // if ((color & -67108864) == 0) {
        //     color |= -16777216;
        // }

        if ((color & 0xFC000000) == 0) {
            color |= 0xFF000000;
        }

        return color;
    }

    private @NonNull String minecraftPureText(@NonNull String text) {
        return new StyledText(text, StyledText.Syntax.MINECRAFT).getPureText();
    }

    private float measure(@NonNull String pureText) {
        if (pureText.isEmpty()) {
            return 0f;
        }

        return runtime.simulate(pureText, 0f, 0f).getLineWidth();
    }

    private @NonNull String trimForward(@NonNull String pureText, float width) {
        SimpleTextProducer.LineInfo lineInfo = runtime.simulate(pureText, 0f, 0f);

        int cpCount = lineInfo.getCodepointCount();
        int fittingCpCount = 0;

        for (int i = 0; i < cpCount; i++) {
            if (lineInfo.getProgressiveLengthAt(i) > width) {
                break;
            }

            fittingCpCount = i + 1;
        }

        int end = pureText.offsetByCodePoints(0, fittingCpCount);
        return pureText.substring(0, end);
    }

    private @NonNull String trimReverse(@NonNull String pureText, float width) {
        int start = pureText.length();

        while (start > 0) {
            int previous = pureText.offsetByCodePoints(start, -1);

            String candidate = pureText.substring(previous);
            if (measure(candidate) > width) {
                break;
            }

            start = previous;
        }

        return pureText.substring(start);
    }

    private void appendWrappedSegment(
            @NonNull String text,
            float maxWidth,
            @NonNull List<String> output) {

        int lineStart = 0;

        while (lineStart < text.length()) {
            String remaining = text.substring(lineStart);
            SimpleTextProducer.LineInfo lineInfo = runtime.simulate(remaining, 0f, 0f);

            if (lineInfo.getCodepointCount() == 0) {
                return;
            }

            WrapBreak wrapBreak = lineBreaker.findWrapBreak(remaining, lineInfo, maxWidth);

            Preconditions.checkState(wrapBreak.consumeEnd() > 0,
                    "Wrapping must always make progress.");

            if (wrapBreak.renderEnd() > 0) {
                output.add(remaining.substring(0, wrapBreak.renderEnd()));
            }

            lineStart += wrapBreak.consumeEnd();
        }
    }

    private static int vanillaColorIndex(char character) {
        char code = toLowerAscii(character);
        if (code >= '0' && code <= '9') {
            return code - '0';
        }
        if (code >= 'a' && code <= 'f') {
            return code - 'a' + 10;
        }

        return -1;
    }

    private static char toLowerAscii(char character) {
        if (character >= 'A' && character <= 'Z') {
            return (char) (character + ('a' - 'A'));
        }

        return character;
    }
}
