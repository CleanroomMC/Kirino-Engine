package com.cleanroommc.kirino.engine.render.core.pipeline.state;

import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;

/**
 * {@link Renderer} is the only place to submit a PSO.
 *
 * @see Renderer
 */
public record PipelineStateObject(
        BlendState blendState,
        DepthState depthState,
        RasterState rasterState,
        ResourceSlot<ShaderProgram> shaderProgram) {
}
