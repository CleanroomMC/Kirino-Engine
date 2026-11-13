package com.cleanroommc.kirino.engine.render.core.shader;

import com.cleanroommc.kirino.gl.shader.*;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.cleanroommc.kirino.utils.ResourceLocation;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

public class ImmediateShaderAccess {

    @FunctionalInterface
    private interface SourceLoader {

        String load(ResourceLocation rl);
    }

    private static final Logger LOGGER = LogManager.getLogger("Kirino ImmediateShaderAccess");
    private static final DefaultShaderAnalyzer ANALYZER = new DefaultShaderAnalyzer();

    private final SourceLoader sourceLoader;

    public ImmediateShaderAccess() {
        this((SourceLoader) rl -> MinecraftResourceUtils.readText(rl, MinecraftResourceUtils.NewLineType.BACK_SLASH_N));
    }

    private ImmediateShaderAccess(SourceLoader sourceLoader) {
        this.sourceLoader = sourceLoader;
    }

    @NonNull
    public static ImmediateShaderAccess fromSourceLoader(@NonNull Function<ResourceLocation, String> sourceLoader) {
        Preconditions.checkNotNull(sourceLoader);

        return new ImmediateShaderAccess((SourceLoader) sourceLoader::apply);
    }

    @NonNull
    public Shader makeShader(net.minecraft.util.@NonNull ResourceLocation rl) {
        Preconditions.checkNotNull(rl);

        return makeShader(new ResourceLocation(rl.getNamespace(), rl.getPath()));
    }

    @NonNull
    public Shader makeShader(net.minecraft.util.@NonNull ResourceLocation rl, @NonNull ShaderAnalyzer analyzer) {
        Preconditions.checkNotNull(rl);

        return makeShader(new ResourceLocation(rl.getNamespace(), rl.getPath()), analyzer);
    }

    /**
     * <p>Note: Shader will be analyzed altogether in this call.
     * It uses the default analyzer {@link DefaultShaderAnalyzer}.</p>
     */
    @NonNull
    public Shader makeShader(@NonNull ResourceLocation rl) {
        return makeShader(rl, ANALYZER);
    }

    /**
     * <p>Note: Shader will be analyzed altogether in this call.</p>
     */
    @NonNull
    public Shader makeShader(@NonNull ResourceLocation rl, @NonNull ShaderAnalyzer analyzer) {
        Preconditions.checkNotNull(rl);
        Preconditions.checkNotNull(analyzer);

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

        String shaderSource = sourceLoader.load(rl);

        LOGGER.debug("{} Shader \"{}\" assembled:\n{}",
                shaderType.toString(),
                rawRl,
                shaderSource);

        Shader result = MethodHolder.initShader(shaderSource, rawRl, shaderType);
        result.analyze(analyzer);

        return result;
    }

    @NonNull
    public Shader makeShader(@NonNull String namespace, @NonNull String path) {
        return makeShader(new ResourceLocation(namespace, path));
    }

    @NonNull
    public Shader makeShader(@NonNull String namespace, @NonNull String path, @NonNull ShaderAnalyzer analyzer) {
        return makeShader(new ResourceLocation(namespace, path), analyzer);
    }

    @NonNull
    public ShaderProgram makeProgram(@NonNull Shader @NonNull ... shaders) {
        return makeProgram(false, shaders);
    }

    @NonNull
    public ShaderProgram makeProgram(boolean dsa, @NonNull Shader @NonNull ... shaders) {
        Preconditions.checkNotNull(shaders);
        for (Shader shader : shaders) {
            Preconditions.checkNotNull(shader);
        }

        return MethodHolder.initShaderProgram(dsa, shaders);
    }

    public void submitToGL(@NonNull Shader @NonNull ... shaders) {
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

    private static final class MethodHolder {
        static final ShaderDelegate DELEGATE;

        static {
            DELEGATE = new ShaderDelegate(
                    ReflectionUtils.getConstructor(Shader.class, String.class, String.class, ShaderType.class),
                    ReflectionUtils.getConstructor(ShaderProgram.class, boolean.class, Shader[].class));

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
         * @see ShaderProgram#ShaderProgram(boolean, Shader...)
         */
        static ShaderProgram initShaderProgram(boolean dsa, Shader... shaders) {
            try {
                return (ShaderProgram) DELEGATE.shaderProgramCtor().invokeExact(dsa, shaders);
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
