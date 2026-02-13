package com.cleanroommc.kirino.engine.graphics.install;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.core.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.debug.*;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.List;

public class GraphicsWorldInstaller implements ModuleInstaller<Graphics> {

    private boolean init = false;

    private void initBootstrapResources(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        FrameFinalizer frameFinalizer = new FrameFinalizer(
                context.logger(),
                context.ext().postProcessingPass,
                context.rs().toneMappingPass,
                context.rs().upscalingPass,
                context.rs().downscalingPass,
                context.rs().enableHDR,
                context.rs().enablePostProcessing);

        //<editor-fold desc="frame finalizer initialization">
        int[] result = new int[1];
        GL11C.glGetIntegerv(GL30.GL_DRAW_FRAMEBUFFER_BINDING, result);
        int drawFbo = result[0];
        GL11C.glGetIntegerv(GL30.GL_READ_FRAMEBUFFER_BINDING, result);
        int readFbo = result[0];
        float[] clearColor = new float[4];
        GL11C.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        float[] clearDepth = new float[1];
        GL11C.glGetFloatv(GL11.GL_DEPTH_CLEAR_VALUE, clearDepth);
        int[] clearStencil = new int[1];
        GL11C.glGetIntegerv(GL11.GL_STENCIL_CLEAR_VALUE, clearStencil);
        int[] viewport = new int[4];
        GL11C.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        frameFinalizer.initResources(Minecraft.getMinecraft().getFramebuffer());

        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        GL11.glClearDepth(clearDepth[0]);
        GL11.glClearStencil(clearStencil[0]);
        //</editor-fold>

        // 1MB
        IndirectDrawBufferGenerator idbGenerator = new IndirectDrawBufferGenerator(1024 * 1024);

        //<editor-fold desc="fullscreen triangle vao initialization">
        AttributeLayout fullscreenTriangleLayout = new AttributeLayout();
        fullscreenTriangleLayout.push(new Stride(12).push(new Slot(Type.FLOAT, 3)));

        EBOView eboView = new EBOView(new GLBuffer());
        VBOView vboView = new VBOView(new GLBuffer());

        ByteBuffer eboByteBuffer = BufferUtils.createByteBuffer(3 * Byte.BYTES);
        eboByteBuffer.put((byte) 0).put((byte) 1).put((byte) 2);
        eboByteBuffer.position(0);
        eboByteBuffer.limit(3 * Byte.BYTES);

        ByteBuffer vboByteBuffer = BufferUtils.createByteBuffer(9 * Float.BYTES);
        vboByteBuffer.asFloatBuffer().put(new float[]{-1, -1, 0, 3, -1, 0, -1, 3, 0});
        vboByteBuffer.position(0);
        vboByteBuffer.limit(9 * Float.BYTES);

        eboView.bind();
        eboView.uploadDirectly(eboByteBuffer);
        eboView.bind(0);

        vboView.bind();
        vboView.uploadDirectly(vboByteBuffer);
        eboView.bind(0);

        VAO fullscreenTriangleVao = new VAO(fullscreenTriangleLayout, eboView, vboView);
        //</editor-fold>

        //<editor-fold desc="dummy vao initialization">
        AttributeLayout dummyLayout = new AttributeLayout();
        dummyLayout.push(new Stride(0));

        VAO dummyVao = new VAO(dummyLayout, null, (VBOView[]) null);
        //</editor-fold>

        storage.put(context.bootstrapResources().frameFinalizer, frameFinalizer);
        storage.put(context.bootstrapResources().idbGenerator, idbGenerator);
        storage.put(context.bootstrapResources().fullscreenTriangleVao, fullscreenTriangleVao);
        storage.put(context.bootstrapResources().dummyVao, dummyVao);

        storage.sealResource(context.bootstrapResources().frameFinalizer);
        storage.sealResource(context.bootstrapResources().idbGenerator);
        storage.sealResource(context.bootstrapResources().fullscreenTriangleVao);
        storage.sealResource(context.bootstrapResources().dummyVao);
    }

    private void initGraphicsRuntimeServices(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        GLStateBackup stateBackup = new GLStateBackup();
        Renderer renderer = new Renderer(storage, storage.get(context.bootstrapResources().dummyVao));

        StagingBufferManager stagingBufferManager = new StagingBufferManager();
        GraphicResourceManager graphicResourceManager = new GraphicResourceManager(stagingBufferManager);

        stagingBufferManager.genPersistentBuffers("default");

        GizmosManager gizmosManager = new GizmosManager(graphicResourceManager);

        InGameDebugHUDManager debugHudManager = new InGameDebugHUDManager();

        for (ImmediateHUD hud : context.ext().debugHuds) {
            debugHudManager.register(hud);
            context.logger().info("Registered debug HUD \"" + hud.getClass().getName() + "\".");
        }

        debugHudManager.lateInit();

        ShaderRegistry shaderRegistry = new ShaderRegistry();

        for (ResourceLocation rl : context.ext().shaderRLs) {
            Shader shader = shaderRegistry.register(rl);
            context.logger().info("Registered " + shader.getShaderType().toString() + " shader \"" + rl + "\".");
            if (shader.getShaderSource().isEmpty()) {
                context.logger().info("Warning! \"" + rl + "\" is empty.");
            }
        }
        shaderRegistry.compile();
        context.logger().info("Shader compilation passed.");

        shaderRegistry.analyze(
                context.shaderIntrospection().glslRegistry,
                context.shaderIntrospection().defaultShaderAnalyzer);

        storage.put(context.graphicsRuntimeServices().stateBackup, stateBackup);
        storage.put(context.graphicsRuntimeServices().renderer, renderer);
        storage.put(context.graphicsRuntimeServices().stagingBufferManager, stagingBufferManager);
        storage.put(context.graphicsRuntimeServices().graphicResourceManager, graphicResourceManager);
        storage.put(context.graphicsRuntimeServices().gizmosManager, gizmosManager);
        storage.put(context.graphicsRuntimeServices().debugHudManager, debugHudManager);
        storage.put(context.graphicsRuntimeServices().shaderRegistry, shaderRegistry);

        storage.sealResource(context.graphicsRuntimeServices().stateBackup);
        storage.sealResource(context.graphicsRuntimeServices().renderer);
        storage.sealResource(context.graphicsRuntimeServices().stagingBufferManager);
        storage.sealResource(context.graphicsRuntimeServices().graphicResourceManager);
        storage.sealResource(context.graphicsRuntimeServices().gizmosManager);
        storage.sealResource(context.graphicsRuntimeServices().debugHudManager);
        storage.sealResource(context.graphicsRuntimeServices().shaderRegistry);
    }

    private void initMinecraftIntegration(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        MinecraftCulling minecraftCulling = new MinecraftCulling();
        storage.put(context.minecraftIntegration().cullingPatch, minecraftCulling);
        storage.put(context.minecraftIntegration().entityRenderingPatch, new MinecraftEntityRendering(minecraftCulling));
        storage.put(context.minecraftIntegration().tesrRenderingPatch, new MinecraftTESRRendering(minecraftCulling));

        storage.sealResource(context.minecraftIntegration().cullingPatch);
        storage.sealResource(context.minecraftIntegration().entityRenderingPatch);
        storage.sealResource(context.minecraftIntegration().tesrRenderingPatch);
    }

    private void initMinecraftAssetProviders(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        storage.put(context.minecraftAssetProviders().blockMeshGenerator, new BlockMeshGenerator(Minecraft.getMinecraft()));

        storage.sealResource(context.minecraftAssetProviders().blockMeshGenerator);
    }

    private void initSceneViewState(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        MeshletGpuRegistry meshletGpuRegistry = new MeshletGpuRegistry();
        meshletGpuRegistry.lateInit();

        MeshletComputeSystem meshletComputeSystem = new MeshletComputeSystem(context.ext().meshletComputeProgram);

        storage.put(context.sceneViewState().meshletGpuRegistry, meshletGpuRegistry);
        storage.put(context.sceneViewState().meshletComputeSystem, meshletComputeSystem);

        storage.sealResource(context.sceneViewState().meshletGpuRegistry);
        storage.sealResource(context.sceneViewState().meshletComputeSystem);
    }

    private void initRenderExtensions(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();
        PostProcessingPass pass = context.ext().postProcessingPass;

        pass.lock();
        if (context.rs().enablePostProcessing) {
            Preconditions.checkState(pass.getSubpassCount() >= 1,
                    "Post-processing is enabled. Post-processing pass must have at least one subpasses at runtime to work as expected.");

            context.ext().postProcessingPass.lateInit(
                    storage.get(context.bootstrapResources().frameFinalizer).getMinecraftFramebuffer(),
                    storage.get(context.bootstrapResources().frameFinalizer).getPingPongFramebuffer(),
                    storage.get(context.bootstrapResources().frameFinalizer).getIntermediateFramebuffer());
        } else {
            Preconditions.checkState(pass.getSubpassCount() == 0,
                    "Post-processing is disabled. Post-processing pass must have exactly zero subpasses at runtime to work as expected.");
        }

        storage.put(context.ext().postProcessingDefaultProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.ext().terrainGpuPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/opaque_terrain.vert", "forge:shaders/opaque_terrain.frag"));

        storage.put(context.ext().chunkCpuPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.ext().gizmosPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.ext().toneMappingPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.ext().upscalingPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.ext().downscalingPassProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.ext().meshletComputeProgram,
                storage.get(context.graphicsRuntimeServices().shaderRegistry).newShaderProgram(
                        "forge:shaders/meshlets2vertices.comp"));

        storage.sealResource(context.ext().postProcessingDefaultProgram);
        storage.sealResource(context.ext().terrainGpuPassProgram);
        storage.sealResource(context.ext().chunkCpuPassProgram);
        storage.sealResource(context.ext().gizmosPassProgram);
        storage.sealResource(context.ext().toneMappingPassProgram);
        storage.sealResource(context.ext().upscalingPassProgram);
        storage.sealResource(context.ext().downscalingPassProgram);
        storage.sealResource(context.ext().meshletComputeProgram);
    }

    private void prepare(WorldContext<Graphics> context) {
        if (init) {
            return;
        }
        GraphicsWorldView view = castGraphics(context);

        KHRDebug.enable(KirinoCommonCore.LOGGER, List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        initBootstrapResources(view);
        initGraphicsRuntimeServices(view);
        initMinecraftIntegration(view);
        initMinecraftAssetProviders(view);
        initSceneViewState(view);
        initRenderExtensions(view);

        init = true;
    }

    @Override
    public void install(@NonNull WorldContext<Graphics> context, @NonNull ResourceLayout layout) {
        context.on(FramePhase.PREPARE, FramePhaseTiming.BEFORE, this::prepare);
    }
}
