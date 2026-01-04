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

    private final float[] pivotXStack = new float[MAX_DEPTH];
    private final float[] pivotYStack = new float[MAX_DEPTH];
    private final LayoutMode[] modeStack = new LayoutMode[MAX_DEPTH];

    private final float[] widthStack  = new float[MAX_DEPTH];
    private final float[] heightStack = new float[MAX_DEPTH];

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

        pivotXStack[depth] = pivotX;
        pivotYStack[depth] = pivotY;
        modeStack[depth] = mode;

        widthStack[depth] = 0f;
        heightStack[depth] = 0f;

        mode = newMode;
        depth++;
    }

    private void pop(LayoutMode expected) {
        Preconditions.checkState(depth > 0, "HUD layout stack underflow.");
        Preconditions.checkState(mode == expected,
                "Mismatched HUD layout. Expected %s but got %s.", mode, expected);

        depth--;

        float usedW = widthStack[depth];
        float usedH = heightStack[depth];

        pivotX = pivotXStack[depth];
        pivotY = pivotYStack[depth];
        mode = modeStack[depth];

        advance(usedW, usedH);
    }

    private void drawRect(float x, float y, float width, float height, int color) {
        float a = (float) (color >> 24 & 255) / 255.0f;
        float r = (float) (color >> 16 & 255) / 255.0f;
        float g = (float) (color >> 8 & 255) / 255.0f;
        float b = (float) (color & 255) / 255.0f;

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

    public void text(String text) {
        float width = getTextWidth(text);
        float height = getTextHeight(text);

        drawRect(pivotX, pivotY, width, height, FONT_BACKGROUND_COLOR.getRGB());
        drawText(text, pivotX + 1, pivotY + 1, FONT_COLOR.getRGB());

        advance(width, height);
    }

    private void advance(float w, float h) {
        if (depth > 0) {
            int i = depth - 1;
            widthStack[i] = Math.max(widthStack[i],  w);
            heightStack[i] = Math.max(heightStack[i], h);
        }

        switch (mode) {
            case HORIZONTAL -> pivotX += w + HORIZONTAL_SPACING;
            case VERTICAL, NONE -> pivotY += h;
        }
    }

    private float getTextWidth(String text) {
        return fontRenderer.getStringWidth(text) + 2;
    }

    private float getTextHeight(String text) {
        return LINE_HEIGHT;
    }
}
