package com.cleanroommc.kirino.gl.texture.atlas;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public record PixelAtlasPayload(int width, int height) {

    static final PayloadInterpreter INTERPRETER = new PayloadInterpreter() {

        @Override
        public int width(@NonNull Object payload) {
            Preconditions.checkNotNull(payload);
            Preconditions.checkArgument(payload instanceof PixelAtlasPayload,
                    "Argument \"payload\" must be a PixelAtlasPayload.");

            return ((PixelAtlasPayload) payload).width();
        }

        @Override
        public int height(@NonNull Object payload) {
            Preconditions.checkNotNull(payload);
            Preconditions.checkArgument(payload instanceof PixelAtlasPayload,
                    "Argument \"payload\" must be a PixelAtlasPayload.");

            return ((PixelAtlasPayload) payload).height();
        }
    };

    public PixelAtlasPayload {
        Preconditions.checkArgument(width > 0,
                "Argument \"width\"=%s must be positive.", width);
        Preconditions.checkArgument(height > 0,
                "Argument \"height\"=%s must be positive.", height);
    }
}
