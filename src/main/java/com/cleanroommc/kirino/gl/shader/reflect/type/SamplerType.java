package com.cleanroommc.kirino.gl.shader.reflect.type;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record SamplerType(
        @NonNull ScalarType sampledType,
        @NonNull TextureDimension dimension,
        boolean arrayed,
        boolean multisample,
        boolean shadow) implements GlslType {

    public SamplerType {
        Preconditions.checkNotNull(sampledType);
        Preconditions.checkNotNull(dimension);
        Preconditions.checkArgument(sampledType == ScalarType.FLOAT
                        || sampledType == ScalarType.INT
                        || sampledType == ScalarType.UINT,
                "Sampler value type \"%s\" must be float, int, or uint.", sampledType);
        dimension.validate(arrayed, multisample);
        Preconditions.checkArgument(!shadow || sampledType == ScalarType.FLOAT && !multisample
                        && dimension != TextureDimension.D3 && dimension != TextureDimension.BUFFER,
                "Shadow sampling is not supported for this sampler configuration.");
    }
}
