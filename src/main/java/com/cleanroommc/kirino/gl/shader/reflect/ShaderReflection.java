package com.cleanroommc.kirino.gl.shader.reflect;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.shader.reflect.execution.ShaderExecution;
import com.cleanroommc.kirino.gl.shader.reflect.resource.*;
import com.cleanroommc.kirino.gl.shader.reflect.type.SubroutineType;
import com.cleanroommc.kirino.gl.shader.reflect.type.TypeRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * No GL context is required.
 */
public final class ShaderReflection {

    private final ShaderType stage;
    private final ShaderSemantics semantics;

    private final ImmutableList<Uniform> atomicCounters;
    private final ImmutableList<Uniform> subroutineUniforms;
    private final ImmutableList<Variable> sharedVariables;
    private final ImmutableList<SubroutineType> subroutineTypes;

    public ShaderReflection(@NonNull ShaderMeta shaderMeta) {
        Preconditions.checkNotNull(shaderMeta);

        stage = shaderMeta.shaderType;
        semantics = new ShaderSemantics(shaderMeta);

        atomicCounters = uniforms()
                .stream()
                .filter(value -> value.storageType() == StorageType.ATOMIC_COUNTER)
                .collect(ImmutableList.toImmutableList());
        subroutineUniforms = uniforms()
                .stream()
                .filter(value -> value.storageType() == StorageType.SUBROUTINE_UNIFORM)
                .collect(ImmutableList.toImmutableList());
        sharedVariables = variables()
                .stream()
                .filter(value -> value.storageType() == StorageType.SHARED)
                .collect(ImmutableList.toImmutableList());
        subroutineTypes = types().types().values()
                .stream()
                .filter(SubroutineType.class::isInstance)
                .map(SubroutineType.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    @NonNull
    public ShaderType stage() {
        return stage;
    }

    @NonNull
    public TypeRegistry types() {
        return semantics.types;
    }

    @NonNull
    public ImmutableList<Uniform> uniforms() {
        return semantics.uniforms.values().asList();
    }

    @NonNull
    public Optional<Uniform> uniform(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.uniforms.get(name));
    }

    @NonNull
    public ImmutableList<Uniform> atomicCounters() {
        return atomicCounters;
    }

    @NonNull
    public ImmutableList<Uniform> subroutineUniforms() {
        return subroutineUniforms;
    }

    @NonNull
    public ImmutableList<Block> uniformBlocks() {
        return semantics.uniformBlocks.values().asList();
    }

    @NonNull
    public Optional<Block> uniformBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.uniformBlocks.get(name));
    }

    @NonNull
    public ImmutableList<Block> storageBlocks() {
        return semantics.storageBlocks.values().asList();
    }

    @NonNull
    public Optional<Block> storageBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.storageBlocks.get(name));
    }

    @NonNull
    public ImmutableList<Variable> inputs() {
        return semantics.inputs.values().asList();
    }

    @NonNull
    public Optional<Variable> input(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.inputs.get(name));
    }

    @NonNull
    public ImmutableList<Variable> outputs() {
        return semantics.outputs.values().asList();
    }

    @NonNull
    public Optional<Variable> output(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.outputs.get(name));
    }

    @NonNull
    public ImmutableList<Block> inputBlocks() {
        return semantics.inputBlocks.values().asList();
    }

    @NonNull
    public Optional<Block> inputBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.inputBlocks.get(name));
    }

    @NonNull
    public ImmutableList<Block> outputBlocks() {
        return semantics.outputBlocks.values().asList();
    }

    @NonNull
    public Optional<Block> outputBlock(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.outputBlocks.get(name));
    }

    @NonNull
    public ImmutableList<Variable> variables() {
        return semantics.variables.values().asList();
    }

    @NonNull
    public Optional<Variable> variable(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.variables.get(name));
    }

    @NonNull
    public ImmutableList<Variable> sharedVariables() {
        return sharedVariables;
    }

    @NonNull
    public ImmutableMap<String, IntegerExpression> constants() {
        return semantics.expressions.constants();
    }

    @NonNull
    public OptionalLong constant(@NonNull String name) {
        return semantics.expressions.constant(name);
    }

    @NonNull
    public ImmutableList<SubroutineFunction> subroutineFunctions() {
        return semantics.functions.values().asList();
    }

    @NonNull
    public Optional<SubroutineFunction> subroutineFunction(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(semantics.functions.get(name));
    }

    @NonNull
    public ImmutableList<SubroutineType> subroutineTypes() {
        return subroutineTypes;
    }

    @NonNull
    public ShaderExecution execution() {
        return semantics.execution;
    }
}
