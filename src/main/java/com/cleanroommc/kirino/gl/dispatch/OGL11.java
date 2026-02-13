package com.cleanroommc.kirino.gl.dispatch;

import org.lwjgl.opengl.GL11;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

import java.nio.*;

sealed class OGL11 implements OpenGL permits OGL12 {
    @Override
    public void glAccum(int op, float value) {
        GL11.glAccum(op, value);
    }

    @Override
    public void glAlphaFunc(int func, float ref) {
        GL11.glAlphaFunc(func, ref);
    }

    @Override
    public boolean glAreTexturesResident(IntBuffer textures, ByteBuffer residences) {
        return GL11.glAreTexturesResident(textures, residences);
    }

    @Override
    public void glArrayElement(int i) {
        GL11.glArrayElement(i);
    }

    @Override
    public void glBegin(int mode) {
        GL11.glBegin(mode);
    }

    @Override
    public void glBindTexture(int target, int texture) {
        GL11.glBindTexture(target, texture);
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, long bitmap_buffer_offset) {
        GL11.glBitmap(width, height, xorig, yorig, xmove, ymove, bitmap_buffer_offset);
    }

    @Override
    public void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, ByteBuffer bitmap) {
        GL11.glBitmap(width, height, xorig, yorig, xmove, ymove, bitmap);
    }

    @Override
    public void glBlendFunc(int sfactor, int dfactor) {
        GL11.glBlendFunc(sfactor, dfactor);
    }

    @Override
    public void glCallList(int list) {
        GL11.glCallList(list);
    }

    @Override
    public void glCallLists(ByteBuffer lists) {
        GL11.glCallLists(lists);
    }

    @Override
    public void glCallLists(IntBuffer lists) {
        GL11.glCallLists(lists);
    }

    @Override
    public void glCallLists(ShortBuffer lists) {
        GL11.glCallLists(lists);
    }

    @Override
    public void glClear(int mask) {
        GL11.glClear(mask);
    }

    @Override
    public void glClearAccum(float red, float green, float blue, float alpha) {
        GL11.glClearAccum(red, green, blue, alpha);
    }

    @Override
    public void glClearColor(float red, float green, float blue, float alpha) {
        GL11.glClearColor(red, green, blue, alpha);
    }

    @Override
    public void glClearDepth(double depth) {
        GL11.glClearDepth(depth);
    }

    @Override
    public void glClearStencil(int s) {
        GL11.glClearStencil(s);
    }

    @Override
    public void glClipPlane(int plane, DoubleBuffer equation) {
        GL11.glClipPlane(plane, equation);
    }

    @Override
    public void glColor3b(byte red, byte green, byte blue) {
        GL11.glColor3b(red, green, blue);
    }

    @Override
    public void glColor3d(double red, double green, double blue) {
        GL11.glColor3d(red, green, blue);
    }

    @Override
    public void glColor3f(float red, float green, float blue) {
        GL11.glColor3f(red, green, blue);
    }

    @Override
    public void glColor3ub(byte red, byte green, byte blue) {
        GL11.glColor3ub(red, green, blue);
    }

    @Override
    public void glColor4b(byte red, byte green, byte blue, byte alpha) {
        GL11.glColor4b(red, green, blue, alpha);
    }

    @Override
    public void glColor4d(double red, double green, double blue, double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    @Override
    public void glColor4f(float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
    }

    @Override
    public void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        GL11.glColor4ub(red, green, blue, alpha);
    }

    @Override
    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        GL11.glColorMask(red, green, blue, alpha);
    }

    @Override
    public void glColorMaterial(int face, int mode) {
        GL11.glColorMaterial(face, mode);
    }

    @Override
    public void glColorPointer(int size, int type, int stride, long pointer_buffer_offset) {
        GL11.glColorPointer(size, type, stride, pointer_buffer_offset);
    }

    @Override
    public void glColorPointer(int size, int type, int stride, ByteBuffer pointer) {
        GL11.glColorPointer(size, type, stride, pointer);
    }

    @Override
    public void glColorPointer(int size, int stride, DoubleBuffer pointer) {
        GL11.glColorPointer(size, stride, pointer);
    }

    @Override
    public void glColorPointer(int size, int stride, FloatBuffer pointer) {
        GL11.glColorPointer(size, stride, pointer);
    }

    @Override
    public void glColorPointer(int size, boolean unsigned, int stride, ByteBuffer pointer) {
        GL11.glColorPointer(size, unsigned ? 5121 : 5120, stride, MemoryUtil.getAddress(pointer));
    }

    @Override
    public void glCopyPixels(int x, int y, int width, int height, int type) {
        GL11.glCopyPixels(x, y, width, height, type);
    }

    @Override
    public void glCopyTexImage1D(int target, int level, int internalFormat, int x, int y, int width, int border) {
        GL11.glCopyTexImage1D(target, level, internalFormat, x, y, width, border);
    }

    @Override
    public void glCopyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        GL11.glCopyTexImage2D(target, level, internalFormat, x, y, width, height, border);
    }

    @Override
    public void glCopyTexSubImage1D(int target, int level, int xoffset, int x, int y, int width) {
        GL11.glCopyTexSubImage1D(target, level, xoffset, x, y, width);
    }

    @Override
    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    }

    @Override
    public void glCullFace(int mode) {
        GL11.glCullFace(mode);
    }

    @Override
    public void glDeleteLists(int list, int range) {
        GL11.glDeleteLists(list, range);
    }

    @Override
    public void glDeleteTextures(int texture) {
        GL11.glDeleteTextures(texture);
    }

    @Override
    public void glDeleteTextures(IntBuffer textures) {
        GL11.glDeleteTextures(textures);
    }

    @Override
    public void glDepthFunc(int func) {
        GL11.glDepthFunc(func);
    }

    @Override
    public void glDepthMask(boolean flag) {
        GL11.glDepthMask(flag);
    }

    @Override
    public void glDepthRange(double zNear, double zFar) {
        GL11.glDepthRange(zNear, zFar);
    }

    @Override
    public void glDisable(int cap) {
        GL11.glDisable(cap);
    }

    @Override
    public void glDisableClientState(int cap) {
        GL11.glDisableClientState(cap);
    }

    @Override
    public void glDrawArrays(int mode, int first, int count) {
        GL11.glDrawArrays(mode, first, count);
    }

    @Override
    public void glDrawBuffer(int mode) {
        GL11.glDrawBuffer(mode);
    }

    @Override
    public void glDrawElements(int mode, int indices_count, int type, long indices_buffer_offset) {
        GL11.glDrawElements(mode, indices_count, type, indices_buffer_offset);
    }

    @Override
    public void glDrawElements(int mode, int count, int type, ByteBuffer indices) {
        GL11.glDrawElements(mode, count, type, MemoryUtil.getAddress(indices));
    }

    @Override
    public void glDrawElements(int mode, ByteBuffer indices) {
        GL11.glDrawElements(mode, indices);
    }

    @Override
    public void glDrawElements(int mode, IntBuffer indices) {
        GL11.glDrawElements(mode, indices);
    }

    @Override
    public void glDrawElements(int mode, ShortBuffer indices) {
        GL11.glDrawElements(mode, indices);
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, long pixels_buffer_offset) {
        GL11.glDrawPixels(width, height, format, type, pixels_buffer_offset);
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, ByteBuffer pixels) {
        GL11.glDrawPixels(width, height, format, type, pixels);
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, IntBuffer pixels) {
        GL11.glDrawPixels(width, height, format, type, pixels);
    }

    @Override
    public void glDrawPixels(int width, int height, int format, int type, ShortBuffer pixels) {
        GL11.glDrawPixels(width, height, format, type, pixels);
    }

    @Override
    public void glEdgeFlag(boolean flag) {
        GL11.glEdgeFlag(flag);
    }

    @Override
    public void glEdgeFlagPointer(int stride, long pointer_buffer_offset) {
        GL11.glEdgeFlagPointer(stride, pointer_buffer_offset);
    }

    @Override
    public void glEdgeFlagPointer(int stride, ByteBuffer pointer) {
        GL11.glEdgeFlagPointer(stride, pointer);
    }

    @Override
    public void glEnable(int cap) {
        GL11.glEnable(cap);
    }

    @Override
    public void glEnableClientState(int cap) {
        GL11.glEnableClientState(cap);
    }

    @Override
    public void glEnd() {
        GL11.glEnd();
    }

    @Override
    public void glEndList() {
        GL11.glEndList();
    }

    @Override
    public void glEvalCoord1d(double u) {
        GL11.glEvalCoord1d(u);
    }

    @Override
    public void glEvalCoord1f(float u) {
        GL11.glEvalCoord1f(u);
    }

    @Override
    public void glEvalCoord2d(double u, double v) {
        GL11.glEvalCoord2d(u, v);
    }

    @Override
    public void glEvalCoord2f(float u, float v) {
        GL11.glEvalCoord2f(u, v);
    }

    @Override
    public void glEvalMesh1(int mode, int i1, int i2) {
        GL11.glEvalMesh1(mode, i1, i2);
    }

    @Override
    public void glEvalMesh2(int mode, int i1, int i2, int j1, int j2) {
        GL11.glEvalMesh2(mode, i1, i2, j1, j2);
    }

    @Override
    public void glEvalPoint1(int i) {
        GL11.glEvalPoint1(i);
    }

    @Override
    public void glEvalPoint2(int i, int j) {
        GL11.glEvalPoint2(i, j);
    }

    @Override
    public void glFeedbackBuffer(int type, FloatBuffer buffer) {
        GL11.glFeedbackBuffer(type, buffer);
    }

    @Override
    public void glFinish() {
        GL11.glFinish();
    }

    @Override
    public void glFlush() {
        GL11.glFlush();
    }

    @Override
    public void glFog(int pname, FloatBuffer params) {
        GL11.glFog(pname, params);
    }

    @Override
    public void glFog(int pname, IntBuffer params) {
        GL11.glFog(pname, params);
    }

    @Override
    public void glFogf(int pname, float param) {
        GL11.glFogf(pname, param);
    }

    @Override
    public void glFogi(int pname, int param) {
        GL11.glFogi(pname, param);
    }

    @Override
    public void glFrontFace(int mode) {
        GL11.glFrontFace(mode);
    }

    @Override
    public void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar) {
        GL11.glFrustum(left, right, bottom, top, zNear, zFar);
    }

    @Override
    public int glGenLists(int range) {
        return GL11.glGenLists(range);
    }

    @Override
    public int glGenTextures() {
        return GL11.glGenTextures();
    }

    @Override
    public void glGenTextures(IntBuffer textures) {
        GL11.glGenTextures(textures);
    }

    @Override
    public boolean glGetBoolean(int pname) {
        return GL11.glGetBoolean(pname);
    }

    @Override
    public void glGetBoolean(int pname, ByteBuffer params) {
        GL11.glGetBoolean(pname, params);
    }

    @Override
    public void glGetClipPlane(int plane, DoubleBuffer equation) {
        GL11.glGetClipPlane(plane, equation);
    }

    @Override
    public double glGetDouble(int pname) {
        return GL11.glGetDouble(pname);
    }

    @Override
    public void glGetDouble(int pname, DoubleBuffer params) {
        GL11.glGetDouble(pname, params);
    }

    @Override
    public int glGetError() {
        return GL11.glGetError();
    }

    @Override
    public float glGetFloat(int pname) {
        return GL11.glGetFloat(pname);
    }

    @Override
    public void glGetFloat(int pname, FloatBuffer params) {
        GL11.glGetFloat(pname, params);
    }

    @Override
    public int glGetInteger(int pname) {
        return GL11.glGetInteger(pname);
    }

    @Override
    public void glGetInteger(int pname, IntBuffer params) {
        GL11.glGetInteger(pname, params);
    }

    @Override
    public void glGetLight(int light, int pname, FloatBuffer params) {
        GL11.glGetLight(light, pname, params);
    }

    @Override
    public void glGetLight(int light, int pname, IntBuffer params) {
        GL11.glGetLight(light, pname, params);
    }

    @Override
    public void glGetMap(int target, int query, DoubleBuffer v) {
        GL11.glGetMap(target, query, v);
    }

    @Override
    public void glGetMap(int target, int query, FloatBuffer v) {
        GL11.glGetMap(target, query, v);
    }

    @Override
    public void glGetMap(int target, int query, IntBuffer v) {
        GL11.glGetMap(target, query, v);
    }

    @Override
    public void glGetMaterial(int face, int pname, FloatBuffer params) {
        GL11.glGetMaterial(face, pname, params);
    }

    @Override
    public void glGetMaterial(int face, int pname, IntBuffer params) {
        GL11.glGetMaterial(face, pname, params);
    }

    @Override
    public void glGetPixelMap(int map, FloatBuffer values) {
        GL11.glGetPixelMap(map, values);
    }

    @Override
    public void glGetPixelMapfv(int map, long values_buffer_offset) {
        GL11.glGetPixelMapfv(map, values_buffer_offset);
    }

    @Override
    public void glGetPixelMapu(int map, IntBuffer values) {
        GL11.glGetPixelMapu(map, values);
    }

    @Override
    public void glGetPixelMapu(int map, ShortBuffer values) {
        GL11.glGetPixelMapu(map, values);
    }

    @Override
    public void glGetPixelMapuiv(int map, long values_buffer_offset) {
        GL11.glGetPixelMapuiv(map, values_buffer_offset);
    }

    @Override
    public void glGetPixelMapusv(int map, long values_buffer_offset) {
        GL11.glGetPixelMapusv(map, values_buffer_offset);
    }

    @Override
    public void glGetPolygonStipple(long mask_buffer_offset) {
        GL11.glGetPolygonStipple(mask_buffer_offset);
    }

    @Override
    public void glGetPolygonStipple(ByteBuffer mask) {
        GL11.glGetPolygonStipple(mask);
    }

    @Override
    public String glGetString(int name) {
        return GL11.glGetString(name);
    }

    @Override
    public void glGetTexEnv(int coord, int pname, FloatBuffer params) {
        GL11.glGetTexEnv(coord, pname, params);
    }

    @Override
    public void glGetTexEnv(int coord, int pname, IntBuffer params) {
        GL11.glGetTexEnv(coord, pname, params);
    }

    @Override
    public float glGetTexEnvf(int coord, int pname) {
        return GL11.glGetTexEnvf(coord, pname);
    }

    @Override
    public int glGetTexEnvi(int coord, int pname) {
        return GL11.glGetTexEnvi(coord, pname);
    }

    @Override
    public void glGetTexGen(int coord, int pname, DoubleBuffer params) {
        GL11.glGetTexGen(coord, pname, params);
    }

    @Override
    public void glGetTexGen(int coord, int pname, FloatBuffer params) {
        GL11.glGetTexGen(coord, pname, params);
    }

    @Override
    public void glGetTexGen(int coord, int pname, IntBuffer params) {
        GL11.glGetTexGen(coord, pname, params);
    }

    @Override
    public double glGetTexGend(int coord, int pname) {
        return GL11.glGetTexGend(coord, pname);
    }

    @Override
    public float glGetTexGenf(int coord, int pname) {
        return GL11.glGetTexGenf(coord, pname);
    }

    @Override
    public int glGetTexGeni(int coord, int pname) {
        return GL11.glGetTexGeni(coord, pname);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, long pixels_buffer_offset) {
        GL11.glGetTexImage(target, level, format, type, pixels_buffer_offset);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, ByteBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, DoubleBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, FloatBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, IntBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    @Override
    public void glGetTexImage(int target, int level, int format, int type, ShortBuffer pixels) {
        GL11.glGetTexImage(target, level, format, type, pixels);
    }

    @Override
    public void glGetTexLevelParameter(int target, int level, int pname, FloatBuffer params) {
        GL11.glGetTexLevelParameter(target, level, pname, params);
    }

    @Override
    public void glGetTexLevelParameter(int target, int level, int pname, IntBuffer params) {
        GL11.glGetTexLevelParameter(target, level, pname, params);
    }

    @Override
    public float glGetTexLevelParameterf(int target, int level, int pname) {
        return GL11.glGetTexLevelParameterf(target, level, pname);
    }

    @Override
    public int glGetTexLevelParameteri(int target, int level, int pname) {
        return GL11.glGetTexLevelParameteri(target, level, pname);
    }

    @Override
    public void glGetTexParameter(int target, int pname, FloatBuffer params) {
        GL11.glGetTexParameter(target, pname, params);
    }

    @Override
    public void glGetTexParameter(int target, int pname, IntBuffer params) {
        GL11.glGetTexParameter(target, pname, params);
    }

    @Override
    public float glGetTexParameterf(int target, int pname) {
        return GL11.glGetTexParameterf(target, pname);
    }

    @Override
    public int glGetTexParameteri(int target, int pname) {
        return GL11.glGetTexParameteri(target, pname);
    }

    @Override
    public void glHint(int target, int mode) {
        GL11.glHint(target, mode);
    }

    @Override
    public void glInitNames() {
        GL11.glInitNames();
    }

    @Override
    public void glInterleavedArrays(int format, int stride, long pointer_buffer_offset) {
        GL11.glInterleavedArrays(format, stride, pointer_buffer_offset);
    }

    @Override
    public void glInterleavedArrays(int format, int stride, ByteBuffer pointer) {
        GL11.glInterleavedArrays(format, stride, pointer);
    }

    @Override
    public void glInterleavedArrays(int format, int stride, DoubleBuffer pointer) {
        GL11.glInterleavedArrays(format, stride, pointer);
    }

    @Override
    public void glInterleavedArrays(int format, int stride, FloatBuffer pointer) {
        GL11.glInterleavedArrays(format, stride, pointer);
    }

    @Override
    public void glInterleavedArrays(int format, int stride, IntBuffer pointer) {
        GL11.glInterleavedArrays(format, stride, pointer);
    }

    @Override
    public void glInterleavedArrays(int format, int stride, ShortBuffer pointer) {
        GL11.glInterleavedArrays(format, stride, pointer);
    }

    @Override
    public boolean glIsEnabled(int cap) {
        return GL11.glIsEnabled(cap);
    }

    @Override
    public boolean glIsList(int list) {
        return GL11.glIsList(list);
    }

    @Override
    public boolean glIsTexture(int texture) {
        return GL11.glIsTexture(texture);
    }

    @Override
    public void glLight(int light, int pname, FloatBuffer params) {
        GL11.glLight(light, pname, params);
    }

    @Override
    public void glLight(int light, int pname, IntBuffer params) {
        GL11.glLight(light, pname, params);
    }

    @Override
    public void glLightModel(int pname, FloatBuffer params) {
        GL11.glLightModel(pname, params);
    }

    @Override
    public void glLightModel(int pname, IntBuffer params) {
        GL11.glLightModel(pname, params);
    }

    @Override
    public void glLightModelf(int pname, float param) {
        GL11.glLightModelf(pname, param);
    }

    @Override
    public void glLightModeli(int pname, int param) {
        GL11.glLightModeli(pname, param);
    }

    @Override
    public void glLightf(int light, int pname, float param) {
        GL11.glLightf(light, pname, param);
    }

    @Override
    public void glLighti(int light, int pname, int param) {
        GL11.glLighti(light, pname, param);
    }

    @Override
    public void glLineStipple(int factor, short pattern) {
        GL11.glLineStipple(factor, pattern);
    }

    @Override
    public void glLineWidth(float width) {
        GL11.glLineWidth(width);
    }

    @Override
    public void glListBase(int base) {
        GL11.glListBase(base);
    }

    @Override
    public void glLoadIdentity() {
        GL11.glLoadIdentity();
    }

    @Override
    public void glLoadMatrix(DoubleBuffer m) {
        GL11.glLoadMatrix(m);
    }

    @Override
    public void glLoadMatrix(FloatBuffer m) {
        GL11.glLoadMatrix(m);
    }

    @Override
    public void glLoadName(int name) {
        GL11.glLoadName(name);
    }

    @Override
    public void glLogicOp(int opcode) {
        GL11.glLogicOp(opcode);
    }

    @Override
    public void glMap1d(int target, double u1, double u2, int stride, int order, DoubleBuffer points) {
        GL11.glMap1d(target, u1, u2, stride, order, points);
    }

    @Override
    public void glMap1f(int target, float u1, float u2, int stride, int order, FloatBuffer points) {
        GL11.glMap1f(target, u1, u2, stride, order, points);
    }

    @Override
    public void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points) {
        GL11.glMap2d(target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points);
    }

    @Override
    public void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points) {
        GL11.glMap2f(target, u1, u2, ustride, uorder, v1, v2, vstride, vorder, points);
    }

    @Override
    public void glMapGrid1d(int un, double u1, double u2) {
        GL11.glMapGrid1d(un, u1, u2);
    }

    @Override
    public void glMapGrid1f(int un, float u1, float u2) {
        GL11.glMapGrid1f(un, u1, u2);
    }

    @Override
    public void glMapGrid2d(int un, double u1, double u2, int vn, double v1, double v2) {
        GL11.glMapGrid2d(un, u1, u2, vn, v1, v2);
    }

    @Override
    public void glMapGrid2f(int un, float u1, float u2, int vn, float v1, float v2) {
        GL11.glMapGrid2f(un, u1, u2, vn, v1, v2);
    }

    @Override
    public void glMaterial(int face, int pname, FloatBuffer params) {
        GL11.glMaterial(face, pname, params);
    }

    @Override
    public void glMaterial(int face, int pname, IntBuffer params) {
        GL11.glMaterial(face, pname, params);
    }

    @Override
    public void glMaterialf(int face, int pname, float param) {
        GL11.glMaterialf(face, pname, param);
    }

    @Override
    public void glMateriali(int face, int pname, int param) {
        GL11.glMateriali(face, pname, param);
    }

    @Override
    public void glMatrixMode(int mode) {
        GL11.glMatrixMode(mode);
    }

    @Override
    public void glMultMatrix(DoubleBuffer m) {
        GL11.glMultMatrix(m);
    }

    @Override
    public void glMultMatrix(FloatBuffer m) {
        GL11.glMultMatrix(m);
    }

    @Override
    public void glNewList(int list, int mode) {
        GL11.glNewList(list, mode);
    }

    @Override
    public void glNormal3b(byte nx, byte ny, byte nz) {
        GL11.glNormal3b(nx, ny, nz);
    }

    @Override
    public void glNormal3d(double nx, double ny, double nz) {
        GL11.glNormal3d(nx, ny, nz);
    }

    @Override
    public void glNormal3f(float nx, float ny, float nz) {
        GL11.glNormal3f(nx, ny, nz);
    }

    @Override
    public void glNormal3i(int nx, int ny, int nz) {
        GL11.glNormal3i(nx, ny, nz);
    }

    @Override
    public void glNormalPointer(int type, int stride, long pointer_buffer_offset) {
        GL11.glNormalPointer(type, stride, pointer_buffer_offset);
    }

    @Override
    public void glNormalPointer(int type, int stride, ByteBuffer pointer) {
        GL11.glNormalPointer(type, stride, pointer);
    }

    @Override
    public void glNormalPointer(int stride, ByteBuffer pointer) {
        GL11.glNormalPointer(stride, pointer);
    }

    @Override
    public void glNormalPointer(int stride, DoubleBuffer pointer) {
        GL11.glNormalPointer(stride, BufferCasts.toByteBuffer(pointer));
    }

    @Override
    public void glNormalPointer(int stride, FloatBuffer pointer) {
        GL11.glNormalPointer(stride, pointer);
    }

    @Override
    public void glNormalPointer(int stride, IntBuffer pointer) {
        GL11.glNormalPointer(stride, pointer);
    }

    @Override
    public void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
        GL11.glOrtho(left, right, bottom, top, zNear, zFar);
    }

    @Override
    public void glPassThrough(float token) {
        GL11.glPassThrough(token);
    }

    @Override
    public void glPixelMap(int map, FloatBuffer values) {
        GL11.glPixelMap(map, values);
    }

    @Override
    public void glPixelMapfv(int map, int values_mapsize, long values_buffer_offset) {
        GL11.glPixelMapfv(map, values_mapsize, values_buffer_offset);
    }

    @Override
    public void glPixelMapu(int map, IntBuffer values) {
        GL11.glPixelMapu(map, values);
    }

    @Override
    public void glPixelMapu(int map, ShortBuffer values) {
        GL11.glPixelMapu(map, values);
    }

    @Override
    public void glPixelMapuiv(int map, int values_mapsize, long values_buffer_offset) {
        GL11.glPixelMapuiv(map, values_mapsize, values_buffer_offset);
    }

    @Override
    public void glPixelMapusv(int map, int values_mapsize, long values_buffer_offset) {
        GL11.glPixelMapusv(map, values_mapsize, values_buffer_offset);
    }

    @Override
    public void glPixelStoref(int pname, float param) {
        GL11.glPixelStoref(pname, param);
    }

    @Override
    public void glPixelStorei(int pname, int param) {
        GL11.glPixelStorei(pname, param);
    }

    @Override
    public void glPixelTransferf(int pname, float param) {
        GL11.glPixelTransferf(pname, param);
    }

    @Override
    public void glPixelTransferi(int pname, int param) {
        GL11.glPixelTransferi(pname, param);
    }

    @Override
    public void glPixelZoom(float xfactor, float yfactor) {
        GL11.glPixelZoom(xfactor, yfactor);
    }

    @Override
    public void glPointSize(float size) {
        GL11.glPointSize(size);
    }

    @Override
    public void glPolygonMode(int face, int mode) {
        GL11.glPolygonMode(face, mode);
    }

    @Override
    public void glPolygonOffset(float factor, float units) {
        GL11.glPolygonOffset(factor, units);
    }

    @Override
    public void glPolygonStipple(long mask_buffer_offset) {
        GL11.glPolygonStipple(mask_buffer_offset);
    }

    @Override
    public void glPolygonStipple(ByteBuffer mask) {
        GL11.glPolygonStipple(mask);
    }

    @Override
    public void glPopAttrib() {
        GL11.glPopAttrib();
    }

    @Override
    public void glPopClientAttrib() {
        GL11.glPopClientAttrib();
    }

    @Override
    public void glPopMatrix() {
        GL11.glPopMatrix();
    }

    @Override
    public void glPopName() {
        GL11.glPopName();
    }

    @Override
    public void glPrioritizeTextures(IntBuffer textures, FloatBuffer priorities) {
        GL11.glPrioritizeTextures(textures, priorities);
    }

    @Override
    public void glPushAttrib(int mask) {
        GL11.glPushAttrib(mask);
    }

    @Override
    public void glPushClientAttrib(int mask) {
        GL11.glPushClientAttrib(mask);
    }

    @Override
    public void glPushMatrix() {
        GL11.glPushMatrix();
    }

    @Override
    public void glPushName(int name) {
        GL11.glPushName(name);
    }

    @Override
    public void glRasterPos2d(double x, double y) {
        GL11.glRasterPos2d(x, y);
    }

    @Override
    public void glRasterPos2f(float x, float y) {
        GL11.glRasterPos2f(x, y);
    }

    @Override
    public void glRasterPos2i(int x, int y) {
        GL11.glRasterPos2i(x, y);
    }

    @Override
    public void glRasterPos3d(double x, double y, double z) {
        GL11.glRasterPos3d(x, y, z);
    }

    @Override
    public void glRasterPos3f(float x, float y, float z) {
        GL11.glRasterPos3f(x, y, z);
    }

    @Override
    public void glRasterPos3i(int x, int y, int z) {
        GL11.glRasterPos3i(x, y, z);
    }

    @Override
    public void glRasterPos4d(double x, double y, double z, double w) {
        GL11.glRasterPos4d(x, y, z, w);
    }

    @Override
    public void glRasterPos4f(float x, float y, float z, float w) {
        GL11.glRasterPos4f(x, y, z, w);
    }

    @Override
    public void glRasterPos4i(int x, int y, int z, int w) {
        GL11.glRasterPos4i(x, y, z, w);
    }

    @Override
    public void glReadBuffer(int mode) {
        GL11.glReadBuffer(mode);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels_buffer_offset) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels_buffer_offset);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, DoubleBuffer pixels) {
        GL11.glReadPixels(x, y, width, height, format, type, MemoryUtil.getAddress(pixels));
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glReadPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        GL11.glReadPixels(x, y, width, height, format, type, pixels);
    }

    @Override
    public void glRectd(double x1, double y1, double x2, double y2) {
        GL11.glRectd(x1, y1, x2, y2);
    }

    @Override
    public void glRectf(float x1, float y1, float x2, float y2) {
        GL11.glRectf(x1, y1, x2, y2);
    }

    @Override
    public void glRecti(int x1, int y1, int x2, int y2) {
        GL11.glRecti(x1, y1, x2, y2);
    }

    @Override
    public int glRenderMode(int mode) {
        return GL11.glRenderMode(mode);
    }

    @Override
    public void glRotated(double angle, double x, double y, double z) {
        GL11.glRotated(angle, x, y, z);
    }

    @Override
    public void glRotatef(float angle, float x, float y, float z) {
        GL11.glRotatef(angle, x, y, z);
    }

    @Override
    public void glScaled(double x, double y, double z) {
        GL11.glScaled(x, y, z);
    }

    @Override
    public void glScalef(float x, float y, float z) {
        GL11.glScalef(x, y, z);
    }

    @Override
    public void glScissor(int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
    }

    @Override
    public void glSelectBuffer(IntBuffer buffer) {
        GL11.glSelectBuffer(buffer);
    }

    @Override
    public void glShadeModel(int mode) {
        GL11.glShadeModel(mode);
    }

    @Override
    public void glStencilFunc(int func, int ref, int mask) {
        GL11.glStencilFunc(func, ref, mask);
    }

    @Override
    public void glStencilMask(int mask) {
        GL11.glStencilMask(mask);
    }

    @Override
    public void glStencilOp(int fail, int zfail, int zpass) {
        GL11.glStencilOp(fail, zfail, zpass);
    }

    @Override
    public void glTexCoord1d(double s) {
        GL11.glTexCoord1d(s);
    }

    @Override
    public void glTexCoord1f(float s) {
        GL11.glTexCoord1f(s);
    }

    @Override
    public void glTexCoord2d(double s, double t) {
        GL11.glTexCoord2d(s, t);
    }

    @Override
    public void glTexCoord2f(float s, float t) {
        GL11.glTexCoord2f(s, t);
    }

    @Override
    public void glTexCoord3d(double s, double t, double r) {
        GL11.glTexCoord3d(s, t, r);
    }

    @Override
    public void glTexCoord3f(float s, float t, float r) {
        GL11.glTexCoord3f(s, t, r);
    }

    @Override
    public void glTexCoord4d(double s, double t, double r, double q) {
        GL11.glTexCoord4d(s, t, r, q);
    }

    @Override
    public void glTexCoord4f(float s, float t, float r, float q) {
        GL11.glTexCoord4f(s, t, r, q);
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, long pointer_buffer_offset) {
        GL11.glTexCoordPointer(size, type, stride, pointer_buffer_offset);
    }

    @Override
    public void glTexCoordPointer(int size, int type, int stride, ByteBuffer pointer) {
        GL11.glTexCoordPointer(size, type, stride, pointer);
    }

    @Override
    public void glTexCoordPointer(int size, int stride, DoubleBuffer pointer) {
        GL11.glTexCoordPointer(size, stride, pointer);
    }

    @Override
    public void glTexCoordPointer(int size, int stride, FloatBuffer pointer) {
        GL11.glTexCoordPointer(size, stride, pointer);
    }

    @Override
    public void glTexCoordPointer(int size, int stride, IntBuffer pointer) {
        GL11.glTexCoordPointer(size, stride, pointer);
    }

    @Override
    public void glTexCoordPointer(int size, int stride, ShortBuffer pointer) {
        GL11.glTexCoordPointer(size, stride, pointer);
    }

    @Override
    public void glTexEnv(int target, int pname, FloatBuffer params) {
        GL11.glTexEnv(target, pname, params);
    }

    @Override
    public void glTexEnv(int target, int pname, IntBuffer params) {
        GL11.glTexEnv(target, pname, params);
    }

    @Override
    public void glTexEnvf(int target, int pname, float param) {
        GL11.glTexEnvf(target, pname, param);
    }

    @Override
    public void glTexEnvi(int target, int pname, int param) {
        GL11.glTexEnvi(target, pname, param);
    }

    @Override
    public void glTexGen(int coord, int pname, DoubleBuffer params) {
        GL11.glTexGen(coord, pname, params);
    }

    @Override
    public void glTexGen(int coord, int pname, FloatBuffer params) {
        GL11.glTexGen(coord, pname, params);
    }

    @Override
    public void glTexGen(int coord, int pname, IntBuffer params) {
        GL11.glTexGen(coord, pname, params);
    }

    @Override
    public void glTexGend(int coord, int pname, double param) {
        GL11.glTexGend(coord, pname, param);
    }

    @Override
    public void glTexGenf(int coord, int pname, float param) {
        GL11.glTexGenf(coord, pname, param);
    }

    @Override
    public void glTexGeni(int coord, int pname, int param) {
        GL11.glTexGeni(coord, pname, param);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, ByteBuffer pixels) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, DoubleBuffer pixels) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, FloatBuffer pixels) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, IntBuffer pixels) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    @Override
    public void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, ShortBuffer pixels) {
        GL11.glTexImage1D(target, level, internalformat, width, border, format, type, pixels);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, DoubleBuffer pixels) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, IntBuffer pixels) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ShortBuffer pixels) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    }

    @Override
    public void glTexParameter(int target, int pname, FloatBuffer param) {
        GL11.glTexParameter(target, pname, param);
    }

    @Override
    public void glTexParameter(int target, int pname, IntBuffer param) {
        GL11.glTexParameter(target, pname, param);
    }

    @Override
    public void glTexParameterf(int target, int pname, float param) {
        GL11.glTexParameterf(target, pname, param);
    }

    @Override
    public void glTexParameteri(int target, int pname, int param) {
        GL11.glTexParameteri(target, pname, param);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, ByteBuffer pixels) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, DoubleBuffer pixels) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, FloatBuffer pixels) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, IntBuffer pixels) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, ShortBuffer pixels) {
        GL11.glTexSubImage1D(target, level, xoffset, width, format, type, pixels);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels_buffer_offset);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, ByteBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, DoubleBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, FloatBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, IntBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, ShortBuffer pixels) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    @Override
    public void glTranslated(double x, double y, double z) {
        GL11.glTranslated(x, y, z);
    }

    @Override
    public void glTranslatef(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    @Override
    public void glVertex2d(double x, double y) {
        GL11.glVertex2d(x, y);
    }

    @Override
    public void glVertex2f(float x, float y) {
        GL11.glVertex2f(x, y);
    }

    @Override
    public void glVertex2i(int x, int y) {
        GL11.glVertex2i(x, y);
    }

    @Override
    public void glVertex3d(double x, double y, double z) {
        GL11.glVertex3d(x, y, z);
    }

    @Override
    public void glVertex3f(float x, float y, float z) {
        GL11.glVertex3f(x, y, z);
    }

    @Override
    public void glVertex3i(int x, int y, int z) {
        GL11.glVertex3i(x, y, z);
    }

    @Override
    public void glVertex4d(double x, double y, double z, double w) {
        GL11.glVertex4d(x, y, z, w);
    }

    @Override
    public void glVertex4f(float x, float y, float z, float w) {
        GL11.glVertex4f(x, y, z, w);
    }

    @Override
    public void glVertex4i(int x, int y, int z, int w) {
        GL11.glVertex4i(x, y, z, w);
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, long pointer_buffer_offset) {
        GL11.glVertexPointer(size, type, stride, pointer_buffer_offset);
    }

    @Override
    public void glVertexPointer(int size, int type, int stride, ByteBuffer pointer) {
        GL11.glVertexPointer(size, type, stride, pointer);
    }

    @Override
    public void glVertexPointer(int size, int stride, DoubleBuffer pointer) {
        GL11.glVertexPointer(size, stride, pointer);
    }

    @Override
    public void glVertexPointer(int size, int stride, FloatBuffer pointer) {
        GL11.glVertexPointer(size, stride, pointer);
    }

    @Override
    public void glVertexPointer(int size, int stride, IntBuffer pointer) {
        GL11.glVertexPointer(size, stride, pointer);
    }

    @Override
    public void glVertexPointer(int size, int stride, ShortBuffer pointer) {
        GL11.glVertexPointer(size, stride, pointer);
    }

    @Override
    public void glViewport(int x, int y, int width, int height) {
        GL11.glViewport(x, y, width, height);
    }
}
