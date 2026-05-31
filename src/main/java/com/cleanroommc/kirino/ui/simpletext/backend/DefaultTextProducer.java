package com.cleanroommc.kirino.ui.simpletext.backend;

import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextProducer;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.ui.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.ui.simpletext.text.CodepointIterator;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
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
public class DefaultTextProducer implements SimpleTextProducer {

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private final static float STANDARD_GLYPH_SIZE = 7;

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

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     * <p>Note: Estimate under {@link #STANDARD_GLYPH_SIZE}.</p>
     */
    private float lineHeightEstimate;

    private void estimateLineHeight() {
        String testText = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";

        final float[] lineHeight = {-1f};

        (new CodepointIterator(testText)).forEachRemaining((IntConsumer) (codepoint) -> {
            int glyph = context.getFont().getGlyphIndex(codepoint);
            if (glyph == 0) {
                return;
            }

            GlyphMetrics metrics = context.getGlyphMetrics(glyph);
            lineHeight[0] = Math.max(lineHeight[0], pixel2screen(metrics.getBearingY(), STANDARD_GLYPH_SIZE));
        });

        lineHeightEstimate = lineHeight[0] * 1.05f;
    }

    public DefaultTextProducer(SimpleTextRuntime context, int pixelSize) {
        Preconditions.checkState(context.getFont().type() == ST_FontBackendType.FREE_TYPE,
                "Must have a FreeType backend context.");

        this.context = context;
        this.pixelSize = pixelSize;

        estimateLineHeight();
    }

    /**
     * @return Pixel -> Minecraft scaled resolution
     */
    private float pixel2screen(float pixel, float glyphSize) {
        return (pixel / (float) pixelSize) * glyphSize;
    }

    /**
     * It moves the pen position instead of returning something.
     */
    private void kerning(int leftGlyph, int rightGlyph, float glyphSize) {
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

                penX += pixel2screen(advanceX, glyphSize);
                penY += pixel2screen(advanceY, glyphSize);
            }
        }
    }

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private float calcLineHeight(String text, float glyphSize) {
        final float[] lineHeight = {lineHeightEstimate};

        (new CodepointIterator(text)).forEachRemaining((IntConsumer) (codepoint) -> {
            int glyph = context.getFont().getGlyphIndex(codepoint);
            if (glyph == 0) {
                return;
            }

            GlyphMetrics metrics = context.getGlyphMetrics(glyph);
            lineHeight[0] = Math.max(lineHeight[0], pixel2screen(metrics.getBearingY(), STANDARD_GLYPH_SIZE));
        });

        return lineHeight[0] / STANDARD_GLYPH_SIZE * glyphSize;
    }

    /**
     * <p>Unit: Minecraft scaled resolution</p>
     */
    private float calcMissingGlyphSize(float glyphSize) {
        return glyphSize;
    }

    @Override
    public float standardFontSize() {
        return STANDARD_GLYPH_SIZE;
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

    private void appendInternal(
            String text,
            float x,
            float y,
            float fontSize,
            float @Nullable [] size,
            int @Nullable [] color,
            int @Nullable [] hint,
            @Nullable LineInfo outLineInfo) {

        Preconditions.checkState((size == null && color == null && hint == null) ||
                (size != null && color != null && hint != null),
                "Arguments \"size\", \"color\", \"hint\" must be all null or all non-null.");

        boolean paramOverride = size != null;

        penX = x;
        penY = y;

        float initPenX = penX;
        float initPenY = penY;
        float maxBearingY = 0;
        float maxY = initPenY;
        float lineHeight = calcLineHeight(text, fontSize); // top to baseline
        float missingGlyphSize = calcMissingGlyphSize(fontSize);

        int prevGlyph = 0;
        int index = 0;
        CodepointIterator iterator = new CodepointIterator(text);

        while (iterator.hasNext()) {
            int codepoint = iterator.nextInt();
            int glyph = context.getFont().getGlyphIndex(codepoint);

            float topY = penY;
            float baselineY = topY + lineHeight;

            if (glyph == 0) {
                cmdList.push(penX, baselineY - missingGlyphSize, missingGlyphSize, missingGlyphSize);
                penX += missingGlyphSize;
            } else {
                if (context.getFont().hasKerning()) {
                    kerning(prevGlyph, glyph, fontSize);
                }

                GlyphMetrics metrics = context.getGlyphMetrics(glyph);

                // bottom-left corner
                float drawX =
                        penX
                        + pixel2screen(metrics.getBearingX() - metrics.getSdfPadding(), fontSize);
                float drawY = baselineY
                        + pixel2screen(- metrics.getBearingY() - metrics.getSdfPadding(), fontSize);

                cmdList.push(
                        glyph,
                        drawX, drawY,
                        pixel2screen(metrics.getSdfWidth(), fontSize),
                        pixel2screen(metrics.getSdfHeight(), fontSize),
                        paramOverride ? size[index] : 1f,
                        paramOverride ? color[index] : Color.WHITE.getRGB(),
                        paramOverride ? hint[index] : 0);

                penX += pixel2screen(metrics.getAdvanceX(), fontSize);

                maxY = Math.max(maxY, baselineY + pixel2screen(metrics.getGlyphHeight() - metrics.getBearingY(), fontSize));
                maxBearingY = Math.max(maxBearingY, pixel2screen(metrics.getBearingY(), fontSize));
            }

            prevGlyph = glyph;
            index++;
        }

        if (outLineInfo != null) {
            outLineInfo.set(
                    penX - initPenX,
                    lineHeight,
                    initPenX,
                    penX,
                    initPenY + lineHeight - maxBearingY,
                    maxY);
        }
    }

    public void append(
            @NonNull String text,
            float x,
            float y,
            float fontSize,
            @Nullable LineInfo outLineInfo) {

        Preconditions.checkState(batching, "Must be batching already.");
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty(),
                "Argument \"text\" must not be empty.");

        if (outLineInfo != null) {
            Preconditions.checkArgument(outLineInfo.isEmpty(),
                    "Argument \"outLineInfo\" must be empty if non-null.");
        }

        appendInternal(text, x, y, fontSize, null, null, null, outLineInfo);
    }

    public void append(
            @NonNull String text,
            float x,
            float y,
            float fontSize,
            float @NonNull [] size,
            int @NonNull [] color,
            int @NonNull [] hint,
            @Nullable LineInfo outLineInfo) {

        Preconditions.checkState(batching, "Must be batching already.");
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty(),
                "Argument \"text\" must not be empty.");
        Preconditions.checkNotNull(size);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(hint);

        int cpCount = CodepointIterator.count(text);
        Preconditions.checkArgument(size.length == cpCount,
                "Argument \"size\"'s lenght=%s must match the codepoint count=%s.",
                size.length, cpCount);
        Preconditions.checkArgument(color.length == cpCount,
                "Argument \"color\"'s lenght=%s must match the codepoint count=%s.",
                size.length, cpCount);
        Preconditions.checkArgument(hint.length == cpCount,
                "Argument \"hint\"'s lenght=%s must match the codepoint count=%s.",
                size.length, cpCount);

        if (outLineInfo != null) {
            Preconditions.checkArgument(outLineInfo.isEmpty(),
                    "Argument \"outLineInfo\" must be empty if non-null.");
        }

        appendInternal(text, x, y, fontSize, size, color, hint, outLineInfo);
    }

    @NonNull
    public TextCommandList submit() {
        Preconditions.checkState(!batching, "Must not be batching.");

        return cmdList;
    }
}
