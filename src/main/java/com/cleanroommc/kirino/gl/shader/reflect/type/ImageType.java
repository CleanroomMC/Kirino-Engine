package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record ImageType(
        @NonNull ScalarType sampledType,
        @NonNull TextureDimension dimension,
        boolean arrayed,
        boolean multisample) implements GlslType {

    public ImageType {
        Preconditions.checkNotNull(sampledType);
        Preconditions.checkNotNull(dimension);
        Preconditions.checkArgument(sampledType == ScalarType.FLOAT
                        || sampledType == ScalarType.INT
                        || sampledType == ScalarType.UINT,
                "Image value type \"%s\" must be float, int, or uint.", sampledType);
        dimension.validate(arrayed, multisample);
    }
}
