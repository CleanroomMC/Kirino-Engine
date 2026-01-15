package com.cleanroommc.kirino.engine.render;

import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;

public class ShaderIntrospection {
    public final GLSLRegistry glslRegistry;
    public final DefaultShaderAnalyzer defaultShaderAnalyzer;

    public ShaderIntrospection(
            GLSLRegistry glslRegistry,
            DefaultShaderAnalyzer defaultShaderAnalyzer) {

        this.glslRegistry = glslRegistry;
        this.defaultShaderAnalyzer = defaultShaderAnalyzer;
    }
}
