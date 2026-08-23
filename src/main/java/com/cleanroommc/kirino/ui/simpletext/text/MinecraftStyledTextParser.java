package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

class MinecraftStyledTextParser implements StyledTextParser {

    static final MinecraftStyledTextParser INSTANCE = new MinecraftStyledTextParser();

    private static final char CONTROL_PREFIX = '§';

    protected MinecraftStyledTextParser() {
    }

    protected @NonNull TextStyle transformStyle(@NonNull TextStyle style) {
        return style;
    }

    private void setStyle(@NonNull StyledTextBuilder builder, @NonNull TextStyle style) {
        builder.style(transformStyle(style));
    }

    /**
     * Helper for the Minecraft FontRenderer specs.
     * <p>
     * Clears {@link TextHintLayout#OBFUSCATED}, {@link TextHintLayout#BOLD},
     * {@link TextHintLayout#ITALIC}, {@link TextHintLayout#STRIKETHROUGH},
     * {@link TextHintLayout#UNDERLINE} altogether.
     */
    private static int clearMinecraftFormatting(int hint) {
        hint = TextHintLayout.OBFUSCATED.set(hint, false);
        hint = TextHintLayout.BOLD.set(hint, false);
        hint = TextHintLayout.ITALIC.set(hint, false);
        hint = TextHintLayout.STRIKETHROUGH.set(hint, false);
        hint = TextHintLayout.UNDERLINE.set(hint, false);
        return hint;
    }

    /**
     * Helper for the Minecraft FontRenderer specs.
     * <p>
     * Sets {@link TextHintLayout#STRIKETHROUGH_COLOR},
     * {@link TextHintLayout#UNDERLINE_COLOR} altogether.
     */
    private static int setMinecraftDecoColor(int hint, int paletteIndex) {
        hint = TextHintLayout.STRIKETHROUGH_COLOR.set(hint, paletteIndex);
        hint = TextHintLayout.UNDERLINE_COLOR.set(hint, paletteIndex);
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
        setStyle(builder, baseStyle);

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

    private void applyFormattingCode(char code, @NonNull TextStyle baseStyle, @NonNull StyledTextBuilder builder) {
        int colorIndex = vanillaColorIndex(code);
        if (colorIndex >= 0) {
            applyColor(colorIndex, baseStyle, builder);
            return;
        }

        switch (code) {
            case 'k' -> enable(TextHintLayout.OBFUSCATED, builder);
            case 'l' -> enable(TextHintLayout.BOLD, builder);
            case 'm' -> enable(TextHintLayout.STRIKETHROUGH, builder);
            case 'n' -> enable(TextHintLayout.UNDERLINE, builder);
            case 'o' -> enable(TextHintLayout.ITALIC, builder);
            case 'r' -> setStyle(builder, baseStyle);
            default -> applyColor(15, baseStyle, builder);
        }
    }

    private void applyColor(int paletteIndex, @NonNull TextStyle baseStyle, @NonNull StyledTextBuilder builder) {
        TextStyle current = builder.currentStyle();
        int hint = current.hint();
        hint = clearMinecraftFormatting(hint);
        hint = setMinecraftDecoColor(hint, paletteIndex);
        int color = TextColorPalette32.replaceRgb(baseStyle.color(), TextColorPalette32.rgb(paletteIndex));
        setStyle(builder, new TextStyle(1f, color, hint));
    }

    private void enable(TextHintLayout.@NonNull Bit bit, @NonNull StyledTextBuilder builder) {
        TextStyle current = builder.currentStyle();
        int hint = bit.set(current.hint(), true);
        setStyle(builder, new TextStyle(1f, current.color(), hint));
    }
}
