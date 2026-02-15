package com.cleanroommc.kirino.engine.render.platform.debug.data.impl;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataService;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.builtin.CommonStatsHUD;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class MeshletGpuTimeline implements DebugDataService {
    private final ResourceSlot<InGameDebugHUDManager> hud;

    public MeshletGpuTimeline(ResourceSlot<InGameDebugHUDManager> hud) {
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

        return KirinoCommonCore.KIRINO_ENGINE.getStorage().get(hud).isEnabled()
                && KirinoCommonCore.KIRINO_ENGINE.getStorage().get(hud).getCurrentHud() == CommonStatsHUD.class;
    }
}
