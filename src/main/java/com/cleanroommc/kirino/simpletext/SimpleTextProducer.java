package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.simpletext.command.TextCommandList;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapLoader;
import com.cleanroommc.kirino.simpletext.glyph.GlyphMetrics;
import com.cleanroommc.kirino.simpletext.text.CodepointIterator;
import com.google.common.base.Preconditions;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

public class SimpleTextProducer {

    /**
     * <p>Unit: ?</p>
     */
    private final static int MISSING_GLYPH_ADV_X = 12;

    private final SimpleTextContext context;

    private final TextCommandList cmdList = new TextCommandList(1024);

    private boolean batching;

    private float penX;
    private float penY;

    public SimpleTextProducer(SimpleTextContext context) {
        this.context = context;
    }

    /**
     * It moves the pen position instead of returning something.
     */
    private void kerning(int leftGlyph, int rightGlyph) {
        if (leftGlyph == 0 || rightGlyph == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);
            try (FT_Vector vector = new FT_Vector(pointer.getByteBuffer(1))) {
                int error = FreeType.FT_Get_Kerning(
                        context.getFontFace(),
                        leftGlyph,
                        rightGlyph,
                        FreeType.FT_KERNING_DEFAULT,
                        vector);

                if (error != FreeType.FT_Err_Ok) {
                    return;
                }

                float advanceX = vector.x() / 64f;
                float advanceY = vector.y() / 64f;

                penX += advanceX;
                penY += advanceY;
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

    public void append(String text, float x, float y, float fontSize) {
        Preconditions.checkState(batching, "Must be batching already.");

        penX = x;
        penY = y;

        int prevGlyph = 0;

        CodepointIterator iterator = new CodepointIterator(text);

        while (iterator.hasNext()) {
            int codepoint = iterator.nextInt();
            int glyph = FreeTypeBitmapLoader.getGlyphIndex(context.getFontFace(), codepoint);

            if (glyph == 0) {
                cmdList.push(penX, penY);
                penX += MISSING_GLYPH_ADV_X;
            } else {
                if (context.hasFontKerning()) {
                    kerning(prevGlyph, glyph);
                }
                // todo: load metrics + reserve atlas
                GlyphMetrics metrics = context.getMetrics(glyph);

//                cmdList.push(
//                        glyph,
//                        penX, penY,);
//                penX += ;
            }

            prevGlyph = glyph;
        }
    }

    public TextCommandList submit() {
        Preconditions.checkState(!batching, "Must not be batching.");

        return cmdList;
    }
}
