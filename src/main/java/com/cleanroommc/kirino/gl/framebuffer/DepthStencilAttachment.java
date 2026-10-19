package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

import java.util.function.BiFunction;

public class DepthStencilAttachment implements FramebufferAttachment {

    public Texture2DAccessor texture2D;
    public final @Nullable BiFunction<Integer, Integer, Texture2DAccessor> texture2DFactory;
    public final GLRenderBuffer renderBuffer;
    public final boolean isTexture;
    public final boolean dsa;

    /**
     * Target-bound mode. {@link #resize(int, int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The texture accessor must be non-DSA.</p>
     */
    public DepthStencilAttachment(@NonNull Texture2DAccessor texture2D) {
        Preconditions.checkNotNull(texture2D);
        Preconditions.checkState(!texture2D.isDsaOn());

        this.texture2D = texture2D;
        texture2DFactory = null;
        renderBuffer = null;
        isTexture = true;
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
    public DepthStencilAttachment(
            @NonNull Texture2DAccessor initialTexture2D,
            @NonNull BiFunction<Integer, Integer, Texture2DAccessor> texture2DFactory) {

        Preconditions.checkNotNull(initialTexture2D);
        Preconditions.checkState(initialTexture2D.isDsaOn());
        Preconditions.checkNotNull(texture2DFactory);

        texture2D = initialTexture2D;
        this.texture2DFactory = texture2DFactory;
        renderBuffer = null;
        isTexture = true;
        dsa = true;
    }

    /**
     * Target-bound mode. {@link #resize(int, int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The renderbuffer must be non-DSA.</p>
     */
    public DepthStencilAttachment(@NonNull GLRenderBuffer renderBuffer) {
        this(renderBuffer, false);
    }

    /**
     * <p>Note: The DSA status of the renderbuffer must match <code>dsa</code>.</p>
     *
     * @param dsa This determines how {@link #resize(int, int, int)} is performed
     */
    public DepthStencilAttachment(@NonNull GLRenderBuffer renderBuffer, boolean dsa) {
        Preconditions.checkNotNull(renderBuffer);
        Preconditions.checkArgument(renderBuffer.dsa == dsa);

        texture2D = null;
        texture2DFactory = null;
        this.renderBuffer = renderBuffer;
        isTexture = false;
        this.dsa = dsa;
    }

    @Override
    public void attach() {
        if (isTexture) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, texture2D.target(), texture2D.texture.textureID, 0);
        } else {
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, renderBuffer.rboID);
        }
    }

    @Override
    public void attach(int fboID) {
        if (isTexture) {
            GL45.glNamedFramebufferTexture(fboID, GL30.GL_DEPTH_STENCIL_ATTACHMENT, texture2D.texture.textureID, 0);
        } else {
            GL45.glNamedFramebufferRenderbuffer(fboID, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, renderBuffer.rboID);
        }
    }

    @Override
    public AttachmentKind kind() {
        return AttachmentKind.DEPTH;
    }

    @Override
    public void resize(int width, int height, int fboID) {
        if (isTexture) {
            if (dsa) {
                Preconditions.checkNotNull(texture2DFactory);
                Texture2DAccessor newTexture2D = Preconditions.checkNotNull(texture2DFactory.apply(width, height));
                Preconditions.checkState(newTexture2D.isDsaOn());

                Texture2DAccessor oldTexture2D = texture2D;
                texture2D = newTexture2D;

                attach(fboID); // overrides the old one

                oldTexture2D.texture.disposeManually();
            } else {
                texture2D.highlevel().resizeAndAllocEmpty(width, height);
            }
        } else {
            if (dsa) {
                renderBuffer.resize(width, height);
            } else {
                renderBuffer.bind();
                renderBuffer.resize(width, height);
                GLRenderBuffer.bind(0);
            }
        }
    }
}
