package com.cleanroommc.kirino.engine.render.debug;

import com.cleanroommc.kirino.engine.render.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.debug.hud.IImmediateHUD;
import org.jspecify.annotations.NonNull;

public class TestHUD implements IImmediateHUD {
    @Override
    public void draw(@NonNull HUDContext hud) {
        hud.beginHorizontal();
        hud.text("lol");
        hud.beginVertical();
        hud.text("v1");
        hud.text("v2");
        hud.endVertical();
        hud.text("end");
        hud.beginVertical();
        hud.text("v1");
        hud.text("v2");
        hud.text("v3");
        hud.endVertical();
        hud.endHorizontal();
        hud.text("second");
        hud.text("third");
    }
}
