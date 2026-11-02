package com.cleanroommc.kirino.gl.texture.atlas;

import com.cleanroommc.kirino.gl.texture.atlas.AbstractPagedPlanarTextureAtlas.TextureData;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public interface PayloadInterpreter {

    int width(@NonNull Object payload);
    int height(@NonNull Object payload);

    @NonNull
    default ByteBuffer pixels(@NonNull TextureData data) {
        Preconditions.checkNotNull(data);

        return data.byteBuffer();
    }
}
