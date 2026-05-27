package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ICS;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import org.jspecify.annotations.NonNull;

public class SimpleTextDebugHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        ICS.instance().text()
                .begin()
                .append("ABCabcijk", 0, 0)
                .append("Hello World JetBrains Mono NL Regular", 0, 12)
                .endDraw();
    }
}
