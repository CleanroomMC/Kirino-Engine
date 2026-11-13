package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.anone.reveal.ReflectiveInvocation;
import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.shader.reflect.ProgramReflection;
import com.cleanroommc.kirino.gl.shader.reflect.ShaderReflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.joml.*;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Optional;

public class ShaderProgram extends GLDisposable {

    private final ImmutableList<Shader> shaders;
    private final int programID;
    private final boolean dsa;

    public ImmutableList<Shader> getShaders() {
        return shaders;
    }

    public int getProgramID() {
        return programID;
    }

    //<editor-fold desc="reflection">
    private ProgramReflection reflection = null;
    private final EnumMap<ShaderType, Optional<ShaderReflection>> shaderReflectionCache = new EnumMap<>(ShaderType.class);

    @NonNull
    public ProgramReflection refl() {
        if (reflection == null) {
            reflection = new ProgramReflection(programID, shaders);
        }

        return reflection;
    }

    @NonNull
    public Shader shader(@NonNull ShaderType stage) {
        Preconditions.checkNotNull(stage);

        return switch (stage) {
            case VERTEX -> vert();
            case FRAGMENT -> frag();
            case GEOMETRY -> geom();
            case TESS_CONTROL -> tesc();
            case TESS_EVALUATION -> tese();
            case COMPUTE -> comp();
        };
    }

    @NonNull
    public Optional<ShaderReflection> shaderRefl(@NonNull ShaderType stage) {
        Preconditions.checkNotNull(stage);

        Optional<ShaderReflection> result = shaderReflectionCache.get(stage);
        if (result == null) {
            result = shader(stage).getShaderMeta().map(ShaderMeta::refl);
            shaderReflectionCache.put(stage, result);
        }
        return result;
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
        return shaderRefl(ShaderType.VERTEX);
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
        return shaderRefl(ShaderType.FRAGMENT);
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
        return shaderRefl(ShaderType.GEOMETRY);
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
        return shaderRefl(ShaderType.TESS_CONTROL);
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
        return shaderRefl(ShaderType.TESS_EVALUATION);
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
        return shaderRefl(ShaderType.COMPUTE);
    }
    //</editor-fold>

    //<editor-fold desc="convenient paths">
    private static final int UNQUERIED = Integer.MIN_VALUE;
    private final Object2IntOpenHashMap<String> uniformLocations = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> uniformBlockIndices = new Object2IntOpenHashMap<>();
    private final Object2IntOpenHashMap<String> storageBlockIndices = new Object2IntOpenHashMap<>();
    private final Int2IntOpenHashMap uniformBlockBindings = new Int2IntOpenHashMap();
    private final Int2IntOpenHashMap storageBlockBindings = new Int2IntOpenHashMap();

    public void clearQueryCaches() {
        uniformLocations.clear();
        uniformBlockIndices.clear();
        storageBlockIndices.clear();
        uniformBlockBindings.clear();
        storageBlockBindings.clear();
    }

    public int uniformLocation(@NonNull String name) {
        Preconditions.checkNotNull(name);

        int location = uniformLocations.getInt(name);
        if (location == UNQUERIED) {
            location = GL20.glGetUniformLocation(programID, name);
            uniformLocations.put(name, location);
        }
        return location;
    }

    public int uniformBlockIndex(@NonNull String name) {
        Preconditions.checkNotNull(name);

        int index = uniformBlockIndices.getInt(name);
        if (index == UNQUERIED) {
            index = GL31.glGetUniformBlockIndex(programID, name);
            uniformBlockIndices.put(name, index);
        }
        return index;
    }

    public int storageBlockIndex(@NonNull String name) {
        Preconditions.checkNotNull(name);

        int index = storageBlockIndices.getInt(name);
        if (index == UNQUERIED) {
            index = GL43.glGetProgramResourceIndex(programID, GL43.GL_SHADER_STORAGE_BLOCK, name);
            storageBlockIndices.put(name, index);
        }
        return index;
    }

    public int uniformBlockBinding(@NonNull String name) {
        int index = uniformBlockIndex(name);
        Preconditions.checkArgument(index != GL31.GL_INVALID_INDEX,
                "Uniform block \"%s\" is not active.", name);

        int binding = uniformBlockBindings.get(index);
        if (binding == UNQUERIED) {
            binding = GL31.glGetActiveUniformBlocki(programID, index, GL31.GL_UNIFORM_BLOCK_BINDING);
            uniformBlockBindings.put(index, binding);
        }
        return binding;
    }

    public int storageBlockBinding(@NonNull String name) {
        int index = storageBlockIndex(name);
        Preconditions.checkArgument(index != GL31.GL_INVALID_INDEX,
                "Storage block \"%s\" is not active.", name);

        int binding = storageBlockBindings.get(index);
        if (binding == UNQUERIED) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer value = stack.mallocInt(1);
                GL43.glGetProgramResourceiv(programID, GL43.GL_SHADER_STORAGE_BLOCK, index, stack.ints(GL43.GL_BUFFER_BINDING), null, value);
                binding = value.get(0);
            }
            storageBlockBindings.put(index, binding);
        }
        return binding;
    }

    public void setUniformBlockBinding(@NonNull String name, int binding) {
        Preconditions.checkArgument(binding >= 0, "Binding must not be negative.");
        int index = uniformBlockIndex(name);
        Preconditions.checkArgument(index != GL31.GL_INVALID_INDEX,
                "Uniform block \"%s\" is not active.", name);

        GL31.glUniformBlockBinding(programID, index, binding);
        uniformBlockBindings.put(index, binding);
    }

    public void setStorageBlockBinding(@NonNull String name, int binding) {
        Preconditions.checkArgument(binding >= 0, "Binding must not be negative.");
        int index = storageBlockIndex(name);
        Preconditions.checkArgument(index != GL31.GL_INVALID_INDEX,
                "Storage block \"%s\" is not active.", name);

        GL43.glShaderStorageBlockBinding(programID, index, binding);
        storageBlockBindings.put(index, binding);
    }

    public void bindUniformBase(@NonNull String name, int bufferID) {
        GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, uniformBlockBinding(name), bufferID);
    }

    public void bindUniformRange(@NonNull String name, int bufferID, long offset, long size) {
        GL30.glBindBufferRange(GL31.GL_UNIFORM_BUFFER, uniformBlockBinding(name), bufferID, offset, size);
    }

    public void bindStorageBase(@NonNull String name, int bufferID) {
        GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, storageBlockBinding(name), bufferID);
    }

    public void bindStorageRange(@NonNull String name, int bufferID, long offset, long size) {
        GL30.glBindBufferRange(GL43.GL_SHADER_STORAGE_BUFFER, storageBlockBinding(name), bufferID, offset, size);
    }

    public void uniform1b(@NonNull String name, boolean value) {
        if (dsa) {
            GL41.glProgramUniform1i(programID, uniformLocation(name), value ? 1 : 0);
        } else {
            GL20.glUniform1i(uniformLocation(name), value ? 1 : 0);
        }
    }

    public void uniform1i(@NonNull String name, int value) {
        if (dsa) {
            GL41.glProgramUniform1i(programID, uniformLocation(name), value);
        } else {
            GL20.glUniform1i(uniformLocation(name), value);
        }
    }

    public void uniform2i(@NonNull String name, int x, int y) {
        if (dsa) {
            GL41.glProgramUniform2i(programID, uniformLocation(name), x, y);
        } else {
            GL20.glUniform2i(uniformLocation(name), x, y);
        }
    }

    public void uniform3i(@NonNull String name, int x, int y, int z) {
        if (dsa) {
            GL41.glProgramUniform3i(programID, uniformLocation(name), x, y, z);
        } else {
            GL20.glUniform3i(uniformLocation(name), x, y, z);
        }
    }

    public void uniform4i(@NonNull String name, int x, int y, int z, int w) {
        if (dsa) {
            GL41.glProgramUniform4i(programID, uniformLocation(name), x, y, z, w);
        } else {
            GL20.glUniform4i(uniformLocation(name), x, y, z, w);
        }
    }

    public void uniform1ui(@NonNull String name, int value) {
        if (dsa) {
            GL41.glProgramUniform1ui(programID, uniformLocation(name), value);
        } else {
            GL30.glUniform1ui(uniformLocation(name), value);
        }
    }

    public void uniform2ui(@NonNull String name, int x, int y) {
        if (dsa) {
            GL41.glProgramUniform2ui(programID, uniformLocation(name), x, y);
        } else {
            GL30.glUniform2ui(uniformLocation(name), x, y);
        }
    }

    public void uniform3ui(@NonNull String name, int x, int y, int z) {
        if (dsa) {
            GL41.glProgramUniform3ui(programID, uniformLocation(name), x, y, z);
        } else {
            GL30.glUniform3ui(uniformLocation(name), x, y, z);
        }
    }

    public void uniform4ui(@NonNull String name, int x, int y, int z, int w) {
        if (dsa) {
            GL41.glProgramUniform4ui(programID, uniformLocation(name), x, y, z, w);
        } else {
            GL30.glUniform4ui(uniformLocation(name), x, y, z, w);
        }
    }

    public void uniform1f(@NonNull String name, float value) {
        if (dsa) {
            GL41.glProgramUniform1f(programID, uniformLocation(name), value);
        } else {
            GL20.glUniform1f(uniformLocation(name), value);
        }
    }

    public void uniform2f(@NonNull String name, float x, float y) {
        if (dsa) {
            GL41.glProgramUniform2f(programID, uniformLocation(name), x, y);
        } else {
            GL20.glUniform2f(uniformLocation(name), x, y);
        }
    }

    public void uniform3f(@NonNull String name, float x, float y, float z) {
        if (dsa) {
            GL41.glProgramUniform3f(programID, uniformLocation(name), x, y, z);
        } else {
            GL20.glUniform3f(uniformLocation(name), x, y, z);
        }
    }

    public void uniform4f(@NonNull String name, float x, float y, float z, float w) {
        if (dsa) {
            GL41.glProgramUniform4f(programID, uniformLocation(name), x, y, z, w);
        } else {
            GL20.glUniform4f(uniformLocation(name), x, y, z, w);
        }
    }

    public void uniform1iv(@NonNull String name, @NonNull IntBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform1iv(programID, uniformLocation(name), values);
        } else {
            GL20.glUniform1iv(uniformLocation(name), values);
        }
    }

    public void uniform1uiv(@NonNull String name, @NonNull IntBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform1uiv(programID, uniformLocation(name), values);
        } else {
            GL30.glUniform1uiv(uniformLocation(name), values);
        }
    }

    public void uniform1fv(@NonNull String name, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform1fv(programID, uniformLocation(name), values);
        } else {
            GL20.glUniform1fv(uniformLocation(name), values);
        }
    }

    public void uniform2fv(@NonNull String name, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform2fv(programID, uniformLocation(name), values);
        } else {
            GL20.glUniform2fv(uniformLocation(name), values);
        }
    }

    public void uniform3fv(@NonNull String name, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform3fv(programID, uniformLocation(name), values);
        } else {
            GL20.glUniform3fv(uniformLocation(name), values);
        }
    }

    public void uniform4fv(@NonNull String name, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniform4fv(programID, uniformLocation(name), values);
        } else {
            GL20.glUniform4fv(uniformLocation(name), values);
        }
    }

    public void uniformMatrix2f(@NonNull String name, boolean transpose, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniformMatrix2fv(programID, uniformLocation(name), transpose, values);
        } else {
            GL20.glUniformMatrix2fv(uniformLocation(name), transpose, values);
        }
    }

    public void uniformMatrix2f(@NonNull String name, @NonNull Matrix2fc value) {
        Preconditions.checkNotNull(value);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (dsa) {
                GL41.glProgramUniformMatrix2fv(programID, uniformLocation(name), false, value.get(stack.mallocFloat(4)));
            } else {
                GL20.glUniformMatrix2fv(uniformLocation(name), false, value.get(stack.mallocFloat(4)));
            }
        }
    }

    public void uniformMatrix3f(@NonNull String name, boolean transpose, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniformMatrix3fv(programID, uniformLocation(name), transpose, values);
        } else {
            GL20.glUniformMatrix3fv(uniformLocation(name), transpose, values);
        }
    }

    public void uniformMatrix3f(@NonNull String name, @NonNull Matrix3fc value) {
        Preconditions.checkNotNull(value);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (dsa) {
                GL41.glProgramUniformMatrix3fv(programID, uniformLocation(name), false, value.get(stack.mallocFloat(9)));
            } else {
                GL20.glUniformMatrix3fv(uniformLocation(name), false, value.get(stack.mallocFloat(9)));
            }
        }
    }

    public void uniformMatrix3x2f(@NonNull String name, boolean transpose, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniformMatrix3x2fv(programID, uniformLocation(name), transpose, values);
        } else {
            GL21.glUniformMatrix3x2fv(uniformLocation(name), transpose, values);
        }
    }

    public void uniformMatrix3x2f(@NonNull String name, @NonNull Matrix3x2fc value) {
        Preconditions.checkNotNull(value);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (dsa) {
                GL41.glProgramUniformMatrix3x2fv(programID, uniformLocation(name), false, value.get(stack.mallocFloat(6)));
            } else {
                GL21.glUniformMatrix3x2fv(uniformLocation(name), false, value.get(stack.mallocFloat(6)));
            }
        }
    }

    public void uniformMatrix4f(@NonNull String name, boolean transpose, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniformMatrix4fv(programID, uniformLocation(name), transpose, values);
        } else {
            GL20.glUniformMatrix4fv(uniformLocation(name), transpose, values);
        }
    }

    public void uniformMatrix4f(@NonNull String name, @NonNull Matrix4fc value) {
        Preconditions.checkNotNull(value);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (dsa) {
                GL41.glProgramUniformMatrix4fv(programID, uniformLocation(name), false, value.get(stack.mallocFloat(16)));
            } else {
                GL20.glUniformMatrix4fv(uniformLocation(name), false, value.get(stack.mallocFloat(16)));
            }
        }
    }

    public void uniformMatrix4x3f(@NonNull String name, boolean transpose, @NonNull FloatBuffer values) {
        Preconditions.checkNotNull(values);

        if (dsa) {
            GL41.glProgramUniformMatrix4x3fv(programID, uniformLocation(name), transpose, values);
        } else {
            GL21.glUniformMatrix4x3fv(uniformLocation(name), transpose, values);
        }
    }

    public void uniformMatrix4x3f(@NonNull String name, @NonNull Matrix4x3fc value) {
        Preconditions.checkNotNull(value);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            if (dsa) {
                GL41.glProgramUniformMatrix4x3fv(programID, uniformLocation(name), false, value.get(stack.mallocFloat(12)));
            } else {
                GL21.glUniformMatrix4x3fv(uniformLocation(name), false, value.get(stack.mallocFloat(12)));
            }
        }
    }
    //</editor-fold>

    /**
     * Target-bound path.
     */
    @ReflectiveInvocation
    private ShaderProgram(@NonNull Shader @NonNull ... shaders) {
        this(false, shaders);
    }

    @ReflectiveInvocation
    private ShaderProgram(boolean dsa, @NonNull Shader @NonNull ... shaders) {
        Preconditions.checkNotNull(shaders);

        this.dsa = dsa;

        uniformLocations.defaultReturnValue(UNQUERIED);
        uniformBlockIndices.defaultReturnValue(UNQUERIED);
        storageBlockIndices.defaultReturnValue(UNQUERIED);
        uniformBlockBindings.defaultReturnValue(UNQUERIED);
        storageBlockBindings.defaultReturnValue(UNQUERIED);

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
