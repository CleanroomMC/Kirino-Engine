package com.cleanroommc.kirino.gl.shader;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface ShaderAnalyzer {

    /**
     * @implNote The result must be deterministic
     */
    @Nullable ShaderMeta analyze(@NonNull String shaderSource);
}
