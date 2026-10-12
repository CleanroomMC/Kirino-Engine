package com.cleanroommc.kirino.gl.framebuffer;

import com.cleanroommc.kirino.gl.framebuffer.meta.AttachmentKind;

public interface FramebufferAttachment {

    /**
     * Attaches to the currently bound framebuffer using the target-bound path.
     */
    void attach();

    /**
     * Attaches directly to the named framebuffer using the DSA path.
     */
    void attach(int fboID);

    /**
     * @return Returns {@link AttachmentKind#COLOR} or {@link AttachmentKind#DEPTH}.
     */
    AttachmentKind kind();

    /**
     * Resizes the backing storage using target-bound or DSA path.
     */
    void resize(int width, int height);
}
