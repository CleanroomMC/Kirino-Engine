package com.cleanroommc.kirino.ui.simplegui;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class SimpleGuiRuntime {

    private final TransientArena arena;
    private final GuiCommandStream stream;
    private final GuiCompiler compiler;
    private final GuiRenderer renderer;

    public SimpleGuiRuntime() {
        // todo: grow mechanism
        arena = new TransientArena(1024);
        stream = new GuiCommandStream(1024);
        compiler = new GuiCompiler(arena);
        renderer = new GuiRenderer(1024, 1024);
    }

    private boolean batch = false;

    @NonNull
    public SimpleGuiRuntime begin() {
        Preconditions.checkState(!batch, "Must not be batching.");

        batch = true;

        arena.reset();
        stream.reset();
        return this;
    }

    @NonNull
    public SimpleGuiRuntime endDraw() {
        Preconditions.checkState(batch, "Must be batching already.");

        batch = false;

        compiler.compile(stream);
        renderer.render(stream);
        return this;
    }

    @NonNull
    public SimpleGuiRuntime append(@NonNull Consumer<GuiCommandStream> action) {
        Preconditions.checkNotNull(action);
        Preconditions.checkState(batch, "Must be batching.");

        action.accept(stream);
        return this;
    }
}
