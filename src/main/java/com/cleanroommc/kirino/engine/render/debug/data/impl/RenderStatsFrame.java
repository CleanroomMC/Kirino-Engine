package com.cleanroommc.kirino.engine.render.debug.data.impl;

import com.cleanroommc.kirino.engine.render.debug.data.IDebugDataService;
import com.cleanroommc.kirino.engine.render.debug.hud.InGameDebugHUDManager;

public class RenderStatsFrame implements IDebugDataService {
    private final InGameDebugHUDManager hud;
    private int drawCalls;

    public RenderStatsFrame(InGameDebugHUDManager hud) {
        this.hud = hud;
    }

    @Override
    public boolean isActive() {
        return hud.isEnabled();
    }

    public void setDrawCalls(int drawCalls) {
        this.drawCalls = drawCalls;
    }

    public int getDrawCalls() {
        return drawCalls;
    }

    public void incrementDrawCalls() {
        drawCalls++;
    }
}
