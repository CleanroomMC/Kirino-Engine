package com.cleanroommc.kirino.ecs;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

interface JobDataInjector {
    void inject(@NonNull Object owner, @Nullable Object value);
}
