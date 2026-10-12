package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

public class DepthStencilAttachment implements FramebufferAttachment {

    public final Texture2DAccessor texture2D;
    public final GLRenderBuffer renderBuffer;
    public final boolean isTexture;
    public final boolean dsa;

    /**
     * Target-bound mode. {@link #resize(int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The texture accessor must be non-DSA.</p>
     */
    public DepthStencilAttachment(@NonNull Texture2DAccessor texture2D) {
        this(texture2D, false);
    }

    /**
     * <p>Note: The DSA status of the texture accessor must match <code>dsa</code>.</p>
     *
     * @param dsa This determines how {@link #resize(int, int)} is performed
     */
    public DepthStencilAttachment(@NonNull Texture2DAccessor texture2D, boolean dsa) {
        Preconditions.checkNotNull(texture2D);
        Preconditions.checkArgument(texture2D.isDsaOn() == dsa);

        this.texture2D = texture2D;
        renderBuffer = null;
        isTexture = true;
        this.dsa = dsa;
    }

    /**
     * Target-bound mode. {@link #resize(int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The renderbuffer must be non-DSA.</p>
     */
    public DepthStencilAttachment(@NonNull GLRenderBuffer renderBuffer) {
        this(renderBuffer, false);
    }

    /**
     * <p>Note: The DSA status of the renderbuffer must match <code>dsa</code>.</p>
     *
     * @param dsa This determines how {@link #resize(int, int)} is performed
     */
    public DepthStencilAttachment(@NonNull GLRenderBuffer renderBuffer, boolean dsa) {
        Preconditions.checkNotNull(renderBuffer);
        Preconditions.checkArgument(renderBuffer.dsa == dsa);

        texture2D = null;
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
    public void resize(int width, int height) {
        if (isTexture) {
            if (dsa) {
                texture2D.highlevel().resizeAndAllocEmpty(width, height);
            } else {
                texture2D.bind();
                texture2D.highlevel().resizeAndAllocEmpty(width, height);
                texture2D.bind(0);
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
