package com.cleanroommc.kirino.engine.render.core.resource.payload;

import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import org.jspecify.annotations.NonNull;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class MeshPayload implements ResourcePayload<MeshPayload> {
    public MeshPayload() {
    }

    public boolean isVboByteBufferFromLwjgl = false;
    public boolean isEboByteBufferFromLwjgl = false;
    public ByteBuffer vboByteBuffer;
    public ByteBuffer eboByteBuffer;
    public AttributeLayout attributeLayout;

    @NonNull
    @Override
    public MeshPayload getPayload() {
        return this;
    }

    private boolean released = false;

    @Override
    public void release() {
        if (!released) {
            released = true;
            attributeLayout = null;
            if (vboByteBuffer != null && vboByteBuffer.isDirect() && MemoryUtil.memAddressSafe(vboByteBuffer) != 0 && isVboByteBufferFromLwjgl) {
                MemoryUtil.memFree(vboByteBuffer);
            }
            vboByteBuffer = null;
            if (eboByteBuffer != null && eboByteBuffer.isDirect() && MemoryUtil.memAddressSafe(eboByteBuffer) != 0 && isEboByteBufferFromLwjgl) {
                MemoryUtil.memFree(eboByteBuffer);
            }
            eboByteBuffer = null;
        }
    }
}
