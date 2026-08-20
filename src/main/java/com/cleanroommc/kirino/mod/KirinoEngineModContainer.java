package com.cleanroommc.kirino.mod;

import com.cleanroommc.kirino.KirinoCommonCore;
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
        meta.url = "https://github.com/CleanroomMC/Kirino-Engine";

        meta.description = """
                (WIP) Minecraft rendering becomes difficult as implicit state and mixins couple rendering behavior. Our primary goal is
                to provide an explicit structure,
                overhaul most of Minecraft’s rendering in a future-proof manner,
                and provide a set of advanced rendering APIs to mod developers.
                """;

        meta.credits = """
                Kirino-Engine is made possible thanks to the efforts of all contributors!
                - [tttsaurus](https://github.com/tttsaurus ) - Core maintainer, architecture design, and overall project coordination
                - [Eerie](https://github.com/Kuba663 ) - Feature development and algorithmic contributions
                - [ChaosStrikez](https://github.com/jchung01 ) - Code refactoring, call-site improvements, and algorithm fixes
                """;

        meta.version = "epoch-1.a6";
        meta.logoFile = "/logo.png";

        meta.authorList.add("tttsaurus");
        meta.authorList.add("Eerie");
        meta.authorList.add("ChaosStrikez");

        meta.modProperties.put("backgroundFile", "/assets/cleanroom/background.png");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}
