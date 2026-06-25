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

    @NonNull
    public CmdRectBuilder border(float width, int color) {
        Preconditions.checkArgument(width >= 0f, "Argument \"width\" must be no smaller than 0.");

        flags |= SG_GuiOp.FLAG_BORDER;
        borderWidth = width;
        borderColor = color;
        return this;
    }

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
