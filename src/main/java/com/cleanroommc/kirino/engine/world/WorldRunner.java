package com.cleanroommc.kirino.engine.world;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.WorldKind;

import java.util.List;

public final class WorldRunner<W extends WorldKind> {
    private final WorldContext<W> context;
    private final List<ModuleInstaller<W>> modules;

    private WorldRunner(WorldContext<W> context, List<ModuleInstaller<W>> modules) {
        this.context = context;
        this.modules = modules;
    }

    @SafeVarargs
    public static <W extends WorldKind> WorldRunner<W> of(
            WorldContext<W> context,
            ModuleInstaller<W>... modules) {

        WorldRunner<W> runner = new WorldRunner<>(context, List.of(modules));

        for (ModuleInstaller<W> module : runner.modules) {
            module.install(context);
        }

        return runner;
    }

    public void run(FramePhase phase) {
        context.run(phase);
    }
}
