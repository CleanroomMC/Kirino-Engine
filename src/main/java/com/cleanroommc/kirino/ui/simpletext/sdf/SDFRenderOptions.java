package com.cleanroommc.kirino.ui.simpletext.sdf;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * @param smoothingScale Antialiasing width multiplier: 1 preserves the original width,
 *                       smaller values sharpen edges, larger values soften them
 */
public record SDFRenderOptions(@NonNull SDFEdgeMode edgeMode, float smoothingScale) {

    public static final SDFRenderOptions DEFAULT = new SDFRenderOptions(SDFEdgeMode.SMOOTH, 1.0f);

    public SDFRenderOptions {
        Preconditions.checkNotNull(edgeMode);
        Preconditions.checkArgument(Float.isFinite(smoothingScale) && smoothingScale > 0,
                "Argument \"smoothingScale\" must be finite and positive.");
    }
}
