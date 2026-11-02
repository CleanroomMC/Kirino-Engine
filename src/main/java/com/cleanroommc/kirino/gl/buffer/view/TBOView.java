package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL44;

public class TBOView extends BufferView {

    public TBOView(GLBuffer buffer) {
        super(buffer);
    }

    public TBOView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL31.GL_TEXTURE_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL44.GL_TEXTURE_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, bufferID);
    }
}
