package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.engine.render.core.pipeline.post.PostProcessingSchedule;
import org.jspecify.annotations.NonNull;

public record EngineInitParams(
        boolean enableHDR,
        boolean enablePostProcessing,
        boolean enableKhrDebug,
        boolean enableShaderDebug,
        @NonNull PostProcessingSchedule postProcessingSchedule) {
}
