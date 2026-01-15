package com.cleanroommc.kirino.engine.world.context;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.render.RenderExtensions;
import com.cleanroommc.kirino.engine.render.RenderStructure;
import com.cleanroommc.kirino.engine.world.type.WorldKind;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public interface WorldContext<W extends WorldKind> {
    @NonNull CleanECSRuntime ecs();
    @NonNull RenderStructure rs();
    @NonNull RenderExtensions ext();
    @NonNull Logger logger();
    @NonNull EventBus bus();
    void run(@NonNull FramePhase phase);
    void on(@NonNull FramePhase phase, @NonNull Consumer<WorldContext<W>> consumer);
}
