package com.cleanroommc.kirino.engine.render.core.debug.hud.event;

import com.cleanroommc.kirino.engine.render.core.debug.hud.IImmediateHUD;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class DebugHUDRegistrationEvent extends Event {
    private final List<IImmediateHUD> debugHuds = new ArrayList<>();

    public void register(IImmediateHUD hud) {
        debugHuds.add(hud);
    }
}
