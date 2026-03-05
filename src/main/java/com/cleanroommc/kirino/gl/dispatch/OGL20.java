package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.MemoryUtil;

import java.nio.*;

sealed class OGL20 extends OGL15 permits OGL21 {
    @Override
    public void glAttachShader(int program, int shader) {
        org.lwjgl.opengl.GL20.glAttachShader(program, shader);
    }

    @Override
    public void glBindAttribLocation(int program, int index, CharSequence name) {
        org.lwjgl.opengl.GL20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glBindAttribLocation(int program, int index, ByteBuffer name) {
        org.lwjgl.opengl.GL20.glBindAttribLocation(program, index, name);
    }

    @Override
    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        org.lwjgl.opengl.GL20.glBlendEquationSeparate(modeRGB, modeAlpha);
    }

    @Override
    public void glCompileShader(int shader) {
        org.lwjgl.opengl.GL20.glCompileShader(shader);
    }

    @Override
    public int glCreateProgram() {
        return org.lwjgl.opengl.GL20.glCreateProgram();
    }

    @Override
    public int glCreateShader(int type) {
        return org.lwjgl.opengl.GL20.glCreateShader(type);
    }

    @Override
    public void glDeleteProgram(int program) {
        org.lwjgl.opengl.GL20.glDeleteProgram(program);
    }

    @Override
    public void glDeleteShader(int shader) {
        org.lwjgl.opengl.GL20.glDeleteShader(shader);
    }

    @Override
    public void glDetachShader(int program, int shader) {
        org.lwjgl.opengl.GL20.glDetachShader(program, shader);
    }

    @Override
    public void glDisableVertexAttribArray(int index) {
        org.lwjgl.opengl.GL20.glDisableVertexAttribArray(index);
    }

    @Override
    public void glDrawBuffers(int buffer) {
        org.lwjgl.opengl.GL20.glDrawBuffers(buffer);
    }

    @Override
    public void glDrawBuffers(IntBuffer buffers) {
        org.lwjgl.opengl.GL20.glDrawBuffers(buffers);
    }

    @Override
    public void glEnableVertexAttribArray(int index) {
        org.lwjgl.opengl.GL20.glEnableVertexAttribArray(index);
    }

    @Override
    public void glGetActiveAttrib(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
        org.lwjgl.opengl.GL20.glGetActiveAttrib(program, index, length, size, type, name);
    }

    @Override
    public String glGetActiveUniform(int program, int index, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetActiveUniform(program, index, maxLength);
    }

    @Override
    public void glGetActiveUniform(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
        org.lwjgl.opengl.GL20.glGetActiveUniform(program, index, length, size, type, name);
    }

    @Override
    public void glGetAttachedShaders(int program, IntBuffer count, IntBuffer shaders) {
        org.lwjgl.opengl.GL20.glGetAttachedShaders(program, count, shaders);
    }

    @Override
    public int glGetAttribLocation(int program, CharSequence name) {
        return org.lwjgl.opengl.GL20.glGetAttribLocation(program, name);
    }

    @Override
    public int glGetAttribLocation(int program, ByteBuffer name) {
        return org.lwjgl.opengl.GL20.glGetAttribLocation(program, name);
    }

    @Override
    public void glGetProgram(int program, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetProgram(program, pname, params);
    }

    @Override
    public String glGetProgramInfoLog(int program, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, maxLength);
    }

    @Override
    public void glGetProgramInfoLog(int program, IntBuffer length, ByteBuffer infoLog) {
        org.lwjgl.opengl.GL20.glGetProgramInfoLog(program, length, infoLog);
    }

    @Override
    public int glGetProgrami(int program, int pname) {
        return org.lwjgl.opengl.GL20.glGetProgrami(program, pname);
    }

    @Override
    public void glGetShader(int shader, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetShader(shader, pname, params);
    }

    @Override
    public String glGetShaderInfoLog(int shader, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, maxLength);
    }

    @Override
    public void glGetShaderInfoLog(int shader, IntBuffer length, ByteBuffer infoLog) {
        org.lwjgl.opengl.GL20.glGetShaderInfoLog(shader, length, infoLog);
    }

    @Override
    public String glGetShaderSource(int shader, int maxLength) {
        return org.lwjgl.opengl.GL20.glGetShaderSource(shader, maxLength);
    }

    @Override
    public void glGetShaderSource(int shader, IntBuffer length, ByteBuffer source) {
        org.lwjgl.opengl.GL20.glGetShaderSource(shader, length, source);
    }

    @Override
    public int glGetShaderi(int shader, int pname) {
        return org.lwjgl.opengl.GL20.glGetShaderi(shader, pname);
    }

    @Override
    public void glGetUniform(int program, int location, FloatBuffer params) {
        org.lwjgl.opengl.GL20.glGetUniform(program, location, params);
    }

    @Override
    public void glGetUniform(int program, int location, IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetUniform(program, location, params);
    }

    @Override
    public int glGetUniformLocation(int program, CharSequence name) {
        return org.lwjgl.opengl.GL20.glGetUniformLocation(program, name);
    }

    @Override
    public int glGetUniformLocation(int program, ByteBuffer name) {
        return org.lwjgl.opengl.GL20.glGetUniformLocation(program, name);
    }

    @Override
    public void glGetVertexAttrib(int index, int pname, DoubleBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttrib(index, pname, params);
    }

    @Override
    public void glGetVertexAttrib(int index, int pname, FloatBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttrib(index, pname, params);
    }

    @Override
    public void glGetVertexAttrib(int index, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL20.glGetVertexAttrib(index, pname, params);
    }

    @Override
    public boolean glIsProgram(int program) {
        return org.lwjgl.opengl.GL20.glIsProgram(program);
    }

    @Override
    public boolean glIsShader(int shader) {
        return org.lwjgl.opengl.GL20.glIsShader(shader);
    }

    @Override
    public void glLinkProgram(int program) {
        org.lwjgl.opengl.GL20.glLinkProgram(program);
    }

    @Override
    public void glShaderSource(int shader, CharSequence string) {
        org.lwjgl.opengl.GL20.glShaderSource(shader, string);
    }

    @Override
    public void glShaderSource(int shader, ByteBuffer string) {
        org.lwjgl.opengl.GL20.glShaderSource(shader, string);
    }

    @Override
    public void glShaderSource(int shader, CharSequence[] strings) {
        org.lwjgl.opengl.GL20.glShaderSource(shader, strings);
    }

    @Override
    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        org.lwjgl.opengl.GL20.glStencilFuncSeparate(face, func, ref, mask);
    }

    @Override
    public void glStencilMaskSeparate(int face, int mask) {
        org.lwjgl.opengl.GL20.glStencilMaskSeparate(face, mask);
    }

    @Override
    public void glStencilOpSeparate(int face, int sfail, int dpfail, int dppass) {
        org.lwjgl.opengl.GL20.glStencilOpSeparate(face, sfail, dpfail, dppass);
    }

    @Override
    public void glUniform1(int location, FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform1(location, values);
    }

    @Override
    public void glUniform1(int location, IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform1(location, values);
    }

    @Override
    public void glUniform1f(int location, float v0) {
        org.lwjgl.opengl.GL20.glUniform1f(location, v0);
    }

    @Override
    public void glUniform1i(int location, int v0) {
        org.lwjgl.opengl.GL20.glUniform1i(location, v0);
    }

    @Override
    public void glUniform2(int location, FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform2(location, values);
    }

    @Override
    public void glUniform2(int location, IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform2(location, values);
    }

    @Override
    public void glUniform2f(int location, float v0, float v1) {
        org.lwjgl.opengl.GL20.glUniform2f(location, v0, v1);
    }

    @Override
    public void glUniform2i(int location, int v0, int v1) {
        org.lwjgl.opengl.GL20.glUniform2i(location, v0, v1);
    }

    @Override
    public void glUniform3(int location, FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform3(location, values);
    }

    @Override
    public void glUniform3(int location, IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform3(location, values);
    }

    @Override
    public void glUniform3f(int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.GL20.glUniform3f(location, v0, v1, v2);
    }

    @Override
    public void glUniform3i(int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.GL20.glUniform3i(location, v0, v1, v2);
    }

    @Override
    public void glUniform4(int location, FloatBuffer values) {
        org.lwjgl.opengl.GL20.glUniform4(location, values);
    }

    @Override
    public void glUniform4(int location, IntBuffer values) {
        org.lwjgl.opengl.GL20.glUniform4(location, values);
    }

    @Override
    public void glUniform4f(int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.GL20.glUniform4f(location, v0, v1, v2, v3);
    }

    @Override
    public void glUniform4i(int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.GL20.glUniform4i(location, v0, v1, v2, v3);
    }

    @Override
    public void glUniformMatrix2(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix2(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix3(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix3(location, transpose, matrices);
    }

    @Override
    public void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
        org.lwjgl.opengl.GL20.glUniformMatrix4(location, transpose, matrices);
    }

    @Override
    public void glUseProgram(int program) {
        org.lwjgl.opengl.GL20.glUseProgram(program);
    }

    @Override
    public void glValidateProgram(int program) {
        org.lwjgl.opengl.GL20.glValidateProgram(program);
    }

    @Override
    public void glVertexAttrib1d(int index, double x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1d(index, x);
    }

    @Override
    public void glVertexAttrib1f(int index, float x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1f(index, x);
    }

    @Override
    public void glVertexAttrib1s(int index, short x) {
        org.lwjgl.opengl.GL20.glVertexAttrib1s(index, x);
    }

    @Override
    public void glVertexAttrib2d(int index, double x, double y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2d(index, x, y);
    }

    @Override
    public void glVertexAttrib2f(int index, float x, float y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2f(index, x, y);
    }

    @Override
    public void glVertexAttrib2s(int index, short x, short y) {
        org.lwjgl.opengl.GL20.glVertexAttrib2s(index, x, y);
    }

    @Override
    public void glVertexAttrib3d(int index, double x, double y, double z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3d(index, x, y, z);
    }

    @Override
    public void glVertexAttrib3f(int index, float x, float y, float z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3f(index, x, y, z);
    }

    @Override
    public void glVertexAttrib3s(int index, short x, short y, short z) {
        org.lwjgl.opengl.GL20.glVertexAttrib3s(index, x, y, z);
    }

    @Override
    public void glVertexAttrib4Nub(int index, byte x, byte y, byte z, byte w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4Nub(index, x, y, z, w);
    }

    @Override
    public void glVertexAttrib4d(int index, double x, double y, double z, double w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4d(index, x, y, z, w);
    }

    @Override
    public void glVertexAttrib4f(int index, float x, float y, float z, float w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4f(index, x, y, z, w);
    }

    @Override
    public void glVertexAttrib4s(int index, short x, short y, short z, short w) {
        org.lwjgl.opengl.GL20.glVertexAttrib4s(index, x, y, z, w);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, long buffer_buffer_offset) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer_buffer_offset);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, int type, boolean normalized, int stride, ByteBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, type, normalized, stride, buffer);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, boolean normalized, int stride, DoubleBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, normalized, stride, buffer);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, boolean normalized, int stride, FloatBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, normalized, stride, buffer);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride, ByteBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, unsigned, normalized, stride, buffer);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride, IntBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, unsigned, normalized, stride, buffer);
    }

    @Override
    public void glVertexAttribPointer(int index, int size, boolean unsigned, boolean normalized, int stride, ShortBuffer buffer) {
        org.lwjgl.opengl.GL20.glVertexAttribPointer(index, size, unsigned, normalized, stride, buffer);
    }
}
