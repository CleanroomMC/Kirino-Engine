package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.gl.shader.DefaultShaderAnalyzer;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class ShaderIntrospection {

    public final DefaultShaderAnalyzer defaultShaderAnalyzer;

    public ShaderIntrospection(
            @NonNull DefaultShaderAnalyzer defaultShaderAnalyzer) {

        Preconditions.checkNotNull(defaultShaderAnalyzer);

        this.defaultShaderAnalyzer = defaultShaderAnalyzer;
    }
}
