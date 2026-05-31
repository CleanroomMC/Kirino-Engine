package com.cleanroommc.kirino.ui.simpletext.atlas;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;

public class Tex2DGlyphAtlas extends AbstractPagedAtlas<Texture2DAccessor, SDFBitmap> {

    public Tex2DGlyphAtlas(int pageWidth, int pageHeight) {
        super(() -> new Texture2DAccessor(true, GLTexture.newDsaTex2D(pageWidth, pageHeight)),
                pageWidth, pageHeight);
    }

    @Override
    void initPage(@NonNull Texture2DAccessor page, int width, int height) {
        Preconditions.checkNotNull(page);

        page.highlevel().allocEmpty(false, TextureFormat.R8_UNORM);
        page.clearTexImage(0, TextureFormat.R8_UNORM.format, TextureFormat.R8_UNORM.type, null);
        page.setCommonParams(FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
    }

    @Override
    void uploadSection(@NonNull SlotHandle<Texture2DAccessor> slot, @NonNull SDFBitmap bitmap) {
        Preconditions.checkNotNull(slot);
        Preconditions.checkNotNull(bitmap);
        Preconditions.checkArgument(slot.getWidth() == bitmap.width(),
                "Slot width=%s must match bitmap width=%s.", slot.getWidth(), bitmap.width());
        Preconditions.checkArgument(slot.getHeight() == bitmap.height(),
                "Slot height=%s must match bitmap height=%s.", slot.getHeight(), bitmap.height());

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        slot.getPage().texSubImage2D(
                0,
                slot.getX(),
                slot.getY(),
                slot.getWidth(),
                slot.getHeight(),
                TextureFormat.R8_UNORM.format,
                TextureFormat.R8_UNORM.type,
                bitmap.byteBuffer());
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
    }
}
