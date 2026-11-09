package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public interface CleanEntityHandle {
    boolean valid();

    boolean isAlive();

    boolean contains(@NonNull Class<? extends CleanComponent> component);

    @NonNull <T extends CleanComponent> Optional<T> readComponent(@NonNull Class<T> component);

    boolean tryDestroy();

    boolean trySetComponent(@NonNull CleanComponent component);

    boolean tryAddComponent(@NonNull CleanComponent component);

    boolean tryRemoveComponent(@NonNull Class<? extends CleanComponent> component);
}
