package com.cleanroommc.kirino.engine.process.graphics.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.gl.semantic.GLKnowledgeKeys;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FramebufferStore;
import com.cleanroommc.kirino.engine.render.usage.McIntegrationBundle;
import com.cleanroommc.kirino.engine.render.usage.McSceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.ClaimedScopeHandle;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.gl.framebuffer.GLRenderBuffer;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4i;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class GraphicsWorldViewImpl implements GraphicsWorldView {

    //<editor-fold desc="boilerplate">
    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final RenderExtensions extensions;
    private final EventBus eventBus;
    private final Logger logger;
    private final ResourceStorage storage;
    private final BuiltinShaderBundle builtinShaderBundle;
    private final RuntimeShaderBundle runtimeShaderBundle;
    private final GraphicsRuntimeBundle graphicsRuntimeBundle;
    private final McIntegrationBundle mcIntegrationBundle;
    private final McSceneViewState mcSceneViewState;
    private final ShaderIntrospection shaderIntrospection;

    public GraphicsWorldViewImpl(
            @NonNull CleanECSRuntime ecs,
            @NonNull RenderStructure render,
            @NonNull RenderExtensions extensions,
            @NonNull EventBus eventBus,
            @NonNull Logger logger,
            @NonNull ResourceStorage storage,
            @NonNull BuiltinShaderBundle builtinShaderBundle,
            @NonNull RuntimeShaderBundle runtimeShaderBundle,
            @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
            @NonNull McIntegrationBundle mcIntegrationBundle,
            @NonNull McSceneViewState mcSceneViewState,
            @NonNull ShaderIntrospection shaderIntrospection) {

        Preconditions.checkNotNull(ecs);
        Preconditions.checkNotNull(render);
        Preconditions.checkNotNull(extensions);
        Preconditions.checkNotNull(eventBus);
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(builtinShaderBundle);
        Preconditions.checkNotNull(runtimeShaderBundle);
        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(mcIntegrationBundle);
        Preconditions.checkNotNull(mcSceneViewState);
        Preconditions.checkNotNull(shaderIntrospection);

        this.ecs = ecs;
        this.render = render;
        this.extensions = extensions;
        this.eventBus = eventBus;
        this.logger = logger;
        this.storage = storage;
        this.builtinShaderBundle = builtinShaderBundle;
        this.runtimeShaderBundle = runtimeShaderBundle;
        this.graphicsRuntimeBundle = graphicsRuntimeBundle;
        this.mcIntegrationBundle = mcIntegrationBundle;
        this.mcSceneViewState = mcSceneViewState;
        this.shaderIntrospection = shaderIntrospection;
    }

    @NonNull
    @Override
    public CleanECSRuntime ecs() {
        return ecs;
    }

    @NonNull
    @Override
    public RenderStructure rs() {
        return render;
    }

    @NonNull
    @Override
    public RenderExtensions ext() {
        return extensions;
    }

    @NonNull
    @Override
    public Logger logger() {
        return logger;
    }

    @NonNull
    @Override
    public EventBus bus() {
        return eventBus;
    }

    @NonNull
    @Override
    public ResourceStorage storage() {
        return storage;
    }

    @NonNull
    @Override
    public ShaderIntrospection shaderi() {
        return shaderIntrospection;
    }

    @NonNull
    @Override
    public BuiltinShaderBundle shaderbb() {
        return builtinShaderBundle;
    }

    @NonNull
    @Override
    public RuntimeShaderBundle shaderrb() {
        return runtimeShaderBundle;
    }

    @NonNull
    @Override
    public GraphicsRuntimeBundle graphicsb() {
        return graphicsRuntimeBundle;
    }

    @NonNull
    @Override
    public McIntegrationBundle mcib() {
        return mcIntegrationBundle;
    }

    @NonNull
    @Override
    public McSceneViewState mcscene() {
        return mcSceneViewState;
    }

    private final Map<FramePhase,
            Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>>> callbacks =
            new Object2ObjectOpenHashMap<>();

    @Override
    public void run(@NonNull FramePhase phase, boolean firstPrepare) {
        KnowledgeRuntime glKnowledge = firstPrepare ? null : storage.get(graphicsb().glKnowledge);

        ClaimedScopeHandle handle = null;
        if (!firstPrepare) {
            switch (phase) {
                case PRE_UPDATE -> handle = enterPreUpdate(glKnowledge);
                case UPDATE -> handle = enterUpdate(glKnowledge);
                case POST_UPDATE -> handle = enterPostUpdate(glKnowledge);
                case RENDER_OPAQUE -> handle = enterRenderOpaque(glKnowledge);
                case RENDER_TRANSPARENT -> handle = enterRenderTransparent(glKnowledge);
                case RENDER_OVERLAY -> handle = enterRenderOverlay(glKnowledge);
            }
        }

        Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>> map = callbacks.get(phase);
        if (map != null) {
            List<Consumer<WorldContext<Graphics>>> list = map.get(FramePhaseTiming.BEFORE);
            if (list != null) {
                for (Consumer<WorldContext<Graphics>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }

        if (!firstPrepare) {
            switch (phase) {
                case PRE_UPDATE -> preUpdate(glKnowledge);
                case UPDATE -> update(glKnowledge);
                case POST_UPDATE -> handle = postUpdate(glKnowledge, handle);
                case RENDER_OPAQUE -> renderOpaque(glKnowledge);
                case RENDER_TRANSPARENT -> renderTransparent(glKnowledge);
                case RENDER_OVERLAY -> renderOverlay(glKnowledge);
            }
        }

        if (map != null) {
            List<Consumer<WorldContext<Graphics>>> list = map.get(FramePhaseTiming.AFTER);
            if (list != null) {
                for (Consumer<WorldContext<Graphics>> consumer : list) {
                    consumer.accept(this);
                }
            }
        }

        if (!firstPrepare) {
            switch (phase) {
                case PRE_UPDATE -> exitPreUpdate(glKnowledge, handle);
                case UPDATE -> exitUpdate(glKnowledge, handle);
                case POST_UPDATE -> exitPostUpdate(glKnowledge, handle);
                case RENDER_OPAQUE -> exitRenderOpaque(glKnowledge, handle);
                case RENDER_TRANSPARENT -> exitRenderTransparent(glKnowledge, handle);
                case RENDER_OVERLAY -> exitRenderOverlay(glKnowledge, handle);
            }
        }
    }

    @Override
    public void on(@NonNull FramePhase phase, @NonNull FramePhaseTiming timing, @NonNull Consumer<WorldContext<Graphics>> consumer) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkNotNull(timing);
        Preconditions.checkNotNull(consumer);

        Map<FramePhaseTiming, List<Consumer<WorldContext<Graphics>>>> map = callbacks.computeIfAbsent(phase, k -> new Object2ObjectOpenHashMap<>());
        List<Consumer<WorldContext<Graphics>>> list = map.computeIfAbsent(timing, k -> new ArrayList<>());
        list.add(consumer);
    }
    //</editor-fold>

    private static void resetCommonBindings() {
        GL20.glUseProgram(0);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
    }

    //<editor-fold desc="enter phase">
    private ClaimedScopeHandle enterPreUpdate(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.updateResolution();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0); // todo: commit these two
        GLRenderBuffer.bind(0);

        // current render target: main framebuffer
        frameFinalizer.bindMainFramebuffer(true);

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GL11.glStencilMask(0xFF);
        GL11.glClearStencil(0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height()));
            cp.know(GLKnowledgeKeys.COLOR_MASK_R, true);
            cp.know(GLKnowledgeKeys.COLOR_MASK_G, true);
            cp.know(GLKnowledgeKeys.COLOR_MASK_B, true);
            cp.know(GLKnowledgeKeys.COLOR_MASK_A, true);
            cp.know(GLKnowledgeKeys.DEPTH_MASK, true);
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private ClaimedScopeHandle enterUpdate(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.bindMainFramebuffer(true);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height()));
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private ClaimedScopeHandle enterRenderOpaque(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.bindMainFramebuffer(true);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height()));
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private ClaimedScopeHandle enterRenderTransparent(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.bindMainFramebuffer(true);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height()));
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private ClaimedScopeHandle enterPostUpdate(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.bindMainFramebuffer(true);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMainFramebuffer().framebuffer.fboID);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height()));
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private ClaimedScopeHandle enterRenderOverlay(KnowledgeRuntime glKnowledge) {
        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.bindMinecraftFramebuffer(true);

        resetCommonBindings();

        glKnowledge.commit(cp -> {
            FramebufferStore store = frameFinalizer.acquireFramebufferStore();
            cp.know(GLKnowledgeKeys.FBO_DRAW, store.getMinecraftFramebuffer().framebufferObject);
            cp.know(GLKnowledgeKeys.FBO_READ, store.getMinecraftFramebuffer().framebufferObject);
            cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, store.getMinecraftFramebuffer().framebufferWidth, store.getMinecraftFramebuffer().framebufferHeight));
            cp.know(GLKnowledgeKeys.SHADER_PROGRAM, 0);
            cp.know(GLKnowledgeKeys.VAO, 0);
            cp.know(GLKnowledgeKeys.VBO, 0);
            cp.know(GLKnowledgeKeys.EBO, 0);
            cp.know(GLKnowledgeKeys.IDB, 0);
        });

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }
    //</editor-fold>

    //<editor-fold desc="exit phase">
    private void exitPreUpdate(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));
    }

    private void exitUpdate(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));
    }

    private void exitRenderOpaque(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));

        // for compatibility purpose only:
        // apply hardcoded states to restore a Minecraft baseline
        resetCommonBindings();
        GL11.glDisable(GL11.GL_BLEND);
        GL14.glBlendEquation(GL14.GL_FUNC_ADD);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO);
        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    private void exitRenderTransparent(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));

        // for compatibility purpose only:
        // apply hardcoded states to restore a Minecraft baseline
        resetCommonBindings();
        GL11.glEnable(GL11.GL_BLEND);
        GL20.glBlendEquationSeparate(
                GL14.GL_FUNC_ADD,
                GL14.GL_FUNC_ADD);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO);
        GL11.glColorMask(true, true, true, true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
    }

    private void exitPostUpdate(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));

        // for compatibility purpose only:
        // apply hardcoded states to restore a Minecraft baseline
        resetCommonBindings();
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL20.glBlendEquationSeparate(
                GL14.GL_FUNC_ADD,
                GL14.GL_FUNC_ADD);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glFrontFace(GL11.GL_CCW);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        // set up fixed-func overlay rendering
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    private void exitRenderOverlay(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();
        glKnowledge.commit(cp -> cp.unknownDomain("gl"));

        // for compatibility purpose only:
        // apply hardcoded states to restore a Minecraft baseline
        resetCommonBindings();
        GL11.glColorMask(true, true, true, true);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL20.glBlendEquationSeparate(
                GL14.GL_FUNC_ADD,
                GL14.GL_FUNC_ADD);
        GL14.glBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }
    //</editor-fold>

    //<editor-fold desc="phase logic">
    private void preUpdate(KnowledgeRuntime glKnowledge) {

    }

    private void update(KnowledgeRuntime glKnowledge) {
        storage().get(graphicsb().graphicResourceManager).runStaging();
        mcscene().scene.tryUpdateWorld(Minecraft.getMinecraft().world);
        mcscene().scene.update();
    }

    private void renderOpaque(KnowledgeRuntime glKnowledge) {
//        if (sceneViewState.scene.isMeshletRenderReady()) {
//            rs().terrainGpuPass.render(
//                    storage,
//                    sceneViewState.camera,
//                    null,
//                    new Object[]{sceneViewState.scene.getMeshletRenderPayload()});
//        }
//        rs().chunkCpuPass.render(sceneViewState.camera);
    }

    private void renderTransparent(KnowledgeRuntime glKnowledge) {
        rs().gizmosPassDesc.acquire().render(storage(), glKnowledge, mcscene().camera);
    }

    private ClaimedScopeHandle postUpdate(KnowledgeRuntime glKnowledge, ClaimedScopeHandle handle) {
        handle.close();

        FrameFinalizer frameFinalizer = storage().get(graphicsb().frameFinalizer);
        frameFinalizer.finalizeFramebuffer(storage(), glKnowledge);
        // current render target: minecraft framebuffer

        HighLevelDC.nextGen();
        LowLevelDC.nextGen();

        return glKnowledge.claim(
                GLKnowledgeKeys.FBO_DRAW,
                GLKnowledgeKeys.FBO_READ,
                GLKnowledgeKeys.VIEWPORT);
    }

    private void renderOverlay(KnowledgeRuntime glKnowledge) {
        InGameDebugHUDManager debugHudManager = storage().get(graphicsb().debugHudManager);
        debugHudManager.updateAndRenderIfNeeded(); // it will capture GL states and restore when enabled
    }
    //</editor-fold>
}
