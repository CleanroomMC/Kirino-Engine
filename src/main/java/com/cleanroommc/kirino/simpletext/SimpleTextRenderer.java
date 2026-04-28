package com.cleanroommc.kirino.simpletext;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.freetype.FT_Face;

public class SimpleTextRenderer {

    private final FreeTypeManager freeTypeManager;
    private final FT_Face face;

    public SimpleTextRenderer(FreeTypeManager freeTypeManager, ResourceLocation rl) {
        this.freeTypeManager = freeTypeManager;
        face = freeTypeManager.load(rl);
    }

//
//    public void draw(char c) {
//        FreeType.FT_Load_Char(face, c, FreeType.FT_LOAD_RENDER);
//
//        FT_Face ftFace = FT_Face.create(face);
//        FT_GlyphSlot glyph = ftFace.glyph();
//        FT_Bitmap bitmap = glyph.bitmap();
//
//    }
}