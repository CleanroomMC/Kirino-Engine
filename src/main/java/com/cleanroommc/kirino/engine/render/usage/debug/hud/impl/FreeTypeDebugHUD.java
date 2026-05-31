package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ImmediateClientServices;
import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeAlphaBitmap;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeBitmapDecoder;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeBitmapLoader;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFBitmap;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGeneratorBruteForceImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;
import org.lwjglx.input.Keyboard;

import java.awt.*;
import java.nio.ByteBuffer;

public class FreeTypeDebugHUD implements ImmediateHUD {

    private boolean input = false;
    private boolean inputProtect = false;

    private char inputChar = 'A';
    private FreeTypeAlphaBitmap bitmap = null;
    private SDFBitmap sdfBitmap = null;

    private float charSize = 20f;

    private Character texCacheTarget = null;
    private Texture2DAccessor charTex2D;

    public FreeTypeDebugHUD() {
        ShutdownManager.registerAsync(() -> {
            if (bitmap != null) {
                bitmap.close();
            }
            if (sdfBitmap != null) {
                sdfBitmap.close();
            }
        });
    }

    @Override
    public void draw(@NonNull HUDContext hud) {
        hud.text("Press [Enter + RShift] to input a char");
        hud.text("Waiting an input: " + (input ? "TRUE" : "FALSE"));

        if (Keyboard.isKeyDown(Keyboard.KEY_RETURN) && Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            if (!input) {
                input = true;
                inputProtect = true;
            }
        }

        if (inputProtect && !Keyboard.isKeyDown(Keyboard.KEY_RETURN) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            inputProtect = false;
            while (Keyboard.next());
        }

        if (!inputProtect && input) {
            if (Keyboard.next()) {
                char newChar = Keyboard.getEventCharacter();
                if (newChar != inputChar) {
                    inputChar = newChar;
                    if (bitmap != null) {
                        bitmap.close();
                        bitmap = null;
                    }
                    if (sdfBitmap != null) {
                        sdfBitmap.close();
                        sdfBitmap = null;
                    }
                }
                input = false;
                inputProtect = false;
            }
        }

        hud.text("Current char: " + inputChar);

        if (bitmap == null) {
            bitmap = genBitmap(inputChar);
        }

        boolean isNull = bitmap == null;
        hud.text("Null bitmap: " + (isNull ? "TRUE" : "FALSE"));

        if (!isNull) {
            hud.text("Width: " + bitmap.width() + ", Height: " + bitmap.height());

            float drawScale = 3f;

            drawBitmap(
                    hud,
                    hud.getTessellator(),
                    bitmap,
                    hud.getPivotX(),
                    hud.getPivotY(),
                    drawScale);

            if (sdfBitmap == null) {
                SDFGeneratorBruteForceImpl generator = new SDFGeneratorBruteForceImpl(16, 16);
                sdfBitmap = generator.compute(bitmap);
            }

            drawSdfBitmap(
                    hud,
                    hud.getTessellator(),
                    sdfBitmap,
                    hud.getPivotX() + bitmap.width() * drawScale,
                    hud.getPivotY(),
                    drawScale);

            if (texCacheTarget == null || texCacheTarget != inputChar) {
                charTex2D = new Texture2DAccessor(true, GLTexture.newDsaTex2D(sdfBitmap.width(), sdfBitmap.height()));

                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
                charTex2D.highlevel().alloc(false, sdfBitmap.byteBuffer(), TextureFormat.R8_UNORM);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

                charTex2D.setCommonParams(FilterMode.LINEAR, FilterMode.LINEAR, WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);

                texCacheTarget = inputChar;
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                charSize++;
            } else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                charSize--;
            }

            charSize = Math.min(50, Math.max(10, charSize));

            float sdfCharWidth = (float) sdfBitmap.width() / sdfBitmap.height() * charSize;
            drawSdfChar(
                    hud,
                    charTex2D.textureID(),
                    hud.getPivotX() + bitmap.width() * drawScale + sdfBitmap.width() * drawScale,
                    hud.getPivotY(),
                    sdfCharWidth,
                    charSize);

            drawChar(
                    hud,
                    bitmap,
                    hud.getPivotX() + bitmap.width() * drawScale + sdfBitmap.width() * drawScale + sdfCharWidth,
                    hud.getPivotY(),
                    charSize);
        }
    }

    private static boolean shaderSetup = false;
    private static ShaderProgram shaderProgram;
    private static FT_Face freeTypeFace = null;

    private static FT_Face face() {
        if (freeTypeFace == null) {
            freeTypeFace = ImmediateClientServices.instance().freetype().load(
                    new ResourceLocation("forge:fonts/jetbrains/jetbrains_mono_nl_regular.ttf"),
                    0,
                    48);
        }
        return freeTypeFace;
    }

    private static void drawSdfChar(HUDContext hud, int texId, float x, float y, float width, float height) {
        if (!shaderSetup) {
            Shader vert = ImmediateClientServices.instance().shader().makeShader(new ResourceLocation("forge:shaders/font_test.vert"));
            Shader frag = ImmediateClientServices.instance().shader().makeShader(new ResourceLocation("forge:shaders/font_test.frag"));
            ImmediateClientServices.instance().shader().submitToGL(vert, frag);
            shaderProgram = ImmediateClientServices.instance().shader().makeProgram(vert, frag);
            shaderSetup = true;
        }

        int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        shaderProgram.use();

        int posXLoc = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "posX");
        int posYLoc = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "posY");
        int widthLoc = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "width");
        int heightLoc = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "height");
        int atlasLoc = GL20.glGetUniformLocation(shaderProgram.getProgramID(), "atlas");

        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        float screenWidth = (float) resolution.getScaledWidth_double();
        float screenHeight = (float) resolution.getScaledHeight_double();
        float posX = x / screenWidth * 2f - 1f;
        float posY = 1f - y / screenHeight * 2f;
        float _width = width / screenWidth * 2f;
        float _height = height / screenHeight * -2f;

        GL20.glUniform1f(posXLoc, posX);
        GL20.glUniform1f(posYLoc, posY);
        GL20.glUniform1f(widthLoc, _width);
        GL20.glUniform1f(heightLoc, _height);

        int texUnit = GL11C.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        GL13.glActiveTexture(GL13.GL_TEXTURE5);
        int prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL20.glUniform1i(atlasLoc, 5);

        ImmediateClientServices.instance().dummyVao().bind();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex);
        GL13.glActiveTexture(texUnit);

        GL20.glUseProgram(prevProg);

        hud.drawRectOutline(x, y, width, height, 0.5f, Color.RED.getRGB());
    }

    private static void drawChar(HUDContext hud, FreeTypeAlphaBitmap bitmap, float x, float y, float charSize) {
        float ratio = charSize / bitmap.height();

        drawBitmap(
                hud,
                hud.getTessellator(),
                bitmap,
                x,
                y,
                ratio);
    }

    private static float smoothStep(float edge0, float edge1, float x) {
        float t = (x - edge0) / (edge1 - edge0);
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private static void drawSdfBitmap(HUDContext hud, Tessellator tessellator, SDFBitmap bitmap, float x, float y, float scale) {
        int width = bitmap.width();
        int height = bitmap.height();
        ByteBuffer buf = bitmap.byteBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        float r = 1, g = 1, b = 1;

        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int index = py * width + px;
                int alphaByte = buf.get(index) & 0xFF;

                float v = alphaByte / 255f;

                float a = smoothStep(0.35f, 0.65f, v);

                if (a <= 0f || a > 1f) {
                    continue;
                }

                float x0 = x + px * scale;
                float y0 = y + py * scale;
                float x1 = x0 + scale;
                float y1 = y0 + scale;

                buffer.pos(x0, y1, 0).color(r, g, b, a).endVertex();
                buffer.pos(x1, y1, 0).color(r, g, b, a).endVertex();
                buffer.pos(x1, y0, 0).color(r, g, b, a).endVertex();
                buffer.pos(x0, y0, 0).color(r, g, b, a).endVertex();
            }
        }

        tessellator.draw();

        hud.drawRectOutline(x, y, width * scale, height * scale, 0.5f, Color.RED.getRGB());
    }

    private static void drawBitmap(HUDContext hud, Tessellator tessellator, FreeTypeAlphaBitmap bitmap, float x, float y, float scale) {
        int width = bitmap.width();
        int height = bitmap.height();
        ByteBuffer buf = bitmap.byteBuffer();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();

        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        float r = 1, g = 1, b = 1;

        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int index = py * width + px;
                int alphaByte = buf.get(index) & 0xFF;

                if (alphaByte == 0) {
                    continue;
                }

                float a = alphaByte / 255f;

                float x0 = x + px * scale;
                float y0 = y + py * scale;
                float x1 = x0 + scale;
                float y1 = y0 + scale;

                buffer.pos(x0, y1, 0).color(r, g, b, a).endVertex();
                buffer.pos(x1, y1, 0).color(r, g, b, a).endVertex();
                buffer.pos(x1, y0, 0).color(r, g, b, a).endVertex();
                buffer.pos(x0, y0, 0).color(r, g, b, a).endVertex();
            }
        }

        tessellator.draw();

        hud.drawRectOutline(x, y, width * scale, height * scale, 0.5f, Color.RED.getRGB());
    }

    @Nullable
    private static FreeTypeAlphaBitmap genBitmap(char c) {
        FT_Bitmap b = FreeTypeBitmapLoader.load(
                face(), c,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING, null);

        if (b == null) {
            return null;
        }

        try {
            return FreeTypeBitmapDecoder.decode(b);
        } catch (Throwable t) {
            return null;
        }
    }
}
