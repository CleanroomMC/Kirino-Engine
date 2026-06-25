package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class CmdLinesBuilder {

    private final GuiCommandStream out;

    private final int vertexNum;
    private final float lineWidth;
    private final float[] vertices;
    private final boolean formsLoop;

    private int flags;

    private final int color0;
    private int color1;
    private int color2;

    private int index = 0;

    CmdLinesBuilder(
            GuiCommandStream out,
            int vertexNum,
            float lineWidth,
            boolean formsLoop,
            int color0) {

        this.out = out;
        this.vertexNum = vertexNum;
        this.lineWidth = lineWidth;
        vertices = new float[vertexNum * 2];
        this.formsLoop = formsLoop;
        this.color0 = color0;
    }

    @NonNull
    public CmdLinesBuilder set(int vertIndex, float x, float y) {
        Preconditions.checkElementIndex(vertIndex, vertexNum);

        vertices[vertIndex * 2] = x;
        vertices[vertIndex * 2 + 1] = y;
        return this;
    }

    @NonNull
    public CmdLinesBuilder put(float x, float y) {
        Preconditions.checkElementIndex(index, vertexNum);

        vertices[index * 2] = x;
        vertices[index * 2 + 1] = y;
        index++;
        return this;
    }

    @NonNull
    public CmdLinesBuilder color1(int color1) {
        flags |= SG_GuiOp.FLAG_COLOR1;
        this.color1 = color1;
        return this;
    }

    @NonNull
    public CmdLinesBuilder color2(int color2) {
        flags |= SG_GuiOp.FLAG_COLOR2;
        this.color2 = color2;
        return this;
    }

    public void emit() {
        if ((flags & SG_GuiOp.FLAG_COLOR2) != 0) {
            Preconditions.checkState((flags & SG_GuiOp.FLAG_COLOR1) != 0,
                    "When FLAG_COLOR2 is enabled, FLAG_COLOR1 must be enabled too.");
        }

        out.writeLines(
                vertexNum,
                lineWidth,
                vertices,
                formsLoop,
                flags,
                color0,
                color1,
                color2);
    }
}
