package com.cleanroommc.kirino.simpletext.freetype;

import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.*;

/**
 * <p>Note: Do not free the method result, {@link FT_Bitmap}, manually. You're not responsible for closing it.
 * Make sure that you consume the method result before the next load call too (returned {@link FT_Bitmap} is
 * not a persistent object).</p>
 */
public final class FreeTypeBitmapLoader {

    private FreeTypeBitmapLoader() {
    }

    /**
     * Load bitmap from char.
     *
     * <p>Note: A <code>null</code> return value marks the failure of this call. You can
     * expect unmodified <code>outMetrics</code> too if it was non-null.</p>
     * <p>Note: returned {@link FT_Bitmap} is valid until the next load call. No manual close!</p>
     * <p>Note: It doesn't put SDF parameters to <code>outMetrics</code>.</p>
     *
     * @param outMetrics It will receive glyph metrics if and only if it's non-null
     */
    @Nullable
    public static FT_Bitmap load(
            @NonNull FT_Face face,
            char c,
            int loadFlags,
            @Nullable GlyphMetrics outMetrics) {

        Preconditions.checkNotNull(face);
        if (outMetrics != null) {
            Preconditions.checkArgument(outMetrics.isEmpty(),
                    "Argument \"metrics\" must be empty if it's non-null.");
        }

        return load(face, (int)c, loadFlags, outMetrics);
    }

    /**
     * Load bitmap from Unicode codepoint.
     *
     * <p>Note: A <code>null</code> return value marks the failure of this call. You can
     * expect unmodified <code>outMetrics</code> too if it was non-null.</p>
     * <p>Note: returned {@link FT_Bitmap} is valid until the next load call. No manual close!</p>
     * <p>Note: It doesn't put SDF parameters to <code>outMetrics</code>.</p>
     *
     * @param outMetrics It will receive glyph metrics if and only if it's non-null
     */
    @Nullable
    public static FT_Bitmap load(
            @NonNull FT_Face face,
            int codepoint,
            int loadFlags,
            @Nullable GlyphMetrics outMetrics) {

        Preconditions.checkNotNull(face);
        if (outMetrics != null) {
            Preconditions.checkArgument(outMetrics.isEmpty(),
                    "Argument \"metrics\" must be empty if it's non-null.");
        }

        int glyphIndex = getGlyphIndex(face, codepoint);
        if (glyphIndex == 0) {
            return null;
        }

        return loadGlyph(face, glyphIndex, loadFlags, outMetrics);
    }

    /**
     * Load bitmap directly from glyph index.
     *
     * <p>Note: A <code>null</code> return value marks the failure of this call. You can
     * expect unmodified <code>outMetrics</code> too if it was non-null.</p>
     * <p>Note: returned {@link FT_Bitmap} is valid until the next load call. No manual close!</p>
     * <p>Note: It doesn't put SDF parameters to <code>outMetrics</code>.</p>
     *
     * @param outMetrics It will receive glyph metrics if and only if it's non-null
     */
    @SuppressWarnings("resource")
    @Nullable
    public static FT_Bitmap loadGlyph(
            @NonNull FT_Face face,
            int glyphIndex,
            int loadFlags,
            @Nullable GlyphMetrics outMetrics) {

        Preconditions.checkNotNull(face);
        if (outMetrics != null) {
            Preconditions.checkArgument(outMetrics.isEmpty(),
                    "Argument \"metrics\" must be empty if it's non-null.");
        }

        int error = FreeType.FT_Load_Glyph(face, glyphIndex, loadFlags);

        if (error != FreeType.FT_Err_Ok) {
            return null;
        }

        FT_GlyphSlot glyph = face.glyph();

        if (glyph == null) {
            return null;
        }

        if (outMetrics != null) {
            // no need to close
            FT_Vector vector = glyph.advance();
            float advanceX = vector.x() / 64f;

            // no need to close
            FT_Bitmap bitmap = glyph.bitmap();
            int width = bitmap.width();
            int height = bitmap.rows();

            outMetrics.set(
                    advanceX,
                    glyph.bitmap_left(),
                    glyph.bitmap_top(),
                    width,
                    height);
        }

        return glyph.bitmap();
    }

    /**
     * Missing glyph returns <code>0</code>.
     */
    public static int getGlyphIndex(@NonNull FT_Face face, int codepoint) {
        Preconditions.checkNotNull(face);

        return FreeType.FT_Get_Char_Index(face, codepoint);
    }
}
