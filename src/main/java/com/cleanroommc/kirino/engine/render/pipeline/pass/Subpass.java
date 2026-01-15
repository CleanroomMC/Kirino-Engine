package com.cleanroommc.kirino.engine.render.pipeline.pass;

import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.IDrawCommand;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.DrawQueue;
import com.cleanroommc.kirino.engine.render.pipeline.state.PipelineStateObject;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public abstract class Subpass {
    protected final ResourceSlot<Renderer> renderer;
    private final PipelineStateObject pso;

    /**
     * @param renderer A global renderer
     * @param pso A pipeline state object (pipeline parameters)
     */
    public Subpass(@NonNull ResourceSlot<Renderer> renderer, @NonNull PipelineStateObject pso) {
        this.renderer = renderer;
        this.pso = pso;
    }

    public final void render(
            @NonNull DrawQueue drawQueue,
            @Nullable ICamera camera,
            @NonNull GraphicResourceManager graphicResourceManager,
            @NonNull IndirectDrawBufferGenerator idbGenerator,
            @Nullable Object payload) {
        DrawQueue dq = drawQueue;
        if (hintCompileDrawQueue()) {
            dq = dq.compile(graphicResourceManager);
        }
        if (hintSimplifyDrawQueue()) {
            dq = dq.simplify(idbGenerator);
        }
        dq = dq.sort();

        renderer.bindPipeline(pso);
        updateShaderProgram(pso.shaderProgram(), camera, payload);

        execute(dq, payload);

        // ensure that everything is cleaned at the end
        IDrawCommand item;
        while ((item = dq.dequeue()) != null) {
            item.recycle();
        }
    }

    protected abstract void updateShaderProgram(@NonNull ShaderProgram shaderProgram, @Nullable ICamera camera, @Nullable Object payload);

    /**
     * Whether to run {@link DrawQueue#compile(GraphicResourceManager)} before {@link #execute(DrawQueue, Object)}.
     *
     * @see DrawQueue#compile(GraphicResourceManager)
     * @return The hint
     */
    protected abstract boolean hintCompileDrawQueue();

    /**
     * Whether to run {@link DrawQueue#simplify(IndirectDrawBufferGenerator)} before {@link #execute(DrawQueue, Object)}.
     *
     * @see DrawQueue#simplify(IndirectDrawBufferGenerator)
     * @return The hint
     */
    protected abstract boolean hintSimplifyDrawQueue();

    @NonNull
    public abstract PassHint passHint();

    /**
     * Draw all {@link LowLevelDC}s here via {@link Renderer}.
     *
     * @implSpec Default implementation: <br/><code>while (drawQueue.dequeue() instanceof LowLevelDC command) { ... }</code>
     * @param drawQueue The queue that stores <b>low-level</b> draw commands
     * @param payload The payload that comes from {@link RenderPass#render(ICamera, BiConsumer, Object[])}
     */
    protected abstract void execute(@NonNull DrawQueue drawQueue, @Nullable Object payload);

    /**
     * Enqueue draw commands, {@link LowLevelDC} or {@link HighLevelDC}, here.
     * Use methods like {@link LowLevelDC#acquire()} to build commands manually OR
     * consume commands from elsewhere.
     *
     * @param drawQueue The draw queue
     */
    public abstract void collectCommands(@NonNull DrawQueue drawQueue);

    public final void decorateCommands(@NonNull DrawQueue drawQueue, @NonNull ISubpassDecorator decorator) {
        // todo
    }
}
