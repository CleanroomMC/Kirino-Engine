package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

public class Texture1DAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public Texture1DAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        Preconditions.checkState(texture.type == TextureType.TEX_1D,
                "Texture type must be TEX_1D.");

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
        return TextureType.TEX_1D;
    }

    @Override
    public void texStorage1D(
            int levels,
            int internalFormat,
            int width) {

        if (dsa) {
            GL45.glTextureStorage1D(textureID(), levels, internalFormat, width);
        } else {
            GL42.glTexStorage1D(target(), levels, internalFormat, width);
        }
    }

    @Override
    public void texImage1D(
            int level,
            int internalFormat,
            int width,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        Preconditions.checkState(!dsa, "DSA \"texImage1D\" is not implemented.");

        GL11.glTexImage1D(target(), level, internalFormat, width, border, format, type, data);
    }

    @Override
    public void texSubImage1D(
            int level,
            int xOffset,
            int width,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        if (dsa) {
            GL45.glTextureSubImage1D(textureID(), level, xOffset, width, format, type, data);
        } else {
            GL11.glTexSubImage1D(target(), level, xOffset, width, format, type, data);
        }
    }

    @Override
    public void compressedTexImage1D(
            int level,
            int internalFormat,
            int width,
            int border,
            @Nullable ByteBuffer data) {

        Preconditions.checkState(!dsa, "DSA \"compressedTexImage1D\" is not implemented.");

        GL13.glCompressedTexImage1D(target(), level, internalFormat, width, border, data);
    }

    @Override
    public void compressedTexSubImage1D(
            int level,
            int xOffset,
            int width,
            int format,
            @Nullable ByteBuffer data) {

        if (dsa) {
            GL45.glCompressedTextureSubImage1D(textureID(), level, xOffset, width, format, data);
        } else {
            GL13.glCompressedTexSubImage1D(target(), level, xOffset, width, format, data);
        }
    }

    @Override
    public void copyTexSubImage1D(
            int level,
            int xOffset,
            int x,
            int y,
            int width) {

        if (dsa) {
            GL45.glCopyTextureSubImage1D(textureID(), level, xOffset, x, y, width);
        } else {
            GL11.glCopyTexSubImage1D(target(), level, xOffset, x, y, width);
        }
    }

    private static class HighlevelOperatorImpl implements HighlevelOperator {

        private final Texture1DAccessor accessor;

        private HighlevelOperatorImpl(Texture1DAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public void resizeAndAllocEmpty(int width) {
            MethodHolder.setExtentX(accessor.texture, width);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                allocEmpty(true);
            } else {
                allocEmpty(true, format);
            }
        }

        @Override
        public void resizeAndAllocEmpty(int width, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);

            allocEmpty(true, format);
        }

        @Override
        public void resizeAndAlloc(int width, @Nullable ByteBuffer byteBuffer) {
            MethodHolder.setExtentX(accessor.texture, width);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                alloc(true, byteBuffer);
            } else {
                alloc(true, byteBuffer, format);
            }
        }

        @Override
        public void resizeAndAlloc(int width, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);

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
                accessor.texImage1D(0, format.internalFormat, accessor.texture.extentX(), 0, format.format, format.type, byteBuffer);
            } else {
                accessor.texStorage1D(1, format.internalFormat, accessor.texture.extentX());
                accessor.texSubImage1D(0, 0, accessor.texture.extentX(), format.format, format.type, byteBuffer);
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
                accessor.texImage1D(0, format.internalFormat, accessor.texture.extentX(), 0, format.format, format.type, null);
            } else {
                accessor.texStorage1D(1, format.internalFormat, accessor.texture.extentX());
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
