package com.cleanroommc.kirino.gl.shader.reflect.execution;

import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record GeometryExecution(
        @Nullable GeometryInput input,
        @NonNull IntegerExpression invocations,
        @Nullable GeometryOutput output,
        @Nullable IntegerExpression maxVertices) implements ShaderExecution {

    public GeometryExecution {
        Preconditions.checkNotNull(invocations);
        Preconditions.checkArgument(invocations.value() == null || invocations.value() > 0,
                "Geometry invocation count must be positive.");
        Preconditions.checkArgument(maxVertices == null || maxVertices.value() == null || maxVertices.value() >= 0,
                "Geometry maximum vertex count must be non-negative.");
    }
}
