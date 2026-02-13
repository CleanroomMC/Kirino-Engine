package com.cleanroommc.kirino.engine.render.core.debug.hud.event;

import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DebugHUDRegistrationEvent extends Event {
    private final List<ImmediateHUD> debugHuds = new ArrayList<>();

    public void register(@NonNull ImmediateHUD hud) {
        debugHuds.add(hud);
    }
}
