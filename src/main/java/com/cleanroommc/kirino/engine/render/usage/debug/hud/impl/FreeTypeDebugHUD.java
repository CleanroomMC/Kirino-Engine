package com.cleanroommc.kirino.engine.render.usage.debug.hud.impl;

import com.cleanroommc.kirino.ImmediateClientServices;
import com.cleanroommc.kirino.engine.render.core.debug.hud.HUDContext;
import com.cleanroommc.kirino.engine.render.core.debug.hud.ImmediateHUD;
import com.cleanroommc.kirino.simpletext.freetype.AlphaBitmap;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeBitmapDecoder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FreeType;
import org.lwjglx.input.Keyboard;

import java.awt.*;
import java.nio.ByteBuffer;

public class FreeTypeDebugHUD implements ImmediateHUD {

    private boolean input = false;
    private boolean inputProtect = false;

    private char inputChar = 'A';
    private AlphaBitmap bitmap = null;

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
            drawBitmap(hud, hud.getTessellator(), bitmap, hud.getPivotX(), hud.getPivotY(), 3f);
        }
    }

    private static void drawBitmap(HUDContext hud, Tessellator tessellator, AlphaBitmap bitmap, float x, float y, float scale) {
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
    private static AlphaBitmap genBitmap(char c) {
        int error = FreeType.FT_Load_Char(
                ImmediateClientServices.instance().text().getFreeTypeFace(),
                c,
                FreeType.FT_LOAD_RENDER);

        if (error != FreeType.FT_Err_Ok) {
            return null;
        }

        FT_GlyphSlot glyph = ImmediateClientServices.instance().text().getFreeTypeFace().glyph();
        if (glyph == null) {
            return null;
        }

        FT_Bitmap bitmap = glyph.bitmap();
        return FreeTypeBitmapDecoder.decode(bitmap);
    }
}
