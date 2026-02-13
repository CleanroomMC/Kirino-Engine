package com.cleanroommc.kirino.engine.render.core.resource.payload;

import org.jspecify.annotations.NonNull;

public interface ResourcePayload<T extends ResourcePayload<T>> {
    @NonNull
    T getPayload();
    void release();
}
