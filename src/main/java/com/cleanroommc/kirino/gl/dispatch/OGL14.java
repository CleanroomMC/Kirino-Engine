package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.opengl.GL14;
import org.lwjglx.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

sealed class OGL14 extends OGL13 permits OGL43 {
    @Override
    public void glBlendColor(float red, float green, float blue, float alpha) {
        GL14.glBlendColor(red, green, blue, alpha);
    }

    @Override
    public void glBlendEquation(int mode) {
        GL14.glBlendEquation(mode);
    }

    @Override
    public void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        GL14.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    @Override
    public void glFogCoordPointer(int type, int stride, long data_buffer_offset) {
        GL14.glFogCoordPointer(type, stride, data_buffer_offset);
    }

    @Override
    public void glFogCoordPointer(int stride, DoubleBuffer data) {
        GL14.glFogCoordPointer(stride, data);
    }

    @Override
    public void glFogCoordPointer(int stride, FloatBuffer data) {
        GL14.glFogCoordPointer(stride, data);
    }

    @Override
    public void glFogCoordd(double coord) {
        GL14.glFogCoordd(coord);
    }

    @Override
    public void glFogCoordf(float coord) {
        GL14.glFogCoordf(coord);
    }

    @Override
    public void glMultiDrawArrays(int mode, IntBuffer piFirst, IntBuffer piCount) {
        GL14.glMultiDrawArrays(mode, piFirst, piCount);
    }

    @Override
    public void glPointParameter(int pname, FloatBuffer params) {
        GL14.glPointParameter(pname, params);
    }

    @Override
    public void glPointParameter(int pname, IntBuffer params) {
        GL14.glPointParameter(pname, params);
    }

    @Override
    public void glPointParameterf(int pname, float param) {
        GL14.glPointParameterf(pname, param);
    }

    @Override
    public void glPointParameteri(int pname, int param) {
        GL14.glPointParameteri(pname, param);
    }

    @Override
    public void glSecondaryColor3b(byte red, byte green, byte blue) {
        GL14.glSecondaryColor3b(red, green, blue);
    }

    @Override
    public void glSecondaryColor3d(double red, double green, double blue) {
        GL14.glSecondaryColor3d(red, green, blue);
    }

    @Override
    public void glSecondaryColor3f(float red, float green, float blue) {
        GL14.glSecondaryColor3f(red, green, blue);
    }

    @Override
    public void glSecondaryColor3ub(byte red, byte green, byte blue) {
        GL14.glSecondaryColor3ub(red, green, blue);
    }

    @Override
    public void glSecondaryColorPointer(int size, int type, int stride, long data_buffer_offset) {
        GL14.glSecondaryColorPointer(size, type, stride, data_buffer_offset);
    }

    @Override
    public void glSecondaryColorPointer(int size, int stride, DoubleBuffer data) {
        GL14.glSecondaryColorPointer(size, stride, data);
    }

    @Override
    public void glSecondaryColorPointer(int size, int stride, FloatBuffer data) {
        GL14.glSecondaryColorPointer(size, stride, data);
    }

    @Override
    public void glSecondaryColorPointer(int size, boolean unsigned, int stride, ByteBuffer data) {
        GL14.glSecondaryColorPointer(size, unsigned ? 5121 : 5120, stride, MemoryUtil.getAddress(data));
    }

    @Override
    public void glWindowPos2d(double x, double y) {
        GL14.glWindowPos2d(x, y);
    }

    @Override
    public void glWindowPos2f(float x, float y) {
        GL14.glWindowPos2f(x, y);
    }

    @Override
    public void glWindowPos2i(int x, int y) {
        GL14.glWindowPos2i(x, y);
    }

    @Override
    public void glWindowPos3d(double x, double y, double z) {
        GL14.glWindowPos3d(x, y, z);
    }

    @Override
    public void glWindowPos3f(float x, float y, float z) {
        GL14.glWindowPos3f(x, y, z);
    }

    @Override
    public void glWindowPos3i(int x, int y, int z) {
        GL14.glWindowPos3i(x, y, z);
    }
}
