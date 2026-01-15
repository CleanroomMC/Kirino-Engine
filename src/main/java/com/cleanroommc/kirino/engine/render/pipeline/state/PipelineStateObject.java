package com.cleanroommc.kirino.engine.render.pipeline.state;

import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;

public record PipelineStateObject(
        BlendState blendState,
        DepthState depthState,
        RasterState rasterState,
        ResourceSlot<ShaderProgram> shaderProgram) {
}
