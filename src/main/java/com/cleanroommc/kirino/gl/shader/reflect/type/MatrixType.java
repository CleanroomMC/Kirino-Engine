package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record MatrixType(@NonNull ScalarType componentType, int columns, int rows) implements GlslType {

    public MatrixType {
        Preconditions.checkNotNull(componentType);
        Preconditions.checkArgument(componentType == ScalarType.FLOAT || componentType == ScalarType.DOUBLE,
                "Matrix component type \"%s\" must be float or double.", componentType);
        Preconditions.checkArgument(columns >= 2 && columns <= 4 && rows >= 2 && rows <= 4,
                "Matrix dimensions \"%s x %s\" must each be between 2 and 4.", columns, rows);
    }
}
