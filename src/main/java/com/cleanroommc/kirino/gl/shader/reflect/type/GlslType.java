package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public sealed interface GlslType permits
        VoidType,
        ScalarType,
        VectorType,
        MatrixType,
        ArrayType,
        StructType,
        SamplerType,
        ImageType,
        AtomicCounterType,
        SubroutineType,
        UnresolvedType {

    @NonNull
    default <T extends GlslType> Optional<T> as(@NonNull Class<T> type) {
        Preconditions.checkNotNull(type);

        return type.isInstance(this) ? Optional.of(type.cast(this)) : Optional.empty();
    }

    @NonNull
    default Optional<GlslType> field(@NonNull String @NonNull ... path) {
        GlslType current = this;
        for (String part : TypePaths.parse(path)) {
            current = TypePaths.child(current, part);
            if (current == null) {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    @NonNull
    default GlslType base() {
        GlslType type = this;
        while (type instanceof ArrayType array) {
            type = array.elementType();
        }
        return type;
    }

    static boolean sameType(@NonNull GlslType left, @NonNull GlslType right) {
        Preconditions.checkNotNull(left);
        Preconditions.checkNotNull(right);

        if (left instanceof ArrayType(GlslType elementType, ArrayExtent extent) && right instanceof ArrayType(GlslType elementType1, ArrayExtent extent1)) {
            return sameType(elementType, elementType1)
                    && (extent.length().isEmpty()
                            || extent1.length().isEmpty()
                            || extent.length().getAsLong() == extent1.length().getAsLong());
        }

        return left.equals(right);
    }
}
