package com.cleanroommc.kirino.engine.render.platform.debug.hud.impl;

import com.cleanroommc.kirino.KirinoClientCore;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.platform.debug.data.impl.MeshletGpuTimeline;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MeshletGpuTimelineHUD implements ImmediateHUD {

    @Override
    public void draw(@NonNull HUDContext hud) {
        var meshletGpuTimeline = KirinoClientCore.DEBUG_SERVICE.get(MeshletGpuTimeline.class);
        boolean drawTimeline = false;
        List<MeshletGpuTimeline.Timeline> writeTimeline = null;
        List<MeshletGpuTimeline.Timeline> computeTimeline = null;
        var meshletGpuTimelineValue = meshletGpuTimeline.fetch();
        if (meshletGpuTimelineValue != null) {
            drawTimeline = true;
            writeTimeline = meshletGpuTimelineValue.getWriteTimeline();
            computeTimeline = meshletGpuTimelineValue.getComputeTimeline();
        }

        if (drawTimeline) {
            hud.text("write timeline count: " + writeTimeline.size());
            hud.text("compute timeline count: " + computeTimeline.size());
        }
    }
}
