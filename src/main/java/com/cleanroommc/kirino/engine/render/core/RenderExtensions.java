package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingEntry;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingManager;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;

import java.util.*;

/**
 * No one is allowed to modify its resources except the engine kernel.
 *
 * @see com.cleanroommc.kirino.engine.process.graphics.install.RenderExtensionsInit
 * @see com.cleanroommc.kirino.engine.process.analysis.install.RenderExtensionsInit
 */
public final class RenderExtensions {

    public final PostProcessingManager postProcessingManager;

    public final Map<ResourceLocation, Optional<ShaderCompileOptions>> rawShaders;
    public final List<PostProcessingEntry> postProcessingEntries;
    public final List<ImmediateHUD> debugHuds;

    public RenderExtensions(
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull BuiltinShaderBundle builtinShaderBundle) {

        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(builtinShaderBundle);

        postProcessingManager = new PostProcessingManager(
                graphicsRuntimeBundle.graphicResourceManager,
                graphicsRuntimeBundle.idbGenerator,
                graphicsRuntimeBundle.renderer,
                graphicsRuntimeBundle.fullscreenTriangleVao);

        rawShaders = new HashMap<>();
        postProcessingEntries = new ArrayList<>();
        debugHuds = new ArrayList<>();
    }
}
