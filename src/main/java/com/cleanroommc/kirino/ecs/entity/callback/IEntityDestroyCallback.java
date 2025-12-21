package com.cleanroommc.kirino.ecs.entity.callback;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import org.jspecify.annotations.NonNull;

public interface IEntityDestroyCallback {
    /**
     * Must not touch {@link EntityManager} here. Might cause concurrent modification exception.
     */
    void beforeDestroy(@NonNull EntityDestroyContext destroyContext);
}
