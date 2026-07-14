package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.core.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.GraphicsRuntimeBundleInit
 */
public final class GraphicsRuntimeBundle {

    public final ResourceSlot<Renderer> renderer;
    public final ResourceSlot<StagingBufferManager> stagingBufferManager;
    public final ResourceSlot<GraphicResourceManager> graphicResourceManager;
    public final ResourceSlot<GizmosManager> gizmosManager;
    public final ResourceSlot<InGameDebugHUDManager> debugHudManager;
    public final ResourceSlot<ShaderRegistry> shaderRegistry;
    public final ResourceSlot<KnowledgeRuntime> glKnowledge;
    public final ResourceSlot<FrameFinalizer> frameFinalizer;
    public final ResourceSlot<IndirectDrawBufferGenerator> idbGenerator;
    public final ResourceSlot<VAO> fullscreenTriangleVao;
    public final ResourceSlot<VAO> dummyVao;

    public GraphicsRuntimeBundle(@NonNull ResourceLayout resourceLayout) {
        Preconditions.checkNotNull(resourceLayout);

        renderer = resourceLayout.slot(Renderer.class, "renderer");
        stagingBufferManager = resourceLayout.slot(StagingBufferManager.class, "staging_buffer_manager");
        graphicResourceManager = resourceLayout.slot(GraphicResourceManager.class, "graphics_resource_manager");
        gizmosManager = resourceLayout.slot(GizmosManager.class, "gizmos_manager");
        debugHudManager = resourceLayout.slot(InGameDebugHUDManager.class, "debug_hud_manager");
        shaderRegistry = resourceLayout.slot(ShaderRegistry.class, "shader_registry");
        glKnowledge = resourceLayout.slot(KnowledgeRuntime.class, "gl_knowledge");
        frameFinalizer = resourceLayout.slot(FrameFinalizer.class, "frame_finalizer");
        idbGenerator = resourceLayout.slot(IndirectDrawBufferGenerator.class, "idb_generator");
        fullscreenTriangleVao = resourceLayout.slot(VAO.class, "fullscreen_tri_vao");
        dummyVao = resourceLayout.slot(VAO.class, "dummy_vao");
    }
}
