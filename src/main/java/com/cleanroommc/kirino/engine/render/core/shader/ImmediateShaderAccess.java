package com.cleanroommc.kirino.engine.render.core.shader;

import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.*;

public class ImmediateShaderAccess {

    private static final Logger LOGGER = LogManager.getLogger("Kirino ImmediateShaderAccess");

    @NonNull
    public Shader makeShader(@NonNull ResourceLocation rl) {
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

        LOGGER.debug("{} Shader \"{}\" assembled:\n{}",
                shaderType.toString(),
                rawRl,
                shaderSource);

        return MethodHolder.initShader(shaderSource, rawRl, shaderType);
    }

    @NonNull
    public ShaderProgram makeProgram(@NonNull Shader @NonNull ... shaders) {
        Preconditions.checkNotNull(shaders);
        for (Shader shader : shaders) {
            Preconditions.checkNotNull(shader);
        }

        return MethodHolder.initShaderProgram(shaders);
    }

    public void submitToGL(Shader... shaders) {
        Preconditions.checkNotNull(shaders);

        for (Shader shader : shaders) {
            Preconditions.checkNotNull(shader);

            shader.compile();
        }

        boolean invalid = false;
        StringBuilder builder = new StringBuilder();
        builder.append("GLSL Shader Compilation Error:\n");
        for (Shader shader : shaders) {
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
                MethodHandle shaderProgramCtor) {
        }
    }
}
