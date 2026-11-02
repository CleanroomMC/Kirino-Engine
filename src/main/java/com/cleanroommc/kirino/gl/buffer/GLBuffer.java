package com.cleanroommc.kirino.gl.buffer;

import com.cleanroommc.kirino.gl.GLDisposable;
import com.cleanroommc.kirino.gl.GLResourceManager;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL45;

/**
 * An owned OpenGL buffer object.
 *
 * <p>Buffers have no intrinsic target. A target only selects how OpenGL interprets a buffer for a
 * particular operation; binding a buffer does not permanently assign that target to the object.</p>
 *
 * <p>Legacy creation with {@code glGenBuffers} reserves a buffer name but does not instantiate the
 * object until its first bind. DSA creation with {@code glCreateBuffers} creates the object
 * immediately. Once instantiated, either buffer can be used through both target-bound and DSA entry points.</p>
 */
public class GLBuffer extends GLDisposable {

    public final int bufferID;

    private GLBuffer(int bufferID) {
        this.bufferID = bufferID;
        GLResourceManager.addDisposable(this);
    }

    private static int createBuffer(boolean dsa) {
        if (dsa) {
            return GL45.glCreateBuffers();
        } else {
            return GL15.glGenBuffers();
        }
    }

    /**
     * Creates a legacy buffer name with {@code glGenBuffers}.
     */
    public GLBuffer() {
        this(createBuffer(false));
    }

    /**
     * Creates a buffer using either legacy or DSA creation.
     */
    public GLBuffer(boolean dsa) {
        this(createBuffer(dsa));
    }

    @Override
    protected void dispose() {
        GL15.glDeleteBuffers(bufferID);
    }
}
