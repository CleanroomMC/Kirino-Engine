package com.cleanroommc.kirino.ecs.entity;

import org.jspecify.annotations.NonNull;

public interface IEntityDestroyCallback {
    void beforeDestroy(@NonNull EntityDestroyContext destroyContext);
}
