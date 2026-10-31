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
}
