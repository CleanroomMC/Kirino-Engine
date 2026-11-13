package com.cleanroommc.kirino.ecs;

import org.jspecify.annotations.NonNull;

interface JobInstantiator {
    @NonNull Object instantiate();
}
