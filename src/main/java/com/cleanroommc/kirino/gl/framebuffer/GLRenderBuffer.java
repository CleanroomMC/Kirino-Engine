package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

public class GLRenderBuffer extends GLDisposable {

    public final int rboID;
    public final boolean dsa;
    private int width;
    private int height;
    private TextureFormat currentFormat = null;

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Nullable
    public TextureFormat currentFormat() {
        return currentFormat;
    }

    /**
     * Target-bound path.
     */
    public GLRenderBuffer(int width, int height) {
        this(width, height, false);
    }

    /**
     * @param dsa Chooses target-bound path or DSA path
     */
    public GLRenderBuffer(int width, int height, boolean dsa) {
        this.width = width;
        this.height = height;
        this.dsa = dsa;
        rboID = dsa ? GL45.glCreateRenderbuffers() : GL30.glGenRenderbuffers();
        GLResourceManager.addDisposable(this);
    }

    public static void bind(int rboID) {
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID);
    }

    public void bind() {
        bind(rboID);
    }

    /**
     * Allocates storage in target-bound mode or DSA mode.
     */
    public void alloc(@NonNull TextureFormat format) {
        Preconditions.checkNotNull(format);

        currentFormat = format;
        if (dsa) {
            GL45.glNamedRenderbufferStorage(rboID, format.internalFormat, width, height);
        } else {
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format.internalFormat, width, height);
        }
    }

    /**
     * Allocates with {@link TextureFormat#RGBA8_UNORM}.
     *
     * @see #alloc(TextureFormat)
     */
    public void alloc() {
        alloc(TextureFormat.RGBA8_UNORM);
    }

    /**
     * Resizes storage in target-bound mode or DSA mode.
     *
     * <p>Note: It resizes with the previously specified texture format. If no format has been specified,
     * it calls {@link #alloc()}.</p>
     */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (currentFormat == null) {
            alloc();
        } else {
            alloc(currentFormat);
        }
    }

    /**
     * Resizes storage in target-bound mode or DSA mode.
     */
    public void resize(int width, int height, @NonNull TextureFormat format) {
        Preconditions.checkNotNull(format);

        this.width = width;
        this.height = height;
        alloc(format);
    }

    @Override
    protected void dispose() {
        GL30.glDeleteRenderbuffers(rboID);
    }
}
