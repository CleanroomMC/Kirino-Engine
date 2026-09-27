package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;

public class PixelUnpackBufferView extends BufferView {

    public PixelUnpackBufferView(GLBuffer buffer) {
        super(buffer);
    }

    public PixelUnpackBufferView(GLBuffer buffer, boolean dsa) {
        super(buffer, dsa);
    }

    @Override
    public int target() {
        return GL21.GL_PIXEL_UNPACK_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL21.GL_PIXEL_UNPACK_BUFFER_BINDING;
    }

    public static void bindRaw(int bufferID) {
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, bufferID);
    }
}
