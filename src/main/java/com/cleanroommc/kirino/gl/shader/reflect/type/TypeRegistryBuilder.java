package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.reflect.ConstantExpressions;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.Precision;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class TypeRegistryBuilder {

    final Map<String, GlslType> types;
    final Map<ShaderMeta.Type, GlslType> resolvedTypes = new IdentityHashMap<>();
    final Map<ShaderMeta.StructDeclaration, StructType> structDefinitions = new IdentityHashMap<>();
    final Map<ShaderMeta.Parameter, Parameter> resolvedParameters = new IdentityHashMap<>();
    private final Map<String, Precision> precisions = new LinkedHashMap<>();
    private final ConstantExpressions expressions;

    TypeRegistryBuilder(Map<String, GlslType> types, ConstantExpressions expressions) {
        this.types = new LinkedHashMap<>(types);
        this.expressions = expressions;
    }

    void collect(ShaderMeta meta) {
        for (ShaderMeta.Declaration declaration : meta.topLevelDeclarations) {
            switch (declaration) {
                case ShaderMeta.StructDeclaration struct -> struct(struct, types);
                case ShaderMeta.UniformDeclaration uniform -> resolve(uniform.type());
                case ShaderMeta.InputOutputDeclaration variable -> resolve(variable.type());
                case ShaderMeta.VariableDeclaration variable -> resolve(variable.type());
                case ShaderMeta.InterfaceBlock block -> members(block.members(), new LinkedHashMap<>(types));
                case ShaderMeta.SubroutineTypeDeclaration subroutine -> {
                    GlslType returnType = resolve(subroutine.returnType());
                    ImmutableList<Parameter> parameters = parameters(subroutine.parameters());
                    register(types, subroutine.name(), new SubroutineType(subroutine.name(), returnType,
                            QualifierSet.resolve(subroutine.qualifiers(), returnType, precisions), parameters));
                }
                case ShaderMeta.SubroutineFunctionDeclaration function -> {
                    resolve(function.returnType());
                    parameters(function.parameters());
                }
                case ShaderMeta.PrecisionDeclaration precision -> {
                    resolve(precision.type());
                    precisions.put(precision.type().name(), Precision.valueOf(precision.precision().toUpperCase(java.util.Locale.ROOT)));
                }
                case ShaderMeta.GlobalLayoutDeclaration ignored -> {
                }
                case ShaderMeta.QualifierDeclaration ignored -> {
                }
            }
        }
    }

    GlslType resolve(ShaderMeta.Type type) {
        return resolve(type, types);
    }

    private GlslType resolve(ShaderMeta.Type type, Map<String, GlslType> scope) {
        GlslType cached = resolvedTypes.get(type);
        if (cached != null) {
            return cached;
        }

        GlslType base;
        if (type.inlineStruct() != null) {
            base = struct(type.inlineStruct(), scope);
        } else {
            String name = Preconditions.checkNotNull(type.name(), "A type reference must have a name.");
            base = scope.get(name);
            if (base == null) {
                base = new UnresolvedType(name);
            }
        }
        GlslType resolved = array(base, type.arrayDimensions());
        resolvedTypes.put(type, resolved);
        return resolved;
    }

    private StructType struct(ShaderMeta.StructDeclaration declaration, Map<String, GlslType> scope) {
        StructType struct = new StructType(declaration.name(), members(declaration.members(), new LinkedHashMap<>(scope)));
        structDefinitions.put(declaration, struct);
        if (declaration.name() != null) {
            register(scope, declaration.name(), struct);
        }
        return struct;
    }

    private ImmutableList<StructMember> members(List<ShaderMeta.Member> declarations, Map<String, GlslType> scope) {
        ImmutableList.Builder<StructMember> members = ImmutableList.builder();
        for (ShaderMeta.Member declaration : declarations) {
            GlslType base = resolve(declaration.type(), scope);
            QualifierSet qualifiers = QualifierSet.resolve(declaration.qualifiers(), base, precisions);
            for (ShaderMeta.Declarator declarator : declaration.declarators()) {
                members.add(new StructMember(declarator.name(), array(base, declarator.arrayDimensions()), qualifiers, declarator.span()));
            }
        }
        return members.build();
    }

    private ImmutableList<Parameter> parameters(List<ShaderMeta.Parameter> declarations) {
        ImmutableList.Builder<Parameter> parameters = ImmutableList.builder();
        Map<String, GlslType> scope = new LinkedHashMap<>(types);
        for (ShaderMeta.Parameter parameter : declarations) {
            ParameterDirection direction = ParameterDirection.IN;
            if (parameter.qualifiers().contains("out")) {
                direction = ParameterDirection.OUT;
            } else if (parameter.qualifiers().contains("inout")) {
                direction = ParameterDirection.INOUT;
            }
            GlslType type = array(resolve(parameter.type(), scope), parameter.arrayDimensions());
            Parameter resolved = new Parameter(parameter.name(), type, direction, parameter.qualifiers().contains("const"), QualifierSet.resolve(parameter.qualifiers(), type, precisions));
            resolvedParameters.put(parameter, resolved);
            parameters.add(resolved);
        }
        return parameters.build();
    }

    private static void register(Map<String, GlslType> scope, String name, GlslType type) {
        Preconditions.checkArgument(!scope.containsKey(name), "Type \"%s\" is already declared.", name);

        scope.put(name, type);
    }

    private GlslType array(GlslType base, List<ShaderMeta.ArrayDimension> dimensions) {
        return array(base, dimensions, expressions);
    }

    static GlslType array(GlslType base, List<ShaderMeta.ArrayDimension> dimensions, ConstantExpressions expressions) {
        GlslType type = base;
        for (int i = dimensions.size() - 1; i >= 0; i--) {
            String expression = dimensions.get(i).expression();
            ArrayExtent extent = expression == null ? ArrayExtent.UNSIZED : ArrayExtent.sized(expressions.expression(expression));
            type = new ArrayType(type, extent);
        }
        return type;
    }
}
