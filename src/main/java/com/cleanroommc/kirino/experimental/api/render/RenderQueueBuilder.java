package com.cleanroommc.kirino.experimental.api.render;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public class RenderQueueBuilder<T extends RenderCommand> {
    private final RenderQueue<T> renderQueue;
    private boolean lock = false;

    @SuppressWarnings("unchecked")
    public RenderQueueBuilder(Class<T> clazz) {
        Preconditions.checkArgument(clazz != RenderCommand.class,
                "Argument \"clazz\" must not be the interface RenderCommand itself.");

        if (clazz == RenderCommandImpl.class) {
            renderQueue = (RenderQueue<T>) new RenderQueueImpl();
        } else if (clazz == RenderCommandSpecialImpl.class) {
            renderQueue = (RenderQueue<T>) new RenderQueueSpecialImpl();
        } else {
            renderQueue = null;
        }

        Preconditions.checkNotNull(renderQueue);
    }

    @NonNull
    public RenderQueueBuilder<T> enqueue(@NonNull T renderCommand) {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        renderQueue.enqueue(renderCommand);
        return this;
    }

    @NonNull
    public RenderQueueBuilder<T> dequeue() {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        renderQueue.dequeue();
        return this;
    }

    @NonNull
    public RenderQueue<T> emit() {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        lock = true;
        return renderQueue;
    }
}
