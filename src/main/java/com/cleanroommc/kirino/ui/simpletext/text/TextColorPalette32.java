package com.cleanroommc.kirino.ui.simpletext.text;

import com.google.common.base.Preconditions;

/**
 * This is a 5-bit color palette.
 *
 * <pre>
 *  0 black
 *  1 dark blue
 *  2 dark green
 *  3 dark aqua
 *  4 dark red
 *  5 dark purple
 *  6 gold
 *  7 gray
 *  8 dark gray
 *  9 blue
 * 10 green
 * 11 aqua
 * 12 red
 * 13 light purple
 * 14 yellow
 * 15 white
 * 16 charcoal
 * 17 neutral gray
 * 18 silver
 * 19 soft white
 * 20 maroon
 * 21 orange
 * 22 olive
 * 23 lime
 * 24 forest green
 * 25 teal
 * 26 sky blue
 * 27 navy
 * 28 violet
 * 29 purple
 * 30 rose
 * 31 pink
 * </pre>
 *
 * <p>Note: Everything is based on ARGB format.</p>
 *
 * @see TextHintABI
 */
final class TextColorPalette32 {

    private TextColorPalette32() {
    }

    static final int COLOR_COUNT = 32;

    private static final int[] RGB = {
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
            0xFFFFFF,
            0x2B2B2B,
            0x808080,
            0xC0C0C0,
            0xE6E6E6,
            0x800000,
            0xFF8000,
            0x808000,
            0x80FF00,
            0x008000,
            0x008080,
            0x0080FF,
            0x000080,
            0x8000FF,
            0x800080,
            0xFF0080,
            0xFF80B5 };

    static int rgb(int index) {
        Preconditions.checkElementIndex(index, RGB.length);

        return RGB[index];
    }

    /**
     * It replaces the low RGB components while preserving the high byte.
     *
     * <p>This supports both:</p>
     * <ul>
     *     <li>{@code 0xRRGGBB}
     *     <li>{@code 0xAARRGGBB}
     * </ul>
     */
    static int replaceRgb(int originalColor, int newColor) {
        return originalColor & 0xFF000000 | newColor & 0x00FFFFFF;
    }

    /**
     * It finds the nearest palette entry index.
     */
    static int nearestIndex(int color) {
        int target = color & 0x00FFFFFF;
        int targetR = target >>> 16 & 0xFF;
        int targetG = target >>> 8 & 0xFF;
        int targetB = target & 0xFF;

        int bestIndex = 0;
        int bestDistance = Integer.MAX_VALUE;

        for (int i = 0; i < RGB.length; i++) {
            int candidate = RGB[i];

            int deltaR = targetR - (candidate >>> 16 & 0xFF);
            int deltaG = targetG - (candidate >>> 8 & 0xFF);
            int deltaB = targetB - (candidate & 0xFF);

            int distance = deltaR * deltaR + deltaG * deltaG + deltaB * deltaB;

            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = i;
                if (distance == 0) {
                    break;
                }
            }
        }

        return bestIndex;
    }
}
