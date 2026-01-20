package com.cleanroommc.kirino.engine.world;

import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.WorldKind;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class WorldRunner<W extends WorldKind> {
    private final WorldContext<W> context;
    private final List<ModuleInstaller<W>> modules;

    private WorldRunner(WorldContext<W> context, List<ModuleInstaller<W>> modules) {
        this.context = context;
        this.modules = modules;
    }

    @NonNull
    @SafeVarargs
    public static <W extends WorldKind> WorldRunner<W> of(
            @NonNull WorldContext<W> context,
            @NonNull ResourceLayout layout,
            @NonNull ModuleInstaller<W> @NonNull ... modules) {

        WorldRunner<W> runner = new WorldRunner<>(context, List.of(modules));

        for (ModuleInstaller<W> module : runner.modules) {
            module.install(context, layout);
        }

        return runner;
    }

    public void run(@NonNull FramePhase phase) {
        context.run(phase);
    }
}
