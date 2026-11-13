package com.cleanroommc.kirino.ecs.api.entity.callback;

import com.cleanroommc.kirino.ecs.api.EntityManager;
import org.jspecify.annotations.NonNull;

public interface EntityDestroyCallback {
    /**
     * Must not touch {@link EntityManager} here. Might cause concurrent modification exception.
     */
    void beforeDestroy(@NonNull EntityDestroyContext destroyContext);
}
