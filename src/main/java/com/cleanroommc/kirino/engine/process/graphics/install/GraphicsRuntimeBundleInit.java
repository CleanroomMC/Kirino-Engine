package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.gl.semantic.GLViolationPolicy;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.core.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.KnowledgeOwner;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.engine.semantic.KnowledgeSupervisor;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
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
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @see com.cleanroommc.kirino.engine.render.core.GraphicsRuntimeBundle
 */
public final class GraphicsRuntimeBundleInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        FrameFinalizer frameFinalizer = new FrameFinalizer(
                context.ext().postProcessingManager,
                context.rs().toneMappingPassDesc,
                context.rs().upscalingPassDesc,
                context.rs().downscalingPassDesc,
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

        frameFinalizer.initResources(
                context.logger(),
                Minecraft.getMinecraft().getFramebuffer(),
                context.rs().postProcessingSchedule);

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

        storage.put(context.graphicsb().frameFinalizer, frameFinalizer);
        storage.put(context.graphicsb().idbGenerator, idbGenerator);
        storage.put(context.graphicsb().fullscreenTriangleVao, fullscreenTriangleVao);
        storage.put(context.graphicsb().dummyVao, dummyVao);

        storage.sealResource(context.graphicsb().frameFinalizer);
        storage.sealResource(context.graphicsb().idbGenerator);
        storage.sealResource(context.graphicsb().fullscreenTriangleVao);
        storage.sealResource(context.graphicsb().dummyVao);

        Renderer renderer = new Renderer(storage, storage.get(context.graphicsb().dummyVao));

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

        for (Map.Entry<ResourceLocation, Optional<ShaderCompileOptions>> entry : context.ext().rawShaders.entrySet()) {
            Shader shader = shaderRegistry.register(
                    context.logger(),
                    entry.getKey(),
                    entry.getValue().isPresent() ? entry.getValue().get() : null,
                    context.rs().enableShaderDebug);

            context.logger().info("Registered " + shader.getShaderType().toString() + " shader \"" + entry.getKey() + "\".");
            if (shader.getShaderSource().isEmpty()) {
                context.logger().info("Warning! \"" + entry.getKey() + "\" is empty.");
            }
        }
        shaderRegistry.submitToGL();
        context.logger().info("Shader compilation passed.");

        shaderRegistry.analyze(
                context.shaderi().glslRegistry,
                context.shaderi().defaultShaderAnalyzer);

        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new GLViolationPolicy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        storage.put(context.graphicsb().renderer, renderer);
        storage.put(context.graphicsb().stagingBufferManager, stagingBufferManager);
        storage.put(context.graphicsb().graphicResourceManager, graphicResourceManager);
        storage.put(context.graphicsb().gizmosManager, gizmosManager);
        storage.put(context.graphicsb().debugHudManager, debugHudManager);
        storage.put(context.graphicsb().shaderRegistry, shaderRegistry);
        storage.put(context.graphicsb().glKnowledge, glKnowledge);

        storage.sealResource(context.graphicsb().renderer);
        storage.sealResource(context.graphicsb().stagingBufferManager);
        storage.sealResource(context.graphicsb().graphicResourceManager);
        storage.sealResource(context.graphicsb().gizmosManager);
        storage.sealResource(context.graphicsb().debugHudManager);
        storage.sealResource(context.graphicsb().shaderRegistry);
        storage.sealResource(context.graphicsb().glKnowledge);

        if (context.rs().enableKhrDebug) {
            KHRDebug.enable(KirinoCommonCore.LOGGER, List.of(
                    new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                    new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));
        }
    }
}
