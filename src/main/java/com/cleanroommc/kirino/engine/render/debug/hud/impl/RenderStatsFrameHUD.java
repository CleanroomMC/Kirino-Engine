package com.cleanroommc.kirino.engine.render.debug.hud.impl;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.debug.data.impl.RenderStatsFrame;
import com.cleanroommc.kirino.engine.render.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.debug.hud.IImmediateHUD;
import org.jspecify.annotations.NonNull;

public class RenderStatsFrameHUD implements IImmediateHUD {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void draw(@NonNull HUDContext hud) {
        var renderStatsFrame = KirinoCore.DEBUG_SERVICE.get(RenderStatsFrame.class);
        int drawCalls = -1;
        if (renderStatsFrame.fetch() != null) {
            drawCalls = renderStatsFrame.fetch().getDrawCalls();
        }

        hud.text("Draw Calls via KE: " + ((drawCalls == -1) ? "UNKNOWN" : drawCalls));
    }
}
