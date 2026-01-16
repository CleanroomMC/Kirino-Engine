package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.core.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class GraphicsRuntimeServices {
    public final ResourceSlot<GLStateBackup> stateBackup;
    public final ResourceSlot<Renderer> renderer;
    public final ResourceSlot<StagingBufferManager> stagingBufferManager;
    public final ResourceSlot<GraphicResourceManager> graphicResourceManager;
    public final ResourceSlot<GizmosManager> gizmosManager;
    public final ResourceSlot<InGameDebugHUDManager> debugHudManager;
    public final ResourceSlot<ShaderRegistry> shaderRegistry;

    public GraphicsRuntimeServices(
            ResourceSlot<GLStateBackup> stateBackup,
            ResourceSlot<Renderer> renderer,
            ResourceSlot<StagingBufferManager> stagingBufferManager,
            ResourceSlot<GraphicResourceManager> graphicResourceManager,
            ResourceSlot<GizmosManager> gizmosManager,
            ResourceSlot<InGameDebugHUDManager> debugHudManager,
            ResourceSlot<ShaderRegistry> shaderRegistry) {

        this.stateBackup = stateBackup;
        this.renderer = renderer;
        this.stagingBufferManager = stagingBufferManager;
        this.graphicResourceManager = graphicResourceManager;
        this.gizmosManager = gizmosManager;
        this.debugHudManager = debugHudManager;
        this.shaderRegistry = shaderRegistry;
    }
}
