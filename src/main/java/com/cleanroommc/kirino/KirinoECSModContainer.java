package com.cleanroommc.kirino;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public final class KirinoECSModContainer extends DummyModContainer {

    public KirinoECSModContainer() {
        super(new ModMetadata());
        KirinoCore.LOGGER.info("Initializing Kirino-ECS's Mod Container.");
        ModMetadata meta = this.getMetadata();

        meta.modId = "kirino_ecs";
        meta.name = "Kirino ECS";
        meta.version = "epoch-1.a1";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
