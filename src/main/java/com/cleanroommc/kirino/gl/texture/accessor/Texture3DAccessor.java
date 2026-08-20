package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

/**
 * <p>Available raw GL operations:</p>
 * <ul>
 *     <li><code>texStorage3D</code></li>
 *     <li><code>texImage3D</code></li>
 *     <li><code>texSubImage3D</code></li>
 *     <li><code>compressedTexImage3D</code></li>
 *     <li><code>compressedTexSubImage3D</code></li>
 *     <li><code>copyTexSubImage3D</code></li>
 * </ul>
 */
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

        //<editor-fold desc="convenient allocation overloads">
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
        //</editor-fold>

        //<editor-fold desc="canonical allocation methods">
        @Override
        public void resizeAndAllocEmpty(int width, int height, int depthOrLayers, @NonNull StorageOptions options, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);
            allocEmpty(options, format);
        }

        @Override
        public void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setExtentZ(accessor.texture, depthOrLayers);
            alloc(options, data, format, generateMipmaps);
        }

        @Override
        public void alloc(@NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);

            int width = accessor.texture.extentX();
            int height = accessor.texture.extentY();
            int depth = accessor.texture.extentZ();
            int levels = options.levels();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(depth > 0);
            Preconditions.checkState(levels > 0);

            int maxMipmapCount = accessor.texture.maxMipmapLevelCount();
            Preconditions.checkState(levels <= maxMipmapCount,
                    "Too many mipmap levels. Input=%s; Expected Max=%s",
                    levels, maxMipmapCount);

            setMipRange(levels);

            if (options.mutable()) {
                if (generateMipmaps) {
                    accessor.texImage3D(0, format.internalFormat, width, height, depth, 0, format.format, format.type, data);
                } else {
                    for (int level = 0; level < levels; level++) {
                        accessor.texImage3D(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), mipExtent(depth, level), 0, format.format, format.type, level == 0 ? data : null);
                    }
                }
            } else {
                accessor.texStorage3D(levels, format.internalFormat, width, height, depth);
                accessor.texSubImage3D(0, 0, 0, 0, width, height, depth, format.format, format.type, data);
            }

            if (generateMipmaps && levels > 1) {
                accessor.genMipmap();
            }

            MethodHolder.setCurrentFormat(accessor.texture, format);
        }

        @Override
        public void allocEmpty(@NonNull StorageOptions options, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            int width = accessor.texture.extentX();
            int height = accessor.texture.extentY();
            int depth = accessor.texture.extentZ();
            int levels = options.levels();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(depth > 0);
            Preconditions.checkState(levels > 0);

            int maxMipmapCount = accessor.texture.maxMipmapLevelCount();
            Preconditions.checkState(levels <= maxMipmapCount,
                    "Too many mipmap levels. Input=%s; Expected Max=%s",
                    levels, maxMipmapCount);

            setMipRange(levels);

            if (options.mutable()) {
                for (int level = 0; level < levels; level++) {
                    accessor.texImage3D(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), mipExtent(depth, level), 0, format.format, format.type, null);
                }
            } else {
                accessor.texStorage3D(levels, format.internalFormat, width, height, depth);
            }

            MethodHolder.setCurrentFormat(accessor.texture, format);
        }
        //</editor-fold>

        @Override
        public void uploadLevel(int level, @NonNull ByteBuffer data) {
            Preconditions.checkNotNull(data);
            Preconditions.checkArgument(level >= 0);

            TextureFormat format = MethodHolder.getCurrentFormat(accessor.texture);

            Preconditions.checkState(format != null,
                    "Texture format has not been specified.");

            uploadLevel(level, data, format);
        }

        @Override
        public void uploadLevel(int level, @NonNull ByteBuffer data, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(level >= 0);

            accessor.texSubImage3D(level, 0, 0, 0, mipExtent(accessor.texture.extentX(), level), mipExtent(accessor.texture.extentY(), level), mipExtent(accessor.texture.extentZ(), level), format.format, format.type, data);
        }

        @Override
        public void generateMipmaps() {
            accessor.genMipmap();
        }

        @Override
        public void setMipRange(int levels) {
            accessor.texParamI(GL12.GL_TEXTURE_BASE_LEVEL, 0);
            accessor.texParamI(GL12.GL_TEXTURE_MAX_LEVEL, levels - 1);
        }

        private static int mipExtent(int baseExtent, int level) {
            return Math.max(1, baseExtent >> level);
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
