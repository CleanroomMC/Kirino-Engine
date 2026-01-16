package com.cleanroommc.kirino.engine.render.core.resource.payload;

import org.jspecify.annotations.NonNull;

public interface IResourcePayload<T extends IResourcePayload<T>> {
    @NonNull
    T getPayload();
    void release();
}
