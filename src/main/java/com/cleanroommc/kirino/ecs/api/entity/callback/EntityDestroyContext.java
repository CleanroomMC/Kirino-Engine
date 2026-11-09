package com.cleanroommc.kirino.ecs.api.entity.callback;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.jspecify.annotations.NonNull;

public interface EntityDestroyContext {
    @NonNull
    <T extends CleanComponent> T getComponent(@NonNull Class<T> component);
}
