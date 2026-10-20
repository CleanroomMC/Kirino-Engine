package com.cleanroommc.kirino.gl.texture.atlas;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class Tex2DAtlas extends AbstractPagedPlanarTextureAtlas<Texture2DAccessor> {

    private final TextureFormat format;
    private final FilterMode filterModeMin;
    private final FilterMode filterModeMag;
    private final WrapMode wrapModeS;
    private final WrapMode wrapModeT;
    private final int unpackAlignment;

    public Tex2DAtlas(
            int pageWidth,
            int pageHeight,
            @NonNull TextureFormat format,
            @NonNull FilterMode filterModeMin,
            @NonNull FilterMode filterModeMag,
            @NonNull WrapMode wrapModeS,
            @NonNull WrapMode wrapModeT,
            int unpackAlignment,
            @NonNull PayloadInterpreter payloadInterpreter) {

        this(pageWidth, pageHeight, true, format,
                filterModeMin, filterModeMag, wrapModeS, wrapModeT, unpackAlignment, payloadInterpreter);
    }

    public Tex2DAtlas(
            int pageWidth,
            int pageHeight,
            boolean powerOfTwoSlots,
            @NonNull TextureFormat format,
            @NonNull FilterMode filterModeMin,
            @NonNull FilterMode filterModeMag,
            @NonNull WrapMode wrapModeS,
            @NonNull WrapMode wrapModeT,
            int unpackAlignment,
            @NonNull PayloadInterpreter payloadInterpreter) {

        super(() -> new Texture2DAccessor(true, GLTexture.newDsaTex2D(pageWidth, pageHeight)),
                pageWidth, pageHeight, powerOfTwoSlots, payloadInterpreter);

        Preconditions.checkNotNull(format);
        Preconditions.checkNotNull(filterModeMin);
        Preconditions.checkNotNull(filterModeMag);
        Preconditions.checkNotNull(wrapModeS);
        Preconditions.checkNotNull(wrapModeT);
        Preconditions.checkArgument(unpackAlignment == 1 || unpackAlignment == 2 || unpackAlignment == 4 || unpackAlignment == 8,
                "Argument \"unpackAlignment\"=%s must be 1, 2, 4, or 8.", unpackAlignment);

        this.format = format;
        this.filterModeMin = filterModeMin;
        this.filterModeMag = filterModeMag;
        this.wrapModeS = wrapModeS;
        this.wrapModeT = wrapModeT;
        this.unpackAlignment = unpackAlignment;
    }

    @Override
    public void close() {
        for (int i = 0; i < getPageCount(); i++) {
            getPage(i).texture.disposeManually();
        }
    }

    @Override
    protected void initPage(@NonNull Texture2DAccessor page, int width, int height) {
        Preconditions.checkNotNull(page);

        page.highlevel().allocEmpty(false, format);
        page.highlevel().clearLevel(0);

        page.setCommonParams(filterModeMin, filterModeMag, wrapModeS, wrapModeT);
    }

    @Override
    protected void uploadSection(@NonNull SlotHandle<Texture2DAccessor> slot, @NonNull TextureData data) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(data);

        ByteBuffer pixels = payloadInterpreter.pixels(data);

        Preconditions.checkNotNull(pixels);
        Preconditions.checkArgument(pixels.isDirect(),
                "Interpreted pixels must be stored in a direct buffer.");

        // potential stall; doesn't matter anyway
        int previousUnpackAlignment = GL11.glGetInteger(GL11.GL_UNPACK_ALIGNMENT);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, unpackAlignment);
        try {
            slot.getPage().highlevel().uploadSubImage(
                    0,
                    slot.getX(),
                    slot.getY(),
                    slot.getWidth(),
                    slot.getHeight(),
                    pixels);
        } finally {
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
        }
    }
}
