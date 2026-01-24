package com.cleanroommc.kirino;

import static com.cleanroommc.kirino.KirinoCommonCore.KIRINO_CONFIG_HUB;
import static com.cleanroommc.kirino.KirinoCommonCore.LOGGER;

public final class KirinoServerCore {
    private KirinoServerCore() {
    }

    public static void init() {
        KirinoCommonCore.init();

        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        LOGGER.info("Kirino Server-Side Initialization");
    }

    public static void postInit() {
        KirinoCommonCore.postInit();

        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        LOGGER.info("Kirino Server-Side Post-Initialization");
    }
}
