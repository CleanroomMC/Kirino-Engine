package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.analysis.install.AnalyticalWorldInstaller;
import com.cleanroommc.kirino.engine.gl.install.GLWorldInstaller;
import com.cleanroommc.kirino.engine.render.*;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.WorldRunner;
import com.cleanroommc.kirino.engine.world.type.GL;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.analysis.view.AnalyticalWorldViewImpl;
import com.cleanroommc.kirino.engine.gl.view.GLWorldViewImpl;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;

public class KirinoEngine {
    public final RenderCoordinator coordinator;
    public final RenderStructure structure;

    private final WorldRunner<GL> glWorld;
    private final WorldRunner<Headless> headlessWorld;

    private final ResourceLayout resourceLayout;

    private KirinoEngine(
            EventBus eventBus,
            Logger logger,
            CleanECSRuntime ecsRuntime,
            boolean enableHDR,
            boolean enablePostProcessing) {

        resourceLayout = new ResourceLayout();

        coordinator = new RenderCoordinator(eventBus, logger, ecsRuntime, enableHDR, enablePostProcessing);
        structure = new RenderStructure();

        ResourceStorage resourceStorage = new ResourceStorage();

        glWorld = WorldRunner.of(new GLWorldViewImpl(ecsRuntime, structure), new GLWorldInstaller());
        headlessWorld = WorldRunner.of(new AnalyticalWorldViewImpl(ecsRuntime, structure), new AnalyticalWorldInstaller());
    }

    public void run(FramePhase phase) {
        headlessWorld.run(phase);
        glWorld.run(phase);
    }
}
