package com.cleanroommc.kirino.gl.dispatch;

import org.lwjglx.opengl.GL44;

import java.nio.*;

sealed class OGL44 extends OGL43 permits OGL45 {
    @Override
    public void glBufferStorage(int target, long size, int flags) {
        GL44.glBufferStorage(target, size, flags);
    }

    @Override
    public void glBufferStorage(int target, ByteBuffer data, int flags) {
        GL44.glBufferStorage(target, data, flags);
    }

    @Override
    public void glBufferStorage(int target, DoubleBuffer data, int flags) {
        GL44.glBufferStorage(target, data, flags);
    }

    @Override
    public void glBufferStorage(int target, FloatBuffer data, int flags) {
        GL44.glBufferStorage(target, data, flags);
    }

    @Override
    public void glBufferStorage(int target, IntBuffer data, int flags) {
        GL44.glBufferStorage(target, data, flags);
    }

    @Override
    public void glBufferStorage(int target, ShortBuffer data, int flags) {
        GL44.glBufferStorage(target, data, flags);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, ByteBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, DoubleBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, FloatBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, IntBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, LongBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexImage(int texture, int level, int format, int type, ShortBuffer data) {
        GL44.glClearTexImage(texture, level, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, LongBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }

    @Override
    public void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer data) {
        GL44.glClearTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, data);
    }
}
