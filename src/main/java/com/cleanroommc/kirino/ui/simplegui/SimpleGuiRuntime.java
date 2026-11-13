package com.cleanroommc.kirino.ui.simplegui;

import com.cleanroommc.anone.value.CanIgnoreReturnValue;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.ui.transform.UiTransformContext;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class SimpleGuiRuntime implements AutoCloseable {

    private final TransientArena arena;
    private final GuiCommandStream stream;
    private final GuiCompiler compiler;
    private final GuiRenderer renderer;

    /**
     * Uses Minecraft's scaled resolution as the logical GUI size.
     */
    public SimpleGuiRuntime(@NonNull ImmediateShaderAccess shaderAccess, @NonNull VAO dummyVao) {
        this(shaderAccess, dummyVao, 0, 0);
    }

    /**
     * @param logicalWidth Logical GUI width. Both dimensions must be finite and positive,
     *                     or both zero to use Minecraft's scaled resolution at draw time
     * @param logicalHeight Logical GUI height. Both dimensions must be finite and positive,
     *                      or both zero to use Minecraft's scaled resolution at draw time
     */
    public SimpleGuiRuntime(
            @NonNull ImmediateShaderAccess shaderAccess,
            @NonNull VAO dummyVao,
            float logicalWidth,
            float logicalHeight) {

        Preconditions.checkNotNull(shaderAccess);
        Preconditions.checkNotNull(dummyVao);

        arena = new TransientArena(128);
        stream = new GuiCommandStream(128);
        compiler = new GuiCompiler(arena);
        renderer = new GuiRenderer(shaderAccess, dummyVao, arena, logicalWidth, logicalHeight);
    }

    private boolean batch = false;

    private UiTransformContext transformContext = UiTransformContext.ndc();

    public void validateTransform(@NonNull UiTransformContext context) {
        Preconditions.checkNotNull(context);

        renderer.validateTransform(context);
    }

    @CanIgnoreReturnValue
    @NonNull
    public SimpleGuiRuntime begin() {
        Preconditions.checkState(!batch, "Must not be batching.");

        batch = true;

        arena.reset();
        stream.reset();
        transformContext = UiTransformContext.ndc();
        return this;
    }

    @CanIgnoreReturnValue
    @NonNull
    public SimpleGuiRuntime begin(@NonNull UiTransformContext context) {
        Preconditions.checkNotNull(context);

        validateTransform(context);

        begin();
        transformContext = context;
        return this;
    }

    @CanIgnoreReturnValue
    @NonNull
    public SimpleGuiRuntime endDraw() {
        Preconditions.checkState(batch, "Must be batching already.");

        batch = false;

        try {
            compiler.compile(stream);
            if (transformContext.isDefault()) {
                renderer.render(stream);
            } else {
                renderer.render(stream, transformContext);
            }
            return this;
        } finally {
            transformContext = UiTransformContext.ndc();
        }
    }

    @CanIgnoreReturnValue
    @NonNull
    public SimpleGuiRuntime append(@NonNull Consumer<GuiCommandStream> action) {
        Preconditions.checkNotNull(action);
        Preconditions.checkState(batch, "Must be batching.");

        action.accept(stream);
        return this;
    }

    @Override
    public void close() {
        arena.close();
        stream.close();
        renderer.close();
    }
}
