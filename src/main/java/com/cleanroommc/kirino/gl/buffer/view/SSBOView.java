package com.cleanroommc.kirino.gl.buffer.view;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import org.lwjgl.opengl.*;

public class SSBOView extends BufferView {
    public SSBOView(GLBuffer buffer) {
        super(buffer);
    }

    @Override
    public int target() {
        return GL43.GL_SHADER_STORAGE_BUFFER;
    }

    @Override
    public int bindingTarget() {
        return GL43.GL_SHADER_STORAGE_BUFFER_BINDING;
    }
}
