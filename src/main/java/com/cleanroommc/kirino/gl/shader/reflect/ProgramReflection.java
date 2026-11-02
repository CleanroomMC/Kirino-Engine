package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.reflect.program.*;
import com.cleanroommc.kirino.gl.shader.reflect.resource.StorageType;
import com.cleanroommc.kirino.gl.shader.reflect.type.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GL context is required.
 */
public final class ProgramReflection {

    private final int programId;
    private final ImmutableList<Shader> shaders;

    private final ImmutableList<ShaderReflection> refls;
    private final ImmutableListMultimap<ShaderType, ShaderReflection> reflsByStage;

    public ProgramReflection(int programId, @NonNull ImmutableList<Shader> shaders) {
        Preconditions.checkArgument(programId > 0, "Program ID must be positive.");
        Preconditions.checkNotNull(shaders);

        this.programId = programId;
        this.shaders = shaders;

        refls = shaders
                .stream()
                .flatMap(shader -> shader.getShaderMeta().stream())
                .map(ShaderMeta::refl)
                .collect(ImmutableList.toImmutableList());
        reflsByStage = refls
                .stream()
                .collect(ImmutableListMultimap.toImmutableListMultimap(ShaderReflection::stage, shader -> shader));
    }

    private void checkProgram() {
        Preconditions.checkState(GL20.glIsProgram(programId),
                "Program \"%s\" is not valid in the current context.", programId);
        Preconditions.checkState(GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) != 0,
                "Program \"%s\" is not successfully linked.", programId);
    }

    private int[] properties(int programInterface, int index, int... properties) {
        int[] values = new int[properties.length];
        GL43.glGetProgramResourceiv(programId, programInterface, index, properties, null, values);
        return values;
    }

    private int[] activeVariables(int programInterface, int index, int count) {
        int[] values = new int[count];
        if (count > 0) {
            GL43.glGetProgramResourceiv(programId, programInterface, index, new int[]{GL43.GL_ACTIVE_VARIABLES}, null, values);
        }
        return values;
    }

    private static boolean matches(String actual, String requested) {
        return actual.equals(requested) || actual.endsWith("[0]") && actual.substring(0, actual.length() - 3).equals(requested);
    }

    @NonNull
    public ImmutableList<ShaderReflection> shaders() {
        return refls;
    }

    @NonNull
    public ImmutableList<ShaderReflection> shaders(@NonNull ShaderType stage) {
        Preconditions.checkNotNull(stage);

        return reflsByStage.get(stage);
    }

    @NonNull
    public ImmutableList<ProgramUniform> uniforms() {
        checkProgram();

        ImmutableList.Builder<ProgramUniform> uniforms = ImmutableList.builder();
        int count = GL43.glGetProgramInterfacei(programId, GL43.GL_UNIFORM, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < count; i++) {
            int[] values = properties(GL43.GL_UNIFORM, i, GL43.GL_TYPE, GL43.GL_ARRAY_SIZE, GL43.GL_LOCATION, GL43.GL_BLOCK_INDEX, GL43.GL_ATOMIC_COUNTER_BUFFER_INDEX);
            if (values[3] >= 0 || values[4] >= 0) {
                continue;
            }

            String name = GL43.glGetProgramResourceName(programId, GL43.GL_UNIFORM, i);
            GlslType type = GlTypes.resolve(values[0], values[1], name);
            GlslType base = type instanceof ArrayType array ? array.elementType() : type;
            ImmutableList.Builder<Integer> units = ImmutableList.builder();
            if (base instanceof SamplerType || base instanceof ImageType) {
                int size = Math.max(1, values[1]);
                for (int element = 0; element < size; element++) {
                    String elementName = name.endsWith("[0]") ? name.substring(0, name.length() - 3) + "[" + element + "]" : name;
                    int location = GL20.glGetUniformLocation(programId, elementName);
                    units.add(location < 0 ? -1 : GL20.glGetUniformi(programId, location));
                }
            }
            uniforms.add(new ProgramUniform(name, type, values[2], units.build()));
        }

        return uniforms.build();
    }

    @NonNull
    public Optional<ProgramUniform> uniform(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return uniforms()
                .stream()
                .filter(value -> matches(value.name(), name))
                .findFirst();
    }

    @NonNull
    public ImmutableList<ProgramBlock> uniformBlocks() {
        checkProgram();

        return blocks(GL43.GL_UNIFORM_BLOCK, GL43.GL_UNIFORM, StorageType.UNIFORM_BLOCK);
    }

    @NonNull
    public Optional<ProgramBlock> uniformBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return uniformBlocks()
                .stream()
                .filter(value -> matches(value.name(), name))
                .findFirst();
    }

    @NonNull
    public ImmutableList<ProgramBlock> storageBlocks() {
        checkProgram();

        return blocks(GL43.GL_SHADER_STORAGE_BLOCK, GL43.GL_BUFFER_VARIABLE, StorageType.SHADER_STORAGE_BLOCK);
    }

    @NonNull
    public Optional<ProgramBlock> storageBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return storageBlocks()
                .stream()
                .filter(value -> matches(value.name(), name))
                .findFirst();
    }

    private ImmutableList<ProgramBlock> blocks(int blockInterface, int memberInterface, StorageType storage) {
        ImmutableList.Builder<ProgramBlock> blocks = ImmutableList.builder();
        int count = GL43.glGetProgramInterfacei(programId, blockInterface, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < count; i++) {
            String name = GL43.glGetProgramResourceName(programId, blockInterface, i);
            String prefix = name.contains("[") ? name.substring(0, name.indexOf('[')) : name;
            int[] values = properties(blockInterface, i, GL43.GL_BUFFER_BINDING, GL43.GL_BUFFER_DATA_SIZE, GL43.GL_NUM_ACTIVE_VARIABLES);
            int[] indices = activeVariables(blockInterface, i, values[2]);

            ImmutableList.Builder<ProgramBlockMember> members = ImmutableList.builder();
            for (int index : indices) {
                int[] member = properties(memberInterface, index, GL43.GL_TYPE, GL43.GL_ARRAY_SIZE, GL43.GL_OFFSET, GL43.GL_ARRAY_STRIDE, GL43.GL_MATRIX_STRIDE, GL43.GL_IS_ROW_MAJOR);
                int[] top = memberInterface == GL43.GL_BUFFER_VARIABLE
                        ? properties(memberInterface, index, GL43.GL_TOP_LEVEL_ARRAY_SIZE, GL43.GL_TOP_LEVEL_ARRAY_STRIDE)
                        : new int[]{0, 0};

                String memberName = GL43.glGetProgramResourceName(programId, memberInterface, index);
                if (memberName.startsWith(prefix + ".")) {
                    memberName = memberName.substring(prefix.length() + 1);
                }

                members.add(new ProgramBlockMember(
                        memberName,
                        GlTypes.resolve(member[0], member[1], memberName, true),
                        member[2],
                        member[3],
                        member[4],
                        member[5] != 0,
                        top[0],
                        top[1]));
            }

            blocks.add(new ProgramBlock(name, storage, i, values[0], values[1], members.build()));
        }

        return blocks.build();
    }

    @NonNull
    public ImmutableList<StageVariable> inputs() {
        checkProgram();

        return stageVariables(GL43.GL_PROGRAM_INPUT);
    }

    @NonNull
    public Optional<StageVariable> input(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return inputs()
                .stream()
                .filter(value -> matches(value.name(), name))
                .findFirst();
    }

    @NonNull
    public ImmutableList<StageVariable> outputs() {
        checkProgram();

        return stageVariables(GL43.GL_PROGRAM_OUTPUT);
    }

    @NonNull
    public Optional<StageVariable> output(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return outputs()
                .stream()
                .filter(value -> matches(value.name(), name))
                .findFirst();
    }

    private ImmutableList<StageVariable> stageVariables(int programInterface) {
        ImmutableList.Builder<StageVariable> variables = ImmutableList.builder();
        int count = GL43.glGetProgramInterfacei(programId, programInterface, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < count; i++) {
            int[] values = properties(programInterface, i, GL43.GL_TYPE, GL43.GL_ARRAY_SIZE, GL43.GL_LOCATION, GL43.GL_IS_PER_PATCH, GL44.GL_LOCATION_COMPONENT);
            int locationIndex = programInterface == GL43.GL_PROGRAM_OUTPUT ? properties(programInterface, i, GL43.GL_LOCATION_INDEX)[0] : 0;
            String name = GL43.glGetProgramResourceName(programId, programInterface, i);
            variables.add(new StageVariable(
                    name,
                    GlTypes.resolve(values[0], values[1], name),
                    values[2],
                    values[4],
                    locationIndex,
                    values[3] != 0));
        }

        return variables.build();
    }

    @NonNull
    public ImmutableList<AtomicCounterBuffer> atomicCounterBuffers() {
        checkProgram();

        ImmutableList.Builder<AtomicCounterBuffer> buffers = ImmutableList.builder();
        int count = GL43.glGetProgramInterfacei(programId, GL42.GL_ATOMIC_COUNTER_BUFFER, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < count; i++) {
            int[] values = properties(GL42.GL_ATOMIC_COUNTER_BUFFER, i, GL43.GL_BUFFER_BINDING, GL43.GL_BUFFER_DATA_SIZE, GL43.GL_NUM_ACTIVE_VARIABLES);

            ImmutableList.Builder<AtomicCounter> counters = ImmutableList.builder();
            for (int index : activeVariables(GL42.GL_ATOMIC_COUNTER_BUFFER, i, values[2])) {
                int[] counter = properties(GL43.GL_UNIFORM, index, GL43.GL_ARRAY_SIZE, GL43.GL_OFFSET);
                counters.add(new AtomicCounter(
                        GL43.glGetProgramResourceName(programId, GL43.GL_UNIFORM, index),
                        counter[0],
                        counter[1]));
            }

            buffers.add(new AtomicCounterBuffer(i, values[0], values[1], counters.build()));
        }

        return buffers.build();
    }

    @NonNull
    public Subroutines subroutines(@NonNull ShaderType stage) {
        Preconditions.checkNotNull(stage);
        checkProgram();

        int functionInterface = switch (stage) {
            case VERTEX -> GL43.GL_VERTEX_SUBROUTINE;
            case FRAGMENT -> GL43.GL_FRAGMENT_SUBROUTINE;
            case GEOMETRY -> GL43.GL_GEOMETRY_SUBROUTINE;
            case TESS_CONTROL -> GL43.GL_TESS_CONTROL_SUBROUTINE;
            case TESS_EVALUATION -> GL43.GL_TESS_EVALUATION_SUBROUTINE;
            case COMPUTE -> GL43.GL_COMPUTE_SUBROUTINE;
        };

        int uniformInterface = switch (stage) {
            case VERTEX -> GL43.GL_VERTEX_SUBROUTINE_UNIFORM;
            case FRAGMENT -> GL43.GL_FRAGMENT_SUBROUTINE_UNIFORM;
            case GEOMETRY -> GL43.GL_GEOMETRY_SUBROUTINE_UNIFORM;
            case TESS_CONTROL -> GL43.GL_TESS_CONTROL_SUBROUTINE_UNIFORM;
            case TESS_EVALUATION -> GL43.GL_TESS_EVALUATION_SUBROUTINE_UNIFORM;
            case COMPUTE -> GL43.GL_COMPUTE_SUBROUTINE_UNIFORM;
        };

        Map<Integer, Subroutine> enumeratedFunctions = new HashMap<>();
        ImmutableList.Builder<Subroutine> functions = ImmutableList.builder();
        int functionCount = GL43.glGetProgramInterfacei(programId, functionInterface, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < functionCount; i++) {
            String name = GL43.glGetProgramResourceName(programId, functionInterface, i);
            if (name.isEmpty()) {
                continue;
            }

            Subroutine function = new Subroutine(name, GL40.glGetSubroutineIndex(programId, stage.glValue, name));
            enumeratedFunctions.put(i, function);
            functions.add(function);
        }
        ImmutableList<Subroutine> resolved = functions.build();

        ImmutableList.Builder<SubroutineUniform> uniforms = ImmutableList.builder();
        int uniformCount = GL43.glGetProgramInterfacei(programId, uniformInterface, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < uniformCount; i++) {
            String name = GL43.glGetProgramResourceName(programId, uniformInterface, i);
            int[] values = properties(uniformInterface, i, GL43.GL_ARRAY_SIZE, GL43.GL_LOCATION, GL43.GL_NUM_COMPATIBLE_SUBROUTINES);
            int[] indices = new int[values[2]];
            if (indices.length > 0) {
                GL43.glGetProgramResourceiv(programId, uniformInterface, i, new int[]{GL43.GL_COMPATIBLE_SUBROUTINES}, null, indices);
            }

            ImmutableList.Builder<Subroutine> compatible = ImmutableList.builder();
            for (int index : indices) {
                Subroutine function = enumeratedFunctions.get(index);
                if (function == null) {
                    function = resolved.stream().filter(value -> value.index() == index).findFirst().orElse(null);
                }
                Preconditions.checkState(function != null,
                        "The driver returned an unknown compatible subroutine index \"%s\".", index);

                compatible.add(function);
            }

            uniforms.add(new SubroutineUniform(name, values[1], values[0], compatible.build()));
        }

        return new Subroutines(resolved, uniforms.build());
    }

    @NonNull
    public ImmutableList<TransformFeedbackBuffer> transformFeedbackBuffers() {
        checkProgram();

        if (GL43.glGetProgramInterfacei(programId, GL43.GL_TRANSFORM_FEEDBACK_VARYING, GL43.GL_ACTIVE_RESOURCES) == 0) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<TransformFeedbackBuffer> buffers = ImmutableList.builder();
        int count = GL43.glGetProgramInterfacei(programId, GL30.GL_TRANSFORM_FEEDBACK_BUFFER, GL43.GL_ACTIVE_RESOURCES);
        for (int i = 0; i < count; i++) {
            int[] values = properties(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, i, GL43.GL_BUFFER_BINDING, GL44.GL_TRANSFORM_FEEDBACK_BUFFER_STRIDE, GL43.GL_NUM_ACTIVE_VARIABLES);
            ImmutableList.Builder<TransformFeedbackVarying> varyings = ImmutableList.builder();
            for (int index : activeVariables(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, i, values[2])) {
                String name = GL43.glGetProgramResourceName(programId, GL43.GL_TRANSFORM_FEEDBACK_VARYING, index);
                int[] varying = properties(GL43.GL_TRANSFORM_FEEDBACK_VARYING, index, GL43.GL_TYPE, GL43.GL_ARRAY_SIZE, GL43.GL_OFFSET);
                if (varying[0] == GL11.GL_NONE) {
                    continue;
                }

                varyings.add(new TransformFeedbackVarying(name, GlTypes.basic(varying[0]), varying[1], varying[2]));
            }

            buffers.add(new TransformFeedbackBuffer(i, values[0], values[1], varyings.build()));
        }

        return buffers.build();
    }
}
