package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.RenderStructure;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.gl.debug.*;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public class GraphicsWorldInstaller implements ModuleInstaller<Graphics> {

    private boolean init = false;

    private void prepare(WorldContext<Graphics> context) {
        if (init) {
            return;
        }

        GraphicsWorldView view = castGraphics(context);

        checkRuntimeConfig(view.rs());

        GraphicsRuntimeBundleInit.init(view);
        BuiltinShaderBundleInit.init(view);
        RuntimeShaderBundleInit.init(view);
        McIntegrationBundleInit.init(view);
        McSceneViewStateInit.init(view);
        RenderExtensionsInit.init(view);

        init = true;
    }

    private static void checkRuntimeConfig(RenderStructure rs) {
        Preconditions.checkState(!rs.enablePostProcessing || (rs.postProcessingSchedule.getSubpassCount() >= 1),
                "PostProcessingSchedule subpass count must be greater than 0 when post-processing is enabled.");
    }

    @Override
    public void install(@NonNull WorldContext<Graphics> context, @NonNull ResourceLayout layout) {
        context.on(FramePhase.PREPARE, FramePhaseTiming.BEFORE, this::prepare);
    }
}
