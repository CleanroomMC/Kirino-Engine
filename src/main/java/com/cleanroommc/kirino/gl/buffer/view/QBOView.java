package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL44;

public class QBOView extends BufferView {

    public QBOView(GLBuffer buffer) {
        super(buffer);
    }

    public QBOView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL44.GL_QUERY_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL44.GL_QUERY_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL44.GL_QUERY_BUFFER, bufferID);
    }
}
