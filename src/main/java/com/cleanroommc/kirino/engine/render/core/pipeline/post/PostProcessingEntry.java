package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public class PostProcessingEntry {

    @FunctionalInterface
    public interface PassConstructor {

        @NonNull
        AbstractPostProcessingPass construct(
                @NonNull ResourceSlot<Renderer> renderer,
                @NonNull PipelineStateObject pso,
                @NonNull ResourceSlot<VAO> fullscreenTriangleVao);
    }

    public final String subpassName;
    public final String[] shaders;
    public final PassConstructor ctor;

    public PostProcessingEntry(
            @NonNull String subpassName,
            @NonNull String @NonNull [] shaders,
            @NonNull PassConstructor ctor) {

        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(shaders);
        for (String shader : shaders) {
            Preconditions.checkNotNull(shader);
        }
        Preconditions.checkNotNull(ctor);

        this.subpassName = subpassName;
        this.shaders = shaders;
        this.ctor = ctor;
    }

    private ResourceSlot<ShaderProgram> shaderProgram = null;

    private void setShaderProgram(@NonNull ResourceSlot<ShaderProgram> shaderProgram) {
        Preconditions.checkNotNull(shaderProgram);

        this.shaderProgram = shaderProgram;
    }

    @NonNull
    ResourceSlot<ShaderProgram> getShaderProgram() {
        return Preconditions.checkNotNull(shaderProgram);
    }
}
