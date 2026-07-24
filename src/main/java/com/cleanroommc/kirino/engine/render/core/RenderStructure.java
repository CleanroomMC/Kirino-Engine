package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.core.pipeline.PassDescriptor;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.builtin.GizmosPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.builtin.GuiPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingSchedule;
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
    public final boolean enableKhrDebug;
    public final boolean enableShaderDebug;
    public final @NonNull PostProcessingSchedule postProcessingSchedule;

    public final PassDescriptor terrainGpuPassDesc;
    public final PassDescriptor chunkCpuPassDesc;
    public final PassDescriptor gizmosPassDesc;
    public final PassDescriptor guiPassDesc;

    public final PassDescriptor toneMappingPassDesc;
    public final PassDescriptor upscalingPassDesc;
    public final PassDescriptor downscalingPassDesc;

    public RenderStructure(
            boolean enableHDR,
            boolean enablePostProcessing,
            boolean enableKhrDebug,
            boolean enableShaderDebug,
            @NonNull PostProcessingSchedule postProcessingSchedule,
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull BuiltinShaderBundle builtinShaderBundle) {

        Preconditions.checkNotNull(postProcessingSchedule);
        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(builtinShaderBundle);

        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;
        this.enableKhrDebug = enableKhrDebug;
        this.enableShaderDebug = enableShaderDebug;
        this.postProcessingSchedule = postProcessingSchedule;

        //<editor-fold desc="terrain gpu pass">
        RenderPass terrainGpuPass = new RenderPass(
                "Terrain GPU",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        terrainGpuPass.addSubpass(
                "Opaque Subpass",
                new OpaqueTerrainPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createOpaquePSO(builtinShaderBundle.terrainGpuPassProgram)));
        terrainGpuPass.seal();

        terrainGpuPassDesc = new PassDescriptor(terrainGpuPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented.");
        //</editor-fold>

        //<editor-fold desc="chunk cpu pass">
        RenderPass chunkCpuPass = new RenderPass(
                "Chunk CPU",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        chunkCpuPass.addSubpass(
                "Opaque Subpass",
                new GizmosPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createOpaquePSO(builtinShaderBundle.chunkCpuPassProgram),
                        graphicsRuntimeBundle.gizmosManager));
        chunkCpuPass.seal();

        chunkCpuPassDesc = new PassDescriptor(chunkCpuPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented.");
        //</editor-fold>

        //<editor-fold desc="gizmos pass">
        RenderPass gizmosPass = new RenderPass(
                "Gizmos",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        gizmosPass.addSubpass(
                "Gizmos Subpass",
                new GizmosPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createGizmosPSO(builtinShaderBundle.gizmosPassProgram),
                        graphicsRuntimeBundle.gizmosManager));
        gizmosPass.seal();

        gizmosPassDesc = new PassDescriptor(gizmosPass);
        //</editor-fold>

        //<editor-fold desc="gui pass">
        // we don't need a shader program for the gui pass. pass a dummy program
        RenderPass guiPass = new RenderPass(
                "GUI",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        guiPass.addSubpass(
                "GUI Subpass",
                new GuiPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createGuiPSO(builtinShaderBundle.postProcessingDefaultProgram)));
        guiPass.seal();

        guiPassDesc = new PassDescriptor(guiPass);
        //</editor-fold>

        //<editor-fold desc="tone mapping pass">
        RenderPass toneMappingPass = new RenderPass(
                "Tone Mapping",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        toneMappingPass.addSubpass(
                "Tone Mapping Subpass",
                new DefaultPostProcessingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.toneMappingPassProgram),
                        graphicsRuntimeBundle.fullscreenTriangleVao));
        toneMappingPass.seal();

        toneMappingPassDesc = new PassDescriptor(toneMappingPass);
        //</editor-fold>

        //<editor-fold desc="up scaling pass">
        RenderPass upscalingPass = new RenderPass(
                "Upscaling",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        upscalingPass.addSubpass(
                "Upscaling Subpass",
                new UpscalingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.upscalingPassProgram)));
        upscalingPass.seal();

        upscalingPassDesc = new PassDescriptor(upscalingPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented");
        //</editor-fold>

        //<editor-fold desc="down scaling pass">
        RenderPass downscalingPass = new RenderPass(
                "Downscaling",
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator);
        downscalingPass.addSubpass(
                "Downscaling Subpass",
                new DownscalingPass(
                        graphicsRuntimeBundle.renderer,
                        PSOPresets.createScreenOverwritePSO(builtinShaderBundle.downscalingPassProgram)));
        downscalingPass.seal();

        downscalingPassDesc = new PassDescriptor(downscalingPass, PassDescriptor.Availability.NOT_IMPLEMENTED,
                "Not fully implemented");
        //</editor-fold>
    }
}
