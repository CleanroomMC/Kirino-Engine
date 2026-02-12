package com.cleanroommc.kirino.experimental.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public sealed interface RenderQueue<T extends RenderCommand> permits RenderQueueImpl, RenderQueueSpecialImpl {
    void enqueue(@NonNull T command);
    @Nullable T dequeue();
    void clear();
}
