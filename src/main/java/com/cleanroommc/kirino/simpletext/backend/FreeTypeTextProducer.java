package com.cleanroommc.kirino.simpletext.backend;

import com.cleanroommc.kirino.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.simpletext.SimpleTextProducer;
import com.cleanroommc.kirino.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.text.CodepointIterator;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

import java.awt.*;
import java.util.function.IntConsumer;

/**
 * The whole "canvas" uses the top-left pivot coordinate system,
 * so do the individual glyphs.
 *
 * @see <a href="https://freetype.org/freetype2/docs/glyphs/glyph-metrics-3.svg">Glyph metrics explanation</a>
 */
public class FreeTypeTextProducer implements SimpleTextProducer {

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private final static int GLYPH_SIZE = 7;

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private final static int MISSING_GLYPH_SIZE = 10;

    private final SimpleTextRuntime context;
    private final int pixelSize;
    private final TextCommandList cmdList = new TextCommandList(1024);

    private boolean batching;

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private float penX;

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private float penY;

    public FreeTypeTextProducer(SimpleTextRuntime context, int pixelSize) {
        Preconditions.checkState(context.getFont().type() == ST_FontBackendType.FREE_TYPE,
                "Must have a FreeType backend context.");

        this.context = context;
        this.pixelSize = pixelSize;
    }

    /**
     * @return Pixel -> Minecraft scaled resolution
     */
    private float pixel2screen(float pixel) {
        return (pixel / (float) pixelSize) * GLYPH_SIZE;
    }

    /**
     * It moves the pen position instead of returning something.
     */
    private void kerning(int leftGlyph, int rightGlyph) {
        if (leftGlyph == 0 || rightGlyph == 0) {
            return;
        }

        Preconditions.checkState(context.getFont().fontObj() instanceof FT_Face,
                "Font object isn't an instance of FT_Face, breaking the protocol!");

        FT_Face face = (FT_Face) context.getFont().fontObj();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);
            try (FT_Vector vector = new FT_Vector(pointer.getByteBuffer(1))) {
                int error = FreeType.FT_Get_Kerning(
                        face,
                        leftGlyph,
                        rightGlyph,
                        FreeType.FT_KERNING_DEFAULT,
                        vector);

                if (error != FreeType.FT_Err_Ok) {
                    return;
                }

                float advanceX = vector.x() / 64f;
                float advanceY = vector.y() / 64f;

                penX += pixel2screen(advanceX);
                penY += pixel2screen(advanceY);
            }
        }
    }

    public void beginBatch() {
        Preconditions.checkState(!batching, "Must not be batching.");

        batching = true;

        cmdList.clear();

        penX = 0;
        penY = 0;
    }

    public void endBatch() {
        Preconditions.checkState(batching, "Must be batching already.");

        batching = false;
    }

    public void append(@NonNull String text, float x, float y) {
        Preconditions.checkState(batching, "Must be batching already.");
        Preconditions.checkNotNull(text);

        penX = x;
        penY = y;

        final float[] lineHeight = {-1f};

        (new CodepointIterator(text)).forEachRemaining((IntConsumer) (codepoint) -> {
            int glyph = context.getFont().getGlyphIndex(codepoint);
            if (glyph == 0) {
                return;
            }

            GlyphMetrics metrics = context.getGlyphMetrics(glyph);
            lineHeight[0] = Math.max(lineHeight[0], pixel2screen(metrics.getBearingY()));
        });

        int prevGlyph = 0;

        CodepointIterator iterator = new CodepointIterator(text);

        while (iterator.hasNext()) {
            int codepoint = iterator.nextInt();
            int glyph = context.getFont().getGlyphIndex(codepoint);

            float topY = penY;
            float baselineY = topY + lineHeight[0];

            if (glyph == 0) {
                cmdList.push(penX, baselineY - MISSING_GLYPH_SIZE, MISSING_GLYPH_SIZE, MISSING_GLYPH_SIZE);
                penX += MISSING_GLYPH_SIZE;
            } else {
                if (context.getFont().hasKerning()) {
                    kerning(prevGlyph, glyph);
                }

                GlyphMetrics metrics = context.getGlyphMetrics(glyph);

                // bottom-left corner
                float drawX = penX + pixel2screen(metrics.getBearingX() - metrics.getSdfPadding());
                float drawY = baselineY + pixel2screen(metrics.getGlyphHeight() - metrics.getBearingY() + metrics.getSdfPadding()) - pixel2screen(metrics.getGlyphHeight());

                cmdList.push(
                        glyph,
                        drawX, drawY,
                        pixel2screen(metrics.getSdfWidth()),
                        pixel2screen(metrics.getSdfHeight()),
                        1f,
                        Color.WHITE.getRGB(),
                        0);

                penX += pixel2screen(metrics.getAdvanceX());
            }

            prevGlyph = glyph;
        }
    }

    @NonNull
    public TextCommandList submit() {
        Preconditions.checkState(!batching, "Must not be batching.");

        return cmdList;
    }
}
