package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.simpletext.freetype.AlphaBitmap;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapDecoder;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.*;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;

import java.nio.ByteBuffer;

/**
 * This is a simple text renderer that is highly coupled with freetype.
 */
public class SimpleTextRenderer {

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final FT_Face face;

    public FT_Face getFreeTypeFace() {
        return face;
    }

    public SimpleTextRenderer(ImmediateShaderAccess shaderAccess, FreeTypeManager freeTypeManager, ResourceLocation rl) {
        this.shaderAccess = shaderAccess;
        this.freeTypeManager = freeTypeManager;
        face = freeTypeManager.load(rl);

        Shader shader = shaderAccess.makeShader(new ResourceLocation("forge:shaders/font.vert"));
        shaderAccess.submitToGL(shader);
    }

    Texture2DAccessor texture2D = null;

    public void init(char c) {
        if (texture2D != null) {
            return;
        }

        FreeType.FT_Load_Char(face, c, FreeType.FT_LOAD_RENDER);
        FT_GlyphSlot glyph = face.glyph();
        FT_Bitmap bitmap = glyph.bitmap();
        AlphaBitmap alphaBitmap = FreeTypeBitmapDecoder.decode(bitmap);

        ByteBuffer byteBuffer = alphaBitmap.byteBuffer();

        int nonZero = 0;
        int zero = 0;

        for (int i = 0; i < byteBuffer.capacity(); i++) {
            int v = byteBuffer.get(i) & 0xFF;
            if (v == 0) zero++;
            else nonZero++;
        }

        KirinoCommonCore.LOGGER.info("debug: width=" + alphaBitmap.width() + " height=" + alphaBitmap.height() + " zero=" + zero + " nonZero=" + nonZero);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        texture2D = new Texture2DAccessor(true, GLTexture.newDsaTex2D(alphaBitmap.width(), alphaBitmap.height()));
        texture2D.highlevel().alloc(false, alphaBitmap.byteBuffer(), TextureFormat.R8_UNORM);
        texture2D.setCommonParams(FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
        texture2D.texParamI(GL33.GL_TEXTURE_SWIZZLE_R, GL11.GL_ONE);
        texture2D.texParamI(GL33.GL_TEXTURE_SWIZZLE_G, GL11.GL_ONE);
        texture2D.texParamI(GL33.GL_TEXTURE_SWIZZLE_B, GL11.GL_ONE);
        texture2D.texParamI(GL33.GL_TEXTURE_SWIZZLE_A, GL11.GL_RED);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
    }

    public void draw(int x, int y, int scale) {
        int prevId = texture2D.fetchCurrentBoundTexID();
        texture2D.bind();

        GlStateManager.color(1f, 1f, 1f, 1f);

        int width = texture2D.texture.extentX() * scale;
        int height = texture2D.texture.extentY() * scale;

        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y, 0).tex(0, 0).endVertex();
        bufferbuilder.pos(x, y + height, 0).tex(0, 1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0).tex(1, 1).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex(1, 0).endVertex();
        tessellator.draw();

        texture2D.bind(prevId);
    }
}