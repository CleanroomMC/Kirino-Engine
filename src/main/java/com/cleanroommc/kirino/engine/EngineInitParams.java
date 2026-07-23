package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingSchedule;

public record EngineInitParams(
        boolean enableHDR,
        boolean enablePostProcessing,
        boolean enableKhrDebug,
        boolean enableShaderDebug,
        PostProcessingSchedule postProcessingSchedule) {
}
