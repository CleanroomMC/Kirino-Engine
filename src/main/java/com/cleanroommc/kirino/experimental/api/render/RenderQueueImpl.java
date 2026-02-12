package com.cleanroommc.kirino.experimental.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

final class RenderQueueImpl implements RenderQueue<RenderCommand> {
    private final Deque<RenderCommand> deque = new ArrayDeque<>();

    RenderQueueImpl() {
    }

    public void enqueue(@NonNull RenderCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public RenderCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }
}
