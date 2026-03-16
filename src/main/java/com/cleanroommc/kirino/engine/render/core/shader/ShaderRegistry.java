package com.cleanroommc.kirino.engine.render.core.shader;

import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderCompileOptions;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderDebugInjection;
import com.cleanroommc.kirino.gl.shader.ShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShaderRegistry {

    // key: rl.toString
    private final Map<String, Shader> shaders = new HashMap<>();

    @NonNull
    public Shader register(@NonNull ResourceLocation rl, @Nullable ShaderCompileOptions options) {
        Preconditions.checkNotNull(rl);

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

        String shaderSource = MinecraftResourceUtils.readText(rl, MinecraftResourceUtils.NewLineType.BACK_SLASH_N);

        // todo: integrate ksmlc
        List<ShaderDebugInjection.Type> debugTypes;
        if (options != null && !(debugTypes = ShaderDebugInjection.parse(options.debugFlags)).isEmpty()) {
            verifyDebugFlags(rl, shaderType, debugTypes);
            shaderSource = injectDebug(shaderSource, debugTypes);
        }
        Shader shader = MethodHolder.initShader(shaderSource, rawRl, shaderType);

        shaders.put(rawRl, shader);
        return shader;
    }

    private void verifyDebugFlags(ResourceLocation rl, ShaderType shaderType, List<ShaderDebugInjection.Type> debugTypes) {
        for (ShaderDebugInjection.Type type : debugTypes) {
            if (shaderType != ShaderType.COMPUTE && (
                    type == ShaderDebugInjection.Type.COMPUTE_FRAME_DEBUG_VEC3F
                    || type == ShaderDebugInjection.Type.COMPUTE_INVOCATION_LIMIT
                    || type == ShaderDebugInjection.Type.COMPUTE_SPECIFIED_INVOCATION
                    || type == ShaderDebugInjection.Type.COMPUTE_STAGE_DEBUG
                    || type == ShaderDebugInjection.Type.COMPUTE_IMAGE_DEBUG)) {
                throw new IllegalStateException(shaderType.toString() + " shader \"" + rl.toString() + "\" must not have the debug flag " + type + ".");
            }
            if (shaderType != ShaderType.VERTEX && (
                    type == ShaderDebugInjection.Type.VERTEX_FRAME_DEBUG_VEC3F
                    || type == ShaderDebugInjection.Type.VERTEX_STAGE_DEBUG)) {
                throw new IllegalStateException(shaderType.toString() + " shader \"" + rl.toString() + "\" must not have the debug flag " + type + ".");
            }
        }
    }

    private String injectDebug(String shaderSource, List<ShaderDebugInjection.Type> debugTypes) {
        // todo

        return shaderSource;
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

    public void analyze(GLSLRegistry glslRegistry, ShaderAnalyzer analyzer) {
        for (Shader shader : shaders.values()) {
            shader.analyze(glslRegistry, analyzer);
        }
    }

    @NonNull
    public ShaderProgram newShaderProgram(@NonNull String @NonNull ... shaderRLs) {
        Preconditions.checkNotNull(shaderRLs);

        for (String rl : shaderRLs) {
            Preconditions.checkNotNull(rl);

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
