package com.cleanroommc.kirino.experimental.api.render;

public class RenderContext<T extends RenderCommand> {
    public final RenderQueueBuilder<T> builder;

    public RenderContext(RenderQueueBuilder<T> builder) {
        this.builder = builder;
    }
}
