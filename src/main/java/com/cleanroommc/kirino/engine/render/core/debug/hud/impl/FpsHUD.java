package com.cleanroommc.kirino.engine.render.core.debug.hud.impl;

import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.IImmediateHUD;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

public class FpsHUD implements IImmediateHUD {
    @Override
    public void draw(@NonNull HUDContext hud) {
        hud.text("FPS: " + Minecraft.getDebugFPS());
    }
}
