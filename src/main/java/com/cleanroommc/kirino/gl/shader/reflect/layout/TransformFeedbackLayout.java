package com.cleanroommc.kirino.gl.shader.reflect.layout;

import com.cleanroommc.kirino.gl.shader.reflect.IntegerExpression;
import org.jspecify.annotations.Nullable;

public record TransformFeedbackLayout(
        @Nullable IntegerExpression buffer,
        @Nullable IntegerExpression offset,
        @Nullable IntegerExpression stride,
        @Nullable IntegerExpression stream) {

    public static final TransformFeedbackLayout EMPTY = new TransformFeedbackLayout(null, null, null, null);
}
