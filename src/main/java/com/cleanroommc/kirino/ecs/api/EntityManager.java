package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyCallback;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EntityManager {
    @NonNull EntityQuery newQuery();

    @NonNull CleanEntityHandle createEntity(@NonNull CleanComponent @NonNull ... components);

    @NonNull CleanEntityHandle createEntity(@Nullable EntityDestroyCallback destroyCallback,
                                            @Nullable EntityCreateCallback createCallback,
                                            @NonNull CleanComponent @NonNull ... components);

    void flush();
}
