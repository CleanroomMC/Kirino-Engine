package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TypePaths {

    private static final Pattern FIELD = Pattern.compile("([a-zA-Z_][a-zA-Z_0-9]*|[0-9]+)|\\[([0-9]+)]");
    private static final List<String> COMPONENT_NAMES = List.of("xyzw", "rgba", "stpq");

    private TypePaths() {
    }

    static List<String> parse(String[] chain) {
        Preconditions.checkNotNull(chain);

        List<String> fields = new ArrayList<>();
        for (String part : chain) {
            Preconditions.checkNotNull(part);
            Preconditions.checkArgument(!part.isEmpty(),
                    "A field access chain term must not be empty.");

            Matcher matcher = FIELD.matcher(part);
            int end = 0;
            while (matcher.find()) {
                int expected = end;
                if (end > 0 && end < part.length() && part.charAt(end) == '.') {
                    expected++;
                    Preconditions.checkArgument(matcher.group(1) != null,
                            "Invalid field access chain term \"%s\".", part);
                } else if (end > 0) {
                    Preconditions.checkArgument(matcher.group(2) != null,
                            "Invalid field access chain term \"%s\".", part);
                }
                Preconditions.checkArgument(matcher.start() == expected,
                        "Invalid field access chain term \"%s\".", part);

                fields.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
                end = matcher.end();
            }
            Preconditions.checkArgument(end == part.length(), "Invalid field access chain term \"%s\".", part);
        }
        return fields;
    }

    private static long requiredLength(ArrayExtent extent) {
        OptionalLong length = extent.length();
        Preconditions.checkState(length.isPresent(),
                "Array length is not known: \"%s\".", extent);

        return length.getAsLong();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static long validIndex(String field, OptionalLong length) {
        try {
            long index = Long.parseLong(field);
            return index >= 0 && (length.isEmpty() || index < length.getAsLong()) ? index : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int component(String field, int components) {
        if (field.length() == 1) {
            for (String names : COMPONENT_NAMES) {
                int index = names.indexOf(field.charAt(0));
                if (index >= 0 && index < components) {
                    return index;
                }
            }
        }
        return (int) validIndex(field, OptionalLong.of(components));
    }

    @Nullable
    static GlslType child(GlslType type, String field) {
        return switch (type) {
            case StructType struct -> struct.member(field).map(StructMember::type).orElse(null);
            case ArrayType array -> validIndex(field, array.extent().length()) >= 0 ? array.elementType() : null;
            case MatrixType matrix -> validIndex(field, OptionalLong.of(matrix.columns())) >= 0 ? new VectorType(matrix.componentType(), matrix.rows()) : null;
            case VectorType vector -> component(field, vector.components()) >= 0 ? vector.componentType() : null;
            default -> null;
        };
    }

    static long unitCount(GlslType type, ToLongFunction<GlslType> count) {
        return switch (type) {
            case VoidType ignored -> 0;
            case VectorType vector -> vector.components();
            case MatrixType matrix -> Math.multiplyExact((long) matrix.columns(), matrix.rows());
            case ArrayType array -> Math.multiplyExact(requiredLength(array.extent()), count.applyAsLong(array.elementType()));
            case StructType struct -> {
                long result = 0;
                for (StructMember member : struct.members()) {
                    result = Math.addExact(result, count.applyAsLong(member.type()));
                }
                yield result;
            }
            case UnresolvedType unresolved -> throw new IllegalStateException(
                    "Cannot flatten unresolved type \"" + unresolved.spelling() + "\".");
            default -> 1;
        };
    }

    static long ordinal(GlslType root, List<String> fields, ToLongFunction<GlslType> count) {
        long ordinal = 0;
        GlslType current = root;
        for (String field : fields) {
            GlslType child = child(current, field);
            Preconditions.checkArgument(child != null,
                    "Cannot select field \"%s\" from \"%s\".", field, current);

            long offset = switch (current) {
                case StructType struct -> {
                    long preceding = 0;
                    for (StructMember member : struct.members()) {
                        if (member.name().equals(field)) {
                            break;
                        }
                        preceding = Math.addExact(preceding, count.applyAsLong(member.type()));
                    }
                    yield preceding;
                }
                case ArrayType array -> Math.multiplyExact(validIndex(field, array.extent().length()), count.applyAsLong(array.elementType()));
                case MatrixType matrix -> Math.multiplyExact(validIndex(field, OptionalLong.of(matrix.columns())), matrix.rows());
                case VectorType vector -> component(field, vector.components());
                default -> throw new IllegalArgumentException("The field access chain extends past a leaf.");
            };
            ordinal = Math.addExact(ordinal, offset);
            current = child;
        }
        Preconditions.checkArgument(!(current instanceof StructType
                        || current instanceof ArrayType
                        || current instanceof VectorType
                        || current instanceof MatrixType
                        || current instanceof VoidType),
                "The field access chain must reach a single leaf.");

        count.applyAsLong(current);
        return ordinal;
    }

    static void traverse(GlslType type, String path, BiConsumer<String, GlslType> visitor) {
        switch (type) {
            case StructType struct -> {
                for (StructMember member : struct.members()) {
                    traverse(member.type(), path.isEmpty() ? member.name() : path + "." + member.name(), visitor);
                }
            }
            case ArrayType array -> {
                long length = requiredLength(array.extent());
                for (long i = 0; i < length; i++) {
                    traverse(array.elementType(), path + "[" + i + "]", visitor);
                }
            }
            case MatrixType matrix -> {
                VectorType columnType = new VectorType(matrix.componentType(), matrix.rows());
                for (int column = 0; column < matrix.columns(); column++) {
                    traverse(columnType, path + "[" + column + "]", visitor);
                }
            }
            case VectorType vector -> {
                for (int i = 0; i < vector.components(); i++) {
                    visitor.accept(path.isEmpty() ? "xyzw".substring(i, i + 1) : path + "." + "xyzw".charAt(i), vector.componentType());
                }
            }
            case VoidType ignored -> {
            }
            default -> visitor.accept(path, type);
        }
    }
}
