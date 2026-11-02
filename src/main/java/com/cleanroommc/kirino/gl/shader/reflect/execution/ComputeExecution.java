package com.cleanroommc.kirino.gl.shader.reflect.execution;

import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.OptionalLong;

public record ComputeExecution(
        @NonNull IntegerExpression localSizeX,
        @NonNull IntegerExpression localSizeY,
        @NonNull IntegerExpression localSizeZ) implements ShaderExecution {

    public ComputeExecution {
        Preconditions.checkNotNull(localSizeX);
        Preconditions.checkNotNull(localSizeY);
        Preconditions.checkNotNull(localSizeZ);
        Preconditions.checkArgument((localSizeX.value() == null || localSizeX.value() > 0)
                        && (localSizeY.value() == null || localSizeY.value() > 0)
                        && (localSizeZ.value() == null || localSizeZ.value() > 0),
                "Compute workgroup dimensions must be positive.");
    }

    @NonNull
    public OptionalLong invocationsPerWorkgroup() {
        return localSizeX.value() == null
                || localSizeY.value() == null
                || localSizeZ.value() == null
                        ? OptionalLong.empty()
                        : OptionalLong.of(Math.multiplyExact(Math.multiplyExact(localSizeX.value(), localSizeY.value()), localSizeZ.value()));
    }
}
