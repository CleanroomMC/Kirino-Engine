package com.cleanroommc.kirino.ecs.job;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface JobDataInjector {
    void inject(@NonNull Object owner, @Nullable Object value);
}
