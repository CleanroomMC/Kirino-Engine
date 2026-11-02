package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalLong;

public record ArrayExtent(@Nullable IntegerExpression size, boolean runtimeSized) {

    public static final ArrayExtent UNSIZED = new ArrayExtent(null, false);
    public static final ArrayExtent RUNTIME_SIZED = new ArrayExtent(null, true);

    public ArrayExtent {
        Preconditions.checkArgument(!runtimeSized || size == null,
                "A runtime-sized array must not have a fixed size expression.");
        if (size != null) {
            Preconditions.checkNotNull(size.expression());
            Preconditions.checkArgument(!size.expression().isBlank(), "An array size expression must not be blank.");
            Preconditions.checkArgument(size.value() == null || size.value() > 0,
                    "Array size \"%s\" must be positive.", size.value());
        }
    }

    @NonNull
    public static ArrayExtent sized(@NonNull IntegerExpression size) {
        Preconditions.checkNotNull(size);

        return new ArrayExtent(size, false);
    }

    @NonNull
    public OptionalLong length() {
        return size == null || size.value() == null ? OptionalLong.empty() : OptionalLong.of(size.value());
    }
}
