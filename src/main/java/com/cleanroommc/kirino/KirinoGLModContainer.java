package com.cleanroommc.kirino;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public final class KirinoGLModContainer extends DummyModContainer {

    public KirinoGLModContainer() {
        super(new ModMetadata());
        KirinoCommonCore.LOGGER.info("Initializing Kirino-GL's Mod Container.");
        ModMetadata meta = this.getMetadata();

        meta.modId = "kirino_gl";
        meta.name = "Kirino GL";
        meta.version = "epoch-1.a2";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
