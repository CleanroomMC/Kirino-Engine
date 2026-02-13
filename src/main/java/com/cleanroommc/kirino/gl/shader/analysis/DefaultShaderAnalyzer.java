package com.cleanroommc.kirino.gl.shader.analysis;

import com.cleanroommc.kirino.gl.shader.ShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;

public class DefaultShaderAnalyzer implements ShaderAnalyzer {
    @Override
    public ShaderMeta analyze(GLSLRegistry glslRegistry, String shaderSource) {
        return ShaderParser.parse(glslRegistry, ShaderTokenizer.tokenize(shaderSource));
    }
}
