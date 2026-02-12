package com.cleanroommc.kirino.engine.render.core.debug.hud.impl;

import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

public class FpsHUD implements ImmediateHUD {
    @Override
    public void draw(@NonNull HUDContext hud) {
        hud.text("FPS: " + Minecraft.getDebugFPS());
    }
}
