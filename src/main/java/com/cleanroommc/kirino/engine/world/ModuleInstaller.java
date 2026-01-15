package com.cleanroommc.kirino.engine.world;

import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.WorldKind;

public interface ModuleInstaller<W extends WorldKind> {
    void install(WorldContext<W> context);
}