package com.cleanroommc.kirino.engine.render.core.pipeline.post.builtin;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueuePolicy;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.Subpass;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class UpscalingPass extends Subpass {

    /**
     * @param renderer A global renderer
     * @param pso A pipeline state object (pipeline parameters)
     */
    public UpscalingPass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso) {
        super(renderer, pso);
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

    @Override
    protected void updateShaderProgram(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera,
            @Nullable Object payload,
            @NonNull ShaderProgram shaderProgram) {

    }

    @Override
    protected void execute(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @NonNull DrawQueue drawQueue,
            @Nullable Object payload) {

    }

    @Override
    public void collectCommands(@NonNull ResourceStorage storage, @NonNull DrawQueue drawQueue) {

    }
}
