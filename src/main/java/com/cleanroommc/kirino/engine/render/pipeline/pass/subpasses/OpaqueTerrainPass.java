package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

public class OpaqueTerrainPass extends Subpass {
    /**
     * @param renderer A global renderer
     * @param pso      A pipeline state object (pipeline parameters)
     */
    public OpaqueTerrainPass(@NonNull Renderer renderer, @NonNull PipelineStateObject pso) {
        super(renderer, pso);
    }

    @Override
    protected void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable ICamera camera, @Nullable Object payload) {
        int worldOffset = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "worldOffset");
        int viewRot = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "viewRot");
        int projection = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "projection");

        Preconditions.checkNotNull(camera);

        Vector3f vec3 = camera.getWorldOffset();
        GL20.glUniform3f(worldOffset, vec3.x, vec3.y, vec3.z);
        GL20C.glUniformMatrix4fv(viewRot, false, camera.getViewRotationBuffer());
        GL20C.glUniformMatrix4fv(projection, false, camera.getProjectionBuffer());
    }

    @Override
    protected boolean hintCompileDrawQueue() {
        return false;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return false;
    }

    @Override
    public @NonNull PassHint passHint() {
        return PassHint.OPAQUE;
    }

    @Override
    protected void execute(@NonNull DrawQueue drawQueue, @Nullable Object payload) {
        renderer.dummyDraw(GL11.GL_TRIANGLES, 0, 100);
    }

    @Override
    public void collectCommands(@NonNull DrawQueue drawQueue) {
    }
}
