package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

final class MinecraftStyledTextParser implements StyledTextParser {

    static final MinecraftStyledTextParser INSTANCE = new MinecraftStyledTextParser();

    private static final char CONTROL_PREFIX = '§';

    private MinecraftStyledTextParser() {
    }

    /**
     * Helper for the Minecraft FontRenderer specs.
     * <p>
     * Clears {@link TextHintABI#OBFUSCATED}, {@link TextHintABI#BOLD}, {@link TextHintABI#ITALIC},
     * {@link TextHintABI#STRIKETHROUGH}, {@link TextHintABI#UNDERLINE} altogether
     */
    private static int clearMinecraftFormatting(int hint) {
        hint = TextHintABI.OBFUSCATED.set(hint, false);
        hint = TextHintABI.BOLD.set(hint, false);
        hint = TextHintABI.ITALIC.set(hint, false);
        hint = TextHintABI.STRIKETHROUGH.set(hint, false);
        hint = TextHintABI.UNDERLINE.set(hint, false);
        return hint;
    }

    /**
     * Helper for the Minecraft FontRenderer specs.
     * <p>
     * Sets {@link TextHintABI#STRIKETHROUGH_COLOR}, {@link TextHintABI#UNDERLINE_COLOR} altogether
     */
    private static int setMinecraftDecoColor(int hint, int paletteIndex) {
        hint = TextHintABI.STRIKETHROUGH_COLOR.set(hint, paletteIndex);
        hint = TextHintABI.UNDERLINE_COLOR.set(hint, paletteIndex);
        return hint;
    }

    private static TextStyle createBaseStyle(TextStyle defaultStyle) {
        int hint = defaultStyle.hint();
        hint = clearMinecraftFormatting(hint);
        int paletteIndex = TextColorPalette32.nearestIndex(defaultStyle.color());
        hint = setMinecraftDecoColor(hint, paletteIndex);
        return new TextStyle(1f, defaultStyle.color(), hint);
    }

    private static int vanillaColorIndex(char code) {
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

    @Override
    public void parse(@NonNull String rawText, @NonNull StyledTextBuilder builder) {
        TextStyle baseStyle = createBaseStyle(builder.defaultStyle());
        builder.style(baseStyle);

        int start = 0;
        int index = 0;

        while (index < rawText.length()) {
            if (rawText.charAt(index) != CONTROL_PREFIX) {
                index++;
                continue;
            }
            if (index + 1 >= rawText.length()) {
                index++;
                continue;
            }

            builder.appendLiteral(rawText, start, index);

            char formattingCode = toLowerAscii(rawText.charAt(index + 1));
            applyFormattingCode(formattingCode, baseStyle, builder);

            index += 2;
            start = index;
        }

        builder.appendLiteral(rawText, start, rawText.length());
    }

    private static void applyFormattingCode(char code, TextStyle baseStyle, StyledTextBuilder builder) {
        int colorIndex = vanillaColorIndex(code);
        if (colorIndex >= 0) {
            applyColor(colorIndex, baseStyle, builder);
            return;
        }

        switch (code) {
            case 'k' -> enable(TextHintABI.OBFUSCATED, builder);
            case 'l' -> enable(TextHintABI.BOLD, builder);
            case 'm' -> enable(TextHintABI.STRIKETHROUGH, builder);
            case 'n' -> enable(TextHintABI.UNDERLINE, builder);
            case 'o' -> enable(TextHintABI.ITALIC, builder);
            case 'r' -> builder.style(baseStyle);
            default -> applyColor(15, baseStyle, builder);
        }
    }

    private static void applyColor(int paletteIndex, TextStyle baseStyle, StyledTextBuilder builder) {
        TextStyle current = builder.currentStyle();
        int hint = current.hint();
        hint = clearMinecraftFormatting(hint);
        hint = setMinecraftDecoColor(hint, paletteIndex);
        int color = TextColorPalette32.replaceRgb(baseStyle.color(), TextColorPalette32.rgb(paletteIndex));
        builder.style(new TextStyle(1f, color, hint));
    }

    private static void enable(TextHintABI.Bit bit, StyledTextBuilder builder) {
        TextStyle current = builder.currentStyle();
        int hint = bit.set(current.hint(), true);
        builder.style(new TextStyle(1f, current.color(), hint));
    }
}
