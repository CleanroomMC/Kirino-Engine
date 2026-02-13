package com.cleanroommc.kirino;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public final class KirinoEngineModContainer extends DummyModContainer {

    public KirinoEngineModContainer() {
        super(new ModMetadata());
        KirinoCommonCore.LOGGER.info("Initializing Kirino-Engine's Mod Container.");
        ModMetadata meta = this.getMetadata();

        meta.modId = "kirino_engine";
        meta.name = "Kirino Engine";

        meta.description = """
                Kirino-Engine combines an ECS-based data-oriented architecture,\s
                explicit modern OpenGL abstractions,\s
                and a hybrid CPU-GPU rendering pipeline to reimagine Minecraft's traditional,\s
                tightly coupled, and CPU-bound rendering.
                """;

        meta.credits = """
                Kirino-Engine is made possible thanks to the efforts of all contributors!
                - [tttsaurus](https://github.com/tttsaurus ) - Core maintainer, architecture design, and overall project coordination
                - [Eerie](https://github.com/Kuba663 ) - Feature development and algorithmic contributions
                - [ChaosStrikez](https://github.com/jchung01 ) - Code refactoring, call-site improvements, and algorithm fixes
                """;

        meta.version = "epoch-1.a4";
        meta.logoFile = "/logo.png";

        meta.authorList.add("tttsaurus");
        meta.authorList.add("Eerie");
        meta.authorList.add("ChaosStrikez");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
