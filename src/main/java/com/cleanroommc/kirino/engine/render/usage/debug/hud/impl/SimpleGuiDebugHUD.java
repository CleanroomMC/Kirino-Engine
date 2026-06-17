package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraft.client.renderer.GlStateManager;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleGuiDebugHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        GlStateManager.disableCull();
//        GlStateManager.enableDepth();
//        GlStateManager.depthMask(true);
        GlStateManager.enableBlend();

        ICS.instance().gui().begin()
//                .append((s) -> {
//                    s.rectEx(10, 10, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 0)
//                            .emit();
//                    s.rectEx(35, 10, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 2)
//                            .emit();
//                    s.rectEx(60, 10, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 5)
//                            .emit();
//                })
//                .append((s) -> {
//                    s.rectEx(10, 35, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 0)
//                            .border(2f, Color.WHITE.getRGB())
//                            .emit();
//                    s.rectEx(35, 35, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 2)
//                            .border(2f, Color.WHITE.getRGB())
//                            .emit();
//                    s.rectEx(60, 35, 20, 20, Color.RED.getRGB())
//                            .radius(5f, 5)
//                            .border(2f, Color.WHITE.getRGB())
//                            .emit();
//                })
                .append((s) -> {
                    s.rectEx(10, 60, 20, 20, Color.RED.getRGB())
                            .radius(5f, 0)
                            .border(2f, Color.WHITE.getRGB())
                            .shadow(1f, 2f, 2f, Color.BLUE.getRGB())
                            .emit();
                    s.rectEx(35, 60, 20, 20, Color.RED.getRGB())
                            .radius(7f, 2)
                            .border(2f, Color.WHITE.getRGB())
                            .shadow(1f, 2f, 2f, Color.BLUE.getRGB())
                            .emit();
                    s.rectEx(60, 60, 20, 20, Color.RED.getRGB())
                            .radius(8f, 5)
                            .border(2f, Color.WHITE.getRGB())
                            .shadow(1f, 2f, 2f, Color.BLUE.getRGB())
                            .emit();
                })
                .endDraw();
    }
}
