package com.cleanroommc.kirino.ui.simpletext.facade;

import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * This is not designed for one-to-one redirections, but only providing {@link net.minecraft.client.gui.FontRenderer}
 * similar convenient paths.
 */
@Deprecated
public interface FontRendererFacade {

    int fontHeight();

    int drawStringWithShadow(
            @NonNull String text,
            float x,
            float y,
            int color);

    int drawString(
            @NonNull String text,
            int x,
            int y,
            int color);

    int drawString(
            @NonNull String text,
            float x,
            float y,
            int color,
            boolean dropShadow);

    void drawSplitString(
            @NonNull String text,
            int x,
            int y,
            int wrapWidth,
            int color);

    int getStringWidth(@NonNull String text);

    int getCharWidth(char character);

    int getWordWrappedHeight(@NonNull String text, int maxWidth);

    @NonNull String trimStringToWidth(
            @NonNull String text,
            int width);

    @NonNull String trimStringToWidth(
            @NonNull String text,
            int width,
            boolean reverse);

    @NonNull List<@NonNull String> listFormattedStringToWidth(
            @NonNull String text,
            int wrapWidth);

    int getColorCode(char character);
}
