package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;

public class VBOView extends BufferView {
    public VBOView(GLBuffer buffer) {
        super(buffer);
    }

    @Override
    public int target() {
        return GL15.GL_ARRAY_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL15.GL_ARRAY_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferID);
    }
}
