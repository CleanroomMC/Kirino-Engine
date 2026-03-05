package com.cleanroommc.kirino.gl.dispatch;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

sealed class OGL30 extends OGL21 permits OGL43 {
    @Override
    public void glBeginConditionalRender(int id, int mode) {
        org.lwjgl.opengl.GL30.glBeginConditionalRender(id, mode);
    }

    @Override
    public void glBeginTransformFeedback(int primitiveMode) {
        org.lwjgl.opengl.GL30.glBeginTransformFeedback(primitiveMode);
    }

    @Override
    public void glBindBufferBase(int target, int index, int buffer) {
        org.lwjgl.opengl.GL30.glBindBufferBase(target, index, buffer);
    }

    @Override
    public void glBindBufferRange(int target, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.GL30.glBindBufferRange(target, index, buffer, offset, size);
    }

    @Override
    public void glBindFragDataLocation(int program, int colorNumber, CharSequence name) {
        org.lwjgl.opengl.GL30.glBindFragDataLocation(program, colorNumber, name);
    }

    @Override
    public void glBindFragDataLocation(int program, int colorNumber, ByteBuffer name) {
        org.lwjgl.opengl.GL30.glBindFragDataLocation(program, colorNumber, name);
    }

    @Override
    public void glBindFramebuffer(int target, int framebuffer) {
        org.lwjgl.opengl.GL30.glBindFramebuffer(target, framebuffer);
    }

    @Override
    public void glBindRenderbuffer(int target, int renderbuffer) {
        org.lwjgl.opengl.GL30.glBindRenderbuffer(target, renderbuffer);
    }

    @Override
    public void glBindVertexArray(int array) {
        org.lwjgl.opengl.GL30.glBindVertexArray(array);
    }

    @Override
    public void glBlitFramebuffer(int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        org.lwjgl.opengl.GL30.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public int glCheckFramebufferStatus(int target) {
        return org.lwjgl.opengl.GL30.glCheckFramebufferStatus(target);
    }

    @Override
    public void glClampColor(int target, int clamp) {
        org.lwjgl.opengl.GL30.glClampColor(target, clamp);
    }

    @Override
    public void glClearBuffer(int buffer, int drawbuffer, FloatBuffer value) {
        org.lwjgl.opengl.GL30.glClearBuffer(buffer, drawbuffer, value);
    }

    @Override
    public void glClearBuffer(int buffer, int drawbuffer, IntBuffer value) {
        org.lwjgl.opengl.GL30.glClearBuffer(buffer, drawbuffer, value);
    }

    @Override
    public void glClearBufferfi(int buffer, int drawbuffer, float depth, int stencil) {
        org.lwjgl.opengl.GL30.glClearBufferfi(buffer, drawbuffer, depth, stencil);
    }

    @Override
    public void glClearBufferu(int buffer, int drawbuffer, IntBuffer value) {
        org.lwjgl.opengl.GL30.glClearBufferu(buffer, drawbuffer, value);
    }

    @Override
    public void glColorMaski(int buf, boolean r, boolean g, boolean b, boolean a) {
        org.lwjgl.opengl.GL30.glColorMaski(buf, r, g, b, a);
    }

    @Override
    public void glDeleteFramebuffers(int framebuffer) {
        org.lwjgl.opengl.GL30.glDeleteFramebuffers(framebuffer);
    }

    @Override
    public void glDeleteFramebuffers(IntBuffer framebuffers) {
        org.lwjgl.opengl.GL30.glDeleteFramebuffers(framebuffers);
    }

    @Override
    public void glDeleteRenderbuffers(int renderbuffer) {
        org.lwjgl.opengl.GL30.glDeleteRenderbuffers(renderbuffer);
    }

    @Override
    public void glDeleteRenderbuffers(IntBuffer renderbuffers) {
        org.lwjgl.opengl.GL30.glDeleteRenderbuffers(renderbuffers);
    }

    @Override
    public void glDeleteVertexArrays(int array) {
        org.lwjgl.opengl.GL30.glDeleteVertexArrays(array);
    }

    @Override
    public void glDeleteVertexArrays(IntBuffer arrays) {
        org.lwjgl.opengl.GL30.glDeleteVertexArrays(arrays);
    }

    @Override
    public void glDisablei(int target, int index) {
        org.lwjgl.opengl.GL30.glDisablei(target, index);
    }

    @Override
    public void glEnablei(int target, int index) {
        org.lwjgl.opengl.GL30.glEnablei(target, index);
    }

    @Override
    public void glEndConditionalRender() {
        org.lwjgl.opengl.GL30.glEndConditionalRender();
    }

    @Override
    public void glEndTransformFeedback() {
        org.lwjgl.opengl.GL30.glEndTransformFeedback();
    }

    @Override
    public void glFlushMappedBufferRange(int target, long offset, long length) {
        org.lwjgl.opengl.GL30.glFlushMappedBufferRange(target, offset, length);
    }

    @Override
    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        org.lwjgl.opengl.GL30.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
    }

    @Override
    public void glFramebufferTexture1D(int target, int attachment, int textarget, int texture, int level) {
        org.lwjgl.opengl.GL30.glFramebufferTexture1D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        org.lwjgl.opengl.GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
    }

    @Override
    public void glFramebufferTexture3D(int target, int attachment, int textarget, int texture, int level, int zoffset) {
        org.lwjgl.opengl.GL30.glFramebufferTexture3D(target, attachment, textarget, texture, level, zoffset);
    }

    @Override
    public void glFramebufferTextureLayer(int target, int attachment, int texture, int level, int layer) {
        org.lwjgl.opengl.GL30.glFramebufferTextureLayer(target, attachment, texture, level, layer);
    }

    @Override
    public int glGenFramebuffers() {
        return org.lwjgl.opengl.GL30.glGenFramebuffers();
    }

    @Override
    public void glGenFramebuffers(IntBuffer framebuffers) {
        org.lwjgl.opengl.GL30.glGenFramebuffers(framebuffers);
    }

    @Override
    public int glGenRenderbuffers() {
        return org.lwjgl.opengl.GL30.glGenRenderbuffers();
    }

    @Override
    public void glGenRenderbuffers(IntBuffer renderbuffers) {
        org.lwjgl.opengl.GL30.glGenRenderbuffers(renderbuffers);
    }

    @Override
    public int glGenVertexArrays() {
        return org.lwjgl.opengl.GL30.glGenVertexArrays();
    }

    @Override
    public void glGenVertexArrays(IntBuffer arrays) {
        org.lwjgl.opengl.GL30.glGenVertexArrays(arrays);
    }

    @Override
    public void glGenerateMipmap(int target) {
        org.lwjgl.opengl.GL30.glGenerateMipmap(target);
    }

    @Override
    public void glGetBoolean(int value, int index, ByteBuffer data) {
        org.lwjgl.opengl.GL30.glGetBoolean(value, index, data);
    }

    @Override
    public int glGetFragDataLocation(int program, CharSequence name) {
        return org.lwjgl.opengl.GL30.glGetFragDataLocation(program, name);
    }

    @Override
    public int glGetFragDataLocation(int program, ByteBuffer name) {
        return org.lwjgl.opengl.GL30.glGetFragDataLocation(program, name);
    }

    @Override
    public void glGetFramebufferAttachmentParameter(int target, int attachment, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetFramebufferAttachmentParameter(target, attachment, pname, params);
    }

    @Override
    public int glGetFramebufferAttachmentParameteri(int target, int attachment, int pname) {
        return org.lwjgl.opengl.GL30.glGetFramebufferAttachmentParameteri(target, attachment, pname);
    }

    @Override
    public void glGetInteger(int value, int index, IntBuffer data) {
        org.lwjgl.opengl.GL30.glGetInteger(value, index, data);
    }

    @Override
    public void glGetRenderbufferParameter(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetRenderbufferParameter(target, pname, params);
    }

    @Override
    public int glGetRenderbufferParameteri(int target, int pname) {
        return org.lwjgl.opengl.GL30.glGetRenderbufferParameteri(target, pname);
    }

    @Override
    public String glGetStringi(int name, int index) {
        return org.lwjgl.opengl.GL30.glGetStringi(name, index);
    }

    @Override
    public void glGetTexParameterI(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetTexParameterI(target, pname, params);
    }

    @Override
    public int glGetTexParameterIi(int target, int pname) {
        return org.lwjgl.opengl.GL30.glGetTexParameterIi(target, pname);
    }

    @Override
    public void glGetTexParameterIu(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetTexParameterIu(target, pname, params);
    }

    @Override
    public int glGetTexParameterIui(int target, int pname) {
        return org.lwjgl.opengl.GL30.glGetTexParameterIui(target, pname);
    }

    @Override
    public String glGetTransformFeedbackVarying(int program, int index, int bufSize, IntBuffer size, IntBuffer type) {
        return org.lwjgl.opengl.GL30.glGetTransformFeedbackVarying(program, index, bufSize, size, type);
    }

    @Override
    public void glGetTransformFeedbackVarying(int program, int index, IntBuffer length, IntBuffer size, IntBuffer type, ByteBuffer name) {
        org.lwjgl.opengl.GL30.glGetTransformFeedbackVarying(program, index, length, size, type, name);
    }

    @Override
    public void glGetUniformu(int program, int location, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetUniformu(program, location, params);
    }

    @Override
    public void glGetVertexAttribI(int index, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetVertexAttribI(index, pname, params);
    }

    @Override
    public void glGetVertexAttribIu(int index, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glGetVertexAttribIu(index, pname, params);
    }

    @Override
    public boolean glIsEnabledi(int target, int index) {
        return org.lwjgl.opengl.GL30.glIsEnabledi(target, index);
    }

    @Override
    public boolean glIsFramebuffer(int framebuffer) {
        return org.lwjgl.opengl.GL30.glIsFramebuffer(framebuffer);
    }

    @Override
    public boolean glIsRenderbuffer(int renderbuffer) {
        return org.lwjgl.opengl.GL30.glIsRenderbuffer(renderbuffer);
    }

    @Override
    public boolean glIsVertexArray(int array) {
        return org.lwjgl.opengl.GL30.glIsVertexArray(array);
    }

    @Override
    public ByteBuffer glMapBufferRange(int target, long offset, long length, int access, ByteBuffer old_buffer) {
        return org.lwjgl.opengl.GL30.glMapBufferRange(target, offset, length, access, old_buffer);
    }

    @Override
    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        org.lwjgl.opengl.GL30.glRenderbufferStorage(target, internalformat, width, height);
    }

    @Override
    public void glRenderbufferStorageMultisample(int target, int samples, int internalformat, int width, int height) {
        org.lwjgl.opengl.GL30.glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    }

    @Override
    public void glTexParameterI(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glTexParameterI(target, pname, params);
    }

    @Override
    public void glTexParameterIi(int target, int pname, int param) {
        org.lwjgl.opengl.GL30.glTexParameterIi(target, pname, param);
    }

    @Override
    public void glTexParameterIu(int target, int pname, IntBuffer params) {
        org.lwjgl.opengl.GL30.glTexParameterIu(target, pname, params);
    }

    @Override
    public void glTexParameterIui(int target, int pname, int param) {
        org.lwjgl.opengl.GL30.glTexParameterIui(target, pname, param);
    }

    @Override
    public void glTransformFeedbackVaryings(int program, CharSequence[] varyings, int bufferMode) {
        org.lwjgl.opengl.GL30.glTransformFeedbackVaryings(program, varyings, bufferMode);
    }

    @Override
    public void glUniform1u(int location, IntBuffer value) {
        org.lwjgl.opengl.GL30.glUniform1u(location, value);
    }

    @Override
    public void glUniform1ui(int location, int v0) {
        org.lwjgl.opengl.GL30.glUniform1ui(location, v0);
    }

    @Override
    public void glUniform2u(int location, IntBuffer value) {
        org.lwjgl.opengl.GL30.glUniform2u(location, value);
    }

    @Override
    public void glUniform2ui(int location, int v0, int v1) {
        org.lwjgl.opengl.GL30.glUniform2ui(location, v0, v1);
    }

    @Override
    public void glUniform3u(int location, IntBuffer value) {
        org.lwjgl.opengl.GL30.glUniform3u(location, value);
    }

    @Override
    public void glUniform3ui(int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.GL30.glUniform3ui(location, v0, v1, v2);
    }

    @Override
    public void glUniform4u(int location, IntBuffer value) {
        org.lwjgl.opengl.GL30.glUniform4u(location, value);
    }

    @Override
    public void glUniform4ui(int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.GL30.glUniform4ui(location, v0, v1, v2, v3);
    }

    @Override
    public void glVertexAttribI1(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI1(index, v);
    }

    @Override
    public void glVertexAttribI1i(int index, int x) {
        org.lwjgl.opengl.GL30.glVertexAttribI1i(index, x);
    }

    @Override
    public void glVertexAttribI1u(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI1u(index, v);
    }

    @Override
    public void glVertexAttribI1ui(int index, int x) {
        org.lwjgl.opengl.GL30.glVertexAttribI1ui(index, x);
    }

    @Override
    public void glVertexAttribI2(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI2(index, v);
    }

    @Override
    public void glVertexAttribI2i(int index, int x, int y) {
        org.lwjgl.opengl.GL30.glVertexAttribI2i(index, x, y);
    }

    @Override
    public void glVertexAttribI2u(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI2u(index, v);
    }

    @Override
    public void glVertexAttribI2ui(int index, int x, int y) {
        org.lwjgl.opengl.GL30.glVertexAttribI2ui(index, x, y);
    }

    @Override
    public void glVertexAttribI3(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI3(index, v);
    }

    @Override
    public void glVertexAttribI3i(int index, int x, int y, int z) {
        org.lwjgl.opengl.GL30.glVertexAttribI3i(index, x, y, z);
    }

    @Override
    public void glVertexAttribI3u(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI3u(index, v);
    }

    @Override
    public void glVertexAttribI3ui(int index, int x, int y, int z) {
        org.lwjgl.opengl.GL30.glVertexAttribI3ui(index, x, y, z);
    }

    @Override
    public void glVertexAttribI4(int index, ByteBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4(index, v);
    }

    @Override
    public void glVertexAttribI4(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4(index, v);
    }

    @Override
    public void glVertexAttribI4(int index, ShortBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4(index, v);
    }

    @Override
    public void glVertexAttribI4i(int index, int x, int y, int z, int w) {
        org.lwjgl.opengl.GL30.glVertexAttribI4i(index, x, y, z, w);
    }

    @Override
    public void glVertexAttribI4u(int index, ByteBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4u(index, v);
    }

    @Override
    public void glVertexAttribI4u(int index, IntBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4u(index, v);
    }

    @Override
    public void glVertexAttribI4u(int index, ShortBuffer v) {
        org.lwjgl.opengl.GL30.glVertexAttribI4u(index, v);
    }

    @Override
    public void glVertexAttribI4ui(int index, int x, int y, int z, int w) {
        org.lwjgl.opengl.GL30.glVertexAttribI4ui(index, x, y, z, w);
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, long buffer_buffer_offset) {
        org.lwjgl.opengl.GL30.glVertexAttribIPointer(index, size, type, stride, buffer_buffer_offset);
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, ByteBuffer buffer) {
        org.lwjgl.opengl.GL30.glVertexAttribIPointer(index, size, type, stride, buffer);
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, IntBuffer buffer) {
        org.lwjgl.opengl.GL30.glVertexAttribIPointer(index, size, type, stride, buffer);
    }

    @Override
    public void glVertexAttribIPointer(int index, int size, int type, int stride, ShortBuffer buffer) {
        org.lwjgl.opengl.GL30.glVertexAttribIPointer(index, size, type, stride, buffer);
    }
}
