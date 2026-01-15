package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.engine.render.debug.hud.IImmediateHUD;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.pipeline.post.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.post.subpasses.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.List;

public class RenderExtensions {
    public final PostProcessingPass postProcessingPass;

    public final List<ResourceLocation> shaderRLs;
    public final List<Triple<
            String,
            String[],
            TriFunction<
                    ResourceSlot<Renderer>,
                    PipelineStateObject,
                    ResourceSlot<VAO>,
                    AbstractPostProcessingPass>>> postProcessingEntries;
    public final List<IImmediateHUD> debugHuds;

    public final ResourceSlot<ShaderProgram> postProcessingDefaultProgram;
    public final ResourceSlot<ShaderProgram> terrainGpuPassProgram;
    public final ResourceSlot<ShaderProgram> chunkCpuPassProgram;
    public final ResourceSlot<ShaderProgram> gizmosPassProgram;
    public final ResourceSlot<ShaderProgram> toneMappingPassProgram;
    public final ResourceSlot<ShaderProgram> upscalingPassProgram;
    public final ResourceSlot<ShaderProgram> downscalingPassProgram;

    public RenderExtensions(
            ResourceSlot<Renderer> renderer,
            ResourceSlot<GraphicResourceManager> graphicResourceManager,
            ResourceSlot<IndirectDrawBufferGenerator> idbGenerator,
            ResourceSlot<VAO> fullscreenTriangleVao,
            ResourceSlot<ShaderProgram> postProcessingDefaultProgram,
            ResourceSlot<ShaderProgram> terrainGpuPassProgram,
            ResourceSlot<ShaderProgram> chunkCpuPassProgram,
            ResourceSlot<ShaderProgram> gizmosPassProgram,
            ResourceSlot<ShaderProgram> toneMappingPassProgram,
            ResourceSlot<ShaderProgram> upscalingPassProgram,
            ResourceSlot<ShaderProgram> downscalingPassProgram) {

        this.postProcessingDefaultProgram = postProcessingDefaultProgram;
        this.terrainGpuPassProgram = terrainGpuPassProgram;
        this.chunkCpuPassProgram = chunkCpuPassProgram;
        this.gizmosPassProgram = gizmosPassProgram;
        this.toneMappingPassProgram = toneMappingPassProgram;
        this.upscalingPassProgram = upscalingPassProgram;
        this.downscalingPassProgram = downscalingPassProgram;

        postProcessingPass = new PostProcessingPass(
                new RenderPass("Post-Processing", graphicResourceManager, idbGenerator),
                renderer,
                fullscreenTriangleVao);

        shaderRLs = new ArrayList<>();
        postProcessingEntries = new ArrayList<>();
        debugHuds = new ArrayList<>();
    }
}
