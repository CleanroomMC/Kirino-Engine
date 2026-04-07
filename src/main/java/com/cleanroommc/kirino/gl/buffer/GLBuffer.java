package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL15;

public class GLBuffer extends GLDisposable {
    public final int bufferID;

    /**
     * <p>Notice: buffers do not have an intrinsic or fixed type in either OpenGL or our abstraction layer.
     * In OpenGL, a buffer object is merely interpreted according to the binding
     * target it is associated with after a <code>bind</code> call using buffer view classes.
     * However, buffers still don't gain a fixed type after <code>bind</code> but you should respect
     * its first binding target type in most cases.</p>
     */
    public GLBuffer() {
        bufferID = GL15.glGenBuffers();
        GLResourceManager.addDisposable(this);
    }

    @Override
    public void dispose() {
        GL15.glDeleteBuffers(bufferID);
    }
}
