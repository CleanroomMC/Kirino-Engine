package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.util.function.BiFunction;

public class ColorAttachment2D implements FramebufferAttachment {

    public final int index;
    public Texture2DAccessor texture2D;
    public final @Nullable BiFunction<Integer, Integer, Texture2DAccessor> texture2DFactory;
    public final boolean dsa;

    /**
     * Target-bound mode. {@link #resize(int, int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The texture accessor must be non-DSA.</p>
     */
    public ColorAttachment2D(int index, @NonNull Texture2DAccessor texture2D) {
        Preconditions.checkNotNull(texture2D);
        Preconditions.checkState(!texture2D.isDsaOn());

        this.index = index;
        this.texture2D = texture2D;
        texture2DFactory = null;
        dsa = false;
    }

    /**
     * DSA mode. {@link #resize(int, int, int)} will be performed via the DSA mode.
     *
     * <p>Note: The initial texture accessor must be DSA.</p>
     * <p>Note: <code>texture2DFactory</code> must be a DSA texture factory
     * (Texture ID: DSA-created or bound once; Texture Accessor: DSA).</p>
     *
     * @param texture2DFactory Input: width, height; Output: allocated DSA texture
     */
    public ColorAttachment2D(
            int index,
            @NonNull Texture2DAccessor initialTexture2D,
            @NonNull BiFunction<Integer, Integer, Texture2DAccessor> texture2DFactory) {

        Preconditions.checkNotNull(initialTexture2D);
        Preconditions.checkState(initialTexture2D.isDsaOn());
        Preconditions.checkNotNull(texture2DFactory);

        this.index = index;
        texture2D = initialTexture2D;
        this.texture2DFactory = texture2DFactory;
        dsa = true;
    }

    @Override
    public void attach() {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + index, texture2D.target(), texture2D.texture.textureID, 0);
    }

    @Override
    public void attach(int fboID) {
        GL45.glNamedFramebufferTexture(fboID, GL30.GL_COLOR_ATTACHMENT0 + index, texture2D.texture.textureID, 0);
    }

    @Override
    public AttachmentKind kind() {
        return AttachmentKind.COLOR;
    }

    @Override
    public void resize(int width, int height, int fboID) {
        if (dsa) {
            Preconditions.checkNotNull(texture2DFactory);
            Texture2DAccessor newTexture2D = Preconditions.checkNotNull(texture2DFactory.apply(width, height));
            Preconditions.checkState(newTexture2D.isDsaOn());

            Texture2DAccessor oldTexture2D = texture2D;
            texture2D = newTexture2D;

            attach(fboID); // overrides the old one

            oldTexture2D.texture.disposeManually();
        } else {
            texture2D.bind();
            texture2D.highlevel().resizeAndAllocEmpty(width, height);
            texture2D.bind(0);
        }
    }
}
