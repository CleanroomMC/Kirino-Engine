package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingManager;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.google.common.base.Preconditions;

/**
 * @see com.cleanroommc.kirino.engine.render.core.RenderExtensions
 */
public final class RenderExtensionsInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();
        PostProcessingManager pass = context.ext().postProcessingManager;

        pass.lock();
        if (context.rs().enablePostProcessing) {
            Preconditions.checkState(pass.getSubpassCount() >= 1,
                    "Post-processing is enabled. Post-processing pass must have at least one subpasses at runtime to work as expected.");

            context.ext().postProcessingManager.lateInit(
                    storage.get(context.graphicsb().frameFinalizer).getMinecraftFramebuffer(),
                    storage.get(context.graphicsb().frameFinalizer).getPingPongFramebuffer(),
                    storage.get(context.graphicsb().frameFinalizer).getIntermediateFramebuffer());
        } else {
            Preconditions.checkState(pass.getSubpassCount() == 0,
                    "Post-processing is disabled. Post-processing pass must have exactly zero subpasses at runtime to work as expected.");
        }
    }
}
