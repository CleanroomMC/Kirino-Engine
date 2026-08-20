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
 *     <li><code>texStorage2D</code></li>
 *     <li><code>texImage2D</code></li>
 *     <li><code>texSubImage2D</code></li>
 *     <li><code>compressedTexImage2D</code></li>
 *     <li><code>compressedTexSubImage2D</code></li>
 *     <li><code>copyTexSubImage2D</code></li>
 * </ul>
 */
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
            @NonNull ByteBuffer data) {

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

        //<editor-fold desc="convenient allocation overloads">
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
        public void resizeAndAlloc(int width, int height, @NonNull ByteBuffer byteBuffer) {
            Preconditions.checkNotNull(byteBuffer);

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
        public void resizeAndAlloc(int width, int height, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(byteBuffer);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);

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
        //</editor-fold>

        //<editor-fold desc="canonical allocation methods">
        @Override
        public void resizeAndAllocEmpty(int width, int height, @NonNull StorageOptions options, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            allocEmpty(options, format);
        }

        @Override
        public void resizeAndAlloc(int width, int height, @NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            alloc(options, data, format, generateMipmaps);
        }

        @Override
        public void alloc(@NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);

            int width = accessor.texture.extentX();
            int height = accessor.texture.extentY();
            int levels = options.levels();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(levels > 0);

            int maxMipmapCount = accessor.texture.maxMipmapLevelCount();
            Preconditions.checkState(levels <= maxMipmapCount,
                    "Too many mipmap levels. Input=%s; Expected Max=%s",
                    levels, maxMipmapCount);

            setMipRange(levels);

            if (options.mutable()) {
                if (generateMipmaps) {
                    accessor.texImage2D(0, format.internalFormat, width, height, 0, format.format, format.type, data);
                } else {
                    for (int level = 0; level < levels; level++) {
                        accessor.texImage2D(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), 0, format.format, format.type, level == 0 ? data : null);
                    }
                }
            } else {
                accessor.texStorage2D(levels, format.internalFormat, width, height);
                accessor.texSubImage2D(0, 0, 0, width, height, format.format, format.type, data);
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
            int levels = options.levels();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(levels > 0);

            int maxMipmapCount = accessor.texture.maxMipmapLevelCount();
            Preconditions.checkState(levels <= maxMipmapCount,
                    "Too many mipmap levels. Input=%s; Expected Max=%s",
                    levels, maxMipmapCount);

            setMipRange(levels);

            if (options.mutable()) {
                for (int level = 0; level < levels; level++) {
                    accessor.texImage2D(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), 0, format.format, format.type, null);
                }
            } else {
                accessor.texStorage2D(levels, format.internalFormat, width, height);
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

            accessor.texSubImage2D(level, 0, 0, mipExtent(accessor.texture.extentX(), level), mipExtent(accessor.texture.extentY(), level), format.format, format.type, data);
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
