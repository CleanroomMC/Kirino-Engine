package com.cleanroommc.kirino.engine.process.analysis.install;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.AnalyticalWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Headless;
import org.jspecify.annotations.NonNull;

public class AnalyticalWorldInstaller implements ModuleInstaller<Headless> {

    private boolean init = false;

    private void prepare(WorldContext<Headless> context) {
        if (init) {
            return;
        }

        AnalyticalWorldView view = castHeadless(context);

        RenderExtensionsInit.init(view);

        init = true;
    }

    @Override
    public void install(@NonNull WorldContext<Headless> context, @NonNull ResourceLayout layout) {
        context.on(FramePhase.PREPARE, FramePhaseTiming.BEFORE, this::prepare);
    }
}
