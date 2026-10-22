package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.reflect.ShaderReflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ShaderMeta {

    public record SourceSpan(int startLine, int startColumn, int endLine, int endColumn) {

        public SourceSpan {
            Preconditions.checkArgument(startLine > 0 && startColumn > 0);
            Preconditions.checkArgument(endLine >= startLine && endColumn > 0);
            Preconditions.checkArgument(endLine != startLine || endColumn >= startColumn);
        }
    }

    public sealed interface Declaration permits
            StructDeclaration,
            UniformDeclaration,
            InterfaceBlock,
            GlobalLayoutDeclaration,
            InputOutputDeclaration,
            VariableDeclaration,
            QualifierDeclaration,
            PrecisionDeclaration,
            SubroutineTypeDeclaration,
            SubroutineFunctionDeclaration {

        @NonNull SourceSpan span();
    }

    /**
     * @param expression <code>null</code> when the qualifier is a bare name such as <code>std140</code> or <code>row_major</code>
     */
    public record Layout(@NonNull String name, @Nullable String expression, @NonNull SourceSpan span) {

        public Layout {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * @param expression <code>null</code> for an unsized array dimension (<code>[]</code>)
     */
    public record ArrayDimension(@Nullable String expression, @NonNull SourceSpan span) {

        public ArrayDimension {
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * @param initializer <code>null</code> when the declarator has no initializer
     */
    public record Declarator(
            @NonNull String name,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @Nullable String initializer,
            @NonNull SourceSpan span) {

        public Declarator {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(arrayDimensions);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * @param name <code>null</code> when <code>inlineStruct</code> is an unnamed struct definition
     * @param inlineStruct <code>null</code> when the declaration refers to a type without defining a struct in place
     */
    public record Type(
            @Nullable String name,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @Nullable StructDeclaration inlineStruct,
            @NonNull SourceSpan span) {

        public Type {
            Preconditions.checkNotNull(arrayDimensions);
            Preconditions.checkNotNull(span);
        }
    }

    public record Member(
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull SourceSpan span) {

        public Member {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * @param name <code>null</code> when parameter name is omitted
     */
    public record Parameter(
            @Nullable String name,
            @NonNull Type type,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull SourceSpan span) {

        public Parameter {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(arrayDimensions);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * struct Light {
     *     vec3 position;
     *     float intensity;
     * } light;
     * }</pre>
     *
     * @param name <code>null</code> when the struct is unnamed
     */
    public record StructDeclaration(
            @Nullable String name,
            @NonNull ImmutableList<Member> members,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @NonNull SourceSpan span) implements Declaration {

        public StructDeclaration {
            Preconditions.checkNotNull(members);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(arrayDimensions);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * uniform vec4 tint;
     *
     * layout(binding = 0) uniform sampler2D albedo;
     * }</pre>
     */
    public record UniformDeclaration(
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull SourceSpan span) implements Declaration {

        public UniformDeclaration {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * layout(std140, binding = 0) uniform Camera {
     *     mat4 viewProjection;
     * } camera;
     * }</pre>
     *
     * @param instance <code>null</code> when the interface block has no instance name
     */
    public record InterfaceBlock(
            @NonNull String storage,
            @NonNull String name,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Member> members,
            @Nullable Declarator instance,
            @NonNull SourceSpan span) implements Declaration {

        public InterfaceBlock {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(members);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * layout(std140, row_major) uniform;
     *
     * layout(local_size_x = 8, local_size_y = 8) in;
     * }</pre>
     */
    public record GlobalLayoutDeclaration(
            @NonNull String storage,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull SourceSpan span) implements Declaration {

        public GlobalLayoutDeclaration {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * layout(location = 0) in vec3 position;
     *
     * layout(location = 0) out vec4 color;
     * }</pre>
     */
    public record InputOutputDeclaration(
            @NonNull String storage,
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull SourceSpan span) implements Declaration {

        public InputOutputDeclaration {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * const int COUNT = 4;
     *
     * shared vec4 scratch[COUNT];
     *
     * float accumulator;
     * }</pre>
     *
     * @param storage <code>null</code> when no explicit storage token
     */
    public record VariableDeclaration(
            @Nullable String storage,
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull SourceSpan span) implements Declaration {

        public VariableDeclaration {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * invariant gl_Position;
     * }</pre>
     *
     * @param storage <code>null</code> when no explicit storage token
     */
    public record QualifierDeclaration(
            @Nullable String storage,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators,
            @NonNull SourceSpan span) implements Declaration {

        public QualifierDeclaration {
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * precision highp float;
     * }</pre>
     */
    public record PrecisionDeclaration(
            @NonNull String precision,
            @NonNull Type type,
            @NonNull SourceSpan span) implements Declaration {

        public PrecisionDeclaration {
            Preconditions.checkNotNull(precision);
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * subroutine vec4 Shade(vec3 normal);
     * }</pre>
     */
    public record SubroutineTypeDeclaration(
            @NonNull String name,
            @NonNull Type returnType,
            @NonNull ImmutableList<Parameter> parameters,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull SourceSpan span) implements Declaration {

        public SubroutineTypeDeclaration {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(returnType);
            Preconditions.checkNotNull(parameters);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(span);
        }
    }

    /**
     * e.g.
     * <pre>{@code
     * subroutine vec4 Shade(vec3 normal);
     *
     * subroutine(Shade) vec4 shadeNormal(vec3 normal) {
     *     return vec4(normal, 1.0);
     * }
     * }</pre>
     */
    public record SubroutineFunctionDeclaration(
            @NonNull String name,
            @NonNull Type returnType,
            @NonNull ImmutableList<Parameter> parameters,
            @NonNull ImmutableList<String> compatibleTypes,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            boolean definition,
            @NonNull SourceSpan span) implements Declaration {

        public SubroutineFunctionDeclaration {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(returnType);
            Preconditions.checkNotNull(parameters);
            Preconditions.checkNotNull(compatibleTypes);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(span);
        }
    }

    public final ShaderType shaderType;
    public final ImmutableList<StructDeclaration> structs;
    public final ImmutableList<UniformDeclaration> uniforms;
    public final ImmutableList<InterfaceBlock> interfaceBlocks;
    public final ImmutableList<GlobalLayoutDeclaration> globalLayouts;
    public final ImmutableList<InputOutputDeclaration> inputOutputs;
    public final ImmutableList<VariableDeclaration> variables;
    public final ImmutableList<QualifierDeclaration> qualifiers;
    public final ImmutableList<PrecisionDeclaration> precisions;
    public final ImmutableList<SubroutineTypeDeclaration> subroutineTypes;
    public final ImmutableList<SubroutineFunctionDeclaration> subroutineFunctions;
    public final ImmutableList<Declaration> topLevelDeclarations;

    ShaderMeta(
            @NonNull ShaderType shaderType,
            @NonNull ImmutableList<StructDeclaration> structs,
            @NonNull ImmutableList<UniformDeclaration> uniforms,
            @NonNull ImmutableList<InterfaceBlock> interfaceBlocks,
            @NonNull ImmutableList<GlobalLayoutDeclaration> globalLayouts,
            @NonNull ImmutableList<InputOutputDeclaration> inputOutputs,
            @NonNull ImmutableList<VariableDeclaration> variables,
            @NonNull ImmutableList<QualifierDeclaration> qualifiers,
            @NonNull ImmutableList<PrecisionDeclaration> precisions,
            @NonNull ImmutableList<SubroutineTypeDeclaration> subroutineTypes,
            @NonNull ImmutableList<SubroutineFunctionDeclaration> subroutineFunctions,
            @NonNull ImmutableList<Declaration> topLevelDeclarations) {

        Preconditions.checkNotNull(shaderType);
        Preconditions.checkNotNull(structs);
        Preconditions.checkNotNull(uniforms);
        Preconditions.checkNotNull(interfaceBlocks);
        Preconditions.checkNotNull(globalLayouts);
        Preconditions.checkNotNull(inputOutputs);
        Preconditions.checkNotNull(variables);
        Preconditions.checkNotNull(qualifiers);
        Preconditions.checkNotNull(precisions);
        Preconditions.checkNotNull(subroutineTypes);
        Preconditions.checkNotNull(subroutineFunctions);
        Preconditions.checkNotNull(topLevelDeclarations);

        this.shaderType = shaderType;
        this.structs = structs;
        this.uniforms = uniforms;
        this.interfaceBlocks = interfaceBlocks;
        this.globalLayouts = globalLayouts;
        this.inputOutputs = inputOutputs;
        this.variables = variables;
        this.qualifiers = qualifiers;
        this.precisions = precisions;
        this.subroutineTypes = subroutineTypes;
        this.subroutineFunctions = subroutineFunctions;
        this.topLevelDeclarations = topLevelDeclarations;
    }

    private ShaderReflection reflection = null;

    @NonNull
    public ShaderReflection refl() {
        if (reflection == null) {
            reflection = new ShaderReflection(this);
        }

        return reflection;
    }
}
