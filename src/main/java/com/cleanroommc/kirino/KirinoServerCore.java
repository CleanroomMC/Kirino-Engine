package com.cleanroommc.kirino;

import com.cleanroommc.kirino.config.KirinoConfig;

import static com.cleanroommc.kirino.KirinoCommonCore.LOGGER;

public final class KirinoServerCore {

    private KirinoServerCore() {
    }

    public static void init() {
        KirinoCommonCore.init();

        if (!KirinoConfig.isEnabled()) {
            return;
        }

        LOGGER.info("---------- Kirino Server-Side Initialization ----------");
    }

    public static void postInit() {
        KirinoCommonCore.postInit();

        if (!KirinoConfig.isEnabled()) {
            return;
        }

        LOGGER.info("---------- Kirino Server-Side Post-Initialization ----------");
    }
}
