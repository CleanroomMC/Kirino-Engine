package com.cleanroommc.kirino.engine.render.debug.hud;

import com.google.common.base.Preconditions;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class HUDContext {

    private enum LayoutMode {
        NONE,
        HORIZONTAL,
        VERTICAL
    }

    private float pivotX = 0f;
    private float pivotY = 0f;

    private static final int MAX_DEPTH = 16;

    private LayoutMode mode = LayoutMode.NONE;
    private int depth = 0;

    private final float[] parentPivotXStack = new float[MAX_DEPTH];
    private final float[] parentPivotYStack = new float[MAX_DEPTH];
    private final LayoutMode[] parentModeStack = new LayoutMode[MAX_DEPTH];

    private final float[] originXStack = new float[MAX_DEPTH];
    private final float[] originYStack = new float[MAX_DEPTH];
    private final float[] maxXStack = new float[MAX_DEPTH];
    private final float[] maxYStack = new float[MAX_DEPTH];

    private static final float LINE_HEIGHT = 12f;
    private static final float HORIZONTAL_SPACING = 4f;

    private static final Color FONT_COLOR = new Color(213, 213, 213);
    private static final Color FONT_BACKGROUND_COLOR = new Color(28, 28, 28, 71);

    private final FontRenderer fontRenderer;
    private final Tessellator tessellator;

    HUDContext(FontRenderer fontRenderer, Tessellator tessellator) {
        this.fontRenderer = fontRenderer;
        this.tessellator = tessellator;
    }

    protected void setPivotX(float pivotX) {
        this.pivotX = pivotX;
    }

    protected void setPivotY(float pivotY) {
        this.pivotY = pivotY;
    }

    public float getPivotX() {
        return pivotX;
    }

    public float getPivotY() {
        return pivotY;
    }

    public int getDepth() {
        return depth;
    }

    public void beginHorizontal() {
        push(LayoutMode.HORIZONTAL);
    }

    public void endHorizontal() {
        pop(LayoutMode.HORIZONTAL);
    }

    public void beginVertical() {
        push(LayoutMode.VERTICAL);
    }

    public void endVertical() {
        pop(LayoutMode.VERTICAL);
    }

    private void push(LayoutMode newMode) {
        Preconditions.checkState(depth < MAX_DEPTH, "HUD layout stack overflow.");

        parentPivotXStack[depth] = pivotX;
        parentPivotYStack[depth] = pivotY;
        parentModeStack[depth] = mode;

        originXStack[depth] = pivotX;
        originYStack[depth] = pivotY;
        maxXStack[depth] = pivotX;
        maxYStack[depth] = pivotY;

        mode = newMode;
        depth++;
    }

    private void pop(LayoutMode expected) {
        Preconditions.checkState(depth > 0, "HUD layout stack underflow.");
        Preconditions.checkState(mode == expected,
                "Mismatched HUD layout. Expected %s but got %s.", expected, mode);

        depth--;

        float usedW = Math.max(0f, maxXStack[depth] - originXStack[depth]);
        float usedH = Math.max(0f, maxYStack[depth] - originYStack[depth]);

        pivotX = parentPivotXStack[depth];
        pivotY = parentPivotYStack[depth];
        mode = parentModeStack[depth];

        advanceChildRect(usedW, usedH);
    }

    public void text(String text) {
        float w = getTextWidth(text);
        float h = getTextHeight(text);

        drawRect(pivotX, pivotY, w, h, FONT_BACKGROUND_COLOR.getRGB());
        drawText(text, pivotX + 1, pivotY + 1, FONT_COLOR.getRGB());

        advanceChildRect(w, h);
    }

    private void advanceChildRect(float w, float h) {
        if (depth > 0) {
            int i = depth - 1;
            float childRight = pivotX + w;
            float childBottom = pivotY + h;
            if (childRight > maxXStack[i]) {
                maxXStack[i] = childRight;
            }
            if (childBottom > maxYStack[i]) {
                maxYStack[i] = childBottom;
            }
        }

        switch (mode) {
            case HORIZONTAL -> pivotX += w + HORIZONTAL_SPACING;
            case VERTICAL, NONE -> pivotY += h;
        }
    }

    private void drawRect(float x, float y, float width, float height, int color) {
        float a = (float) (color >> 24 & 255) / 255f;
        float r = (float) (color >> 16 & 255) / 255f;
        float g = (float) (color >> 8 & 255) / 255f;
        float b = (float) (color & 255) / 255f;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y + height, 0).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y + height, 0).color(r, g, b, a).endVertex();
        buffer.pos(x + width, y, 0).color(r, g, b, a).endVertex();
        buffer.pos(x, y, 0).color(r, g, b, a).endVertex();
        tessellator.draw();
    }

    private void drawText(String text, float x, float y, int color) {
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, Math.round(y), 0);
        fontRenderer.drawString(text, 0, 0, color, false);
        GlStateManager.popMatrix();
    }

    private float getTextWidth(String text) {
        return fontRenderer.getStringWidth(text) + 2;
    }

    private float getTextHeight(String text) {
        return LINE_HEIGHT;
    }
}
