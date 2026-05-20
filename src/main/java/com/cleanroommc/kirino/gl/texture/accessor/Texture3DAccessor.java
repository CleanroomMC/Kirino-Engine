package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class Texture3DAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public Texture3DAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        Preconditions.checkState(texture.type == TextureType.TEX_3D,
                "Texture type must be TEX_3D.");

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
        return TextureType.TEX_3D;
    }

    @Override
    public void texStorage3D(
            int levels,
            int internalFormat,
            int width,
            int height,
            int depthOrLayers) {

        if (dsa) {
            GL45.glTextureStorage3D(textureID(), levels, internalFormat, width, height, depthOrLayers);
        } else {
            GL42.glTexStorage3D(target(), levels, internalFormat, width, height, depthOrLayers);
        }
    }

    @Override
    public void texImage3D(
            int level,
            int internalFormat,
            int width,
            int height,
            int depthOrLayers,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        Preconditions.checkState(!dsa, "DSA \"texImage3D\" is not implemented.");

        GL12.glTexImage3D(target(), level, internalFormat, width, height, depthOrLayers, border, format, type, data);
    }

    @Override
    public void texSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int width,
            int height,
            int depthOrLayer,
            int format,
            int type,
            @NonNull ByteBuffer data) {

        if (dsa) {
            GL45.glTextureSubImage3D(textureID(), level, xOffset, yOffset, zOffset, width, height, depthOrLayer, format, type, data);
        } else {
            GL12.glTexSubImage3D(target(), level, xOffset, yOffset, zOffset, width, height, depthOrLayer, format, type, data);
        }
    }

    @Override
    public void compressedTexImage3D(
            int level,
            int internalFormat,
            int width,
            int height,
            int depthOrLayers,
            int border,
            @Nullable ByteBuffer data) {

        Preconditions.checkState(!dsa, "DSA \"compressedTexImage3D\" is not implemented.");

        GL13.glCompressedTexImage3D(target(), level, internalFormat, width, height, depthOrLayers, border, data);
    }

    @Override
    public void compressedTexSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int width,
            int height,
            int depthOrLayers,
            int format,
            @NonNull ByteBuffer data) {

        Preconditions.checkNotNull(data);

        if (dsa) {
            GL45.glCompressedTextureSubImage3D(textureID(), level, xOffset, yOffset, zOffset, width, height, depthOrLayers, format, data);
        } else {
            GL13.glCompressedTexSubImage3D(target(), level, xOffset, yOffset, zOffset, width, height, depthOrLayers, format, data);
        }
    }

    @Override
    public void copyTexSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int x,
            int y,
            int width,
            int height) {

        if (dsa) {
            GL45.glCopyTextureSubImage3D(textureID(), level, xOffset, yOffset, zOffset, x, y, width, height);
        } else {
            GL12.glCopyTexSubImage3D(target(), level, xOffset, yOffset, zOffset, x, y, width, height);
        }
    }

    private static class HighlevelOperatorImpl implements HighlevelOperator {

        private final Texture3DAccessor accessor;

        private HighlevelOperatorImpl(Texture3DAccessor accessor) {
            this.accessor = accessor;
        }

        @Override
        public void resizeAndAllocEmpty(int width, int height, int depthOrLayers) {
            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                allocEmpty(true);
            } else {
                allocEmpty(true, format);
            }
        }

        @Override
        public void resizeAndAllocEmpty(int width, int height, int depthOrLayers, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);

            allocEmpty(true, format);
        }

        @Override
        public void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull ByteBuffer byteBuffer) {
            Preconditions.checkNotNull(byteBuffer);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);
            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);
            if (format == null) {
                alloc(true, byteBuffer);
            } else {
                alloc(true, byteBuffer, format);
            }
        }

        @Override
        public void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);

            alloc(true, byteBuffer, format);
        }

        @Override
        public void alloc(boolean mutable, @NonNull ByteBuffer byteBuffer) {
            Preconditions.checkNotNull(byteBuffer);

            alloc(mutable, byteBuffer, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void alloc(boolean mutable, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(format);

            MethodHolder.setCurrentFormat(accessor.texture, format);

            if (mutable) {
                accessor.texImage3D(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.extentZ(), 0, format.format, format.type, byteBuffer);
            } else {
                accessor.texStorage3D(1, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.extentZ());
                accessor.texSubImage3D(0, 0, 0, 0, accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.extentZ(), format.format, format.type, byteBuffer);
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
                accessor.texImage3D(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.extentZ(), 0, format.format, format.type, null);
            } else {
                accessor.texStorage3D(1, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.extentZ());
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
