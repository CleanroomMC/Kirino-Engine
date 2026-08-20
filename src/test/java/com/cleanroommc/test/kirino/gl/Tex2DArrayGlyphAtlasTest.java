package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.texture.accessor.Texture2DArrayAccessor;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GLTestExtension.class)
public class Tex2DArrayGlyphAtlasTest {

    private static ByteBuffer readLayer(Texture2DArrayAccessor texture, int layer) {
        int width = texture.texture.extentX();
        int height = texture.texture.extentY();

        ByteBuffer result = MemoryUtil.memAlloc(width * height);

        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL45.glGetTextureSubImage(
                texture.textureID(),
                0,
                0,
                0,
                layer,
                width,
                height,
                1,
                GL11.GL_RED,
                GL11.GL_UNSIGNED_BYTE,
                result);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 4);

        return result;
    }

    @Test
    public void testPaging() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            Tex2DArrayGlyphAtlas atlas = new Tex2DArrayGlyphAtlas(5, 5);

            SDFBitmap bitmap = new SDFBitmap(4, 4, MemoryUtil.memAlloc(16));
            bitmap.byteBuffer().put(0, (byte) 3);
            bitmap.byteBuffer().put(15, (byte) 4);

            atlas.allocate(bitmap);

            assertEquals(1, atlas.getPageCount());

            bitmap.byteBuffer().put(0, (byte) 5);
            bitmap.byteBuffer().put(15, (byte) 6);

            atlas.allocate(bitmap);

            assertEquals(2, atlas.getPageCount());

            bitmap.close();

            Tex2DArrayGlyphAtlas.LayerPage layerPage0 = atlas.getPage(0);
            ByteBuffer result0 = readLayer(layerPage0.getTexture(), layerPage0.getLayer());

            assertEquals(3, result0.get(0));
            assertEquals(4, result0.get(18));

            MemoryUtil.memFree(result0);

            Tex2DArrayGlyphAtlas.LayerPage layerPage1 = atlas.getPage(1);
            ByteBuffer result1 = readLayer(layerPage1.getTexture(), layerPage1.getLayer());

            assertEquals(5, result1.get(0));
            assertEquals(6, result1.get(18));

            MemoryUtil.memFree(result1);
        });
    }
}
