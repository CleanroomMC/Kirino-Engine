package com.cleanroommc.kirino.ecs.api.entity.callback;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.jspecify.annotations.NonNull;

public interface EntityDestroyContext {
    @NonNull
    <T extends CleanComponent> T getComponent(@NonNull Class<T> component);

    byte getByte(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    short getShort(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    int getInt(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    long getLong(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    float getFloat(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    double getDouble(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);

    boolean getBool(@NonNull Class<? extends CleanComponent> component, @NonNull String @NonNull ... path);
}
