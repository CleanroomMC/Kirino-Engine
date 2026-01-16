package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.subpasses.GizmosPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.subpasses.OpaqueTerrainPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses.DefaultPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses.DownscalingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses.UpscalingPass;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;

public class RenderStructure {
    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    public final RenderPass terrainGpuPass;
    public final RenderPass chunkCpuPass;
    public final RenderPass gizmosPass;

    public final RenderPass toneMappingPass;
    public final RenderPass upscalingPass;
    public final RenderPass downscalingPass;

    public RenderStructure(
            boolean enableHDR,
            boolean enablePostProcessing,
            ResourceSlot<Renderer> renderer,
            ResourceSlot<GraphicResourceManager> graphicResourceManager,
            ResourceSlot<IndirectDrawBufferGenerator> idbGenerator,
            ResourceSlot<GizmosManager> gizmosManager,
            ResourceSlot<VAO> fullscreenTriangleVao,
            ResourceSlot<ShaderProgram> terrainGpuPassProgram,
            ResourceSlot<ShaderProgram> chunkCpuPassProgram,
            ResourceSlot<ShaderProgram> gizmosPassProgram,
            ResourceSlot<ShaderProgram> toneMappingPassProgram,
            ResourceSlot<ShaderProgram> upscalingPassProgram,
            ResourceSlot<ShaderProgram> downscalingPassProgram) {

        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;

        terrainGpuPass = new RenderPass("Terrain GPU", graphicResourceManager, idbGenerator);
        terrainGpuPass.addSubpass("Opaque Pass", new OpaqueTerrainPass(renderer, PSOPresets.createOpaquePSO(terrainGpuPassProgram)));

        chunkCpuPass = new RenderPass("Chunk CPU", graphicResourceManager, idbGenerator);
        chunkCpuPass.addSubpass("Opaque Pass", new GizmosPass(renderer, PSOPresets.createOpaquePSO(chunkCpuPassProgram), gizmosManager));

        gizmosPass = new RenderPass("Gizmos", graphicResourceManager, idbGenerator);
        gizmosPass.addSubpass("Gizmos Pass", new GizmosPass(
                renderer,
                PSOPresets.createGizmosPSO(gizmosPassProgram),
                gizmosManager));

        toneMappingPass = new RenderPass("Tone Mapping", graphicResourceManager, idbGenerator);
        toneMappingPass.addSubpass("Tone Mapping Pass", new DefaultPostProcessingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(toneMappingPassProgram),
                fullscreenTriangleVao));

        upscalingPass = new RenderPass("Upscaling", graphicResourceManager, idbGenerator);
        upscalingPass.addSubpass("Upscaling Pass", new UpscalingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(upscalingPassProgram)));

        downscalingPass = new RenderPass("Downscaling", graphicResourceManager, idbGenerator);
        downscalingPass.addSubpass("Downscaling Pass", new DownscalingPass(
                renderer,
                PSOPresets.createScreenOverwritePSO(downscalingPassProgram)));
    }
}
