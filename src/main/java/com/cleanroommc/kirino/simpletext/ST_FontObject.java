package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;

/**
 * This is an abstract font face representation for SimpleText.
 *
 * <p>Key Assumption: There is only <b>one</b> impl class for each {@link ST_FontBackendType}, so
 * the backend type can therefore act as a protocol.</p>
 */
public interface ST_FontObject {
    @NonNull ST_FontBackendType type();

    /**
     * @implSpec <p>Must follow the specifications:</p>
     *           <ul>
     *               <li>When <code>{@link #type()} == FREE_TYPE</code>, {@link FT_Face} must be returned.</li>
     *           </ul>
     */
    @NonNull Object fontObj();
    boolean hasKerning();
    int getGlyphIndex(int codepoint);

    /**
     * <p>Note: A <code>null</code> return value marks the failure of this call. You can
     * expect unmodified <code>outMetrics</code> too if it was non-null.</p>
     * <p>Note: It doesn't put SDF parameters to <code>outMetrics</code>.</p>
     *
     * @implSpec <p>Must follow the specifications:</p>
     *           <ul>
     *               <li>A <code>null</code> return value marks the failure of this call. You should
     *               then keep <code>outMetrics</code> unmodified if it was non-null.</li>
     *               <li>Must not put SDF parameters to <code>outMetrics</code>.</li>
     *           </ul>
     *
     * @param outMetrics It will receive glyph metrics if and only if it's non-null
     */
    @Nullable ST_Bitmap loadGlyph(int glyphIndex, int payload, @Nullable GlyphMetrics outMetrics);
}
