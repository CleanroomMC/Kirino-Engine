package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.opengl.GL13;
import org.lwjglx.MemoryUtil;

import java.nio.*;

sealed class OGL13 extends OGL12 permits OGL14 {
    @Override
    public void glActiveTexture(int texture) {
        GL13.glActiveTexture(texture);
    }

    @Override
    public void glClientActiveTexture(int texture) {
        GL13.glClientActiveTexture(texture);
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexImage1D(target, level, internalformat, width, border, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, ByteBuffer data) {
        GL13.glCompressedTexImage1D(target, level, internalformat, width, border, data);
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, ByteBuffer data) {
        GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, data);
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, ByteBuffer data) {
        GL13.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border, data);
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexSubImage1D(target, level, xoffset, width, format, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, ByteBuffer data) {
        GL13.glCompressedTexSubImage1D(target, level, xoffset, width, format, data);
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, ByteBuffer data) {
        GL13.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, data);
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int data_imageSize, long data_buffer_offset) {
        GL13.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, ByteBuffer data) {
        GL13.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, data);
    }

    @Override
    public void glGetCompressedTexImage(int target, int lod, long img_buffer_offset) {
        GL13.glGetCompressedTexImage(target, lod, img_buffer_offset);
    }

    @Override
    public void glGetCompressedTexImage(int target, int lod, ByteBuffer img) {
        GL13.glGetCompressedTexImage(target, lod, img);
    }

    @Override
    public void glGetCompressedTexImage(int target, int lod, IntBuffer img) {
        GL13.glGetCompressedTexImage(target, lod, MemoryUtil.getAddress(img));
    }

    @Override
    public void glGetCompressedTexImage(int target, int lod, ShortBuffer img) {
        GL13.glGetCompressedTexImage(target, lod, MemoryUtil.getAddress(img));
    }

    @Override
    public void glLoadTransposeMatrix(DoubleBuffer m) {
        GL13.glLoadTransposeMatrix(m);
    }

    @Override
    public void glLoadTransposeMatrix(FloatBuffer m) {
        GL13.glLoadTransposeMatrix(m);
    }

    @Override
    public void glMultTransposeMatrix(DoubleBuffer m) {
        GL13.glMultTransposeMatrix(m);
    }

    @Override
    public void glMultTransposeMatrix(FloatBuffer m) {
        GL13.glMultTransposeMatrix(m);
    }

    @Override
    public void glMultiTexCoord1d(int target, double s) {
        GL13.glMultiTexCoord1d(target, s);
    }

    @Override
    public void glMultiTexCoord1f(int target, float s) {
        GL13.glMultiTexCoord1f(target, s);
    }

    @Override
    public void glMultiTexCoord2d(int target, double s, double t) {
        GL13.glMultiTexCoord2d(target, s, t);
    }

    @Override
    public void glMultiTexCoord2f(int target, float s, float t) {
        GL13.glMultiTexCoord2f(target, s, t);
    }

    @Override
    public void glMultiTexCoord3d(int target, double s, double t, double r) {
        GL13.glMultiTexCoord3d(target, s, t, r);
    }

    @Override
    public void glMultiTexCoord3f(int target, float s, float t, float r) {
        GL13.glMultiTexCoord3f(target, s, t, r);
    }

    @Override
    public void glMultiTexCoord4d(int target, double s, double t, double r, double q) {
        GL13.glMultiTexCoord4d(target, s, t, r, q);
    }

    @Override
    public void glMultiTexCoord4f(int target, float s, float t, float r, float q) {
        GL13.glMultiTexCoord4f(target, s, t, r, q);
    }

    @Override
    public void glSampleCoverage(float value, boolean invert) {
        GL13.glSampleCoverage(value, invert);
    }
}
