package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

public class PixelPackBufferView extends BufferView {

    public PixelPackBufferView(GLBuffer buffer) {
        super(buffer);
    }

    public PixelPackBufferView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL21.GL_PIXEL_PACK_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL21.GL_PIXEL_PACK_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, bufferID);
    }
}
