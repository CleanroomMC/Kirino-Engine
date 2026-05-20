package com.cleanroommc.kirino.simpletext.atlas;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.simpletext.sdf.SDFBitmap;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public class GlyphAtlas extends AbstractPagedAtlas<Texture2DAccessor, SDFBitmap> {

    public GlyphAtlas(int pageWidth, int pageHeight) {
        super(() -> new Texture2DAccessor(true, GLTexture.newDsaTex2D(pageWidth, pageHeight)),
                pageWidth, pageHeight);
    }

    @Override
    void initPage(@NonNull Texture2DAccessor page, int width, int height) {
        Preconditions.checkNotNull(page);

        page.highlevel().allocEmpty(false, TextureFormat.R8_UNORM);
    }

    @Override
    void uploadSection(@NonNull SlotHandle<Texture2DAccessor> slot, @NonNull SDFBitmap bitmap) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(bitmap);
        Preconditions.checkArgument(slot.getWidth() == bitmap.width(),
                "Slot width=%s must match bitmap width=%s.", slot.getWidth(), bitmap.width());
        Preconditions.checkArgument(slot.getHeight() == bitmap.height(),
                "Slot height=%s must match bitmap height=%s.", slot.getHeight(), bitmap.height());

        slot.getPage().texSubImage2D(
                0,
                slot.getX(),
                slot.getY(),
                slot.getWidth(),
                slot.getHeight(),
                TextureFormat.R8_UNORM.format,
                TextureFormat.R8_UNORM.type,
                bitmap.byteBuffer());
    }
}
