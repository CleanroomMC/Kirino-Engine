package com.cleanroommc.kirino.engine.render.debug.hud.impl;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.debug.data.impl.FpsHistory;
import com.cleanroommc.kirino.engine.render.debug.data.impl.RenderStatsFrame;
import com.cleanroommc.kirino.engine.render.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.debug.hud.IImmediateHUD;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;

public class CommonStatsHUD implements IImmediateHUD {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void draw(@NonNull HUDContext hud) {
        var renderStatsFrame = KirinoCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        int drawCalls = -1;
        if (renderStatsFrame.fetch() != null) {
            drawCalls = renderStatsFrame.fetch().getDrawCalls();
        }

        hud.text("Draw Calls via KE: " + ((drawCalls == -1) ? "UNKNOWN" : drawCalls));

        var fpsHistory = KirinoCore.DEBUG_SERVICE.get(FpsHistory.class);
        boolean fpsGraph = false;
        int[] fpsSnapshot = null;
        int logicalFpsIndex = 0;
        int physicafFpsIndex = 0;
        int maxFps = 0;
        if (fpsHistory.fetch() != null) {
            fpsGraph = true;
            fpsSnapshot = fpsHistory.fetch().snapshot();
            logicalFpsIndex = fpsHistory.fetch().logicalIndex();
            physicafFpsIndex = fpsHistory.fetch().physicalIndex();
            maxFps = fpsHistory.fetch().maxFpsEver();
        }

        if (fpsGraph) {
            hud.text("Curr FPS: " + Math.min(Minecraft.getDebugFPS(), fpsSnapshot[physicafFpsIndex]) +
                    ", Max FPS: " + maxFps);

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

    private static void drawFpsGraph(
            @NonNull Tessellator tessellator,
            float x,
            float y,
            float width,
            float height,
            int fpsIndex,
            int @NonNull [] fpsSnapshot,
            int maxFps
    ) {
        Preconditions.checkNotNull(tessellator);
        Preconditions.checkNotNull(fpsSnapshot);

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
