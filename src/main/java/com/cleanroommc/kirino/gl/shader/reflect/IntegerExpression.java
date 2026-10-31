package com.cleanroommc.kirino.gl.shader.reflect;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.OptionalLong;

public record IntegerExpression(@NonNull String expression, @Nullable Long value) {

    public IntegerExpression {
        Preconditions.checkNotNull(expression);
        Preconditions.checkArgument(!expression.isBlank(), "An expression must not be blank.");
    }

    @NonNull
    public OptionalLong resolved() {
        return value == null ? OptionalLong.empty() : OptionalLong.of(value);
    }

    public long requireValue() {
        Preconditions.checkState(value != null, "Expression \"%s\" is unresolved.", expression);

        return value;
    }
}
