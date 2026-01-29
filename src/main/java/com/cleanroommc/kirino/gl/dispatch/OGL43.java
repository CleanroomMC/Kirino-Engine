package com.cleanroommc.kirino.gl.dispatch;

import org.lwjglx.opengl.GL43;
import org.lwjglx.opengl.KHRDebugCallback;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

sealed class OGL43 implements OpenGL permits OGL44 {
    @Override
    public void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        GL43.glBindVertexBuffer(bindingindex, buffer, offset, stride);
    }

    @Override
    public void glClearBufferData(int target, int internalformat, int format, int type, ByteBuffer data) {
        GL43.glClearBufferData(target, internalformat, format, type, data);
    }

    @Override
    public void glClearBufferSubData(int target, int internalformat, long offset, long size, int format, int type, ByteBuffer data) {
        GL43.glClearBufferSubData(target, internalformat, offset, size, format, type, data);
    }

    @Override
    public void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
        GL43.glCopyImageSubData(srcName, srcTarget, srcLevel, srcX, srcY, srcZ, dstName, dstTarget, dstLevel, dstX, dstY, dstZ, srcWidth, srcHeight, srcDepth);
    }

    @Override
    public void glDebugMessageCallback(KHRDebugCallback callback) {
        GL43.glDebugMessageCallback(callback);
    }

    @Override
    public void glDebugMessageControl(int source, int type, int severity, IntBuffer ids, boolean enabled) {
        GL43.glDebugMessageControl(source, type, severity, ids, enabled);
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, CharSequence buf) {
        GL43.glDebugMessageInsert(source, type, id, severity, buf);
    }

    @Override
    public void glDebugMessageInsert(int source, int type, int id, int severity, ByteBuffer buf) {
        GL43.glDebugMessageInsert(source, type, id, severity, buf);
    }

    @Override
    public void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        GL43.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
    }

    @Override
    public void glDispatchComputeIndirect(long indirect) {
        GL43.glDispatchComputeIndirect(indirect);
    }

    @Override
    public void glFramebufferParameteri(int target, int pname, int param) {
        GL43.glFramebufferParameteri(target, pname, param);
    }

    @Override
    public int glGetDebugMessageLog(int count, IntBuffer sources, IntBuffer types, IntBuffer ids, IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog) {
        return GL43.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog);
    }

    @Override
    public void glGetFramebufferParameter(int target, int pname, IntBuffer params) {
        GL43.glGetFramebufferParameter(target, pname, params);
    }

    @Override
    public int glGetFramebufferParameteri(int target, int pname) {
        return GL43.glGetFramebufferParameteri(target, pname);
    }

    @Override
    public void glGetInternalformat(int target, int internalformat, int pname, LongBuffer params) {
        GL43.glGetInternalformat(target, internalformat, pname, params);
    }

    @Override
    public long glGetInternalformati64(int target, int internalformat, int pname) {
        return GL43.glGetInternalformati64(target, internalformat, pname);
    }

    @Override
    public String glGetObjectLabel(int identifier, int name, int bufSize) {
        return GL43.glGetObjectLabel(identifier, name, bufSize);
    }

    @Override
    public void glGetObjectLabel(int identifier, int name, IntBuffer length, ByteBuffer label) {
        GL43.glGetObjectLabel(identifier, name, length, label);
    }

    @Override
    public void glGetProgramInterface(int program, int programInterface, int pname, IntBuffer params) {
        GL43.glGetProgramInterface(program, programInterface, pname, params);
    }

    @Override
    public int glGetProgramInterfacei(int program, int programInterface, int pname) {
        return GL43.glGetProgramInterfacei(program, programInterface, pname);
    }

    @Override
    public void glGetProgramResource(int program, int programInterface, int index, IntBuffer props, IntBuffer length, IntBuffer params) {
        GL43.glGetProgramResource(program, programInterface, index, props, length, params);
    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, CharSequence name) {
        return GL43.glGetProgramResourceIndex(program, programInterface, name);
    }

    @Override
    public int glGetProgramResourceIndex(int program, int programInterface, ByteBuffer name) {
        return GL43.glGetProgramResourceIndex(program, programInterface, name);
    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, CharSequence name) {
        return GL43.glGetProgramResourceLocation(program, programInterface, name);
    }

    @Override
    public int glGetProgramResourceLocation(int program, int programInterface, ByteBuffer name) {
        return GL43.glGetProgramResourceLocation(program, programInterface, name);
    }

    @Override
    public int glGetProgramResourceLocationIndex(int program, int programInterface, CharSequence name) {
        return GL43.glGetProgramResourceLocationIndex(program, programInterface, name);
    }

    @Override
    public int glGetProgramResourceLocationIndex(int program, int programInterface, ByteBuffer name) {
        return GL43.glGetProgramResourceLocationIndex(program, programInterface, name);
    }

    @Override
    public String glGetProgramResourceName(int program, int programInterface, int index, int bufSize) {
        return GL43.glGetProgramResourceName(program, programInterface, index, bufSize);
    }

    @Override
    public void glGetProgramResourceName(int program, int programInterface, int index, IntBuffer length, ByteBuffer name) {
        GL43.glGetProgramResourceName(program, programInterface, index, length, name);
    }

    @Override
    public void glInvalidateBufferData(int buffer) {
        GL43.glInvalidateBufferData(buffer);
    }

    @Override
    public void glInvalidateBufferSubData(int buffer, long offset, long length) {
        GL43.glInvalidateBufferSubData(buffer, offset, length);
    }

    @Override
    public void glInvalidateFramebuffer(int target, IntBuffer attachments) {
        GL43.glInvalidateFramebuffer(target, attachments);
    }

    @Override
    public void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height) {
        GL43.glInvalidateSubFramebuffer(target, attachments, x, y, width, height);
    }

    @Override
    public void glInvalidateTexImage(int texture, int level) {
        GL43.glInvalidateTexImage(texture, level);
    }

    @Override
    public void glInvalidateTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth) {
        GL43.glInvalidateTexSubImage(texture, level, xoffset, yoffset, zoffset, width, height, depth);
    }

    @Override
    public void glMultiDrawArraysIndirect(int mode, long indirect_buffer_offset, int primcount, int stride) {
        GL43.glMultiDrawArraysIndirect(mode, indirect_buffer_offset, primcount, stride);
    }

    @Override
    public void glMultiDrawArraysIndirect(int mode, ByteBuffer indirect, int primcount, int stride) {
        GL43.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
    }

    @Override
    public void glMultiDrawArraysIndirect(int mode, IntBuffer indirect, int primcount, int stride) {
        GL43.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
    }

    @Override
    public void glMultiDrawElementsIndirect(int mode, int type, long indirect_buffer_offset, int primcount, int stride) {
        GL43.glMultiDrawElementsIndirect(mode, type, indirect_buffer_offset, primcount, stride);
    }

    @Override
    public void glMultiDrawElementsIndirect(int mode, int type, ByteBuffer indirect, int primcount, int stride) {
        GL43.glMultiDrawElementsIndirect(mode, type, indirect, primcount, stride);
    }

    @Override
    public void glMultiDrawElementsIndirect(int mode, int type, IntBuffer indirect, int primcount, int stride) {
        GL43.glMultiDrawElementsIndirect(mode, type, indirect, primcount, stride);
    }

    @Override
    public void glObjectLabel(int identifier, int name, CharSequence label) {
        GL43.glObjectLabel(identifier, name, label);
    }

    @Override
    public void glObjectLabel(int identifier, int name, ByteBuffer label) {
        GL43.glObjectLabel(identifier, name, label);
    }

    @Override
    public void glPopDebugGroup() {
        GL43.glPopDebugGroup();
    }

    @Override
    public void glPushDebugGroup(int source, int id, CharSequence message) {
        GL43.glPushDebugGroup(source, id, message);
    }

    @Override
    public void glPushDebugGroup(int source, int id, ByteBuffer message) {
        GL43.glPushDebugGroup(source, id, message);
    }

    @Override
    public void glShaderStorageBlockBinding(int program, int storageBlockIndex, int storageBlockBinding) {
        GL43.glShaderStorageBlockBinding(program, storageBlockIndex, storageBlockBinding);
    }

    @Override
    public void glTexBufferRange(int target, int internalformat, int buffer, long offset, long size) {
        GL43.glTexBufferRange(target, internalformat, buffer, offset, size);
    }

    @Override
    public void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations) {
        GL43.glTexStorage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
    }

    @Override
    public void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations) {
        GL43.glTexStorage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
    }

    @Override
    public void glTextureView(int texture, int target, int origtexture, int internalformat, int minlevel, int numlevels, int minlayer, int numlayers) {
        GL43.glTextureView(texture, target, origtexture, internalformat, minlevel, numlevels, minlayer, numlayers);
    }

    @Override
    public void glVertexAttribBinding(int attribindex, int bindingindex) {
        GL43.glVertexAttribBinding(attribindex, bindingindex);
    }

    @Override
    public void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        GL43.glVertexAttribFormat(attribindex, size, type, normalized, relativeoffset);
    }

    @Override
    public void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        GL43.glVertexAttribIFormat(attribindex, size, type, relativeoffset);
    }

    @Override
    public void glVertexAttribLFormat(int attribindex, int size, int type, int relativeoffset) {
        GL43.glVertexAttribLFormat(attribindex, size, type, relativeoffset);
    }

    @Override
    public void glVertexBindingDivisor(int bindingindex, int divisor) {
        GL43.glVertexBindingDivisor(bindingindex, divisor);
    }
}
