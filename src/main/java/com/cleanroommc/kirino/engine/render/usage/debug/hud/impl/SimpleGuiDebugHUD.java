package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraft.client.renderer.GlStateManager;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleGuiDebugHUD implements ImmediateHUD {

    private static final Color COLOR_1 = new Color(148, 172, 191);
    private static final Color COLOR_2 = new Color(74, 98, 116);
    private static final Color COLOR_3 = new Color(79, 175, 178);

    private final StopWatch stopWatch = new StopWatch();
    private float theta = 0f;

    @Override
    public void draw(@NonNull HUDContext hud) {
        float deltaTime = 0f;
        if (!stopWatch.isStarted()) {
            stopWatch.start();
        } else {
            stopWatch.stop();
            deltaTime = (float) (stopWatch.getNanoTime() / 1E9d);
            stopWatch.reset();
            stopWatch.start();
        }

        GlStateManager.disableCull();
//        GlStateManager.enableDepth();
//        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();

        theta += deltaTime * 2f;
        if (theta >= 2f * Math.PI) {
            theta -= (float) (2f * Math.PI);
        }
        float cos = 2f * (float) Math.cos(theta);
        float sin = 2f * (float) Math.sin(theta);

        ICS.instance().gui().begin()
                .append((s) -> {
                    s.rectEx(10, 10, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .emit();
                    s.rectEx(35, 10, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .emit();
                    s.rectEx(60, 10, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(10, 35, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .border(2f, COLOR_2.getRGB())
                            .emit();
                    s.rectEx(35, 35, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .border(2f, COLOR_2.getRGB())
                            .emit();
                    s.rectEx(60, 35, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .border(2f, COLOR_2.getRGB())
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(10, 60, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(35, 60, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(60, 60, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(10, 85, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(35, 85, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(60, 85, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(90, 10, 20, 20, COLOR_1.getRGB())
                            .emit();
                    s.rectEx(115, 10, 20, 20, COLOR_1.getRGB())
                            .border(2f, COLOR_2.getRGB())
                            .emit();
                    s.rectEx(140, 10, 20, 20, COLOR_1.getRGB())
                            .shadow(0f, 1f, 1f, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(165, 10, 20, 20, COLOR_1.getRGB())
                            .border(2f, COLOR_2.getRGB())
                            .shadow(0f, 1f, 1f, COLOR_3.getRGB())
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(90, 40, 50, 50, COLOR_1.getRGB())
                            .radius(0f, 6)
                            .border(3f, COLOR_2.getRGB())
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(150, 40, 50, 50, COLOR_1.getRGB())
                            .radius(0f, 6)
                            .shadow(1f, cos, sin, COLOR_3.getRGB())
                            .emit();
                })
                .endDraw();

        ICS.instance().gui().begin()
                .append((s) -> {
                    s.lines(3, 4f, true, COLOR_1.getRGB())
                            .put(100, 100)
                            .put(120, 120)
                            .put(140, 100)
                            .emit();
                    s.lines(6, 4f, true, COLOR_1.getRGB())
                            .color1(COLOR_2.getRGB())
                            .put(150, 100)
                            .put(155, 130)
                            .put(160, 100)
                            .put(165, 130)
                            .put(170, 100)
                            .put(175, 130)
                            .emit();
                    s.lines(3, 4f, false, COLOR_1.getRGB())
                            .color1(COLOR_2.getRGB())
                            .color2(COLOR_3.getRGB())
                            .put(100, 140)
                            .put(105, 160)
                            .put(180, 140)
                            .emit();
                })
                .endDraw();
    }
}
