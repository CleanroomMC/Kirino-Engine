package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public class TFBOView extends BufferView {

    public TFBOView(GLBuffer buffer) {
        super(buffer);
    }

    public TFBOView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL30.GL_TRANSFORM_FEEDBACK_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, bufferID);
    }
}
