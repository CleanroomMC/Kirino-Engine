package com.cleanroommc.kirino.engine.render.pipeline.pass.subpasses;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import com.cleanroommc.kirino.engine.render.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;

import java.util.List;

public class GizmosPass extends Subpass {
    private final ResourceSlot<GizmosManager> gizmosManager;

    /**
     * @param renderer      A global renderer
     * @param pso           A pipeline state object (pipeline parameters)
     * @param gizmosManager The gizmos manager
     */
    public GizmosPass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso, @NonNull ResourceSlot<GizmosManager> gizmosManager) {
        super(renderer, pso);
        this.gizmosManager = gizmosManager;
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
        return true;
    }

    @Override
    protected boolean hintSimplifyDrawQueue() {
        return true;
    }

    @NonNull
    @Override
    public PassHint passHint() {
        return PassHint.OTHER;
    }

    @Override
    protected void execute(@NonNull DrawQueue drawQueue, @Nullable Object payload) {
        while (drawQueue.dequeue() instanceof LowLevelDC command) {
            renderer.draw(command);
        }
    }

    @Override
    public void collectCommands(@NonNull DrawQueue drawQueue) {
        List<HighLevelDC> list =  gizmosManager.getDrawCommands();
        for (HighLevelDC command : list) {
            drawQueue.enqueue(command);
        }
        list.clear();
    }
}
