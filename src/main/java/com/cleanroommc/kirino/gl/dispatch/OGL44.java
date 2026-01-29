package com.cleanroommc.kirino.gl.dispatch;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjglx.opengl.GL44;

import java.nio.*;

sealed class OGL44 implements OpenGL permits OGL45 {
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

    @Override
    public void glBindTextureUnit(int unit, int texture) {
        throw new NotImplementedException("glBindTextureUnit wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glBlitNamedFramebuffer(int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        throw new NotImplementedException("glBlitNamedFramebuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCheckNamedFramebufferStatus(int framebuffer, int target) {
        throw new NotImplementedException("glCheckNamedFramebufferStatus wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClearNamedBufferData(int buffer, int internalformat, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, FloatBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClearNamedFramebufferu(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glClipControl(int origin, int depth) {
        throw new NotImplementedException("glClearControl wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int data_imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size) {
        throw new NotImplementedException("glCopyNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCopyTextureSubImage1D(int texture, int level, int xoffset, int x, int y, int width) {
        throw new NotImplementedException("glCopyTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCopyTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        throw new NotImplementedException("glCopyTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCopyTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
        throw new NotImplementedException("glCopyTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateBuffers() {
        throw new NotImplementedException("glCreateBuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateBuffers(IntBuffer buffers) {
        throw new NotImplementedException("glCreateBuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateFramebuffers() {
        throw new NotImplementedException("glCreateFramebuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateFramebuffers(IntBuffer framebuffers) {
        throw new NotImplementedException("glCreateFramebuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateProgramPipelines() {
        throw new NotImplementedException("glCreateProgramPipelines wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateProgramPipelines(IntBuffer pipelines) {
        throw new NotImplementedException("glCreateProgramPipelines wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateQueries(int target) {
        throw new NotImplementedException("glCreateQueries wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateQueries(int target, IntBuffer ids) {
        throw new NotImplementedException("glCreateQueries wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateRenderbuffers() {
        throw new NotImplementedException("glCreateRenderbuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateRenderbuffers(IntBuffer renderbuffers) {
        throw new NotImplementedException("glCreateRenderbuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateSamplers() {
        throw new NotImplementedException("glCreateSamplers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateSamplers(IntBuffer samplers) {
        throw new NotImplementedException("glCreateSamplers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateTextures(int target) {
        throw new NotImplementedException("glCreateTextures wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateTextures(int target, IntBuffer textures) {
        throw new NotImplementedException("glCreateTextures wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateTransformFeedbacks() {
        throw new NotImplementedException("glCreateTransformFeedbacks wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateTransformFeedbacks(IntBuffer ids) {
        throw new NotImplementedException("glCreateTransformFeedbacks wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glCreateVertexArrays() {
        throw new NotImplementedException("glCreateVertexArrays wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glCreateVertexArrays(IntBuffer arrays) {
        throw new NotImplementedException("glCreateVertexArrays wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glDisableVertexArrayAttrib(int vaobj, int index) {
        throw new NotImplementedException("glDisableVertexArrayAttrib wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glEnableVertexArrayAttrib(int vaobj, int index) {
        throw new NotImplementedException("glEnableVertexArrayAttrib wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        throw new NotImplementedException("glFlushMappedNamedBufferRange wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGenerateTextureMipmap(int texture) {
        throw new NotImplementedException("glGenerateTextureMipmap wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureImage(int texture, int level, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetCompressedTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureImage(int texture, int level, ByteBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ByteBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, FloatBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, IntBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ShortBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetGraphicsResetStatus() {
        throw new NotImplementedException("glGetGraphicsResetStatus wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferParameter(int buffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedBufferParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferParameter(int buffer, int pname, LongBuffer params) {
        throw new NotImplementedException("glGetNamedBufferParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public long glGetNamedBufferParameteri64(int buffer, int pname) {
        throw new NotImplementedException("glGetNamedBufferParameteri64 wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetNamedBufferParameteri(int buffer, int pname) {
        throw new NotImplementedException("glGetNamedBufferParameteri wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameter(int framebuffer, int attachment, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedFramebufferAttachmentParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedFramebufferParameter(int framebuffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedFramebufferParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetNamedRenderbufferParameter(int renderbuffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedRenderbufferParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureLevelParameter(int texture, int level, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTextureLevelParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureLevelParameter(int texture, int level, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureLevelParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public float glGetTextureLevelParameterf(int texture, int level, int pname) {
        throw new NotImplementedException("glGetTextureLevelParameterf wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTextureLevelParameteri(int texture, int level, int pname) {
        throw new NotImplementedException("glGetTextureLevelParameteri wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureParameter(int texture, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTextureParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureParameter(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureParameterI(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameterI wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTextureParameterIi(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterIi wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureParameterIu(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameterIu wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTextureParameterIui(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterIui wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public float glGetTextureParameterf(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterf wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTextureParameteri(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameteri wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTransformFeedback(int xfb, int pname, int index, IntBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTransformFeedback(int xfb, int pname, int index, LongBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetTransformFeedback(int xfb, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public long glGetTransformFeedbacki64(int xfb, int pname, int index) {
        throw new NotImplementedException("glGetTransformFeedbacki64 wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTransformFeedbacki(int xfb, int pname) {
        throw new NotImplementedException("glGetTransformFeedbacki wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public int glGetTransformFeedbacki(int xfb, int pname, int index) {
        throw new NotImplementedException("glGetTransformFeedbacki wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetVertexArray(int vaobj, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetVertexArray wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public long glGetVertexArrayIndexed64i(int vaobj, int index, int pname) {
        throw new NotImplementedException("glGetVertexArrayIndexed64i wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetVertexArrayIndexed64i(int vaobj, int index, int pname, LongBuffer param) {
        throw new NotImplementedException("glGetVertexArrayIndexed64i wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetVertexArrayIndexed(int vaobj, int index, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetVertexArrayIndexed wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetnUniform(int program, int location, FloatBuffer params) {
        throw new NotImplementedException("glGetnUniform wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetnUniform(int program, int location, IntBuffer params) {
        throw new NotImplementedException("glGetnUniform wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glGetnUniformu(int program, int location, IntBuffer params) {
        throw new NotImplementedException("glGetnUniformu wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glInvalidateNamedFramebufferData(int framebuffer, IntBuffer attachments) {
        throw new NotImplementedException("glInvalidateNamedFramebufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glInvalidateNamedFramebufferSubData(int framebuffer, IntBuffer attachments, int x, int y, int width, int height) {
        throw new NotImplementedException("glInvalidateNamedFramebufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public ByteBuffer glMapNamedBuffer(int buffer, int access, long length, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public ByteBuffer glMapNamedBuffer(int buffer, int access, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public ByteBuffer glMapNamedBufferRange(int buffer, long offset, long length, int access, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBufferRange wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glMemoryBarrierByRegion(int barriers) {
        throw new NotImplementedException("glMemoryBarrierByRegion wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, long data_size, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, ByteBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, DoubleBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, FloatBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, IntBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferData(int buffer, ShortBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, long size, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, ByteBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, DoubleBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, FloatBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, IntBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferStorage(int buffer, ShortBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferDrawBuffer(int framebuffer, int mode) {
        throw new NotImplementedException("glNamedFramebufferDrawBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferDrawBuffers(int framebuffer, IntBuffer bufs) {
        throw new NotImplementedException("glNamedFramebufferDrawBuffers wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferParameteri(int framebuffer, int pname, int param) {
        throw new NotImplementedException("glNamedFramebufferParameteri wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferReadBuffer(int framebuffer, int mode) {
        throw new NotImplementedException("glNamedFramebufferReadBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferRenderbuffer(int framebuffer, int attachment, int renderbuffertarget, int renderbuffer) {
        throw new NotImplementedException("glNamedFramebufferRenderbuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferTexture(int framebuffer, int attachment, int texture, int level) {
        throw new NotImplementedException("glNamedFramebufferTexture wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedFramebufferTextureLayer(int framebuffer, int attachment, int texture, int level, int layer) {
        throw new NotImplementedException("glNamedFramebufferTextureLayer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedRenderbufferStorage(int renderbuffer, int internalformat, int width, int height) {
        throw new NotImplementedException("glNamedRenderbufferStorage wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glNamedRenderbufferStorageMultisample(int renderbuffer, int samples, int internalformat, int width, int height) {
        throw new NotImplementedException("glNamedRenderbufferStorageMultisample wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glReadnPixels wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glReadnPixels wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glReadnPixels wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glReadnPixels wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glReadnPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glReadnPixels wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureBarrier() {
        throw new NotImplementedException("glTextureBarrier wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureBuffer(int texture, int internalformat, int buffer) {
        throw new NotImplementedException("glTextureBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureBufferRange(int texture, int internalformat, int buffer, long offset, long size) {
        throw new NotImplementedException("glTextureBufferRange wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameter(int texture, int pname, FloatBuffer params) {
        throw new NotImplementedException("glTextureParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameter(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameter wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameterI(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameterI wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameterIu(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameterIu wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameterf(int texture, int pname, float param) {
        throw new NotImplementedException("glTextureParameterf wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureParameteri(int texture, int pname, int param) {
        throw new NotImplementedException("glTextureParameteri wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureStorage1D(int texture, int levels, int internalformat, int width) {
        throw new NotImplementedException("glTextureStorage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureStorage2D(int texture, int levels, int internalformat, int width, int height) {
        throw new NotImplementedException("glTextureStorage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureStorage2DMultisample(int texture, int samples, int internalformat, int width, int height, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTextureStorage2DMultisample wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureStorage3D(int texture, int levels, int internalformat, int width, int height, int depth) {
        throw new NotImplementedException("glTextureStorage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureStorage3DMultisample(int texture, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTextureStorage3DMultisample wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTransformFeedbackBufferBase(int xfb, int index, int buffer) {
        throw new NotImplementedException("glTransformFeedbackBufferBase wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glTransformFeedbackBufferRange(int xfb, int index, int buffer, long offset, long size) {
        throw new NotImplementedException("glTransformFeedbackBufferRange wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public boolean glUnmapNamedBuffer(int buffer) {
        throw new NotImplementedException("glUnmapNamedBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex) {
        throw new NotImplementedException("glVertexArrayAttribBinding wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribFormat wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayAttribIFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribIFormat wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayAttribLFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribLFormat wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayBindingDivisor(int vaobj, int bindingindex, int divisor) {
        throw new NotImplementedException("glVertexArrayBindingDivisor wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayElementBuffer(int vaobj, int buffer) {
        throw new NotImplementedException("glVertexArrayElementBuffer wasn't implemented in OpenGL 44 yet");
    }

    @Override
    public void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride) {
        throw new NotImplementedException("glVertexArrayVertexBuffer wasn't implemented in OpenGL 44 yet");
    }
}
