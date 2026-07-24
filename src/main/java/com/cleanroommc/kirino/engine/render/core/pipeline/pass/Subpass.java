package com.cleanroommc.kirino.engine.render.core.pipeline.pass;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueuePolicy;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.DrawCommand;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.core.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.ClaimedScopeHandle;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class Subpass {

    protected final ResourceSlot<Renderer> renderer;
    private final PipelineStateObject pso;

    /**
     * @param renderer A global renderer
     * @param pso A pipeline state object (pipeline parameters)
     */
    public Subpass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso) {
        Preconditions.checkNotNull(renderer);
        Preconditions.checkNotNull(pso);

        this.renderer = renderer;
        this.pso = pso;
    }

    public final void render(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @NonNull DrawQueue drawQueue,
            @Nullable Camera camera,
            @NonNull GraphicResourceManager graphicResourceManager,
            @NonNull IndirectDrawBufferGenerator idbGenerator,
            @Nullable Object payload) {

        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(glKnowledge);
        Preconditions.checkNotNull(drawQueue);
        Preconditions.checkNotNull(graphicResourceManager);
        Preconditions.checkNotNull(idbGenerator);

        if (hintCompileDrawQueue()) {
            drawQueue.compile(graphicResourceManager);
        }
        DrawQueuePolicy policy = hintDrawQueuePolicy();
        if (policy != null && policy.allowsReordering()) {
            drawQueue.sort(policy);
        }
        if (hintSimplifyDrawQueue()) {
            drawQueue.simplify(idbGenerator);
        }

        // this is the pso scope
        try (ClaimedScopeHandle ignored = storage.get(renderer).bindPipeline(pso, glKnowledge)) {
            updateShaderProgram(storage, glKnowledge, camera, payload, storage.get(pso.shaderProgram()));
            execute(storage, glKnowledge, drawQueue, payload);
        }

        // ensure that everything is cleaned at the end
        // since there is no constraint that all commands must be consumed
        DrawCommand item;
        while ((item = drawQueue.dequeue()) != null) {
            item.recycle();
        }
    }

    @Nullable
    public abstract DrawQueuePolicy hintDrawQueuePolicy();

    /**
     * Whether to run {@link DrawQueue#compile(GraphicResourceManager)} before {@link #execute(ResourceStorage, KnowledgeRuntime, DrawQueue, Object)}.
     *
     * @see DrawQueue#compile(GraphicResourceManager)
     * @return The hint
     */
    protected abstract boolean hintCompileDrawQueue();

    /**
     * Whether to run {@link DrawQueue#simplify(IndirectDrawBufferGenerator)} before {@link #execute(ResourceStorage, KnowledgeRuntime, DrawQueue, Object)}.
     *
     * @see DrawQueue#simplify(IndirectDrawBufferGenerator)
     * @return The hint
     */
    protected abstract boolean hintSimplifyDrawQueue();

    /**
     * It'll be executed right before {@link #execute(ResourceStorage, KnowledgeRuntime, DrawQueue, Object)}
     *
     * <p>Note: During its execution, the corresponding PSO is applied and GL knowledge is claimed.</p>
     *
     * @param payload The payload that comes from {@link RenderPass#render(ResourceStorage, KnowledgeRuntime, Camera, SubpassCallback, SubpassScopeProvider, Object[])}
     */
    protected abstract void updateShaderProgram(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @Nullable Camera camera,
            @Nullable Object payload,
            @NonNull ShaderProgram shaderProgram);

    /**
     * It's intended to consume all {@link LowLevelDC}s here via {@link Renderer}, OR draw your own stuff and apply your own logic.
     * It'll be executed right after {@link #updateShaderProgram(ResourceStorage, KnowledgeRuntime, Camera, Object, ShaderProgram)}
     *
     * <p>Note: During its execution, the corresponding PSO is applied and GL knowledge is claimed.</p>
     *
     * @param drawQueue The queue that stores only <b>low-level</b> draw commands
     * @param payload The payload that comes from {@link RenderPass#render(ResourceStorage, KnowledgeRuntime, Camera, SubpassCallback, SubpassScopeProvider, Object[])}
     * @implNote Default implementation: <br/><code>while (drawQueue.dequeue() instanceof LowLevelDC command) { ... }</code>
     */
    protected abstract void execute(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge,
            @NonNull DrawQueue drawQueue,
            @Nullable Object payload);

    /**
     * Enqueue draw commands, {@link LowLevelDC} or {@link HighLevelDC}, here.
     * Use methods like {@link LowLevelDC#acquire()} to build commands manually OR
     * consume commands from elsewhere.
     *
     * @param drawQueue The draw queue to be enqueued
     */
    public abstract void collectCommands(
            @NonNull ResourceStorage storage,
            @NonNull DrawQueue drawQueue);

    public final void decorateCommands(
            @NonNull ResourceStorage storage,
            @NonNull DrawQueue drawQueue,
            @NonNull SubpassDecorator decorator) {

        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(drawQueue);
        Preconditions.checkNotNull(decorator);

        // todo
    }
}
