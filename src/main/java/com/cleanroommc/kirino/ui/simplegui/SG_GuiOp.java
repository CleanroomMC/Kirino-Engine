package com.cleanroommc.kirino.ui.simplegui;

final class SG_GuiOp {

    static final int DRAW_RECT = 1;
    static final int DRAW_LINES = 2;
    static final int DRAW_BEZIER = 3;
    static final int PUSH_CLIP = 99;
    static final int POP_CLIP = 100;

    // DRAW_RECT
    static final int FLAG_RADIUS = 1;
    static final int FLAG_BORDER = 1 << 1;
    static final int FLAG_SHADOW = 1 << 2;

    // DRAW_LINES
    static final int FLAG_COLOR1 = 1;
    static final int FLAG_COLOR2 = 1 << 1;

    static final int FLAG_COMPILED = 1 << 30;
}
