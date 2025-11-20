package com.cleanroommc.kirino.engine.render.shader;

import com.cleanroommc.kirino.gl.shader.IShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShaderRegistry {
    // key: rl
    private final Map<String, Shader> shaders = new HashMap<>();

    public Shader register(ResourceLocation rl) {
        String rawRl = rl.toString();
        int lastDot = rawRl.lastIndexOf('.');
        if (lastDot == -1) {
            throw new IllegalStateException("Invalid Shader ResourceLocation " + rawRl + ". Can't parse the shader type.");
        }
        String suffix = rawRl.substring(lastDot + 1);
        ShaderType shaderType = ShaderType.parse(suffix);
        if (shaderType == null) {
            throw new IllegalStateException("Invalid Shader ResourceLocation " + rawRl + ". Can't parse the shader type.");
        }
        String shaderSource = MinecraftResourceUtils.readText(rl, true);
        Shader shader = MethodHolder.initShader(shaderSource, rawRl, shaderType);
        shaders.put(rawRl, shader);
        return shader;
    }

    public void compile() {
        for (Shader shader : shaders.values()) {
            shader.compile();
        }
        boolean invalid = false;
        StringBuilder builder = new StringBuilder();
        builder.append("Shader Compilation Error:\n");
        for (Shader shader : shaders.values()) {
            if (!shader.isValid()) {
                invalid = true;
                builder.append("[Error from ").append(shader.getShaderName()).append("]: ");
                builder.append(shader.getErrorLog()).append("\n");
            }
        }
        if (invalid) {
            throw new RuntimeException(builder.toString());
        }
    }

    public void analyze(GLSLRegistry glslRegistry, IShaderAnalyzer analyzer) {
        for (Shader shader : shaders.values()) {
            shader.analyze(glslRegistry, analyzer);
        }
    }

    public ShaderProgram newShaderProgram(String... shaderRLs) {
        for (String rl : shaderRLs) {
            if (!shaders.containsKey(rl)) {
                throw new IllegalStateException("Shader " +  rl + " isn't registered.");
            }
        }
        Shader[] shaders1 = new Shader[shaderRLs.length];
        for (int i = 0; i < shaders1.length; i++) {
            shaders1[i] = shaders.get(shaderRLs[i]);
        }
        return MethodHolder.initShaderProgram(shaders1);
    }

    public ShaderProgram newShaderProgram(ResourceLocation... shaderRLs) {
        return newShaderProgram(Arrays.stream(shaderRLs).map(ResourceLocation::toString).toList().toArray(new String[0]));
    }

    private static class MethodHolder {
        static final ShaderDelegate DELEGATE;

        static {
            DELEGATE = new ShaderDelegate(
                    ReflectionUtils.getConstructor(Shader.class, String.class, String.class, ShaderType.class),
                    ReflectionUtils.getConstructor(ShaderProgram.class, Shader[].class));

            Preconditions.checkNotNull(DELEGATE.shaderCtor());
            Preconditions.checkNotNull(DELEGATE.shaderProgramCtor());
        }
        /**
         * @see Shader#Shader(String, String, ShaderType)
         */
        static Shader initShader(String shaderSource, String shaderName, ShaderType shaderType) {
            try {
                return (Shader) DELEGATE.shaderCtor().invokeExact(shaderSource, shaderName, shaderType);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see ShaderProgram#ShaderProgram(Shader...)
         */
        @SuppressWarnings("ConfusingArgumentToVarargsMethod")
        static ShaderProgram initShaderProgram(Shader... shaders) {
            try {
                return (ShaderProgram) DELEGATE.shaderProgramCtor().invokeExact(shaders);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record ShaderDelegate(
                MethodHandle shaderCtor,
                MethodHandle shaderProgramCtor) {}
    }
}
