package com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses;

import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.vao.VAO;
import org.jspecify.annotations.NonNull;

public class DefaultPostProcessingPass extends AbstractPostProcessingPass {
    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public DefaultPostProcessingPass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso, @NonNull ResourceSlot<VAO> fullscreenTriangleVao) {
        super(renderer, pso, fullscreenTriangleVao);
    }
}
