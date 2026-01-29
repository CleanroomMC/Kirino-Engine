package com.cleanroommc.kirino.gl.dispatch;

import org.lwjglx.opengl.GL45;

import java.nio.*;

final class OGL45 extends OGL44 {
    @Override
    public void glBindTextureUnit(int unit, int texture) {
        GL45.glBindTextureUnit(unit, texture);
    }

    @Override
    public void glBlitNamedFramebuffer(int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        GL45.glBlitNamedFramebuffer(readFramebuffer, drawFramebuffer, srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    }

    @Override
    public int glCheckNamedFramebufferStatus(int framebuffer, int target) {
        return GL45.glCheckNamedFramebufferStatus(framebuffer, target);
    }

    @Override
    public void glClearNamedBufferData(int buffer, int internalformat, int format, int type, ByteBuffer data) {
        GL45.glClearNamedBufferData(buffer, internalformat, format, type, data);
    }

    @Override
    public void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format, int type, ByteBuffer data) {
        GL45.glClearNamedBufferData(buffer, internalformat, format, type, data);
    }

    @Override
    public void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, FloatBuffer value) {
        GL45.glClearNamedFramebuffer(framebuffer, buffer, drawbuffer, value);
    }

    @Override
    public void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        GL45.glClearNamedFramebuffer(framebuffer, buffer, drawbuffer, value);
    }

    @Override
    public void glClearNamedFramebufferu(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        GL45.glClearNamedFramebufferu(framebuffer, buffer, drawbuffer, value);
    }

    @Override
    public void glClipControl(int origin, int depth) {
        GL45.glClipControl(origin, depth);
    }

    @Override
    public void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int data_imageSize, long data_buffer_offset) {
        GL45.glCompressedTextureSubImage1D(texture, level, xoffset, width, format, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, ByteBuffer data) {
        GL45.glCompressedTextureSubImage1D(texture, level, xoffset, width, format, data);
    }

    @Override
    public void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        GL45.glCompressedTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, data_imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, ByteBuffer data) {
        GL45.glCompressedTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, data);
    }

    @Override
    public void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, long data_buffer_offset) {
        GL45.glCompressedTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data_buffer_offset);
    }

    @Override
    public void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, ByteBuffer data) {
        GL45.glCompressedTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, imageSize, data);
    }

    @Override
    public void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size) {
        GL45.glCopyNamedBufferSubData(readBuffer, writeBuffer, readOffset, writeOffset, size);
    }

    @Override
    public void glCopyTextureSubImage1D(int texture, int level, int xoffset, int x, int y, int width) {
        GL45.glCopyTextureSubImage1D(texture, level, xoffset, x, y, width);
    }

    @Override
    public void glCopyTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        GL45.glCopyTextureSubImage2D(texture, level, xoffset, yoffset, x, y, width, height);
    }

    @Override
    public void glCopyTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
        GL45.glCopyTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    @Override
    public int glCreateBuffers() {
        return GL45.glCreateBuffers();
    }

    @Override
    public void glCreateBuffers(IntBuffer buffers) {
        GL45.glCreateBuffers(buffers);
    }

    @Override
    public int glCreateFramebuffers() {
        return GL45.glCreateFramebuffers();
    }

    @Override
    public void glCreateFramebuffers(IntBuffer framebuffers) {
        GL45.glCreateFramebuffers(framebuffers);
    }

    @Override
    public int glCreateProgramPipelines() {
        return GL45.glCreateProgramPipelines();
    }

    @Override
    public void glCreateProgramPipelines(IntBuffer pipelines) {
        GL45.glCreateProgramPipelines(pipelines);
    }

    @Override
    public int glCreateQueries(int target) {
        return GL45.glCreateQueries(target);
    }

    @Override
    public void glCreateQueries(int target, IntBuffer ids) {
        GL45.glCreateQueries(target, ids);
    }

    @Override
    public int glCreateRenderbuffers() {
        return GL45.glCreateRenderbuffers();
    }

    @Override
    public void glCreateRenderbuffers(IntBuffer renderbuffers) {
        GL45.glCreateRenderbuffers(renderbuffers);
    }

    @Override
    public int glCreateSamplers() {
        return GL45.glCreateSamplers();
    }

    @Override
    public void glCreateSamplers(IntBuffer samplers) {
        GL45.glCreateSamplers(samplers);
    }

    @Override
    public int glCreateTextures(int target) {
        return GL45.glCreateTextures(target);
    }

    @Override
    public void glCreateTextures(int target, IntBuffer textures) {
        GL45.glCreateTextures(target, textures);
    }

    @Override
    public int glCreateTransformFeedbacks() {
        return GL45.glCreateTransformFeedbacks();
    }

    @Override
    public void glCreateTransformFeedbacks(IntBuffer ids) {
        GL45.glCreateTransformFeedbacks(ids);
    }

    @Override
    public int glCreateVertexArrays() {
        return GL45.glCreateVertexArrays();
    }

    @Override
    public void glCreateVertexArrays(IntBuffer arrays) {
        GL45.glCreateVertexArrays(arrays);
    }

    @Override
    public void glDisableVertexArrayAttrib(int vaobj, int index) {
        GL45.glDisableVertexArrayAttrib(vaobj, index);
    }

    @Override
    public void glEnableVertexArrayAttrib(int vaobj, int index) {
        GL45.glEnableVertexArrayAttrib(vaobj, index);
    }

    @Override
    public void glFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        GL45.glFlushMappedNamedBufferRange(buffer, offset, length);
    }

    @Override
    public void glGenerateTextureMipmap(int texture) {
        GL45.glGenerateTextureMipmap(texture);
    }

    @Override
    public void glGetCompressedTextureImage(int texture, int level, int pixels_bufSize, long pixels_buffer_offset) {
        GL45.glGetCompressedTextureImage(texture, level, pixels_bufSize, pixels_buffer_offset);
    }

    @Override
    public void glGetCompressedTextureImage(int texture, int level, ByteBuffer pixels) {
        GL45.glGetCompressedTextureImage(texture, level, pixels);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int pixels_bufSize, long pixels_buffer_offset) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels_bufSize, pixels_buffer_offset);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ByteBuffer pixels) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, DoubleBuffer pixels) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, FloatBuffer pixels) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, IntBuffer pixels) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels);
    }

    @Override
    public void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ShortBuffer pixels) {
        GL45.glGetCompressedTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, pixels);
    }

    @Override
    public int glGetGraphicsResetStatus() {
        return GL45.glGetGraphicsResetStatus();
    }

    @Override
    public void glGetNamedBufferParameter(int buffer, int pname, IntBuffer params) {
        GL45.glGetNamedBufferParameter(buffer, pname, params);
    }

    @Override
    public void glGetNamedBufferParameter(int buffer, int pname, LongBuffer params) {
        GL45.glGetNamedBufferParameter(buffer, pname, params);
    }

    @Override
    public long glGetNamedBufferParameteri64(int buffer, int pname) {
        return GL45.glGetNamedBufferParameteri64(buffer, pname);
    }

    @Override
    public int glGetNamedBufferParameteri(int buffer, int pname) {
        return GL45.glGetNamedBufferParameteri(buffer, pname);
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    @Override
    public void glGetNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        GL45.glGetNamedBufferSubData(buffer, offset, data);
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameter(int framebuffer, int attachment, int pname, IntBuffer params) {
        GL45.glGetNamedFramebufferAttachmentParameter(framebuffer, attachment, pname, params);
    }

    @Override
    public void glGetNamedFramebufferParameter(int framebuffer, int pname, IntBuffer params) {
        GL45.glGetNamedFramebufferParameter(framebuffer, pname, params);
    }

    @Override
    public void glGetNamedRenderbufferParameter(int renderbuffer, int pname, IntBuffer params) {
        GL45.glGetNamedRenderbufferParameter(renderbuffer, pname, params);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        GL45.glGetTextureImage(texture, level, format, type, pixels_bufSize, pixels_buffer_offset);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, ByteBuffer pixels) {
        GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, DoubleBuffer pixels) {
        GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, FloatBuffer pixels) {
        GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, IntBuffer pixels) {
        GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    @Override
    public void glGetTextureImage(int texture, int level, int format, int type, ShortBuffer pixels) {
        GL45.glGetTextureImage(texture, level, format, type, pixels);
    }

    @Override
    public void glGetTextureLevelParameter(int texture, int level, int pname, FloatBuffer params) {
        GL45.glGetTextureLevelParameter(texture, level, pname, params);
    }

    @Override
    public void glGetTextureLevelParameter(int texture, int level, int pname, IntBuffer params) {
        GL45.glGetTextureLevelParameter(texture, level, pname, params);
    }

    @Override
    public float glGetTextureLevelParameterf(int texture, int level, int pname) {
        return GL45.glGetTextureLevelParameterf(texture, level, pname);
    }

    @Override
    public int glGetTextureLevelParameteri(int texture, int level, int pname) {
        return GL45.glGetTextureLevelParameteri(texture, level, pname);
    }

    @Override
    public void glGetTextureParameter(int texture, int pname, FloatBuffer params) {
        GL45.glGetTextureParameter(texture, pname, params);
    }

    @Override
    public void glGetTextureParameter(int texture, int pname, IntBuffer params) {
        GL45.glGetTextureParameter(texture, pname, params);
    }

    @Override
    public void glGetTextureParameterI(int texture, int pname, IntBuffer params) {
        GL45.glGetTextureParameterI(texture, pname, params);
    }

    @Override
    public int glGetTextureParameterIi(int texture, int pname) {
        return GL45.glGetTextureParameterIi(texture, pname);
    }

    @Override
    public void glGetTextureParameterIu(int texture, int pname, IntBuffer params) {
        GL45.glGetTextureParameterIu(texture, pname, params);
    }

    @Override
    public int glGetTextureParameterIui(int texture, int pname) {
        return GL45.glGetTextureParameterIui(texture, pname);
    }

    @Override
    public float glGetTextureParameterf(int texture, int pname) {
        return GL45.glGetTextureParameterf(texture, pname);
    }

    @Override
    public int glGetTextureParameteri(int texture, int pname) {
        return GL45.glGetTextureParameteri(texture, pname);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels_bufSize, pixels_buffer_offset);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        GL45.glGetTextureSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    @Override
    public void glGetTransformFeedback(int xfb, int pname, int index, IntBuffer param) {
        GL45.glGetTransformFeedback(xfb, pname, index, param);
    }
	@Override
	public void glGetTransformFeedback(int xfb, int pname, int index, LongBuffer param) {
        GL45.glGetTransformFeedback(xfb, pname, index, param);
    }
	@Override
	public void glGetTransformFeedback(int xfb, int pname, IntBuffer param) {
        GL45.glGetTransformFeedback(xfb, pname, param);
    }
	@Override
	public long glGetTransformFeedbacki64(int xfb, int pname, int index) {
        return GL45.glGetTransformFeedbacki64(xfb, pname, index);
    }
	@Override
	public int glGetTransformFeedbacki(int xfb, int pname) {
        return GL45.glGetTransformFeedbacki(xfb, pname);
    }
	@Override
	public int glGetTransformFeedbacki(int xfb, int pname, int index) {
        return GL45.glGetTransformFeedbacki(xfb, pname, index);
    }
	@Override
	public void glGetVertexArray(int vaobj, int pname, IntBuffer param) {
        GL45.glGetVertexArray(vaobj, pname, param);
    }
	@Override
	public long glGetVertexArrayIndexed64i(int vaobj, int index, int pname) {
        return GL45.glGetVertexArrayIndexed64i(vaobj, index, pname);
    }
	@Override
	public void glGetVertexArrayIndexed64i(int vaobj, int index, int pname, LongBuffer param) {
        GL45.glGetVertexArrayIndexed64i(vaobj, index, pname, param);
    }
	@Override
	public void glGetVertexArrayIndexed(int vaobj, int index, int pname, IntBuffer param) {
        GL45.glGetVertexArrayIndexed(vaobj, index, pname, param);
    }
	@Override
	public void glGetnUniform(int program, int location, FloatBuffer params) {
        GL45.glGetnUniform(program, location, params);
    }
	@Override
	public void glGetnUniform(int program, int location, IntBuffer params) {
        GL45.glGetnUniform(program, location, params);
    }
	@Override
	public void glGetnUniformu(int program, int location, IntBuffer params) {
        GL45.glGetnUniformu(program, location, params);
    }
	@Override
	public void glInvalidateNamedFramebufferData(int framebuffer, IntBuffer attachments) {
        GL45.glInvalidateNamedFramebufferData(framebuffer, attachments);
    }
	@Override
	public void glInvalidateNamedFramebufferSubData(int framebuffer, IntBuffer attachments, int x, int y, int width, int height) {
        GL45.glInvalidateNamedFramebufferSubData(framebuffer, attachments, x, y, width, height);
    }
	@Override
	public ByteBuffer glMapNamedBuffer(int buffer, int access, long length, ByteBuffer old_buffer) {
        return GL45.glMapNamedBuffer(buffer, access, length, old_buffer);
    }
	@Override
	public ByteBuffer glMapNamedBuffer(int buffer, int access, ByteBuffer old_buffer) {
        return GL45.glMapNamedBuffer(buffer, access, old_buffer);
    }
	@Override
	public ByteBuffer glMapNamedBufferRange(int buffer, long offset, long length, int access, ByteBuffer old_buffer) {
        return GL45.glMapNamedBufferRange(buffer, offset, length, access, old_buffer);
    }
	@Override
	public void glMemoryBarrierByRegion(int barriers) {
        GL45.glMemoryBarrierByRegion(barriers);
    }
	@Override
	public void glNamedBufferData(int buffer, long data_size, int usage) {
        GL45.glNamedBufferData(buffer, data_size, usage);
    }
	@Override
	public void glNamedBufferData(int buffer, ByteBuffer data, int usage) {
        GL45.glNamedBufferData(buffer, data, usage);
    }
	@Override
	public void glNamedBufferData(int buffer, DoubleBuffer data, int usage) {
        GL45.glNamedBufferData(buffer, data, usage);
    }
	@Override
	public void glNamedBufferData(int buffer, FloatBuffer data, int usage) {
        GL45.glNamedBufferData(buffer, data, usage);
    }
	@Override
	public void glNamedBufferData(int buffer, IntBuffer data, int usage) {
        GL45.glNamedBufferData(buffer, data, usage);
    }
	@Override
	public void glNamedBufferData(int buffer, ShortBuffer data, int usage) {
        GL45.glNamedBufferData(buffer, data, usage);
    }
	@Override
	public void glNamedBufferStorage(int buffer, long size, int flags) {
        GL45.glNamedBufferStorage(buffer, size, flags);
    }
	@Override
	public void glNamedBufferStorage(int buffer, ByteBuffer data, int flags) {
        GL45.glNamedBufferStorage(buffer, data, flags);
    }
	@Override
	public void glNamedBufferStorage(int buffer, DoubleBuffer data, int flags) {
        GL45.glNamedBufferStorage(buffer, data, flags);
    }
	@Override
	public void glNamedBufferStorage(int buffer, FloatBuffer data, int flags) {
        GL45.glNamedBufferStorage(buffer, data, flags);
    }
	@Override
	public void glNamedBufferStorage(int buffer, IntBuffer data, int flags) {
        GL45.glNamedBufferStorage(buffer, data, flags);
    }
	@Override
	public void glNamedBufferStorage(int buffer, ShortBuffer data, int flags) {
        GL45.glNamedBufferStorage(buffer, data, flags);
    }
	@Override
	public void glNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        GL45.glNamedBufferSubData(buffer, offset, data);
    }
	@Override
	public void glNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        GL45.glNamedBufferSubData(buffer, offset, data);
    }
	@Override
	public void glNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        GL45.glNamedBufferSubData(buffer, offset, data);
    }
	@Override
	public void glNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        GL45.glNamedBufferSubData(buffer, offset, data);
    }
	@Override
	public void glNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        GL45.glNamedBufferSubData(buffer, offset, data);
    }
	@Override
	public void glNamedFramebufferDrawBuffer(int framebuffer, int mode) {
        GL45.glNamedFramebufferDrawBuffer(framebuffer, mode);
    }
	@Override
	public void glNamedFramebufferDrawBuffers(int framebuffer, IntBuffer bufs) {
        GL45.glNamedFramebufferDrawBuffers(framebuffer, bufs);
    }
	@Override
	public void glNamedFramebufferParameteri(int framebuffer, int pname, int param) {
        GL45.glNamedFramebufferParameteri(framebuffer, pname, param);
    }
	@Override
	public void glNamedFramebufferReadBuffer(int framebuffer, int mode) {
        GL45.glNamedFramebufferReadBuffer(framebuffer, mode);
    }
	@Override
	public void glNamedFramebufferRenderbuffer(int framebuffer, int attachment, int renderbuffertarget, int renderbuffer) {
        GL45.glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
    }
	@Override
	public void glNamedFramebufferTexture(int framebuffer, int attachment, int texture, int level) {
        GL45.glNamedFramebufferTexture(framebuffer, attachment, texture, level);
    }
	@Override
	public void glNamedFramebufferTextureLayer(int framebuffer, int attachment, int texture, int level, int layer) {
        GL45.glNamedFramebufferTextureLayer(framebuffer, attachment, texture, level, layer);
    }
	@Override
	public void glNamedRenderbufferStorage(int renderbuffer, int internalformat, int width, int height) {
        GL45.glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
    }
	@Override
	public void glNamedRenderbufferStorageMultisample(int renderbuffer, int samples, int internalformat, int width, int height) {
        GL45.glNamedRenderbufferStorageMultisample(renderbuffer, samples, internalformat, width, height);
    }
	@Override
	public void glReadnPixels(int x, int y, int width, int height, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        GL45.glReadnPixels(x, y, width, height, format, type, pixels_bufSize, pixels_buffer_offset);
    }
	@Override
	public void glReadnPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }
	@Override
	public void glReadnPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }
	@Override
	public void glReadnPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }
	@Override
	public void glReadnPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        GL45.glReadnPixels(x, y, width, height, format, type, pixels);
    }
	@Override
	public void glTextureBarrier() {
        GL45.glTextureBarrier();
    }
	@Override
	public void glTextureBuffer(int texture, int internalformat, int buffer) {
        GL45.glTextureBuffer(texture, internalformat, buffer);
    }
	@Override
	public void glTextureBufferRange(int texture, int internalformat, int buffer, long offset, long size) {
        GL45.glTextureBufferRange(texture, internalformat, buffer, offset, size);
    }
	@Override
	public void glTextureParameter(int texture, int pname, FloatBuffer params) {
        GL45.glTextureParameter(texture, pname, params);
    }
	@Override
	public void glTextureParameter(int texture, int pname, IntBuffer params) {
        GL45.glTextureParameter(texture, pname, params);
    }
	@Override
	public void glTextureParameterI(int texture, int pname, IntBuffer params) {
        GL45.glTextureParameterI(texture, pname, params);
    }
	@Override
	public void glTextureParameterIu(int texture, int pname, IntBuffer params) {
        GL45.glTextureParameterIu(texture, pname, params);
    }
	@Override
	public void glTextureParameterf(int texture, int pname, float param) {
        GL45.glTextureParameterf(texture, pname, param);
    }
	@Override
	public void glTextureParameteri(int texture, int pname, int param) {
        GL45.glTextureParameteri(texture, pname, param);
    }
	@Override
	public void glTextureStorage1D(int texture, int levels, int internalformat, int width) {
        GL45.glTextureStorage1D(texture, levels, internalformat, width);
    }
	@Override
	public void glTextureStorage2D(int texture, int levels, int internalformat, int width, int height) {
        GL45.glTextureStorage2D(texture, levels, internalformat, width, height);
    }
	@Override
	public void glTextureStorage2DMultisample(int texture, int samples, int internalformat, int width, int height, boolean fixedsamplelocations) {
        GL45.glTextureStorage2DMultisample(texture, samples, internalformat, width, height, fixedsamplelocations);
    }
	@Override
	public void glTextureStorage3D(int texture, int levels, int internalformat, int width, int height, int depth) {
        GL45.glTextureStorage3D(texture, levels, internalformat, width, height, depth);
    }
	@Override
	public void glTextureStorage3DMultisample(int texture, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations) {
        GL45.glTextureStorage3DMultisample(texture, samples, internalformat, width, height, depth, fixedsamplelocations);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels_buffer_offset);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ByteBuffer pixels) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, DoubleBuffer pixels) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, FloatBuffer pixels) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, IntBuffer pixels) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }
	@Override
	public void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ShortBuffer pixels) {
        GL45.glTextureSubImage1D(texture, level, xoffset, width, format, type, pixels);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels_buffer_offset);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, DoubleBuffer pixels) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, FloatBuffer pixels) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }
	@Override
	public void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ShortBuffer pixels) {
        GL45.glTextureSubImage2D(texture, level, xoffset, yoffset, width, height, format, type, pixels);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels_buffer_offset);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
	@Override
	public void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        GL45.glTextureSubImage3D(texture, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
	@Override
	public void glTransformFeedbackBufferBase(int xfb, int index, int buffer) {
        GL45.glTransformFeedbackBufferBase(xfb, index, buffer);
    }
	@Override
	public void glTransformFeedbackBufferRange(int xfb, int index, int buffer, long offset, long size) {
        GL45.glTransformFeedbackBufferRange(xfb, index, buffer, offset, size);
    }
	@Override
	public boolean glUnmapNamedBuffer(int buffer) {
        return GL45.glUnmapNamedBuffer(buffer);
    }
	@Override
	public void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex) {
        GL45.glVertexArrayAttribBinding(vaobj, attribindex, bindingindex);
    }
	@Override
	public void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        GL45.glVertexArrayAttribFormat(vaobj, attribindex, size, type, normalized, relativeoffset);
    }
	@Override
	public void glVertexArrayAttribIFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        GL45.glVertexArrayAttribIFormat(vaobj, attribindex, size, type, relativeoffset);
    }
	@Override
	public void glVertexArrayAttribLFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        GL45.glVertexArrayAttribLFormat(vaobj, attribindex, size, type, relativeoffset);
    }
	@Override
	public void glVertexArrayBindingDivisor(int vaobj, int bindingindex, int divisor) {
        GL45.glVertexArrayBindingDivisor(vaobj, bindingindex, divisor);
    }
	@Override
	public void glVertexArrayElementBuffer(int vaobj, int buffer) {
        GL45.glVertexArrayElementBuffer(vaobj, buffer);
    }
	@Override
	public void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride) {
        GL45.glVertexArrayVertexBuffer(vaobj, bindingindex, buffer, offset, stride);
    }
}
