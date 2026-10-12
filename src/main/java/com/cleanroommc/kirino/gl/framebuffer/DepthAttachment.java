package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;

public class DepthAttachment implements FramebufferAttachment {

    public final Texture2DAccessor texture2D;
    public final boolean dsa;

    /**
     * Target-bound mode. {@link #resize(int, int)} will be performed via the target-bound mode.
     *
     * <p>Note: The texture accessor must be non-DSA.</p>
     */
    public DepthAttachment(@NonNull Texture2DAccessor texture2D) {
        this(texture2D, false);
    }

    /**
     * <p>Note: The DSA status of the texture accessor must match <code>dsa</code>.</p>
     *
     * @param dsa This determines how {@link #resize(int, int)} is performed
     */
    public DepthAttachment(@NonNull Texture2DAccessor texture2D, boolean dsa) {
        Preconditions.checkNotNull(texture2D);
        Preconditions.checkArgument(texture2D.isDsaOn() == dsa);

        this.texture2D = texture2D;
        this.dsa = dsa;
    }

    @Override
    public void attach() {
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texture2D.target(), texture2D.texture.textureID, 0);
    }

    @Override
    public void attach(int fboID) {
        GL45.glNamedFramebufferTexture(fboID, GL30.GL_DEPTH_ATTACHMENT, texture2D.texture.textureID, 0);
    }

    @Override
    public AttachmentKind kind() {
        return AttachmentKind.DEPTH;
    }

    @Override
    public void resize(int width, int height) {
        if (dsa) {
            texture2D.highlevel().resizeAndAllocEmpty(width, height);
        } else {
            texture2D.bind();
            texture2D.highlevel().resizeAndAllocEmpty(width, height);
            texture2D.bind(0);
        }
    }
}
