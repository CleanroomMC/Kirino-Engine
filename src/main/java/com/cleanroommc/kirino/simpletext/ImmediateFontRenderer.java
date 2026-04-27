package com.cleanroommc.kirino.simpletext;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

public class ImmediateFontRenderer {

    private long library;
    private long face;

    public ImmediateFontRenderer(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            PointerBuffer pointer = stack.mallocPointer(1);
            FreeType.FT_Init_FreeType(pointer);
//            library = pointer.get(0);
//
//            PointerBuffer facePtr = stack.mallocPointer(1);
//            FreeType.FT_New_Face(library, path, 0, facePtr);
//            face = facePtr.get(0);
//
//            FreeType.FT_Set_Pixel_Sizes(face, 0, 48);
        }
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