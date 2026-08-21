package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

import java.awt.*;

public record TextStyle(float size, int color, int hint) {

    public static final TextStyle DEFAULT = new TextStyle(1f, Color.WHITE.getRGB(), 0);

    // <editor-fold desc="hint helpers">
    public boolean underlineEnabled() {
        return TextHintLayout.UNDERLINE.get(hint);
    }

    public int underlineColor() {
        return paletteColor(TextHintLayout.UNDERLINE_COLOR.get(hint));
    }

    public boolean underlineShadowEnabled() {
        return TextHintLayout.UNDERLINE_SHADOW_ENABLED.get(hint);
    }

    public boolean strikethroughEnabled() {
        return TextHintLayout.STRIKETHROUGH.get(hint);
    }

    public int strikethroughColor() {
        return paletteColor(TextHintLayout.STRIKETHROUGH_COLOR.get(hint));
    }

    public boolean strikethroughRounded() {
        return TextHintLayout.STRIKETHROUGH_ROUNDED.get(hint);
    }

    public boolean strikethroughOutlineEnabled() {
        return TextHintLayout.STRIKETHROUGH_OUTLINE_ENABLED.get(hint);
    }

    public int strikethroughOutlineColor() {
        return paletteColor(TextHintLayout.STRIKETHROUGH_OUTLINE_COLOR.get(hint));
    }

    private int paletteColor(int paletteIndex) {
        return TextColorPalette32.replaceRgb(0xFF000000, TextColorPalette32.rgb(paletteIndex));
    }
    // </editor-fold>

    @NonNull
    public TextStyle withSize(float size) {
        if (this.size == size) {
            return this;
        }

        return new TextStyle(size, color, hint);
    }

    @NonNull
    public TextStyle withColor(int color) {
        if (this.color == color) {
            return this;
        }

        return new TextStyle(size, color, hint);
    }

    @NonNull
    public TextStyle withHint(int hint) {
        if (this.hint == hint) {
            return this;
        }

        return new TextStyle(size, color, hint);
    }
}
