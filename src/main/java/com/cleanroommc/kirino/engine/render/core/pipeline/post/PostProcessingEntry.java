package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.ResourceLocation;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

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
    public final ResourceLocation[] shaders;
    public final PassConstructor ctor;

    public PostProcessingEntry(
            @NonNull String subpassName,
            @NonNull String @NonNull [] shaders,
            @NonNull PassConstructor ctor) {

        this(subpassName, Arrays.stream(Preconditions.checkNotNull(shaders)).map(ResourceLocation::new).toArray(ResourceLocation[]::new), ctor);
    }

    public PostProcessingEntry(
            @NonNull String subpassName,
            net.minecraft.util.@NonNull ResourceLocation @NonNull [] shaders,
            @NonNull PassConstructor ctor) {

        this(subpassName, Arrays
                .stream(Preconditions.checkNotNull(shaders))
                .map(rl -> new ResourceLocation(rl.getNamespace(), rl.getPath()))
                .toArray(ResourceLocation[]::new), ctor);
    }

    public PostProcessingEntry(
            @NonNull String subpassName,
            @NonNull ResourceLocation @NonNull [] shaders,
            @NonNull PassConstructor ctor) {

        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(shaders);
        for (ResourceLocation shader : shaders) {
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
