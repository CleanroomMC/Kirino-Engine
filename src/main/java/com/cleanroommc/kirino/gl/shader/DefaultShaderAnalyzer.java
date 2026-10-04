package com.cleanroommc.kirino.gl.shader;

import com.cleanroommc.kirino.gl.shader.parser.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.StringReader;

public final class DefaultShaderAnalyzer implements ShaderAnalyzer {

    private record ShaderMetaBuilder(@NonNull ASTTranslationUnit astUnit) {

        @NonNull
        ShaderMeta build() {
            ImmutableList.Builder<ShaderMeta.StructDeclaration> structs = ImmutableList.builder();
            ImmutableList.Builder<ShaderMeta.UniformDeclaration> uniforms = ImmutableList.builder();
            ImmutableList.Builder<ShaderMeta.InterfaceBlock> interfaceBlocks = ImmutableList.builder();

            for (int i = 0; i < astUnit.jjtGetNumChildren(); i++) {
                Node child = astUnit.jjtGetChild(i);
                if (child instanceof ASTStructDeclaration struct) {
                    structs.add(structDeclaration(struct));
                } else if (child instanceof ASTUniformDeclaration uniform) {
                    uniforms.add(uniformDeclaration(uniform));
                } else if (child instanceof ASTInterfaceBlockDeclaration interfaceBlock) {
                    interfaceBlocks.add(interfaceBlock(interfaceBlock));
                }
            }

            return new ShaderMeta(structs.build(), uniforms.build(), interfaceBlocks.build());
        }

        @NonNull
        private static String value(@NonNull SimpleNode node) {
            return Preconditions.checkNotNull((String) node.jjtGetValue());
        }

        @Nullable
        private static String nullableValue(@NonNull SimpleNode node) {
            return (String) node.jjtGetValue();
        }

        @NonNull
        private static <T extends Node> ImmutableList<T> directChildren(@NonNull Node node, @NonNull Class<T> type) {
            ImmutableList.Builder<T> children = ImmutableList.builder();
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node child = node.jjtGetChild(i);
                if (type.isInstance(child)) {
                    children.add(type.cast(child));
                }
            }
            return children.build();
        }

        @Nullable
        private static <T extends Node> T directChild(@NonNull Node node, @NonNull Class<T> type) {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node child = node.jjtGetChild(i);
                if (type.isInstance(child)) {
                    return type.cast(child);
                }
            }
            return null;
        }

        private static ShaderMeta.@NonNull Member member(@NonNull ASTMemberDeclaration node) {
            return new ShaderMeta.Member(
                    type(requireDirectChild(node, ASTTypeSpecifier.class)),
                    layouts(node),
                    qualifiers(node),
                    declarators(requireDirectChild(node, ASTDeclaratorList.class)));
        }

        private static ShaderMeta.@NonNull Type type(@NonNull ASTTypeSpecifier node) {
            ASTStructSpecifier inlineStruct = directChild(node, ASTStructSpecifier.class);
            if (inlineStruct != null) {
                ShaderMeta.StructDeclaration definition = structDeclaration(inlineStruct, ImmutableList.of());
                return new ShaderMeta.Type(definition.name(), arrayDimensions(node), definition);
            }

            return new ShaderMeta.Type(
                    value(node),
                    arrayDimensions(node),
                    null);
        }

        @NonNull
        private static ImmutableList<ShaderMeta.Layout> layouts(@NonNull Node node) {
            ImmutableList.Builder<ShaderMeta.Layout> layouts = ImmutableList.builder();
            for (ASTLayoutQualifier qualifier : directChildren(node, ASTLayoutQualifier.class)) {
                for (ASTLayoutQualifierItem item : directChildren(qualifier, ASTLayoutQualifierItem.class)) {
                    GLSLResourceParser.LayoutQualifierValue layout = (GLSLResourceParser.LayoutQualifierValue) item.jjtGetValue();
                    layouts.add(new ShaderMeta.Layout(layout.name(), layout.expression()));
                }
            }
            return layouts.build();
        }

        @NonNull
        private static ImmutableList<String> qualifiers(@NonNull Node node) {
            ImmutableList.Builder<String> qualifiers = ImmutableList.builder();
            for (ASTQualifier qualifier : directChildren(node, ASTQualifier.class)) {
                qualifiers.add(value(qualifier));
            }
            return qualifiers.build();
        }

        @NonNull
        private static ImmutableList<ShaderMeta.Declarator> declarators(@NonNull ASTDeclaratorList node) {
            ImmutableList.Builder<ShaderMeta.Declarator> declarators = ImmutableList.builder();
            for (ASTDeclarator declarator : directChildren(node, ASTDeclarator.class)) {
                declarators.add(declarator(declarator));
            }
            return declarators.build();
        }

        private static ShaderMeta.@NonNull Declarator declarator(@NonNull SimpleNode node) {
            ASTInitializer initializer = directChild(node, ASTInitializer.class);
            return new ShaderMeta.Declarator(
                    value(node),
                    arrayDimensions(node),
                    initializer == null ? null : value(initializer));
        }

        @NonNull
        private static ImmutableList<ShaderMeta.ArrayDimension> arrayDimensions(@NonNull Node node) {
            ASTArraySpecifier specifier = directChild(node, ASTArraySpecifier.class);
            if (specifier == null) {
                return ImmutableList.of();
            }

            ImmutableList.Builder<ShaderMeta.ArrayDimension> dimensions = ImmutableList.builder();
            for (ASTArrayDimension dimension : directChildren(specifier, ASTArrayDimension.class)) {
                dimensions.add(new ShaderMeta.ArrayDimension(nullableValue(dimension)));
            }
            return dimensions.build();
        }

        @NonNull
        private static <T extends Node> T requireDirectChild(@NonNull Node node, @NonNull Class<T> type) {
            return Preconditions.checkNotNull(directChild(node, type),
                    "Missing \"%s\" child under \"%s\".", type.getSimpleName(), node.getClass().getSimpleName());
        }

        private static ShaderMeta.@NonNull StructDeclaration structDeclaration(
                @NonNull ASTStructDeclaration node) {

            ASTStructSpecifier specifier = requireDirectChild(node, ASTStructSpecifier.class);
            ASTDeclaratorList declaratorList = directChild(node, ASTDeclaratorList.class);
            return structDeclaration(specifier, declaratorList == null ? ImmutableList.of() : declarators(declaratorList));
        }

        private static ShaderMeta.@NonNull StructDeclaration structDeclaration(
                @NonNull ASTStructSpecifier node,
                @NonNull ImmutableList<ShaderMeta.Declarator> declarators) {

            ASTStructName name = directChild(node, ASTStructName.class);
            ImmutableList.Builder<ShaderMeta.Member> members = ImmutableList.builder();
            for (ASTMemberDeclaration member : directChildren(node, ASTMemberDeclaration.class)) {
                members.add(member(member));
            }
            return new ShaderMeta.StructDeclaration(name == null ? null : value(name), members.build(), declarators);
        }

        private static ShaderMeta.@NonNull UniformDeclaration uniformDeclaration(
                @NonNull ASTUniformDeclaration node) {

            return new ShaderMeta.UniformDeclaration(
                    type(requireDirectChild(node, ASTTypeSpecifier.class)),
                    layouts(node),
                    qualifiers(node),
                    declarators(requireDirectChild(node, ASTDeclaratorList.class)));
        }

        private static ShaderMeta.@NonNull InterfaceBlock interfaceBlock(
                @NonNull ASTInterfaceBlockDeclaration node) {

            ImmutableList.Builder<ShaderMeta.Member> members = ImmutableList.builder();
            for (ASTMemberDeclaration member : directChildren(node, ASTMemberDeclaration.class)) {
                members.add(member(member));
            }

            ASTInstanceDeclarator instance = directChild(node, ASTInstanceDeclarator.class);
            return new ShaderMeta.InterfaceBlock(
                    value(requireDirectChild(node, ASTStorageQualifier.class)),
                    value(requireDirectChild(node, ASTBlockName.class)),
                    layouts(node),
                    qualifiers(node),
                    members.build(),
                    instance == null ? null : declarator(instance));
        }
    }

    @NonNull
    private static ASTTranslationUnit parse(@NonNull String source) throws ParseException {
        Preconditions.checkNotNull(source);

        return new GLSLResourceParser(new StringReader(source)).TranslationUnit();
    }

    @Nullable
    @Override
    public ShaderMeta analyze(@NonNull String shaderSource) {
        final ASTTranslationUnit astUnit;
        try {
            astUnit = parse(shaderSource);
        } catch (ParseException e) {
            return null;
        }

        return (new ShaderMetaBuilder(astUnit)).build();
    }
}
