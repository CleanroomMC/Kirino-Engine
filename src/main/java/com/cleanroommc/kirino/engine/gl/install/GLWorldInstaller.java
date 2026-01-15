package com.cleanroommc.kirino.engine.gl.install;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.gl.view.GLWorldViewImpl;
import com.cleanroommc.kirino.engine.render.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.debug.hud.IImmediateHUD;
import com.cleanroommc.kirino.engine.render.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.pipeline.post.PostProcessingPass;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.ModuleInstaller;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.GL;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.EBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
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

public class GLWorldInstaller implements ModuleInstaller<GL> {

    private boolean init = false;

    private void initBootstrapResources(GLWorldViewImpl context) {
        ResourceStorage storage = context.resourceStorage;

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

        storage.put(context.bootstrapResources.frameFinalizer, frameFinalizer);
        storage.put(context.bootstrapResources.idbGenerator, idbGenerator);
        storage.put(context.bootstrapResources.fullscreenTriangleVao, fullscreenTriangleVao);
        storage.put(context.bootstrapResources.dummyVao, dummyVao);
    }

    private void initGraphicsRuntimeServices(GLWorldViewImpl context) {
        ResourceStorage storage = context.resourceStorage;

        GLStateBackup stateBackup = new GLStateBackup();
        Renderer renderer = new Renderer(storage, storage.get(context.bootstrapResources.dummyVao));

        StagingBufferManager stagingBufferManager = new StagingBufferManager();
        GraphicResourceManager graphicResourceManager = new GraphicResourceManager(stagingBufferManager);

        stagingBufferManager.genPersistentBuffers("default");

        GizmosManager gizmosManager = new GizmosManager(graphicResourceManager);

        InGameDebugHUDManager debugHudManager = new InGameDebugHUDManager();

        for (IImmediateHUD hud : context.ext().debugHuds) {
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
                context.shaderIntrospection.glslRegistry,
                context.shaderIntrospection.defaultShaderAnalyzer);

        storage.put(context.graphicsRuntimeServices.stateBackup, stateBackup);
        storage.put(context.graphicsRuntimeServices.renderer, renderer);
        storage.put(context.graphicsRuntimeServices.stagingBufferManager, stagingBufferManager);
        storage.put(context.graphicsRuntimeServices.graphicResourceManager, graphicResourceManager);
        storage.put(context.graphicsRuntimeServices.gizmosManager, gizmosManager);
        storage.put(context.graphicsRuntimeServices.debugHudManager, debugHudManager);
        storage.put(context.graphicsRuntimeServices.shaderRegistry, shaderRegistry);
    }

    private void initMinecraftAssetProviders(GLWorldViewImpl context) {
        ResourceStorage storage = context.resourceStorage;

        storage.put(context.minecraftAssetProviders.blockMeshGenerator, new BlockMeshGenerator(Minecraft.getMinecraft()));
    }

    private void initSceneViewState(GLWorldViewImpl context) {
        ResourceStorage storage = context.resourceStorage;

        MeshletGpuRegistry meshletGpuRegistry = new MeshletGpuRegistry();
        meshletGpuRegistry.lateInit();

        storage.put(context.sceneViewState.meshletGpuRegistry, meshletGpuRegistry);
    }

    private void initRenderExtensions(GLWorldViewImpl context) {
        ResourceStorage storage = context.resourceStorage;
        PostProcessingPass pass = context.ext().postProcessingPass;

        pass.lock();
        if (context.rs().enablePostProcessing) {
            Preconditions.checkState(pass.getSubpassCount() >= 1,
                    "Post-processing is enabled. Post-processing pass must have at least one subpasses at runtime to work as expected.");

            context.ext().postProcessingPass.lateInit(
                    storage.get(context.bootstrapResources.frameFinalizer).getMinecraftFramebuffer(),
                    storage.get(context.bootstrapResources.frameFinalizer).getPingPongFramebuffer(),
                    storage.get(context.bootstrapResources.frameFinalizer).getIntermediateFramebuffer());
        } else {
            Preconditions.checkState(pass.getSubpassCount() == 0,
                    "Post-processing is disabled. Post-processing pass must have exactly zero subpasses at runtime to work as expected.");
        }

        storage.put(context.ext().postProcessingDefaultProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"));

        storage.put(context.ext().terrainGpuPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/opaque_terrain.vert", "forge:shaders/opaque_terrain.frag"));

        storage.put(context.ext().chunkCpuPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.ext().gizmosPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.ext().toneMappingPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"));

        storage.put(context.ext().upscalingPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"));

        storage.put(context.ext().downscalingPassProgram,
                storage.get(context.graphicsRuntimeServices.shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"));
    }

    private void prepare(WorldContext<GL> context) {
        if (init) {
            return;
        }
        if (!(context instanceof GLWorldViewImpl glWorldView)) {
            throw new RuntimeException("WorldContext is not an instance of GLWorldViewImpl.");
        }

        initBootstrapResources(glWorldView);
        initGraphicsRuntimeServices(glWorldView);
        initMinecraftAssetProviders(glWorldView);
        initSceneViewState(glWorldView);
        initRenderExtensions(glWorldView);

        glWorldView.sceneViewState.scene.computeShaderProgram = glWorldView.resourceStorage
                .get(glWorldView.graphicsRuntimeServices.shaderRegistry)
                .newShaderProgram("forge:shaders/meshlets2vertices.comp");

        init = true;
    }

    @Override
    public void install(@NonNull WorldContext<GL> context) {
        context.on(FramePhase.PREPARE, this::prepare);
    }
}
