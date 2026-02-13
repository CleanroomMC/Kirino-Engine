package com.cleanroommc.kirino.experimental.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

final class RenderQueueSpecialImpl implements RenderQueue<RenderCommandSpecialImpl> {
    private final Deque<RenderCommandSpecialImpl> deque = new ArrayDeque<>();

    RenderQueueSpecialImpl() {
    }

    public void enqueue(@NonNull RenderCommandSpecialImpl command) {
        deque.offerLast(command);
    }

    @Nullable
    public RenderCommandSpecialImpl dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }
}
