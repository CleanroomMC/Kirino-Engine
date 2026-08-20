package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueuePolicy;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public abstract class AbstractPostProcessingPass extends Subpass {

    private final ResourceSlot<VAO> fullscreenTriangleVao;

    /**
     * @param renderer A global renderer
     * @param pso A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public AbstractPostProcessingPass(
            @NonNull ResourceSlot<Renderer> renderer,
            @NonNull PipelineStateObject pso,
            @NonNull ResourceSlot<VAO> fullscreenTriangleVao) {

        super(renderer, pso);
        Preconditions.checkNotNull(fullscreenTriangleVao);

        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    @Nullable
    @Override
    public DrawQueuePolicy hintDrawQueuePolicy() {
        return null;
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void updateShaderProgram(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera,
            @Nullable Object payload,
            @NonNull ShaderProgram shaderProgram) {

        Framebuffer framebuffer = (Framebuffer) payload;
        ColorAttachment colorAttachment = (ColorAttachment) framebuffer.getColorAttachment(0);

        int screenTexture = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "screenTexture");

        // test
        int[] res = new int[1];
        GL11C.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, res);
        int texUnit = res[0];

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        colorAttachment.texture2D.bind();
        GL20.glUniform1i(screenTexture, 3);
        GL13.glActiveTexture(texUnit);
    }

    @Override
    protected void execute(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @NonNull DrawQueue drawQueue,
            @Nullable Object payload) {

        Renderer renderer_ = storage.get(renderer);
        while (drawQueue.dequeue() instanceof LowLevelDC command) {
            renderer_.draw(command);
        }
    }

    @Override
    public void collectCommands(@NonNull ResourceStorage storage, @NonNull DrawQueue drawQueue) {
        drawQueue.enqueue(LowLevelDC.acquire().fillElement(
                storage.get(fullscreenTriangleVao).vaoID,
                GL11.GL_TRIANGLES,
                3,
                GL11.GL_UNSIGNED_BYTE,
                0));
    }
}
