package com.cleanroommc.kirino.gl.texture;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

public class GLTexture extends GLDisposable {

    /**
     * So called "texture name" under OpenGL context.
     */
    public final int textureID;
    public final TextureType type;

    protected int extentX = 0;
    protected int extentY = 0;
    protected int extentZ = 0;
    protected int layers = 0;
    protected int samples = 0;

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

    protected TextureFormat currentFormat = null;

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
    public static GLTexture newDsaTexBuffe() {
        return newTexBuffer(true, false);
    }

    public void dispose() {
        GL11.glDeleteTextures(textureID);
    }
}
