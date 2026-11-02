package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL42;

public class ACBOView extends BufferView {

    public ACBOView(GLBuffer buffer) {
        super(buffer);
    }

    public ACBOView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL42.GL_ATOMIC_COUNTER_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL42.GL_ATOMIC_COUNTER_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL42.GL_ATOMIC_COUNTER_BUFFER, bufferID);
    }
}
