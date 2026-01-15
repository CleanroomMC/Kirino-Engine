package com.cleanroommc.kirino.engine.world.context;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.render.RenderStructure;
import com.cleanroommc.kirino.engine.world.type.WorldKind;

public interface WorldContext<W extends WorldKind> {
    CleanECSRuntime ecs();
    RenderStructure render();
    void run(FramePhase phase);
    void on(FramePhase phase, Runnable runnable);
}
