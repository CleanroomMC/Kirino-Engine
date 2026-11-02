package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.ShaderMeta;
import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.cleanroommc.kirino.gl.shader.reflect.ConstantExpressions;
import com.cleanroommc.kirino.gl.shader.reflect.qualifier.QualifierSet;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

public final class TypeRegistry {

    private final ImmutableMap<String, GlslType> types;
    private final ImmutableMap<String, StructType> structs;
    private final Map<ShaderMeta.Type, GlslType> resolvedTypes;
    private final Map<ShaderMeta.StructDeclaration, StructType> structDefinitions;
    private final Map<ShaderMeta.Parameter, Parameter> parameters;
    private final ConstantExpressions expressions;
    private final ConcurrentMap<GlslType, Long> unitCounts = new ConcurrentHashMap<>();

    public TypeRegistry(@NonNull ShaderMeta shaderMeta, @NonNull ConstantExpressions expressions) {
        Preconditions.checkNotNull(shaderMeta);
        Preconditions.checkNotNull(expressions);

        this.expressions = expressions;
        TypeRegistryBuilder builder = new TypeRegistryBuilder(BuiltinTypes.TYPES, expressions);
        builder.collect(shaderMeta);
        types = ImmutableMap.copyOf(builder.types);
        resolvedTypes = Collections.unmodifiableMap(new IdentityHashMap<>(builder.resolvedTypes));
        structDefinitions = Collections.unmodifiableMap(new IdentityHashMap<>(builder.structDefinitions));
        parameters = Collections.unmodifiableMap(new IdentityHashMap<>(builder.resolvedParameters));
        ImmutableMap.Builder<String, StructType> structs = ImmutableMap.builder();
        types.forEach((name, type) -> {
            if (type instanceof StructType struct) {
                structs.put(name, struct);
            }
        });
        this.structs = structs.buildOrThrow();
    }

    @NonNull
    public ImmutableMap<String, GlslType> types() {
        return types;
    }

    @NonNull
    public Optional<GlslType> type(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(types.get(name));
    }

    @NonNull
    public <T extends GlslType> Optional<T> type(@NonNull String name, @NonNull Class<T> type) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);

        return type(name).flatMap(value -> value.as(type));
    }

    @NonNull
    public ImmutableMap<String, StructType> structs() {
        return structs;
    }

    @NonNull
    public Optional<StructType> struct(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(structs.get(name));
    }

    @NonNull
    public GlslType resolve(ShaderMeta.@NonNull Type type) {
        Preconditions.checkNotNull(type);

        GlslType resolved = resolvedTypes.get(type);
        return resolved != null ? resolved : new TypeRegistryBuilder(types, expressions).resolve(type);
    }

    @NonNull
    public GlslType resolve(ShaderMeta.@NonNull Type type, ShaderMeta.@NonNull Declarator declarator) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(declarator);

        return inferArray(TypeRegistryBuilder.array(resolve(type), declarator.arrayDimensions(), expressions), declarator.initializer());
    }

    @NonNull
    public GlslType resolve(ShaderMeta.@NonNull StructDeclaration declaration, ShaderMeta.@NonNull Declarator declarator) {
        Preconditions.checkNotNull(declaration);
        Preconditions.checkNotNull(declarator);
        StructType type = structDefinitions.get(declaration);
        Preconditions.checkArgument(type != null, "The struct declaration does not belong to this registry.");

        GlslType array = TypeRegistryBuilder.array(type, declaration.arrayDimensions(), expressions);
        return inferArray(TypeRegistryBuilder.array(array, declarator.arrayDimensions(), expressions), declarator.initializer());
    }

    @NonNull
    public Parameter resolve(ShaderMeta.@NonNull Parameter parameter) {
        Preconditions.checkNotNull(parameter);

        Parameter resolved = parameters.get(parameter);
        if (resolved != null) {
            return resolved;
        }

        ParameterDirection direction = parameter.qualifiers().contains("inout")
                ? ParameterDirection.INOUT
                : parameter.qualifiers().contains("out")
                        ? ParameterDirection.OUT
                        : ParameterDirection.IN;

        GlslType type = TypeRegistryBuilder.array(resolve(parameter.type()), parameter.arrayDimensions(), expressions);
        return new Parameter(parameter.name(), type, direction, parameter.qualifiers().contains("const"), QualifierSet.resolve(parameter.qualifiers(), type, Map.of()));
    }

    @NonNull
    public ArrayExtent extent(ShaderMeta.@NonNull ArrayDimension dimension) {
        Preconditions.checkNotNull(dimension);

        return dimension.expression() == null ? ArrayExtent.UNSIZED : ArrayExtent.sized(expressions.expression(dimension.expression()));
    }

    @NonNull
    public static Optional<GlslType> builtin(@NonNull String name) {
        Preconditions.checkNotNull(name);

        return Optional.ofNullable(BuiltinTypes.TYPES.get(name));
    }

    @NonNull
    public static Optional<String> builtinName(@NonNull GlslType type) {
        Preconditions.checkNotNull(type);

        return BuiltinTypes.TYPES.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(type))
                .map(Map.Entry::getKey).findFirst();
    }

    private GlslType inferArray(GlslType type, String initializer) {
        if (!(type instanceof ArrayType(GlslType elementType, ArrayExtent extent)) || initializer == null) {
            return type;
        }

        int start = -1;
        int brackets = 0;
        for (int i = 0; i < initializer.length(); i++) {
            char c = initializer.charAt(i);
            if (c == '[') {
                brackets++;
            } else if (c == ']') {
                brackets--;
            } else if (c == '(' && brackets == 0) {
                start = i;
                break;
            }
        }
        char close = ')';
        if (initializer.startsWith("{")) {
            start = 0;
            close = '}';
        }
        if (start < 0
                || !initializer.endsWith(String.valueOf(close))
                || close == ')' && !initializer.substring(0, start).contains("[")) {
            return type;
        }

        List<String> elements = new ArrayList<>();
        int depth = 0;
        int elementStart = start + 1;
        for (int i = elementStart; i < initializer.length() - 1; i++) {
            char c = initializer.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                depth++;
            } else if (c == ')' || c == ']' || c == '}') {
                depth--;
            } else if (c == ',' && depth == 0) {
                elements.add(initializer.substring(elementStart, i).strip());
                elementStart = i + 1;
            }
        }
        String last = initializer.substring(elementStart, initializer.length() - 1).strip();
        if (!last.isEmpty()) {
            elements.add(last);
        }
        if (elements.isEmpty()) {
            return type;
        }
        if (extent.size() == null && !extent.runtimeSized()) {
            long count = elements.size();
            extent = ArrayExtent.sized(new IntegerExpression(Long.toString(count), count));
        }
        GlslType element = inferArray(elementType, elements.getFirst());
        return new ArrayType(element, extent);
    }

    /**
     * Accepts field chains such as <code>"lights[1]", "position", "x"</code> or <code>"lights[1].position.x"</code>.
     * An empty chain selects the root type. Missing fields and out-of-range indices return an empty result.
     */
    @NonNull
    public Optional<GlslType> fieldType(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        Preconditions.checkNotNull(name);
        List<String> fields = TypePaths.parse(fieldAccessChain);

        return type(name).flatMap(type -> fieldType(type, fields));
    }

    @NonNull
    public Optional<GlslType> fieldType(@NonNull GlslType type, @NonNull String @NonNull ... fieldAccessChain) {
        Preconditions.checkNotNull(type);

        return fieldType(type, TypePaths.parse(fieldAccessChain));
    }

    private Optional<GlslType> fieldType(GlslType type, List<String> fields) {
        GlslType current = type;
        for (String field : fields) {
            current = TypePaths.child(current, field);
            if (current == null) {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    /**
     * Counts logical leaf values, not bytes or buffer padding. Vectors flatten to components and matrices
     * to columns, then components. Opaque values count as one. Unknown array lengths and unresolved types fail.
     */
    public long flattenedUnitCount(@NonNull String name) {
        return flattenedUnitCount(requireType(name));
    }

    public long flattenedUnitCount(@NonNull GlslType type) {
        Preconditions.checkNotNull(type);

        Long cached = unitCounts.get(type);
        if (cached != null) {
            return cached;
        }

        long count = TypePaths.unitCount(type, this::flattenedUnitCount);
        unitCounts.putIfAbsent(type, count);
        return count;
    }

    /**
     * Returns the logical leaf ordinal, using the same ordering as {@link #traverse(String, BiConsumer)}.
     * The chain must reach a single leaf. This is not a byte offset.
     */
    public long getFieldOrdinal(@NonNull String name, @NonNull String @NonNull ... fieldAccessChain) {
        return getFieldOrdinal(requireType(name), fieldAccessChain);
    }

    public long getFieldOrdinal(@NonNull GlslType type, @NonNull String @NonNull ... fieldAccessChain) {
        Preconditions.checkNotNull(type);

        return TypePaths.ordinal(type, TypePaths.parse(fieldAccessChain), this::flattenedUnitCount);
    }

    /**
     * Visits logical leaves in declaration order, with paths such as <code>lights[0].position.x</code>.
     * Numeric arrays use ascending indices; matrices use column indices. A scalar root has the empty path.
     */
    public void traverse(@NonNull String name, @NonNull BiConsumer<String, GlslType> visitor) {
        traverse(requireType(name), visitor);
    }

    public void traverse(@NonNull GlslType type, @NonNull BiConsumer<String, GlslType> visitor) {
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(visitor);

        flattenedUnitCount(type);
        TypePaths.traverse(type, "", visitor);
    }

    private GlslType requireType(String name) {
        Preconditions.checkNotNull(name);
        GlslType type = types.get(name);
        Preconditions.checkArgument(type != null, "Unknown type \"%s\".", name);

        return type;
    }
}
