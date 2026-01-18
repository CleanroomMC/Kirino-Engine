package com.cleanroommc.kirino.engine.world;

import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.WorldKind;
import org.jspecify.annotations.NonNull;

public interface ModuleInstaller<W extends WorldKind> {
    void install(@NonNull WorldContext<W> context);
}
