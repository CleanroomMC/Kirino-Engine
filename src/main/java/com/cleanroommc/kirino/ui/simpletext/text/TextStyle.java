package com.cleanroommc.kirino.ui.simpletext.text;

import org.jspecify.annotations.NonNull;

import java.awt.*;

public record TextStyle(float size, int color, int hint) {

    public static final TextStyle DEFAULT = new TextStyle(1f, Color.WHITE.getRGB(), 0);

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
