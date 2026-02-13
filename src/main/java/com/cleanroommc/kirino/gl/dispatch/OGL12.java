package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.opengl.GL12;

import java.nio.*;

sealed class OGL12 extends OGL11 permits OGL13 {
    @Override
    public void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
        GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, int indices_count, int type, long indices_buffer_offset) {
        GL12.glDrawRangeElements(mode, start, end, indices_count, type, indices_buffer_offset);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, ByteBuffer indices) {
        GL12.glDrawRangeElements(mode, start, end, indices);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, IntBuffer indices) {
        GL12.glDrawRangeElements(mode, start, end, indices);
    }

    @Override
    public void glDrawRangeElements(int mode, int start, int end, ShortBuffer indices) {
        GL12.glDrawRangeElements(mode, start, end, indices);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, ByteBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, DoubleBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, FloatBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, IntBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, ShortBuffer pixels) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
}
