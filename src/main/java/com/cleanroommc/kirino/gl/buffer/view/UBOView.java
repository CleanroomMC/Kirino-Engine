package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

public class UBOView extends BufferView {

    public UBOView(GLBuffer buffer) {
        super(buffer);
    }

    public UBOView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL31.GL_UNIFORM_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL31.GL_UNIFORM_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bufferID);
    }
}
