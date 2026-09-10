package com.cleanroommc.kirino.mod;

import com.cleanroommc.kirino.KirinoCommonCore;
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
        meta.version = "epoch-1.a4";

        meta.description = """
                (WIP) This is a subsystem of Kirino-Engine, providing a set of low-level
                OpenGL abstractions covering a considerable subset of modern GL.
                It aims high usability and strong interoperability with raw GL calls.
                """;

        meta.parent = "kirino_engine";

        meta.modProperties.put("backgroundFile", "/assets/cleanroom/background.png");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
