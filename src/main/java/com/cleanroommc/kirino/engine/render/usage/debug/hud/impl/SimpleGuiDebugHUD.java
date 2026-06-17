package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraft.client.renderer.GlStateManager;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleGuiDebugHUD implements ImmediateHUD {

    private static final Color COLOR_1 = new Color(148, 172, 191);
    private static final Color COLOR_2 = new Color(74, 98, 116);
    private static final Color COLOR_3 = new Color(79, 175, 178);

    @Override
    public void draw(@NonNull HUDContext hud) {
        GlStateManager.disableCull();
//        GlStateManager.enableDepth();
//        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();

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
                    s.rectEx(10, 85, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(35, 85, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(60, 85, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
                            .emit();
                })
                .append((s) -> {
                    s.rectEx(10, 60, 15, 15, COLOR_1.getRGB())
                            .radius(5f, 1)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(35, 60, 15, 15, COLOR_1.getRGB())
                            .radius(6f, 3)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
                            .emit();
                    s.rectEx(60, 60, 15, 15, COLOR_1.getRGB())
                            .radius(7f, 5)
                            .border(2f, COLOR_2.getRGB())
                            .shadow(0f, 2f, 2f, COLOR_3.getRGB())
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
                .endDraw();
    }
}
