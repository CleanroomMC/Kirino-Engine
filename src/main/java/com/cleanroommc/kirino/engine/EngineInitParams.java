package com.cleanroommc.kirino.engine;

public record EngineInitParams(
        boolean enableHDR,
        boolean enablePostProcessing,
        boolean enableKhrDebug,
        boolean enableShaderDebug) {
}
