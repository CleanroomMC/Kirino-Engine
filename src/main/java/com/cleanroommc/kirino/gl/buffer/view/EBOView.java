package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;

public class EBOView extends BufferView {
    public EBOView(GLBuffer buffer) {
        super(buffer);
    }

    @Override
    public int target() {
        return GL15.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, bufferID);
    }
}
