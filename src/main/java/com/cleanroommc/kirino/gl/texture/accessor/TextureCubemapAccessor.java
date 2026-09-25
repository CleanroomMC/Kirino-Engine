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
 *     <li><code>fetchCubeTexLevelParamI</code></li>
 *     <li><code>fetchCubeTexLevelParamF</code></li>
 *     <li><code>getCubeTexImage</code></li>
 *     <li><code>getCompressedCubeTexImage</code></li>
 *     <li><code>cubeTexImage2D</code></li>
 *     <li><code>cubeTexSubImage2D</code></li>
 *     <li><code>compressedCubeTexImage2D</code></li>
 *     <li><code>compressedCubeTexSubImage2D</code></li>
 *     <li><code>copyCubeTexSubImage2D</code></li>
 * </ul>
 */
public class TextureCubemapAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public TextureCubemapAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        Preconditions.checkState(texture.type == TextureType.CUBEMAP,
                "Texture type must be CUBEMAP.");

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
        return TextureType.CUBEMAP;
    }

    /**
     * Downloads all six faces in {@link CubeFace#layer} order.
     */
    @Override
    public void getTexImage(int level, int format, int type, @NonNull ByteBuffer data) {
        Preconditions.checkNotNull(data);

        if (dsa) {
            super.getTexImage(level, format, type, data);
        } else {
            for (CubeFace face : CubeFace.values()) {
                getCubeTexImage(face, level, format, type, faceSlice(data, face));
            }
        }
    }

    /**
     * Downloads all six compressed faces in {@link CubeFace#layer} order.
     */
    @Override
    public void getCompressedTexImage(int level, @NonNull ByteBuffer data) {
        Preconditions.checkNotNull(data);

        if (dsa) {
            super.getCompressedTexImage(level, data);
        } else {
            for (CubeFace face : CubeFace.values()) {
                getCompressedCubeTexImage(face, level, faceSlice(data, face));
            }
        }
    }

    /**
     * Queries the positive X face, matching the face implicitly selected by the
     * DSA texture-level query.
     *
     * @see #fetchCubeTexLevelParamI(CubeFace, int, int)
     */
    @Override
    public int fetchTexLevelParamI(int level, int pname) {
        return fetchCubeTexLevelParamI(CubeFace.POS_X, level, pname);
    }

    /**
     * Queries the positive X face, matching the face implicitly selected by the
     * DSA texture-level query.
     *
     * @see #fetchCubeTexLevelParamF(CubeFace, int, int)
     */
    @Override
    public float fetchTexLevelParamF(int level, int pname) {
        return fetchCubeTexLevelParamF(CubeFace.POS_X, level, pname);
    }

    /**
     * <p>Note: DSA texture-level queries cannot select a cubemap face and always
     * query positive X. Querying another face is only supported by the legacy path.</p>
     */
    @Override
    public int fetchCubeTexLevelParamI(@NonNull CubeFace face, int level, int pname) {
        Preconditions.checkNotNull(face);

        if (dsa) {
            Preconditions.checkArgument(face == CubeFace.POS_X,
                    "DSA texture-level queries only support the positive X cubemap face.");
            return super.fetchTexLevelParamI(level, pname);
        }
        return GL11.glGetTexLevelParameteri(face.glValue, level, pname);
    }

    /**
     * <p>Note: DSA texture-level queries cannot select a cubemap face and always
     * query positive X. Querying another face is only supported by the legacy path.</p>
     */
    @Override
    public float fetchCubeTexLevelParamF(@NonNull CubeFace face, int level, int pname) {
        Preconditions.checkNotNull(face);

        if (dsa) {
            Preconditions.checkArgument(face == CubeFace.POS_X,
                    "DSA texture-level queries only support the positive X cubemap face.");
            return super.fetchTexLevelParamF(level, pname);
        }
        return GL11.glGetTexLevelParameterf(face.glValue, level, pname);
    }

    /**
     * <p><i><b>Caution</b></i>: The DSA path uses the texture extent shadow state to
     * avoid an implicit texture level query. This is one of the few places that relies
     * on {@link GLTexture} shadow states to function reliably.</p>
     */
    @Override
    public void getCubeTexImage(
            @NonNull CubeFace face,
            int level,
            int format,
            int type,
            @NonNull ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkNotNull(data);

        if (dsa) {
            int width = mipExtent(texture.extentX(), level);
            int height = mipExtent(texture.extentY(), level);
            GL45.glGetTextureSubImage(
                    textureID(),
                    level,
                    0,
                    0,
                    face.layer,
                    width,
                    height,
                    1,
                    format,
                    type,
                    data);
        } else {
            GL11.glGetTexImage(face.glValue, level, format, type, data);
        }
    }

    /**
     * <p><i><b>Caution</b></i>: The DSA path uses the texture extent shadow state to
     * avoid an implicit texture level query. This is one of the few places that relies
     * on {@link GLTexture} shadow states to function reliably.</p>
     */
    @Override
    public void getCompressedCubeTexImage(
            @NonNull CubeFace face,
            int level,
            @NonNull ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkNotNull(data);

        if (dsa) {
            int width = mipExtent(texture.extentX(), level);
            int height = mipExtent(texture.extentY(), level);
            GL45.glGetCompressedTextureSubImage(
                    textureID(),
                    level,
                    0,
                    0,
                    face.layer,
                    width,
                    height,
                    1,
                    data);
        } else {
            GL13.glGetCompressedTexImage(face.glValue, level, data);
        }
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

    /**
     * <p>Note: Non-DSA only.</p>
     */
    @Override
    public void cubeTexImage2D(
            @NonNull CubeFace face,
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkState(!dsa, "DSA \"cubeTexImage2D\" is not implemented.");

        GL11.glTexImage2D(face.glValue, level, internalFormat, width, height, border, format, type, data);
    }

    @Override
    public void cubeTexSubImage2D(
            @NonNull CubeFace face,
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            @NonNull ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkNotNull(data);

        if (dsa) {
            GL45.glTextureSubImage3D(textureID(), level, xOffset, yOffset, face.layer, width, height, 1, format, type, data);
        } else {
            GL11.glTexSubImage2D(face.glValue, level, xOffset, yOffset, width, height, format, type, data);
        }
    }

    /**
     * <p>Note: Non-DSA only.</p>
     */
    @Override
    public void compressedCubeTexImage2D(
            @NonNull CubeFace face,
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            @Nullable ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkState(!dsa, "DSA \"compressedCubeTexImage2D\" is not implemented.");

        GL13.glCompressedTexImage2D(face.glValue, level, internalFormat, width, height, border, data);
    }

    @Override
    public void compressedCubeTexSubImage2D(
            @NonNull CubeFace face,
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            @NonNull ByteBuffer data) {

        Preconditions.checkNotNull(face);
        Preconditions.checkNotNull(data);

        if (dsa) {
            GL45.glCompressedTextureSubImage3D(textureID(), level, xOffset, yOffset, face.layer, width, height, 1, format, data);
        } else {
            GL13.glCompressedTexSubImage2D(face.glValue, level, xOffset, yOffset, width, height, format, data);
        }
    }

    /**
     * Copies a two-dimensional framebuffer region into one cubemap face.
     *
     * <p><b>Source</b>: <code>GL_READ_FRAMEBUFFER</code> + <code>GL_READ_BUFFER</code></p>
     */
    @Override
    public void copyCubeTexSubImage2D(
            @NonNull CubeFace face,
            int level,
            int xOffset,
            int yOffset,
            int x,
            int y,
            int width,
            int height) {

        Preconditions.checkNotNull(face);

        if (dsa) {
            GL45.glCopyTextureSubImage3D(textureID(), level, xOffset, yOffset, face.layer, x, y, width, height);
        } else {
            GL11.glCopyTexSubImage2D(face.glValue, level, xOffset, yOffset, x, y, width, height);
        }
    }

    private static ByteBuffer faceSlice(@NonNull ByteBuffer data, @NonNull CubeFace face) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(face);
        Preconditions.checkArgument(data.remaining() % CubeFace.values().length == 0,
                "Cubemap destination buffer size must be divisible by six.");

        int faceSize = data.remaining() / CubeFace.values().length;
        int faceStart = data.position() + Math.multiplyExact(face.layer, faceSize);
        ByteBuffer faceData = data.duplicate();
        faceData.position(faceStart);
        faceData.limit(Math.addExact(faceStart, faceSize));
        return faceData.slice().order(data.order());
    }

    private static int mipExtent(int baseExtent, int level) {
        Preconditions.checkArgument(level >= 0);
        Preconditions.checkState(baseExtent > 0,
                "Texture extent must be greater than zero.");

        return Math.max(1, baseExtent >> level);
    }

    private static class HighlevelOperatorImpl implements TextureAccessorHighlevel.HighlevelOperator {

        private final TextureCubemapAccessor accessor;

        private HighlevelOperatorImpl(TextureCubemapAccessor accessor) {
            this.accessor = accessor;
        }

        //<editor-fold desc="convenient allocation overloads">
        @Override
        public void resizeAndAllocEmpty(int width, int height) {
            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            TextureFormat format = accessor.texture.currentFormat();
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
            TextureFormat format = accessor.texture.currentFormat();
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
            checkDimensions();

            MethodHolder.setCurrentFormat(accessor.texture, format);

            if (mutable) {
                texImageAllFaces(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), 0, format.format, format.type, byteBuffer);
            } else {
                accessor.texStorage2D(1, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY());
                texSubImageAllFaces(0, 0, 0, accessor.texture.extentX(), accessor.texture.extentY(), format.format, format.type, byteBuffer);
            }
        }

        @Override
        public void allocEmpty(boolean mutable) {
            allocEmpty(mutable, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);
            checkDimensions();

            MethodHolder.setCurrentFormat(accessor.texture, format);

            if (mutable) {
                texImageAllFaces(0, format.internalFormat, accessor.texture.extentX(), accessor.texture.extentY(), 0, format.format, format.type, null);
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
            checkDimensions();

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
                    texImageAllFaces(0, format.internalFormat, width, height, 0, format.format, format.type, data);
                } else {
                    for (int level = 0; level < levels; level++) {
                        texImageAllFaces(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), 0, format.format, format.type, level == 0 ? data : null);
                    }
                }
            } else {
                accessor.texStorage2D(levels, format.internalFormat, width, height);
                texSubImageAllFaces(0, 0, 0, width, height, format.format, format.type, data);
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
            checkDimensions();

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
                    texImageAllFaces(level, format.internalFormat, mipExtent(width, level), mipExtent(height, level), 0, format.format, format.type, null);
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

            TextureFormat format = accessor.texture.currentFormat();

            Preconditions.checkState(format != null,
                    "Texture format has not been specified.");

            uploadLevel(level, data, format);
        }

        @Override
        public void uploadLevel(int level, @NonNull ByteBuffer data, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(level >= 0);

            texSubImageAllFaces(level, 0, 0, mipExtent(accessor.texture.extentX(), level), mipExtent(accessor.texture.extentY(), level), format.format, format.type, data);
        }

        @Override
        public void uploadSubImage(
                int level,
                int xOffset,
                int yOffset,
                int width,
                int height,
                @NonNull ByteBuffer data) {

            Preconditions.checkNotNull(data);
            Preconditions.checkArgument(level >= 0);

            TextureFormat format = accessor.texture.currentFormat();

            Preconditions.checkState(format != null,
                    "Texture format has not been specified.");

            uploadSubImage(level, xOffset, yOffset, width, height, data, format);
        }

        @Override
        public void uploadSubImage(
                int level,
                int xOffset,
                int yOffset,
                int width,
                int height,
                @NonNull ByteBuffer data,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(level >= 0);

            texSubImageAllFaces(level, xOffset, yOffset, width, height, format.format, format.type, data);
        }

        @Override
        public void clearLevel(int level, @Nullable ByteBuffer data) {
            Preconditions.checkArgument(level >= 0);

            TextureFormat format = accessor.texture.currentFormat();

            Preconditions.checkState(format != null,
                    "Texture format has not been specified.");

            clearLevel(level, data, format);
        }

        @Override
        public void clearLevel(int level, @Nullable ByteBuffer data, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(level >= 0);

            accessor.clearTexImage(level, format.format, format.type, data);
        }

        @Override
        public void downloadLevel(int level, @NonNull ByteBuffer data) {
            Preconditions.checkNotNull(data);
            Preconditions.checkArgument(level >= 0);

            TextureFormat format = accessor.texture.currentFormat();

            Preconditions.checkState(format != null,
                    "Texture format has not been specified.");

            downloadLevel(level, data, format);
        }

        @Override
        public void downloadLevel(int level, @NonNull ByteBuffer data, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(format);
            Preconditions.checkArgument(level >= 0);

            accessor.getTexImage(level, format.format, format.type, data);
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

        private void checkDimensions() {
            int width = accessor.texture.extentX();
            int height = accessor.texture.extentY();

            Preconditions.checkState(width == height,
                    "Cubemap width=%s must match height=%s.", width, height);
        }

        private void texImageAllFaces(
                int level,
                int internalFormat,
                int width,
                int height,
                int border,
                int format,
                int type,
                @Nullable ByteBuffer data) {

            for (CubeFace face : CubeFace.values()) {
                accessor.cubeTexImage2D(face, level, internalFormat, width, height, border, format, type, data);
            }
        }

        private void texSubImageAllFaces(
                int level,
                int xOffset,
                int yOffset,
                int width,
                int height,
                int format,
                int type,
                @Nullable ByteBuffer data) {

            for (CubeFace face : CubeFace.values()) {
                accessor.cubeTexSubImage2D(face, level, xOffset, yOffset, width, height, format, type, data);
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
