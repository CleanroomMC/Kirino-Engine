package com.cleanroommc.kirino.gl.shader.reflect.execution;

import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

public record TessControlExecution(@Nullable IntegerExpression outputVertices) implements ShaderExecution {

    public TessControlExecution {
        Preconditions.checkArgument(outputVertices == null
                        || outputVertices.value() == null
                        || outputVertices.value() > 0,
                "Tessellation output vertex count must be positive.");
    }
}
