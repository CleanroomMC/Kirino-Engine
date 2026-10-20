package com.cleanroommc.kirino.gl.texture.atlas;

import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

/**
 * RGBA8_UNORM, linear filtering, clamp-to-edge wrapping, 4-byte unpack alignment.
 *
 * <p>Note: Expects {@link PixelAtlasPayload} metadata and tightly packed row-major RGBA pixels.</p>
 */
public class Rgba8Unorm2DArrayPixelAtlas extends Tex2DArrayAtlas {

    public Rgba8Unorm2DArrayPixelAtlas(int pageWidth, int pageHeight) {
        super(pageWidth, pageHeight, TextureFormat.RGBA8_UNORM,
                FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE,
                4, PixelAtlasPayload.INTERPRETER);
    }

    public Rgba8Unorm2DArrayPixelAtlas(
            int pageWidth,
            int pageHeight,
            int initialLayerCapacity) {

        super(pageWidth, pageHeight, initialLayerCapacity, TextureFormat.RGBA8_UNORM,
                FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE,
                4, PixelAtlasPayload.INTERPRETER);
    }

    public Rgba8Unorm2DArrayPixelAtlas(
            int pageWidth,
            int pageHeight,
            int initialLayerCapacity,
            boolean powerOfTwoSlots) {

        super(pageWidth, pageHeight, initialLayerCapacity, powerOfTwoSlots, TextureFormat.RGBA8_UNORM,
                FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE,
                4, PixelAtlasPayload.INTERPRETER);
    }

    /**
     * @param pixels Tightly packed row-major RGBA pixels
     */
    @NonNull
    public SlotHandle<LayerPage> allocate(@NonNull ByteBuffer pixels, int width, int height) {
        Preconditions.checkNotNull(pixels);

        return allocate(pixels, new PixelAtlasPayload(width, height));
    }
}
