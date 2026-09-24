package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.accessor.TextureAccessorHighlevel;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

/**
 * <b>Raw OpenGL Interoperability:</b>
 *
 * <p>Texture metadata, <b><i>including {@link #extentX}, {@link #extentY},
 * {@link #extentZ}, {@link #layers}, {@link #samples}, {@link #currentFormat}</i></b>,
 * is shadowed by this object.</p>
 *
 * <p>The shadow state is updated only by operations performed through {@link TextureAccessorHighlevel.HighlevelOperator}.
 * Direct OpenGL operations that allocate or redefine texture storage do not update
 * it and may therefore leave the wrapper desynchronized from the underlying GL object.</p>
 *
 * <p>Note: Users must either keep allocation operations within {@link TextureAccessorHighlevel.HighlevelOperator}
 * or manually restore the internal shadow state via <code>setXXXInternal</code>.
 * This GL abstraction layer does not attempt to detect or recover from external mutations.</p>
 */
public class GLTexture extends GLDisposable {

    /**
     * So-called "texture name" under OpenGL DSA context.
     */
    public final int textureID;
    public final TextureType type;

    private int extentX = 0;
    private int extentY = 0;
    private int extentZ = 0;
    private int layers = 0;
    private int samples = 0;
    private TextureFormat currentFormat = null;

    /**
     * @return Width; x-dim
     */
    public int extentX() {
        return extentX;
    }

    /**
     * @return Height; y-dim
     */
    public int extentY() {
        return extentY;
    }

    /**
     * @return Depth; z-dim
     */
    public int extentZ() {
        return extentZ;
    }

    public int layers() {
        return layers;
    }

    public int samples() {
        return samples;
    }

    /**
     * @return The format specified by the latest high-level allocation, or <code>null</code>
     * if no high-level allocation has specified it yet
     */
    @Nullable
    public TextureFormat currentFormat() {
        return currentFormat;
    }

    //<editor-fold desc="internal">
    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setExtentXInternal(int extentX) {
        this.extentX = extentX;
    }

    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setExtentYInternal(int extentY) {
        this.extentY = extentY;
    }

    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setExtentZInternal(int extentZ) {
        this.extentZ = extentZ;
    }

    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setLayersInternal(int layers) {
        this.layers = layers;
    }

    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setSamplesInternal(int samples) {
        this.samples = samples;
    }

    /**
     * <b>No need to use in most of the scenarios!</b>
     * <p>Note: Only call it when you want to continue using {@link TextureAccessorHighlevel.HighlevelOperator}
     * reliably and safely, <i>AND</i> you're sure that the internal shadow state is desynchronized!</p>
     *
     * @see TextureAccessorHighlevel.HighlevelOperator#highlevel()
     */
    public void setCurrentFormatInternal(@NonNull TextureFormat format) {
        Preconditions.checkNotNull(format);

        currentFormat = format;
    }
    //</editor-fold>

    //<editor-fold desc="constructors">
    private GLTexture(int textureID, TextureType type) {
        this.textureID = textureID;
        this.type = type;
    }

    private static int createTexture(TextureType type, boolean dsa) {
        if (dsa) {
            return GL45.glCreateTextures(type.glValue);
        } else {
            return GL11.glGenTextures();
        }
    }

    private static void legacyInitBind(TextureType type, int id) {
        int prev = GL11.glGetInteger(type.bindingTarget());
        GL11.glBindTexture(type.glValue, id);
        GL11.glBindTexture(type.glValue, prev);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex1D(boolean dsa, boolean forceInit, int extentX) {
        int id = createTexture(TextureType.TEX_1D, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_1D, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_1D);
        tex.extentX = extentX;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex1D(boolean, boolean, int)
     */
    @NonNull
    public static GLTexture newDsaTex1D(int extentX) {
        return newTex1D(true, false, extentX);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex2D(boolean dsa, boolean forceInit, int extentX, int extentY) {
        int id = createTexture(TextureType.TEX_2D, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_2D, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_2D);
        tex.extentX = extentX;
        tex.extentY = extentY;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex2D(boolean, boolean, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex2D(int extentX, int extentY) {
        return newTex2D(true, false, extentX, extentY);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex3D(boolean dsa, boolean forceInit, int extentX, int extentY, int extentZ) {
        int id = createTexture(TextureType.TEX_3D, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_3D, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_3D);
        tex.extentX = extentX;
        tex.extentY = extentY;
        tex.extentZ = extentZ;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex3D(boolean, boolean, int, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex3D(int extentX, int extentY, int extentZ) {
        return newTex3D(true, false, extentX, extentY, extentZ);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex1DArray(boolean dsa, boolean forceInit, int extentX, int layers) {
        int id = createTexture(TextureType.TEX_1D_ARRAY, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_1D_ARRAY, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_1D_ARRAY);
        tex.extentX = extentX;
        tex.layers = layers;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex1DArray(boolean, boolean, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex1DArray(int extentX, int layers) {
        return newTex1DArray(true, false, extentX, layers);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex2DArray(boolean dsa, boolean forceInit, int extentX, int extentY, int layers) {
        int id = createTexture(TextureType.TEX_2D_ARRAY, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_2D_ARRAY, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_2D_ARRAY);
        tex.extentX = extentX;
        tex.extentY = extentY;
        tex.layers = layers;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex2DArray(boolean, boolean, int, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex2DArray(int extentX, int extentY, int layers) {
        return newTex2DArray(true, false, extentX, extentY, layers);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newCubemap(boolean dsa, boolean forceInit, int extent) {
        int id = createTexture(TextureType.CUBEMAP, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.CUBEMAP, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.CUBEMAP);
        tex.extentX = extent;
        tex.extentY = extent;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newCubemap(boolean, boolean, int)
     */
    @NonNull
    public static GLTexture newDsaCubemap(int extent) {
        return newCubemap(true, false, extent);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newCubemapArray(boolean dsa, boolean forceInit, int extent, int cubeCount) {
        int id = createTexture(TextureType.CUBEMAP_ARRAY, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.CUBEMAP_ARRAY, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.CUBEMAP_ARRAY);
        tex.extentX = extent;
        tex.extentY = extent;
        tex.layers = cubeCount;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newCubemapArray(boolean, boolean, int, int)
     */
    @NonNull
    public static GLTexture newDsaCubemapArray(int extent, int cubeCount) {
        return newCubemapArray(true, false, extent, cubeCount);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex2DMS(boolean dsa, boolean forceInit, int extentX, int extentY, int samples) {
        int id = createTexture(TextureType.TEX_2D_MS, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_2D_MS, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_2D_MS);
        tex.extentX = extentX;
        tex.extentY = extentY;
        tex.samples = samples;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex2DMS(boolean, boolean, int, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex2DMS(int extentX, int extentY, int samples) {
        return newTex2DMS(true, false, extentX, extentY, samples);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTex2DMSArray(boolean dsa, boolean forceInit, int extentX, int extentY, int layers, int samples) {
        int id = createTexture(TextureType.TEX_2D_MS_ARRAY, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_2D_MS_ARRAY, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_2D_MS_ARRAY);
        tex.extentX = extentX;
        tex.extentY = extentY;
        tex.layers = layers;
        tex.samples = samples;

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTex2DMSArray(boolean, boolean, int, int, int, int)
     */
    @NonNull
    public static GLTexture newDsaTex2DMSArray(int extentX, int extentY, int layers, int samples) {
        return newTex2DMSArray(true, false, extentX, extentY, layers, samples);
    }

    /**
     * <p>Notice: legacy OpenGL textures and DSA textures use different mechanism regarding texture types.
     * Legacy OpenGL creates a typeless texture name and the actual texture type is fixed after its first bind.
     * However, DSA textures get their name and type altogether in one creation call.
     * <br>
     * Unlike OpenGL buffers, textures do have an intrinsic type.
     * Regarding the legacy behavior vs DSA behavior, our abstraction layer
     * enforces a Java level immutable type regardless.</p>
     *
     * @param dsa Whether to use DSA or legacy texture creation
     * @param forceInit Whether to bind and unbind to fix the texture type if <code>dsa == false</code>
     */
    @NonNull
    public static GLTexture newTexBuffer(boolean dsa, boolean forceInit) {
        int id = createTexture(TextureType.TEX_BUFFER, dsa);

        if (!dsa && forceInit) {
            legacyInitBind(TextureType.TEX_BUFFER, id);
        }

        GLTexture tex = new GLTexture(id, TextureType.TEX_BUFFER);

        GLResourceManager.addDisposable(tex);
        return tex;
    }

    /**
     * @see #newTexBuffer(boolean, boolean)
     */
    @NonNull
    public static GLTexture newDsaTexBuffer() {
        return newTexBuffer(true, false);
    }
    //</editor-fold>

    /**
     * It calculates the maximum mipmap level index of a full mipmap chain based on
     * the current base-level extents.
     *
     * <p>Note: This is GL agnostic.</p>
     * <p>Note: It relies on the shadow states maintained by this wrapper.</p>
     */
    public int maxMipmapLevel() {
        Preconditions.checkState(type.supportsMipmaps(),
                "This texture type \"%s\" doesn't support mipmaps.", type);

        int maxExtent = switch (type) {
            case TEX_1D,
                 TEX_1D_ARRAY -> extentX;

            case TEX_2D,
                 TEX_2D_ARRAY,
                 CUBEMAP,
                 CUBEMAP_ARRAY -> Math.max(extentX, extentY);

            case TEX_3D -> Math.max(extentX, Math.max(extentY, extentZ));

            case TEX_2D_MS,
                 TEX_2D_MS_ARRAY,
                 TEX_BUFFER -> 0;
        };

        Preconditions.checkState(maxExtent > 0,
                "Cannot calculate mipmap levels for \"%s\" with non-greater-than-zero base extent \"%s\".",
                type,
                maxExtent);

        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(maxExtent);
    }

    /**
     * It calculates the number of levels in a complete mipmap chain based on
     * the current base-level extents.
     *
     * <p>Note: This is GL agnostic.</p>
     * <p>Note: It relies on the shadow states maintained by this wrapper.</p>
     */
    public int maxMipmapLevelCount() {
        Preconditions.checkState(type.supportsMipmaps(),
                "This texture type \"%s\" doesn't support mipmaps.", type);

        return maxMipmapLevel() + 1;
    }

    protected void dispose() {
        GL11.glDeleteTextures(textureID);
    }
}
