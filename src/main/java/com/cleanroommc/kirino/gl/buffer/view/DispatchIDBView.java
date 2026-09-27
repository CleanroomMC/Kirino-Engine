package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

public class DispatchIDBView extends BufferView {

    public DispatchIDBView(GLBuffer buffer) {
        super(buffer);
    }

    public DispatchIDBView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL43.GL_DISPATCH_INDIRECT_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL43.GL_DISPATCH_INDIRECT_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL43.GL_DISPATCH_INDIRECT_BUFFER, bufferID);
    }
}
