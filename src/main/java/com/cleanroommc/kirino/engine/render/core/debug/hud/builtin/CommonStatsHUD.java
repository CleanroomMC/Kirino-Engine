package com.cleanroommc.kirino.engine.render.core.debug.hud.builtin;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.engine.render.core.debug.data.builtin.FpsHistory;
import com.cleanroommc.kirino.engine.render.core.debug.data.builtin.RenderStatsFrame;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class CommonStatsHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        var renderStatsFrame = KirinoClientCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        int drawCalls = -1;
        var renderStatsFrameValue = renderStatsFrame.fetch();
        if (renderStatsFrameValue != null) {
            drawCalls = renderStatsFrameValue.getDrawCalls();
        }

        hud.text("Draw Calls via KE: " + ((drawCalls == -1) ? "UNKNOWN" : drawCalls));

        var fpsHistory = KirinoClientCore.DEBUG_SERVICE.get(FpsHistory.class);
        boolean drawFpsGraph = false;
        int[] fpsSnapshot = null;
        int logicalFpsIndex = 0;
        int physicafFpsIndex = 0;
        int maxFps = 0;
        var fpsHistoryValue = fpsHistory.fetch();
        if (fpsHistoryValue != null) {
            drawFpsGraph = true;
            fpsSnapshot = fpsHistoryValue.snapshot();
            logicalFpsIndex = fpsHistoryValue.logicalIndex();
            physicafFpsIndex = fpsHistoryValue.physicalIndex();
            maxFps = fpsHistoryValue.maxFpsEver();
        }

        if (drawFpsGraph) {
            hud.text("Curr FPS: " + Math.min(Minecraft.getDebugFPS(), fpsSnapshot[physicafFpsIndex]) +
                    ", Max FPS: " + maxFps +
                    ", Rolling Avg: " + (int) Arrays.stream(fpsSnapshot).average().orElse(0));

            float x = hud.getPivotX();
            float y = hud.getPivotY();
            hud.empty(120, 50);

            drawFpsGraph(
                    hud.getTessellator(),
                    x + 5, y + 5, 110, 40,
                    logicalFpsIndex,
                    fpsSnapshot,
                    maxFps);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void drawFpsGraph(
            Tessellator tessellator,
            float x, float y, float width, float height,
            int fpsIndex,
            int[] fpsSnapshot,
            int maxFps) {

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        BufferBuilder buffer = tessellator.getBuffer();

        float bgA = 0.6f;
        float bgR = 0.1f;
        float bgG = 0.1f;
        float bgB = 0.1f;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x, y + height, 0).color(bgR, bgG, bgB, bgA).endVertex();
        buffer.pos(x + width, y + height, 0).color(bgR, bgG, bgB, bgA).endVertex();
        buffer.pos(x + width, y, 0).color(bgR, bgG, bgB, bgA).endVertex();
        buffer.pos(x, y, 0).color(bgR, bgG, bgB, bgA).endVertex();
        tessellator.draw();

        int n = fpsSnapshot.length;

        // not power of 2
        if (!(n > 0 && (n & (n - 1)) == 0)) {
            return;
        }

        int mask = n - 1;
        float stepX = width / (n - 1);

        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

        for (int i = 0; i < n; i++) {
            int ringIndex = (fpsIndex + i) & mask;

            int fps = fpsSnapshot[ringIndex];
            if (fps < 0) {
                fps = 0;
            }
            if (fps > maxFps) {
                fps = maxFps;
            }

            float t = fps / (float) maxFps;
            float px = x + i * stepX;
            float py = y + height - (t * height);

            float u = 1.0f - t;
            float r = u < 0.5f ? (u * 2.0f) : 1.0f;
            float g = u < 0.5f ? 1.0f : (2.0f - u * 2.0f);
            float b = 0.0f;

            buffer.pos(px, py, 0).color(r, g, b, 1.0f).endVertex();
        }

        tessellator.draw();
    }
}
