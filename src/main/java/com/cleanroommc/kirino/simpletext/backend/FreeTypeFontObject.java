package com.cleanroommc.kirino.simpletext.backend;

import com.cleanroommc.kirino.simpletext.ST_Bitmap;
import com.cleanroommc.kirino.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.simpletext.ST_FontObject;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeAlphaBitmap;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapDecoder;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapLoader;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

public class FreeTypeFontObject implements ST_FontObject {

    private final FT_Face face;
    private final boolean hasKerning;

    public FreeTypeFontObject(FT_Face face) {
        this.face = face;
        hasKerning = FreeType.FT_HAS_KERNING(face);
    }

    @NonNull
    @Override
    public ST_FontBackendType type() {
        return ST_FontBackendType.FREE_TYPE;
    }

    @NonNull
    @Override
    public Object fontObj() {
        return face;
    }

    @Override
    public boolean hasKerning() {
        return hasKerning;
    }

    @Override
    public int getGlyphIndex(int codepoint) {
        return FreeTypeBitmapLoader.getGlyphIndex(face, codepoint);
    }

    @Nullable
    @Override
    public ST_Bitmap loadGlyph(int glyphIndex, int payload, @Nullable GlyphMetrics outMetrics) {
        if (outMetrics == null) {
            FT_Bitmap bitmap = FreeTypeBitmapLoader.loadGlyph(face, glyphIndex, payload, null);
            if (bitmap == null) {
                return null;
            }
            try {
                return FreeTypeBitmapDecoder.decode(bitmap);
            } catch (Throwable t) {
                return null;
            }
        } else {
            GlyphMetrics out = new GlyphMetrics();
            FT_Bitmap bitmap = FreeTypeBitmapLoader.loadGlyph(face, glyphIndex, payload, out);
            if (bitmap == null) {
                return null;
            }
            if (bitmap.buffer(1) == null) {
                outMetrics.set(
                        out.getAdvanceX(),
                        out.getBearingX(),
                        out.getBearingY(),
                        out.getGlyphWidth(),
                        out.getGlyphHeight());
                return ST_Bitmap.EMPTY;
            }
            try {
                FreeTypeAlphaBitmap alphaBitmap = FreeTypeBitmapDecoder.decode(bitmap);
                outMetrics.set(
                        out.getAdvanceX(),
                        out.getBearingX(),
                        out.getBearingY(),
                        out.getGlyphWidth(),
                        out.getGlyphHeight());
                return alphaBitmap;
            } catch (Throwable t) {
                return null;
            }
        }
    }
}
