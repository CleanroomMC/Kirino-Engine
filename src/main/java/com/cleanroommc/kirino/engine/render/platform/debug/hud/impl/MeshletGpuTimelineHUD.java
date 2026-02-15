package com.cleanroommc.kirino.engine.render.platform.debug.hud.impl;

import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import org.jspecify.annotations.NonNull;

public class MeshletGpuTimelineHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        hud.text("WIP");
    }
}
