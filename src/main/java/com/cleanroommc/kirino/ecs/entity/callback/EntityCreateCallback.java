package com.cleanroommc.kirino.ecs.entity.callback;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import org.jspecify.annotations.NonNull;

public interface EntityCreateCallback {
    /**
     * Must not touch {@link EntityManager} here. Might cause concurrent modification exception.
     */
    void beforeCreate(@NonNull EntityCreateContext createContext);
}
