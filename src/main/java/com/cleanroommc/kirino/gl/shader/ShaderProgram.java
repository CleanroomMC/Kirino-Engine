package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.anone.reveal.ReflectiveInvocation;
import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.shader.reflect.ProgramReflection;
import com.cleanroommc.kirino.gl.shader.reflect.ShaderReflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Arrays;
import java.util.Optional;

public class ShaderProgram extends GLDisposable {

    private final ImmutableList<Shader> shaders;
    private final int programID;

    public ImmutableList<Shader> getShaders() {
        return shaders;
    }

    public int getProgramID() {
        return programID;
    }

    //<editor-fold desc="reflection & convenient paths">
    private ProgramReflection reflection = null;

    @NonNull
    public ProgramReflection refl() {
        if (reflection == null) {
            reflection = new ProgramReflection(programID, shaders);
        }

        return reflection;
    }

    private int vertIndexCache = -1;

    @NonNull
    public Shader vert() {
        if (vertIndexCache != -1) {
            return shaders.get(vertIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.VERTEX) {
                vertIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("VERTEX shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> vertRefl() {
        return vert().getShaderMeta().map(ShaderMeta::refl);
    }

    private int fragIndexCache = -1;

    @NonNull
    public Shader frag() {
        if (fragIndexCache != -1) {
            return shaders.get(fragIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.FRAGMENT) {
                fragIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("FRAGMENT shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> fragRefl() {
        return frag().getShaderMeta().map(ShaderMeta::refl);
    }

    private int geomIndexCache = -1;

    @NonNull
    public Shader geom() {
        if (geomIndexCache != -1) {
            return shaders.get(geomIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.GEOMETRY) {
                geomIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("GEOMETRY shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> geomRefl() {
        return geom().getShaderMeta().map(ShaderMeta::refl);
    }

    private int tescIndexCache = -1;

    @NonNull
    public Shader tesc() {
        if (tescIndexCache != -1) {
            return shaders.get(tescIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.TESS_CONTROL) {
                tescIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("TESS_CONTROL shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> tescRefl() {
        return tesc().getShaderMeta().map(ShaderMeta::refl);
    }

    private int teseIndexCache = -1;

    @NonNull
    public Shader tese() {
        if (teseIndexCache != -1) {
            return shaders.get(teseIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.TESS_EVALUATION) {
                teseIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("TESS_EVALUATION shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> teseRefl() {
        return tese().getShaderMeta().map(ShaderMeta::refl);
    }

    private int compIndexCache = -1;

    @NonNull
    public Shader comp() {
        if (compIndexCache != -1) {
            return shaders.get(compIndexCache);
        }

        for (int i = 0; i < shaders.size(); i++) {
            if (shaders.get(i).getShaderType() == ShaderType.COMPUTE) {
                compIndexCache = i;
                return shaders.get(i);
            }
        }

        throw new IllegalStateException("COMPUTE shader does not exist.");
    }

    @NonNull
    public Optional<ShaderReflection> compRefl() {
        return comp().getShaderMeta().map(ShaderMeta::refl);
    }
    //</editor-fold>

    @ReflectiveInvocation
    private ShaderProgram(@NonNull Shader @NonNull ... shaders) {
        Preconditions.checkNotNull(shaders);
        for (Shader shader : shaders) {
            Preconditions.checkNotNull(shader);
            Preconditions.checkState(shader.isValid(), "Shader must be valid.");
            Preconditions.checkState(shader.isSetup(), "Shader must be set up.");
        }

        ImmutableList.Builder<Shader> builder = ImmutableList.builder();
        this.shaders = builder.addAll(Arrays.asList(shaders)).build();
        programID = GL20.glCreateProgram();
        for (Shader shader : shaders) {
            GL20.glAttachShader(programID, shader.getShaderID());
        }
        GL20.glLinkProgram(programID);

        int status = GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(programID, 1024);
            throw new RuntimeException("Shader program link failed: " + log);
        }

        GLResourceManager.addDisposable(this);
    }

    public void use() {
        GL20.glUseProgram(programID);
    }

    public void use0() {
        GL20.glUseProgram(0);
    }

    public static void useRaw(int programID) {
        GL20.glUseProgram(programID);
    }

    @Override
    public int disposePriority() {
        return 100; // earlier than shaders
    }

    @Override
    protected void dispose() {
        for (Shader shader : shaders) {
            GL20.glDetachShader(programID, shader.getShaderID());
        }
        GL20.glDeleteProgram(programID);
    }
}
