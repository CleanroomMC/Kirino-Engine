package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class Texture2DAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public Texture2DAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        Preconditions.checkState(texture.type == TextureType.TEX_2D,
                "Texture type must be TEX_2D.");

        this.texture = texture;
    }

    @Override
    public int textureID() {
        return texture.textureID;
    }

    @Override
    public int target() {
        return type().glValue;
    }

    @Override
    public int bindingTarget() {
        return type().bindingTarget();
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

        Preconditions.checkState(!dsa, "DSA \"texImage2D\" is not implemented.");

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

        Preconditions.checkState(!dsa, "DSA \"compressedTexImage2D\" is not implemented.");

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

    private static class HighlevelOperatorImpl implements TextureAccessorHighlevel.HighlevelOperator {

        private final Texture2DAccessor accessor;

        private HighlevelOperatorImpl(Texture2DAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public void resizeAndAllocEmpty(int width, int height) {
            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                allocEmpty(true);
            } else {
                allocEmpty(true, format);
            }
        }

        @Override
        public void resizeAndAllocEmpty(int width, int height, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);

            allocEmpty(true, format);
        }

        @Override
        public void resizeAndAlloc(int width, int height, @Nullable ByteBuffer byteBuffer) {
            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                alloc(true, byteBuffer);
            } else {
                alloc(true, byteBuffer, format);
            }
        }

        @Override
        public void resizeAndAlloc(int width, int height, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);

            alloc(true, byteBuffer, format);
        }

        @Override
        public void alloc(boolean mutable, @Nullable ByteBuffer byteBuffer) {
            alloc(mutable, byteBuffer, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void alloc(boolean mutable, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setCurrentFormat(accessor.texture, format);

            if (mutable) {
                accessor.texImage2D(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), 0, format.format, format.type, byteBuffer);
            } else {
                accessor.texStorage2D(1, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY());
                accessor.texSubImage2D(0, 0, 0, accessor.texture.extentX(), accessor.texture.extentY(), format.format, format.type, byteBuffer);
            }
        }

        @Override
        public void allocEmpty(boolean mutable) {
            allocEmpty(mutable, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setCurrentFormat(accessor.texture, format);

            if (mutable) {
                accessor.texImage2D(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), 0, format.format, format.type, null);
            } else {
                accessor.texStorage2D(1, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY());
            }
        }
    }

    private HighlevelOperatorImpl highlevelOperator = null;

    @NonNull
    @Override
    public HighlevelOperator highlevel() {
        if (highlevelOperator == null) {
            highlevelOperator = new HighlevelOperatorImpl(this);
        }
        return highlevelOperator;
    }
}
