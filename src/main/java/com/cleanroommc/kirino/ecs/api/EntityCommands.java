package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyCallback;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface EntityCommands {
    @NonNull
    CleanEntityHandle createEntity(@NonNull CleanComponent @NonNull ... components);

    @NonNull
    CleanEntityHandle createEntity(@Nullable EntityDestroyCallback destroyCallback,
                                   @Nullable EntityCreateCallback createCallback,
                                   @NonNull CleanComponent @NonNull ... components);

    boolean tryDestroy(int entityID);

    boolean trySetComponent(int entityID, @NonNull CleanComponent component);

    boolean tryAddComponent(int entityID, @NonNull CleanComponent component);

    boolean tryRemoveComponent(int entityID, @NonNull Class<? extends CleanComponent> component);
}
