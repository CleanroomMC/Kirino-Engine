package com.cleanroommc.kirino.ecs.entity.callback;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import org.jspecify.annotations.NonNull;

public interface EntityCreateContext {
    @NonNull
    <T extends CleanComponent> T getComponent(@NonNull Class<T> component);
}
