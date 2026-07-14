package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.gl.debug.*;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GraphicsWorldInstaller implements ModuleInstaller<Graphics> {

    private boolean init = false;

    private void prepare(WorldContext<Graphics> context) {
        if (init) {
            return;
        }

        GraphicsWorldView view = castGraphics(context);

        KHRDebug.enable(KirinoCommonCore.LOGGER, List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        GraphicsRuntimeBundleInit.init(view);
        BuiltinShaderBundleInit.init(view);
        McIntegrationBundleInit.init(view);
        McSceneViewStateInit.init(view);
        RenderExtensionsInit.init(view);

        init = true;
    }

    @Override
    public void install(@NonNull WorldContext<Graphics> context, @NonNull ResourceLayout layout) {
        context.on(FramePhase.PREPARE, FramePhaseTiming.BEFORE, this::prepare);
    }
}
