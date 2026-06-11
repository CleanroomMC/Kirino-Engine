package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class SimpleGuiDebugHUD implements ImmediateHUD {

    boolean flag = true;

    @Override
    public void draw(@NonNull HUDContext hud) {
        if (flag) {
            flag = false;
            ICS.instance().gui().begin()
                    .append((s) -> {
//                        s.rectEx(10, 10, 20, 20, Color.WHITE.getRGB())
//                                .radius(5f, 0)
//                                .emit();
//                        s.rectEx(35, 10, 20, 20, Color.WHITE.getRGB())
//                                .radius(5f, 2)
//                                .emit();
//                        s.rectEx(60, 10, 20, 20, Color.WHITE.getRGB())
//                                .radius(5f, 5)
//                                .emit();
                    })
                    .endDraw();
        }
    }
}
