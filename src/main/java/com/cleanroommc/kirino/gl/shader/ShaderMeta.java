package com.cleanroommc.kirino.gl.shader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ShaderMeta {


    public sealed interface Declaration permits
            StructDeclaration,
            UniformDeclaration,
            InterfaceBlock,
            GlobalLayoutDeclaration,
            InputOutputDeclaration {
    }

    /**
     * @param expression <code>null</code> when the qualifier is a bare name such as <code>std140</code> or <code>row_major</code>
     */
    public record Layout(@NonNull String name, @Nullable String expression) {

        public Layout {
            Preconditions.checkNotNull(name);
        }
    }

    /**
     * @param expression <code>null</code> for an unsized array dimension (<code>[]</code>)
     */
    public record ArrayDimension(@Nullable String expression) {
    }

    /**
     * @param initializer <code>null</code> when the declarator has no initializer
     */
    public record Declarator(
            @NonNull String name,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @Nullable String initializer) {

        public Declarator {
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(arrayDimensions);
        }
    }

    /**
     * @param name <code>null</code> when <code>inlineStruct</code> is an unnamed struct definition
     * @param inlineStruct <code>null</code> when the declaration refers to a type without defining a struct in place
     */
    public record Type(
            @Nullable String name,
            @NonNull ImmutableList<ArrayDimension> arrayDimensions,
            @Nullable StructDeclaration inlineStruct) {

        public Type {
            Preconditions.checkNotNull(arrayDimensions);
        }
    }

    public record Member(
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators) {

        public Member {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
        }
    }

    /**
     * @param name <code>null</code> when the struct is unnamed
     */
    public record StructDeclaration(
            @Nullable String name,
            @NonNull ImmutableList<Member> members,
            @NonNull ImmutableList<Declarator> declarators) implements Declaration {

        public StructDeclaration {
            Preconditions.checkNotNull(members);
            Preconditions.checkNotNull(declarators);
        }
    }

    public record UniformDeclaration(
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators) implements Declaration {

        public UniformDeclaration {
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
        }
    }

    /**
     * @param instance <code>null</code> when the interface block has no instance name
     */
    public record InterfaceBlock(
            @NonNull String storage,
            @NonNull String name,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Member> members,
            @Nullable Declarator instance) implements Declaration {

        public InterfaceBlock {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(name);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(members);
        }
    }

    public record GlobalLayoutDeclaration(
            @NonNull String storage,
            @NonNull ImmutableList<Layout> layouts) implements Declaration {

        public GlobalLayoutDeclaration {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(layouts);
        }
    }

    public record InputOutputDeclaration(
            @NonNull String storage,
            @NonNull Type type,
            @NonNull ImmutableList<Layout> layouts,
            @NonNull ImmutableList<String> qualifiers,
            @NonNull ImmutableList<Declarator> declarators) implements Declaration {

        public InputOutputDeclaration {
            Preconditions.checkNotNull(storage);
            Preconditions.checkNotNull(type);
            Preconditions.checkNotNull(layouts);
            Preconditions.checkNotNull(qualifiers);
            Preconditions.checkNotNull(declarators);
        }
    }

    public final ImmutableList<StructDeclaration> structs;
    public final ImmutableList<UniformDeclaration> uniforms;
    public final ImmutableList<InterfaceBlock> interfaceBlocks;
    public final ImmutableList<GlobalLayoutDeclaration> globalLayouts;
    public final ImmutableList<InputOutputDeclaration> inputOutputs;
    public final ImmutableList<Declaration> topLevelDeclarations;

    ShaderMeta(
            @NonNull ImmutableList<StructDeclaration> structs,
            @NonNull ImmutableList<UniformDeclaration> uniforms,
            @NonNull ImmutableList<InterfaceBlock> interfaceBlocks,
            @NonNull ImmutableList<GlobalLayoutDeclaration> globalLayouts,
            @NonNull ImmutableList<InputOutputDeclaration> inputOutputs,
            @NonNull ImmutableList<Declaration> topLevelDeclarations) {

        Preconditions.checkNotNull(structs);
        Preconditions.checkNotNull(uniforms);
        Preconditions.checkNotNull(interfaceBlocks);
        Preconditions.checkNotNull(globalLayouts);
        Preconditions.checkNotNull(inputOutputs);
        Preconditions.checkNotNull(topLevelDeclarations);

        this.structs = structs;
        this.uniforms = uniforms;
        this.interfaceBlocks = interfaceBlocks;
        this.globalLayouts = globalLayouts;
        this.inputOutputs = inputOutputs;
        this.topLevelDeclarations = topLevelDeclarations;
    }
}
