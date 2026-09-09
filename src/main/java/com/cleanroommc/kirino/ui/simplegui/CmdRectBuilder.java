package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class CmdRectBuilder {

    private final GuiCommandStream out;

    private final float x, y, width, height;
    private final int color;

    private int flags;

    private float radius;
    private int cornerType;

    private float borderWidth;
    private int borderColor;

    private float shadowBlur;
    private float shadowX;
    private float shadowY;
    private int shadowColor;

    CmdRectBuilder(GuiCommandStream out, float x, float y, float width, float height, int color) {
        this.out = out;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    /**
     * Enables rounded-corner geometry for this rectangle.
     *
     * <p>The corner geometry is selected by <code>type</code>:</p>
     * <ul>
     *     <li><code>0</code>: quarter-circle corner, 5 vertices per corner</li>
     *     <li><code>1</code>: quarter-circle corner, 10 vertices per corner</li>
     *     <li><code>2</code>: superellipse with <code>n = 4</code>, 8 vertices per corner</li>
     *     <li><code>3</code>: superellipse with <code>n = 4</code>, 16 vertices per corner</li>
     *     <li><code>4</code>: superellipse with <code>n = 5</code>, 8 vertices per corner</li>
     *     <li><code>5</code>: superellipse with <code>n = 5</code>, 16 vertices per corner</li>
     *     <li><code>6</code>: square corners</li>
     * </ul>
     *
     * <p><code>r</code> may be <code>0</code>, although this is generally not recommended.
     * For <code>type == 6</code>, <code>r</code> has no effect; type <code>6</code> exists to
     * represent a square within the rounded-rectangle rendering path.</p>
     *
     * @param r The corner radius
     * @param type The corner geometry type
     */
    @NonNull
    public CmdRectBuilder radius(float r, int type) {
        Preconditions.checkArgument(r >= 0f, "Argument \"r\" must be no smaller than 0.");
        Preconditions.checkArgument(2f * r <= Math.min(width, height),
                "\"2 * r\"=%s must be no greater than the minimum of \"width\"=%s and \"height\"=%s.",
                2f * r, width, height);
        Preconditions.checkArgument(
                type == 0 ||
                        type == 1 ||
                        type == 2 ||
                        type == 3 ||
                        type == 4 ||
                        type == 5 ||
                        type == 6, "Argument \"type\"=%s must be either 0, 1, 2, 3, 4, 5, 6.", type);

        flags |= SG_GuiOp.FLAG_RADIUS;
        radius = r;
        cornerType = type;
        return this;
    }

    /**
     * Enables an outer border around the rectangle.
     *
     * <p>The border expands outward from the rectangle boundary and doesn't
     * consume or shrink the rectangle's interior area.</p>
     *
     * <p>For rounded rectangles, enabling a border also affects the appearance
     * of the soft shadow. Without a border, the shadow generally appears flatter;
     * with a border enabled, the transition appears softer.
     * This is part of the current rounded-rectangle rendering behavior.</p>
     *
     * @param width The outward border width
     * @param color The border color
     */
    @NonNull
    public CmdRectBuilder border(float width, int color) {
        Preconditions.checkArgument(width >= 0f, "Argument \"width\" must be no smaller than 0.");

        flags |= SG_GuiOp.FLAG_BORDER;
        borderWidth = width;
        borderColor = color;
        return this;
    }

    /**
     * Enables the shadow/background effect for this rectangle.
     *
     * <p>The semantics differ depending on whether rounded-corner geometry is enabled.</p>
     *
     * <p>For a regular rectangle, <code>blur</code> may be any non-negative value and
     * directly specifies the outward padding of an additional background.
     * <code>offsetX</code> and <code>offsetY</code> translate that background. In this mode,
     * the effect is not a true soft shadow.</p>
     *
     * <p>For a rounded rectangle, <code>blur</code> must be in the closed interval
     * <code>[0, 1]</code> and controls a soft shadow: larger values produce a wider blur.
     * The offset determines the shadow direction, while its magnitude determines
     * the outward extension of the shadow. By the way, a zero-length offset
     * is valid, although generally not recommended.</p>
     *
     * @param blur The background padding or soft-shadow blur amount, depending on the rectangle mode
     * @param offsetX The horizontal shadow/background offset
     * @param offsetY The vertical shadow/background offset
     * @param color The shadow/background color
     */
    @NonNull
    public CmdRectBuilder shadow(float blur, float offsetX, float offsetY, int color) {
        Preconditions.checkArgument(blur >= 0f, "Argument \"blur\" must be no smaller than 0.");
        if ((flags & SG_GuiOp.FLAG_RADIUS) != 0) {
            Preconditions.checkArgument(blur <= 1f, "Argument \"blur\" must be in [0, 1] when FLAG_RADIUS is on.");
        }

        flags |= SG_GuiOp.FLAG_SHADOW;
        shadowBlur = blur;
        shadowX = offsetX;
        shadowY = offsetY;
        shadowColor = color;
        return this;
    }

    public void emit() {
        out.writeRectEx(
                x, y,
                width, height,
                color,
                flags,
                radius,
                cornerType,
                borderWidth, borderColor,
                shadowBlur, shadowX, shadowY, shadowColor);
    }
}
