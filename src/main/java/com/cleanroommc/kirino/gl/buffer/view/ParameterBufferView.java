package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL46;

public class ParameterBufferView extends BufferView {

    public ParameterBufferView(GLBuffer buffer) {
        super(buffer);
    }

    public ParameterBufferView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL46.GL_PARAMETER_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL46.GL_PARAMETER_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL46.GL_PARAMETER_BUFFER, bufferID);
    }
}
