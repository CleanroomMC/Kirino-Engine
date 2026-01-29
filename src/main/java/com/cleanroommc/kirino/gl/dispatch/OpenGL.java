package com.cleanroommc.kirino.gl.dispatch;

import org.apache.commons.lang3.NotImplementedException;

import java.nio.*;

sealed interface OpenGL permits OGL44 {
    // <editor-fold desc="OpenGL 44">
    default void glBufferStorage(int target, long size, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glBufferStorage(int target, ByteBuffer data, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glBufferStorage(int target, DoubleBuffer data, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glBufferStorage(int target, FloatBuffer data, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glBufferStorage(int target, IntBuffer data, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glBufferStorage(int target, ShortBuffer data, int flags) {
        throw new NotImplementedException("glBufferStorage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, DoubleBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, FloatBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, IntBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, LongBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexImage(int texture, int level, int format, int type, ShortBuffer data) {
        throw new NotImplementedException("glClearTexImage is only avaliable for OpenGL 44 and above");
    }
	default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, LongBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    default void glClearTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer data) {
        throw new NotImplementedException("glClearTexSubImage is only avaliable for OpenGL 44 and above");
    }
    // </editor-fold>
    // <editor-fold desc="OpenGL 45">
    default void glBindTextureUnit(int unit, int texture) {
        throw new NotImplementedException("glBindTextureUnit is only avaliable for OpenGL 45 and above");
    }

    default void glBlitNamedFramebuffer(int readFramebuffer, int drawFramebuffer, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
        throw new NotImplementedException("glBlitNamedFramebuffer is only avaliable for OpenGL 45 and above");
    }

    default int glCheckNamedFramebufferStatus(int framebuffer, int target) {
        throw new NotImplementedException("glCheckNamedFramebufferStatus is only avaliable for OpenGL 45 and above");
    }

    default void glClearNamedBufferData(int buffer, int internalformat, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glClearNamedBufferSubData(int buffer, int internalformat, long offset, long size, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, FloatBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer is only avaliable for OpenGL 45 and above");
    }

    default void glClearNamedFramebuffer(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer is only avaliable for OpenGL 45 and above");
    }

    default void glClearNamedFramebufferu(int framebuffer, int buffer, int drawbuffer, IntBuffer value) {
        throw new NotImplementedException("glClearNamedFramebuffer is only avaliable for OpenGL 45 and above");
    }

    default void glClipControl(int origin, int depth) {
        throw new NotImplementedException("glClearControl is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int data_imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage1D(int texture, int level, int xoffset, int width, int format, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, long data_buffer_offset) {
        throw new NotImplementedException("glCompressedTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glCompressedTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int imageSize, ByteBuffer data) {
        throw new NotImplementedException("glCompressedTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glCopyNamedBufferSubData(int readBuffer, int writeBuffer, long readOffset, long writeOffset, long size) {
        throw new NotImplementedException("glCopyNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glCopyTextureSubImage1D(int texture, int level, int xoffset, int x, int y, int width) {
        throw new NotImplementedException("glCopyTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glCopyTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        throw new NotImplementedException("glCopyTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glCopyTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height) {
        throw new NotImplementedException("glCopyTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default int glCreateBuffers() {
        throw new NotImplementedException("glCreateBuffers is only avaliable for OpenGL 45 and above");
    }

    default void glCreateBuffers(IntBuffer buffers) {
        throw new NotImplementedException("glCreateBuffers is only avaliable for OpenGL 45 and above");
    }

    default int glCreateFramebuffers() {
        throw new NotImplementedException("glCreateFramebuffers is only avaliable for OpenGL 45 and above");
    }

    default void glCreateFramebuffers(IntBuffer framebuffers) {
        throw new NotImplementedException("glCreateFramebuffers is only avaliable for OpenGL 45 and above");
    }

    default int glCreateProgramPipelines() {
        throw new NotImplementedException("glCreateProgramPipelines is only avaliable for OpenGL 45 and above");
    }

    default void glCreateProgramPipelines(IntBuffer pipelines) {
        throw new NotImplementedException("glCreateProgramPipelines is only avaliable for OpenGL 45 and above");
    }

    default int glCreateQueries(int target) {
        throw new NotImplementedException("glCreateQueries is only avaliable for OpenGL 45 and above");
    }

    default void glCreateQueries(int target, IntBuffer ids) {
        throw new NotImplementedException("glCreateQueries is only avaliable for OpenGL 45 and above");
    }

    default int glCreateRenderbuffers() {
        throw new NotImplementedException("glCreateRenderbuffers is only avaliable for OpenGL 45 and above");
    }

    default void glCreateRenderbuffers(IntBuffer renderbuffers) {
        throw new NotImplementedException("glCreateRenderbuffers is only avaliable for OpenGL 45 and above");
    }

    default int glCreateSamplers() {
        throw new NotImplementedException("glCreateSamplers is only avaliable for OpenGL 45 and above");
    }

    default void glCreateSamplers(IntBuffer samplers) {
        throw new NotImplementedException("glCreateSamplers is only avaliable for OpenGL 45 and above");
    }

    default int glCreateTextures(int target) {
        throw new NotImplementedException("glCreateTextures is only avaliable for OpenGL 45 and above");
    }

    default void glCreateTextures(int target, IntBuffer textures) {
        throw new NotImplementedException("glCreateTextures is only avaliable for OpenGL 45 and above");
    }

    default int glCreateTransformFeedbacks() {
        throw new NotImplementedException("glCreateTransformFeedbacks is only avaliable for OpenGL 45 and above");
    }

    default void glCreateTransformFeedbacks(IntBuffer ids) {
        throw new NotImplementedException("glCreateTransformFeedbacks is only avaliable for OpenGL 45 and above");
    }

    default int glCreateVertexArrays() {
        throw new NotImplementedException("glCreateVertexArrays is only avaliable for OpenGL 45 and above");
    }

    default void glCreateVertexArrays(IntBuffer arrays) {
        throw new NotImplementedException("glCreateVertexArrays is only avaliable for OpenGL 45 and above");
    }

    default void glDisableVertexArrayAttrib(int vaobj, int index) {
        throw new NotImplementedException("glDisableVertexArrayAttrib is only avaliable for OpenGL 45 and above");
    }

    default void glEnableVertexArrayAttrib(int vaobj, int index) {
        throw new NotImplementedException("glEnableVertexArrayAttrib is only avaliable for OpenGL 45 and above");
    }

    default void glFlushMappedNamedBufferRange(int buffer, long offset, long length) {
        throw new NotImplementedException("glFlushMappedNamedBufferRange is only avaliable for OpenGL 45 and above");
    }

    default void glGenerateTextureMipmap(int texture) {
        throw new NotImplementedException("glGenerateTextureMipmap is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureImage(int texture, int level, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetCompressedTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureImage(int texture, int level, ByteBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ByteBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, FloatBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, IntBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetCompressedTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, ShortBuffer pixels) {
        throw new NotImplementedException("glGetCompressedTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default int glGetGraphicsResetStatus() {
        throw new NotImplementedException("glGetGraphicsResetStatus is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferParameter(int buffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedBufferParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferParameter(int buffer, int pname, LongBuffer params) {
        throw new NotImplementedException("glGetNamedBufferParameter is only avaliable for OpenGL 45 and above");
    }

    default long glGetNamedBufferParameteri64(int buffer, int pname) {
        throw new NotImplementedException("glGetNamedBufferParameteri64 is only avaliable for OpenGL 45 and above");
    }

    default int glGetNamedBufferParameteri(int buffer, int pname) {
        throw new NotImplementedException("glGetNamedBufferParameteri is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        throw new NotImplementedException("glGetNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedFramebufferAttachmentParameter(int framebuffer, int attachment, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedFramebufferAttachmentParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedFramebufferParameter(int framebuffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedFramebufferParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetNamedRenderbufferParameter(int renderbuffer, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetNamedRenderbufferParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureImage(int texture, int level, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glGetTextureImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureLevelParameter(int texture, int level, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTextureLevelParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureLevelParameter(int texture, int level, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureLevelParameter is only avaliable for OpenGL 45 and above");
    }

    default float glGetTextureLevelParameterf(int texture, int level, int pname) {
        throw new NotImplementedException("glGetTextureLevelParameterf is only avaliable for OpenGL 45 and above");
    }

    default int glGetTextureLevelParameteri(int texture, int level, int pname) {
        throw new NotImplementedException("glGetTextureLevelParameteri is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureParameter(int texture, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTextureParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureParameter(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameter is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureParameterI(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameterI is only avaliable for OpenGL 45 and above");
    }

    default int glGetTextureParameterIi(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterIi is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureParameterIu(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTextureParameterIu is only avaliable for OpenGL 45 and above");
    }

    default int glGetTextureParameterIui(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterIui is only avaliable for OpenGL 45 and above");
    }

    default float glGetTextureParameterf(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameterf is only avaliable for OpenGL 45 and above");
    }

    default int glGetTextureParameteri(int texture, int pname) {
        throw new NotImplementedException("glGetTextureParameteri is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTextureSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glGetTextureSubImage is only avaliable for OpenGL 45 and above");
    }

    default void glGetTransformFeedback(int xfb, int pname, int index, IntBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback is only avaliable for OpenGL 45 and above");
    }

    default void glGetTransformFeedback(int xfb, int pname, int index, LongBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback is only avaliable for OpenGL 45 and above");
    }

    default void glGetTransformFeedback(int xfb, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetTransformFeedback is only avaliable for OpenGL 45 and above");
    }

    default long glGetTransformFeedbacki64(int xfb, int pname, int index) {
        throw new NotImplementedException("glGetTransformFeedbacki64 is only avaliable for OpenGL 45 and above");
    }

    default int glGetTransformFeedbacki(int xfb, int pname) {
        throw new NotImplementedException("glGetTransformFeedbacki is only avaliable for OpenGL 45 and above");
    }

    default int glGetTransformFeedbacki(int xfb, int pname, int index) {
        throw new NotImplementedException("glGetTransformFeedbacki is only avaliable for OpenGL 45 and above");
    }

    default void glGetVertexArray(int vaobj, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetVertexArray is only avaliable for OpenGL 45 and above");
    }

    default long glGetVertexArrayIndexed64i(int vaobj, int index, int pname) {
        throw new NotImplementedException("glGetVertexArrayIndexed64i is only avaliable for OpenGL 45 and above");
    }

    default void glGetVertexArrayIndexed64i(int vaobj, int index, int pname, LongBuffer param) {
        throw new NotImplementedException("glGetVertexArrayIndexed64i is only avaliable for OpenGL 45 and above");
    }

    default void glGetVertexArrayIndexed(int vaobj, int index, int pname, IntBuffer param) {
        throw new NotImplementedException("glGetVertexArrayIndexed is only avaliable for OpenGL 45 and above");
    }

    default void glGetnUniform(int program, int location, FloatBuffer params) {
        throw new NotImplementedException("glGetnUniform is only avaliable for OpenGL 45 and above");
    }

    default void glGetnUniform(int program, int location, IntBuffer params) {
        throw new NotImplementedException("glGetnUniform is only avaliable for OpenGL 45 and above");
    }

    default void glGetnUniformu(int program, int location, IntBuffer params) {
        throw new NotImplementedException("glGetnUniformu is only avaliable for OpenGL 45 and above");
    }

    default void glInvalidateNamedFramebufferData(int framebuffer, IntBuffer attachments) {
        throw new NotImplementedException("glInvalidateNamedFramebufferData is only avaliable for OpenGL 45 and above");
    }

    default void glInvalidateNamedFramebufferSubData(int framebuffer, IntBuffer attachments, int x, int y, int width, int height) {
        throw new NotImplementedException("glInvalidateNamedFramebufferSubData is only avaliable for OpenGL 45 and above");
    }

    default ByteBuffer glMapNamedBuffer(int buffer, int access, long length, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBuffer is only avaliable for OpenGL 45 and above");
    }

    default ByteBuffer glMapNamedBuffer(int buffer, int access, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBuffer is only avaliable for OpenGL 45 and above");
    }

    default ByteBuffer glMapNamedBufferRange(int buffer, long offset, long length, int access, ByteBuffer old_buffer) {
        throw new NotImplementedException("glMapNamedBufferRange is only avaliable for OpenGL 45 and above");
    }

    default void glMemoryBarrierByRegion(int barriers) {
        throw new NotImplementedException("glMemoryBarrierByRegion is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, long data_size, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, ByteBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, DoubleBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, FloatBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, IntBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferData(int buffer, ShortBuffer data, int usage) {
        throw new NotImplementedException("glNamedBufferData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, long size, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, ByteBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, DoubleBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, FloatBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, IntBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferStorage(int buffer, ShortBuffer data, int flags) {
        throw new NotImplementedException("glNamedBufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferSubData(int buffer, long offset, ByteBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferSubData(int buffer, long offset, DoubleBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferSubData(int buffer, long offset, FloatBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferSubData(int buffer, long offset, IntBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedBufferSubData(int buffer, long offset, ShortBuffer data) {
        throw new NotImplementedException("glNamedBufferSubData is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferDrawBuffer(int framebuffer, int mode) {
        throw new NotImplementedException("glNamedFramebufferDrawBuffer is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferDrawBuffers(int framebuffer, IntBuffer bufs) {
        throw new NotImplementedException("glNamedFramebufferDrawBuffers is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferParameteri(int framebuffer, int pname, int param) {
        throw new NotImplementedException("glNamedFramebufferParameteri is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferReadBuffer(int framebuffer, int mode) {
        throw new NotImplementedException("glNamedFramebufferReadBuffer is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferRenderbuffer(int framebuffer, int attachment, int renderbuffertarget, int renderbuffer) {
        throw new NotImplementedException("glNamedFramebufferRenderbuffer is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferTexture(int framebuffer, int attachment, int texture, int level) {
        throw new NotImplementedException("glNamedFramebufferTexture is only avaliable for OpenGL 45 and above");
    }

    default void glNamedFramebufferTextureLayer(int framebuffer, int attachment, int texture, int level, int layer) {
        throw new NotImplementedException("glNamedFramebufferTextureLayer is only avaliable for OpenGL 45 and above");
    }

    default void glNamedRenderbufferStorage(int renderbuffer, int internalformat, int width, int height) {
        throw new NotImplementedException("glNamedRenderbufferStorage is only avaliable for OpenGL 45 and above");
    }

    default void glNamedRenderbufferStorageMultisample(int renderbuffer, int samples, int internalformat, int width, int height) {
        throw new NotImplementedException("glNamedRenderbufferStorageMultisample is only avaliable for OpenGL 45 and above");
    }

    default void glReadnPixels(int x, int y, int width, int height, int format, int type, int pixels_bufSize, long pixels_buffer_offset) {
        throw new NotImplementedException("glReadnPixels is only avaliable for OpenGL 45 and above");
    }

    default void glReadnPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glReadnPixels is only avaliable for OpenGL 45 and above");
    }

    default void glReadnPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glReadnPixels is only avaliable for OpenGL 45 and above");
    }

    default void glReadnPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glReadnPixels is only avaliable for OpenGL 45 and above");
    }

    default void glReadnPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glReadnPixels is only avaliable for OpenGL 45 and above");
    }

    default void glTextureBarrier() {
        throw new NotImplementedException("glTextureBarrier is only avaliable for OpenGL 45 and above");
    }

    default void glTextureBuffer(int texture, int internalformat, int buffer) {
        throw new NotImplementedException("glTextureBuffer is only avaliable for OpenGL 45 and above");
    }

    default void glTextureBufferRange(int texture, int internalformat, int buffer, long offset, long size) {
        throw new NotImplementedException("glTextureBufferRange is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameter(int texture, int pname, FloatBuffer params) {
        throw new NotImplementedException("glTextureParameter is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameter(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameter is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameterI(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameterI is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameterIu(int texture, int pname, IntBuffer params) {
        throw new NotImplementedException("glTextureParameterIu is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameterf(int texture, int pname, float param) {
        throw new NotImplementedException("glTextureParameterf is only avaliable for OpenGL 45 and above");
    }

    default void glTextureParameteri(int texture, int pname, int param) {
        throw new NotImplementedException("glTextureParameteri is only avaliable for OpenGL 45 and above");
    }

    default void glTextureStorage1D(int texture, int levels, int internalformat, int width) {
        throw new NotImplementedException("glTextureStorage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureStorage2D(int texture, int levels, int internalformat, int width, int height) {
        throw new NotImplementedException("glTextureStorage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureStorage2DMultisample(int texture, int samples, int internalformat, int width, int height, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTextureStorage2DMultisample is only avaliable for OpenGL 45 and above");
    }

    default void glTextureStorage3D(int texture, int levels, int internalformat, int width, int height, int depth) {
        throw new NotImplementedException("glTextureStorage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureStorage3DMultisample(int texture, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTextureStorage3DMultisample is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage1D(int texture, int level, int xoffset, int width, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage1D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage2D(int texture, int level, int xoffset, int yoffset, int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage2D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTextureSubImage3D(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTextureSubImage3D is only avaliable for OpenGL 45 and above");
    }

    default void glTransformFeedbackBufferBase(int xfb, int index, int buffer) {
        throw new NotImplementedException("glTransformFeedbackBufferBase is only avaliable for OpenGL 45 and above");
    }

    default void glTransformFeedbackBufferRange(int xfb, int index, int buffer, long offset, long size) {
        throw new NotImplementedException("glTransformFeedbackBufferRange is only avaliable for OpenGL 45 and above");
    }

    default boolean glUnmapNamedBuffer(int buffer) {
        throw new NotImplementedException("glUnmapNamedBuffer is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayAttribBinding(int vaobj, int attribindex, int bindingindex) {
        throw new NotImplementedException("glVertexArrayAttribBinding is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayAttribFormat(int vaobj, int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribFormat is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayAttribIFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribIFormat is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayAttribLFormat(int vaobj, int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexArrayAttribLFormat is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayBindingDivisor(int vaobj, int bindingindex, int divisor) {
        throw new NotImplementedException("glVertexArrayBindingDivisor is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayElementBuffer(int vaobj, int buffer) {
        throw new NotImplementedException("glVertexArrayElementBuffer is only avaliable for OpenGL 45 and above");
    }

    default void glVertexArrayVertexBuffer(int vaobj, int bindingindex, int buffer, long offset, int stride) {
        throw new NotImplementedException("glVertexArrayVertexBuffer is only avaliable for OpenGL 45 and above");
    }
    // </editor-fold>
}
