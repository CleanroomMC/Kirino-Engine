package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.core.pipeline.PassDescriptor;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.builtin.GizmosPass;
import com.cleanroommc.kirino.engine.render.usage.pipeline.pass.OpaqueTerrainPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.builtin.DefaultPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.builtin.DownscalingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.builtin.UpscalingPass;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * {@link RenderStructure} is the only one that is aware of the runtime engine configurations,
 * and is the only one that possesses {@link RenderPass}.
 */
public final class RenderStructure {

    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    public final PassDescriptor terrainGpuPassDesc;
    public final PassDescriptor chunkCpuPassDesc;
    public final PassDescriptor gizmosPassDesc;

    public final PassDescriptor toneMappingPassDesc;
    public final PassDescriptor upscalingPassDesc;
    public final PassDescriptor downscalingPassDesc;

    public RenderStructure(
            boolean enableHDR,
            boolean enablePostProcessing,
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull BuiltinShaderBundle builtinShaderBundle) {

        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(builtinShaderBundle);

        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;

        RenderPass terrainGpuPass = new RenderPass(
                "Terrain GPU",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        terrainGpuPass.addSubpass(
                "Opaque Pass",
                new OpaqueTerrainPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createOpaquePSO(builtinShaderBundle.terrainGpuPassProgram)));

        terrainGpuPassDesc = new PassDescriptor(terrainGpuPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented.");

        RenderPass chunkCpuPass = new RenderPass(
                "Chunk CPU",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        chunkCpuPass.addSubpass(
                "Opaque Pass",
                new GizmosPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createOpaquePSO(builtinShaderBundle.chunkCpuPassProgram), graphicsRuntimeBundle.gizmosManager));

        chunkCpuPassDesc = new PassDescriptor(chunkCpuPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented.");

        RenderPass gizmosPass = new RenderPass(
                "Gizmos",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        gizmosPass.addSubpass(
                "Gizmos Pass",
                new GizmosPass(
                        graphicsRuntimeBundle.renderer, PSOPresets.createGizmosPSO(builtinShaderBundle.gizmosPassProgram),
                        graphicsRuntimeBundle.gizmosManager));

        gizmosPassDesc = new PassDescriptor(gizmosPass);

        RenderPass toneMappingPass = new RenderPass(
                "Tone Mapping",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        toneMappingPass.addSubpass(
                "Tone Mapping Pass",
                new DefaultPostProcessingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.toneMappingPassProgram),
                        graphicsRuntimeBundle.fullscreenTriangleVao));

        toneMappingPassDesc = new PassDescriptor(toneMappingPass);

        RenderPass upscalingPass = new RenderPass(
                "Upscaling",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        upscalingPass.addSubpass(
                "Upscaling Pass",
                new UpscalingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.upscalingPassProgram)));

        upscalingPassDesc = new PassDescriptor(upscalingPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented");

        RenderPass downscalingPass = new RenderPass(
                "Downscaling",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        downscalingPass.addSubpass(
                "Downscaling Pass",
                new DownscalingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.downscalingPassProgram)));

        downscalingPassDesc = new PassDescriptor(downscalingPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented");
    }
}
