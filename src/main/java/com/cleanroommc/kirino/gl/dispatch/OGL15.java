package com.cleanroommc.kirino.gl.dispatch;

import java.nio.*;

sealed class OGL15 extends OGL14 permits OGL20 {
    @Override
    public void glBeginQuery(int target, int id) {
        org.lwjgl.opengl.GL15.glBeginQuery(target, id);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        org.lwjgl.opengl.GL15.glBindBuffer(target, buffer);
    }

    @Override
    public void glBufferData(int target, long data_size, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data_size, usage);
    }

    @Override
    public void glBufferData(int target, ByteBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, DoubleBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, FloatBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, IntBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferData(int target, ShortBuffer data, int usage) {
        org.lwjgl.opengl.GL15.glBufferData(target, data, usage);
    }

    @Override
    public void glBufferSubData(int target, long offset, ByteBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    @Override
    public void glBufferSubData(int target, long offset, DoubleBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    @Override
    public void glBufferSubData(int target, long offset, FloatBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    @Override
    public void glBufferSubData(int target, long offset, IntBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    @Override
    public void glBufferSubData(int target, long offset, ShortBuffer data) {
        org.lwjgl.opengl.GL15.glBufferSubData(target, offset, data);
    }

    @Override
    public void glDeleteBuffers(int buffer) {
        org.lwjgl.opengl.GL15.glDeleteBuffers(buffer);
    }

    @Override
    public void glDeleteBuffers(IntBuffer buffers) {
        org.lwjgl.opengl.GL15.glDeleteBuffers(buffers);
    }

    @Override
    public void glDeleteQueries(int id) {
        org.lwjgl.opengl.GL15.glDeleteQueries(id);
    }

    @Override
    public void glDeleteQueries(IntBuffer ids) {
        org.lwjgl.opengl.GL15.glDeleteQueries(ids);
    }

    @Override
    public void glEndQuery(int target) {
        org.lwjgl.opengl.GL15.glEndQuery(target);
    }

    @Override
    public int glGenBuffers() {
        return org.lwjgl.opengl.GL15.glGenBuffers();
    }

    @Override
    public void glGenBuffers(IntBuffer buffers) {
        org.lwjgl.opengl.GL15.glGenBuffers(buffers);
    }

    @Override
    public int glGenQueries() {
        return org.lwjgl.opengl.GL15.glGenQueries();
    }

    @Override
    public void glGenQueries(IntBuffer ids) {
        org.lwjgl.opengl.GL15.glGenQueries(ids);
    }

    @Override
    public void glGetBufferParameter(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetBufferParameter(target, pname, params);
    }

    @Override
    public int glGetBufferParameteri(int target, int pname) {
        return org.lwjgl.opengl.GL15.glGetBufferParameteri(target, pname);
    }

    @Override
    public void glGetBufferSubData(int target, long offset, ByteBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    @Override
    public void glGetBufferSubData(int target, long offset, DoubleBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    @Override
    public void glGetBufferSubData(int target, long offset, FloatBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    @Override
    public void glGetBufferSubData(int target, long offset, IntBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    @Override
    public void glGetBufferSubData(int target, long offset, ShortBuffer data) {
        org.lwjgl.opengl.GL15.glGetBufferSubData(target, offset, data);
    }

    @Override
    public void glGetQuery(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQuery(target, pname, params);
    }

    @Override
    public void glGetQueryObject(int id, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQueryObject(id, pname, params);
    }

    @Override
    public int glGetQueryObjecti(int id, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryObjecti(id, pname);
    }

    @Override
    public void glGetQueryObjectu(int id, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL15.glGetQueryObjectu(id, pname, params);
    }

    @Override
    public int glGetQueryObjectui(int id, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryObjectui(id, pname);
    }

    @Override
    public int glGetQueryi(int target, int pname) {
        return org.lwjgl.opengl.GL15.glGetQueryi(target, pname);
    }

    @Override
    public boolean glIsBuffer(int buffer) {
        return org.lwjgl.opengl.GL15.glIsBuffer(buffer);
    }

    @Override
    public boolean glIsQuery(int id) {
        return org.lwjgl.opengl.GL15.glIsQuery(id);
    }

    @Override
    public ByteBuffer glMapBuffer(int target, int access, long length, ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL15.glMapBuffer(target, access, length, old_buffer);
    }

    @Override
    public ByteBuffer glMapBuffer(int target, int access, ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL15.glMapBuffer(target, access, old_buffer);
    }

    @Override
    public boolean glUnmapBuffer(int target) {
        return org.lwjgl.opengl.GL15.glUnmapBuffer(target);
    }
}
