package com.cleanroommc.kirino.engine.render.core.debug.data.impl;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.core.debug.data.IDebugDataService;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class RenderStatsFrame implements IDebugDataService {
    private final ResourceSlot<InGameDebugHUDManager> hud;
    private int drawCalls;

    public RenderStatsFrame(ResourceSlot<InGameDebugHUDManager> hud) {
        this.hud = hud;
    }

    @Override
    public boolean isActive() {
        if (!KirinoCore.KIRINO_ENGINE.resourceStorage.has(hud)) {
            return false;
        }
        return KirinoCore.KIRINO_ENGINE.resourceStorage.get(hud).isEnabled();
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
