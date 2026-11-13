package com.cleanroommc.kirino.ui.transform;

import com.google.common.base.Preconditions;
import org.joml.Matrix4fc;
import org.jspecify.annotations.NonNull;

final class UiTransformChecks {

    private UiTransformChecks() {
    }

    static void positive(float value, @NonNull String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(Float.isFinite(value) && value > 0,
                "\"%s\" must be finite and positive.", name);
    }

    static void position(double x, double y, double z) {
        Preconditions.checkArgument(Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z),
                "Position must be finite.");
    }

    static void matrix(@NonNull Matrix4fc matrix) {
        Preconditions.checkNotNull(matrix);

        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                Preconditions.checkArgument(Float.isFinite(matrix.get(column, row)),
                        "Matrix must be finite.");
            }
        }
    }
}
