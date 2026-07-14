package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.RenderPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.AbstractPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.tuple.Triple;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.RenderExtensionsInit
 * @see com.cleanroommc.kirino.engine.process.analysis.install.RenderExtensionsInit
 */
public final class RenderExtensions {

    public final PostProcessingPass postProcessingPass;

    public final Map<ResourceLocation, Optional<ShaderCompileOptions>> rawShaders;
    public final List<Triple<
            String,
            String[],
            TriFunction<
                    ResourceSlot<Renderer>,
                    PipelineStateObject,
                    ResourceSlot<VAO>,
                    AbstractPostProcessingPass>>> postProcessingEntries; // todo: definitely want to refactor
    public final List<ImmediateHUD> debugHuds;

    public RenderExtensions(
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull BuiltinShaderBundle builtinShaderBundle) {

        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(builtinShaderBundle);

        postProcessingPass = new PostProcessingPass(
                new RenderPass("Post-Processing", graphicsRuntimeBundle.graphicResourceManager, graphicsRuntimeBundle.idbGenerator),
                graphicsRuntimeBundle.renderer,
                graphicsRuntimeBundle.fullscreenTriangleVao);

        rawShaders = new HashMap<>();
        postProcessingEntries = new ArrayList<>();
        debugHuds = new ArrayList<>();
    }
}
