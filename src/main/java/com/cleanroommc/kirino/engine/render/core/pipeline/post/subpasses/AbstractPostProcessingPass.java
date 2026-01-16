package com.cleanroommc.kirino.engine.render.core.pipeline.post.subpasses;

import com.cleanroommc.kirino.engine.render.core.camera.ICamera;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

public abstract class AbstractPostProcessingPass extends Subpass {
    private final ResourceSlot<VAO> fullscreenTriangleVao;

    /**
     * @param renderer              A global renderer
     * @param pso                   A pipeline state object (pipeline parameters)
     * @param fullscreenTriangleVao The global fullscreen triangle VAO
     */
    public AbstractPostProcessingPass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso, @NonNull ResourceSlot<VAO> fullscreenTriangleVao) {
        super(renderer, pso);
        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable ICamera camera, @Nullable Object payload) {
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
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @NonNull
    @Override
    public PassHint passHint() {
        return PassHint.OTHER;
    }

    @Override
    protected void execute(@NonNull ResourceStorage storage, @NonNull DrawQueue drawQueue, @Nullable Object payload) {
        while (drawQueue.dequeue() instanceof LowLevelDC command) {
            storage.get(renderer).draw(command);
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
