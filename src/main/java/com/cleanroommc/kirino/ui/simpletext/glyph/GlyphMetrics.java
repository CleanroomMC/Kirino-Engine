package com.cleanroommc.kirino.ui.simpletext.glyph;

import com.google.common.base.Preconditions;

/**
 * <p>Glyph metrics for the horizontal layout.</p>
 * It resembles a builder pattern. Call {@link #set(float, int, int, int, int)} and {@link #setSdf(int)}
 * to initialize the metrics.
 */
public final class GlyphMetrics {

    private boolean empty = true;

    /**
     * A {@link GlyphMetrics} is only considered empty when no parameters have been set.
     */
    public boolean isEmpty() {
        return empty;
    }

    private boolean set = false;
    private float advanceX;
    private int bearingX;
    private int bearingY;
    private int glyphWidth;
    private int glyphHeight;

    /**
     * <p>Unit: pixel</p>
     */
    public float getAdvanceX() {
        return advanceX;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getBearingX() {
        return bearingX;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getBearingY() {
        return bearingY;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getGlyphWidth() {
        return glyphWidth;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getGlyphHeight() {
        return glyphHeight;
    }

    /**
     * <p>Note: It must only be called once.</p>
     */
    public void set(
            float advanceX,
            int bearingX,
            int bearingY,
            int glyphWidth,
            int glyphHeight) {

        Preconditions.checkState(!set, "Must not set parameters twice.");

        set = true;
        empty = false;

        this.advanceX = advanceX;
        this.bearingX = bearingX;
        this.bearingY = bearingY;
        this.glyphWidth = glyphWidth;
        this.glyphHeight = glyphHeight;
    }

    private boolean sdfSet = false;
    private int sdfWidth;
    private int sdfHeight;
    private int sdfPadding;

    /**
     * <p>Note: It must only be called once.</p>
     */
    public void setSdf(int sdfPadding) {
        Preconditions.checkState(!sdfSet, "Must not set SDF parameters twice.");

        sdfSet = true;
        empty = false;

        sdfWidth = glyphWidth + sdfPadding * 2;
        sdfHeight = glyphHeight + sdfPadding * 2;
        this.sdfPadding = sdfPadding;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getSdfWidth() {
        return sdfWidth;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getSdfHeight() {
        return sdfHeight;
    }

    /**
     * <p>Unit: pixel</p>
     */
    public int getSdfPadding() {
        return sdfPadding;
    }
}
