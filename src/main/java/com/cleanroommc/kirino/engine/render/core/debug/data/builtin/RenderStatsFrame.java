package com.cleanroommc.kirino.engine.render.core.debug.data.builtin;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataService;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class RenderStatsFrame implements DebugDataService {
    private final ResourceSlot<InGameDebugHUDManager> hud;
    private int drawCalls;

    public RenderStatsFrame(ResourceSlot<InGameDebugHUDManager> hud) {
        this.hud = hud;
    }

    @Override
    public boolean isActive() {
        if (KirinoCommonCore.KIRINO_ENGINE.getStorage() == null) {
            return false;
        }
        if (!KirinoCommonCore.KIRINO_ENGINE.getStorage().has(hud)) {
            return false;
        }

        return KirinoCommonCore.KIRINO_ENGINE.getStorage().get(hud).isEnabled();
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
