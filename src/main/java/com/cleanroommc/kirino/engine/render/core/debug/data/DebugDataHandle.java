package com.cleanroommc.kirino.engine.render.core.debug.data;

import org.jspecify.annotations.Nullable;

public class DebugDataHandle<T extends IDebugDataService> {
    private final T service;

    DebugDataHandle(T service) {
        this.service = service;
    }

    /**
     * It only returns <code>null</code> when the service is inactive.
     */
    @Nullable
    public T fetch() {
        if (service.isActive()) {
            return service;
        } else {
            return null;
        }
    }
}
