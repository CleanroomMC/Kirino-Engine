package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Optional;

/**
 * {@link #compile()} is the only GL dependent functionality in this class.
 * Everything else is GL agnostic, and you're free to access them at any time and anywhere,
 * especially {@link #analyze(ShaderAnalyzer)}.
 */
public class Shader extends GLDisposable {

    private Shader(
            @NonNull String shaderSource,
            @NonNull String shaderName,
            @NonNull ShaderType shaderType) {

        Preconditions.checkNotNull(shaderSource);
        Preconditions.checkNotNull(shaderName);
        Preconditions.checkNotNull(shaderType);

        this.shaderName = shaderName;
        this.shaderSource = shaderSource;
        this.shaderType = shaderType;
    }

    private final String shaderName;
    private final String shaderSource;
    private final ShaderType shaderType;
    private int shaderID;
    private boolean valid = true;
    private String errorLog = "";
    private boolean setup;
    private ShaderMeta shaderMeta = null;

    @NonNull
    public String getShaderName() {
        return shaderName;
    }

    @NonNull
    public String getShaderSource() {
        return shaderSource;
    }

    @NonNull
    public ShaderType getShaderType() {
        return shaderType;
    }

    /**
     * Only available and makes sense after compilation.
     */
    public int getShaderID() {
        return shaderID;
    }

    /**
     * Only available and makes sense after compilation.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Marks whether {@link #compile()} has been run.
     */
    public boolean isSetup() {
        return setup;
    }

    /**
     * Only available and makes sense after compilation.
     */
    @NonNull
    public String getErrorLog() {
         return errorLog;
    }

    /**
     * Call {@link #analyze(ShaderAnalyzer)} first and then try read the result.
     *
     * <p>Note: {@link #analyze(ShaderAnalyzer)} doesn't guarantee a successful analysis.</p>
     * <p>Note: You can run {@link #analyze(ShaderAnalyzer)} without a GL context.</p>
     * <p><b>Suggestion</b>: You should cache the result in hot paths.</p>
     */
    @NonNull
    public Optional<ShaderMeta> getShaderMeta() {
        return Optional.ofNullable(shaderMeta);
    }

    /**
     * Read the result from {@link #getShaderMeta()}.
     */
    public void analyze(@NonNull ShaderAnalyzer analyzer) {
        Preconditions.checkNotNull(analyzer);

        shaderMeta = analyzer.analyze(shaderSource);
    }

    /**
     * <p>Note: Most likely shouldn't be called manually by clients.
     * Other manager classes will wire the process.</p>
     * <p>Note: Can be executed multiple times without crashing.</p>
     */
    public void compile() {
        if (setup) {
            return;
        }

        shaderID = GL20.glCreateShader(shaderType.glValue);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            errorLog = GL20.glGetShaderInfoLog(shaderID, 1024);
            GL20.glDeleteShader(shaderID);
            shaderID = 0;
            valid = false;
        }

        setup = true;

        if (shaderID != 0) {
            GLResourceManager.addDisposable(this);
        }
    }

    @Override
    protected void dispose() {
        GL20.glDeleteShader(shaderID);
    }
}
