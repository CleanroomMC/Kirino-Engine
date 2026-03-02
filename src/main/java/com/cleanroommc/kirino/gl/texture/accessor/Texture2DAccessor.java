package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class Texture2DAccessor extends TextureDSAAccessor {

    public final GLTexture texture;

    public Texture2DAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        this.texture = texture;
    }

    @Override
    public int textureID() {
        return texture.textureID;
    }

    @Override
    public int target() {
        return TextureType.TEX_2D.glValue;
    }

    @Override
    public int bindingTarget() {
        return TextureType.TEX_2D.bindingTarget();
    }

    @NonNull
    @Override
    public TextureType type() {
        return TextureType.TEX_2D;
    }

    @Override
    public void texStorage2D(
            int levels,
            int internalFormat,
            int width,
            int height) {

        if (dsa) {
            GL45.glTextureStorage2D(textureID(), levels, internalFormat, width, height);
        } else {
            GL42.glTexStorage2D(target(), levels, internalFormat, width, height);
        }
    }

    @Override
    public void texImage2D(
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        GL11.glTexImage2D(target(), level, internalFormat, width, height, border, format, type, data);
    }

    @Override
    public void texSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        if (dsa) {
            GL45.glTextureSubImage2D(textureID(), level, xOffset, yOffset, width, height, format, type, data);
        } else {
            GL11.glTexSubImage2D(target(), level, xOffset, yOffset, width, height, format, type, data);
        }
    }

    @Override
    public void compressedTexImage2D(
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            @Nullable ByteBuffer data) {

        GL13.glCompressedTexImage2D(target(), level, internalFormat, width, height, border, data);
    }

    @Override
    public void compressedTexSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            @Nullable ByteBuffer data) {

        if (dsa) {
            GL45.glCompressedTextureSubImage2D(textureID(), level, xOffset, yOffset, width, height, format, data);
        } else {
            GL13.glCompressedTexSubImage2D(target(), level, xOffset, yOffset, width, height, format, data);
        }
    }

    @Override
    public void copyTexSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int x,
            int y,
            int width,
            int height) {

        if (dsa) {
            GL45.glCopyTextureSubImage2D(textureID(), level, xOffset, yOffset, x, y, width, height);
        } else {
            GL11.glCopyTexSubImage2D(target(), level, xOffset, yOffset, x, y, width, height);
        }
    }
}
