package com.cleanroommc.kirino.simpletext.freetype;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

public final class FreeTypeBitmapLoader {

    private FreeTypeBitmapLoader() {
    }

    /**
     * <p>Note: Do not free the result.</p>
     */
    @Nullable
    public static FT_Bitmap load(@NonNull FT_Face face, char c, int loadFlags) {
        Preconditions.checkNotNull(face);

        int error = FreeType.FT_Load_Char(face, c, loadFlags);

        if (error != FreeType.FT_Err_Ok) {
            return null;
        }

        FT_GlyphSlot glyph = face.glyph();
        if (glyph == null) {
            return null;
        }

        return glyph.bitmap();
    }
}
