package com.cleanroommc.kirino.gl.dispatch;

import org.apache.commons.lang3.NotImplementedException;
import org.lwjgl.opengl.KHRDebugCallback;
import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

import java.nio.*;

public sealed interface OpenGL permits OGL11 {
    // <editor-fold desc="OpenGL 11">
    default void glAccum(int op, float value) {
        throw new NotImplementedException("glAccum is only avaliable for OpenGL 11 and above");
    }

    default void glAlphaFunc(int func, float ref) {
        throw new NotImplementedException("glAlphaFunc is only avaliable for OpenGL 11 and above");
    }

    default boolean glAreTexturesResident(IntBuffer textures, ByteBuffer residences) {
        throw new NotImplementedException("glAreTexturesResident is only avaliable for OpenGL 11 and above");
    }

    default void glArrayElement(int i) {
        throw new NotImplementedException("glArrayElement is only avaliable for OpenGL 11 and above");
    }

    default void glBegin(int mode) {
        throw new NotImplementedException("glBegin is only avaliable for OpenGL 11 and above");
    }

    default void glBindTexture(int target, int texture) {
        throw new NotImplementedException("glBindTexture is only avaliable for OpenGL 11 and above");
    }

    default void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, long bitmap_buffer_offset) {
        throw new NotImplementedException("glBitmap is only avaliable for OpenGL 11 and above");
    }

    default void glBitmap(int width, int height, float xorig, float yorig, float xmove, float ymove, ByteBuffer bitmap) {
        throw new NotImplementedException("glBitmap is only avaliable for OpenGL 11 and above");
    }

    default void glBlendFunc(int sfactor, int dfactor) {
        throw new NotImplementedException("glBlendFunc is only avaliable for OpenGL 11 and above");
    }

    default void glCallList(int list) {
        throw new NotImplementedException("glCallList is only avaliable for OpenGL 11 and above");
    }

    default void glCallLists(ByteBuffer lists) {
        throw new NotImplementedException("glCallLists is only avaliable for OpenGL 11 and above");
    }

    default void glCallLists(IntBuffer lists) {
        throw new NotImplementedException("glCallLists is only avaliable for OpenGL 11 and above");
    }

    default void glCallLists(ShortBuffer lists) {
        throw new NotImplementedException("glCallLists is only avaliable for OpenGL 11 and above");
    }

    default void glClear(int mask) {
        throw new NotImplementedException("glClear is only avaliable for OpenGL 11 and above");
    }

    default void glClearAccum(float red, float green, float blue, float alpha) {
        throw new NotImplementedException("glClearAccum is only avaliable for OpenGL 11 and above");
    }

    default void glClearColor(float red, float green, float blue, float alpha) {
        throw new NotImplementedException("glClearColor is only avaliable for OpenGL 11 and above");
    }

    default void glClearDepth(double depth) {
        throw new NotImplementedException("glClearDepth is only avaliable for OpenGL 11 and above");
    }

    default void glClearStencil(int s) {
        throw new NotImplementedException("glClearStencil is only avaliable for OpenGL 11 and above");
    }

    default void glClipPlane(int plane, DoubleBuffer equation) {
        throw new NotImplementedException("glClipPlane is only avaliable for OpenGL 11 and above");
    }

    default void glColor3b(byte red, byte green, byte blue) {
        throw new NotImplementedException("glColor3b is only avaliable for OpenGL 11 and above");
    }

    default void glColor3d(double red, double green, double blue) {
        throw new NotImplementedException("glColor3d is only avaliable for OpenGL 11 and above");
    }

    default void glColor3f(float red, float green, float blue) {
        throw new NotImplementedException("glColor3f is only avaliable for OpenGL 11 and above");
    }

    default void glColor3ub(byte red, byte green, byte blue) {
        throw new NotImplementedException("glColor3ub is only avaliable for OpenGL 11 and above");
    }

    default void glColor4b(byte red, byte green, byte blue, byte alpha) {
        throw new NotImplementedException("glColor4b is only avaliable for OpenGL 11 and above");
    }

    default void glColor4d(double red, double green, double blue, double alpha) {
        throw new NotImplementedException("glColor4d is only avaliable for OpenGL 11 and above");
    }

    default void glColor4f(float red, float green, float blue, float alpha) {
        throw new NotImplementedException("glColor4f is only avaliable for OpenGL 11 and above");
    }

    default void glColor4ub(byte red, byte green, byte blue, byte alpha) {
        throw new NotImplementedException("glColor4ub is only avaliable for OpenGL 11 and above");
    }

    default void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        throw new NotImplementedException("glColorMask is only avaliable for OpenGL 11 and above");
    }

    default void glColorMaterial(int face, int mode) {
        throw new NotImplementedException("glColorMaterial is only avaliable for OpenGL 11 and above");
    }

    default void glColorPointer(int size, int type, int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glColorPointer is only avaliable for OpenGL 11 and above");
    }

    default void glColorPointer(int size, int type, int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glColorPointer is only avaliable for OpenGL 11 and above");
    }

    default void glColorPointer(int size, int stride, DoubleBuffer pointer) {
        throw new NotImplementedException("glColorPointer is only avaliable for OpenGL 11 and above");
    }

    default void glColorPointer(int size, int stride, FloatBuffer pointer) {
        throw new NotImplementedException("glColorPointer is only avaliable for OpenGL 11 and above");
    }

    default void glColorPointer(int size, boolean unsigned, int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glColorPointer is only avaliable for OpenGL 11 and above");
    }

    default void glCopyPixels(int x, int y, int width, int height, int type) {
        throw new NotImplementedException("glCopyPixels is only avaliable for OpenGL 11 and above");
    }

    default void glCopyTexImage1D(int target, int level, int internalFormat, int x, int y, int width, int border) {
        throw new NotImplementedException("glCopyTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glCopyTexImage2D(int target, int level, int internalFormat, int x, int y, int width, int height, int border) {
        throw new NotImplementedException("glCopyTexImage2D is only avaliable for OpenGL 11 and above");
    }

    default void glCopyTexSubImage1D(int target, int level, int xoffset, int x, int y, int width) {
        throw new NotImplementedException("glCopyTexSubImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        throw new NotImplementedException("glCopyTexSubImage2D is only avaliable for OpenGL 11 and above");
    }

    default void glCullFace(int mode) {
        throw new NotImplementedException("glCullFace is only avaliable for OpenGL 11 and above");
    }

    default void glDeleteLists(int list, int range) {
        throw new NotImplementedException("glDeleteLists is only avaliable for OpenGL 11 and above");
    }

    default void glDeleteTextures(int texture) {
        throw new NotImplementedException("glDeleteTextures is only avaliable for OpenGL 11 and above");
    }

    default void glDeleteTextures(IntBuffer textures) {
        throw new NotImplementedException("glDeleteTextures is only avaliable for OpenGL 11 and above");
    }

    default void glDepthFunc(int func) {
        throw new NotImplementedException("glDepthFunc is only avaliable for OpenGL 11 and above");
    }

    default void glDepthMask(boolean flag) {
        throw new NotImplementedException("glDepthMask is only avaliable for OpenGL 11 and above");
    }

    default void glDepthRange(double zNear, double zFar) {
        throw new NotImplementedException("glDepthRange is only avaliable for OpenGL 11 and above");
    }

    default void glDisable(int cap) {
        throw new NotImplementedException("glDisable is only avaliable for OpenGL 11 and above");
    }

    default void glDisableClientState(int cap) {
        throw new NotImplementedException("glDisableClientState is only avaliable for OpenGL 11 and above");
    }

    default void glDrawArrays(int mode, int first, int count) {
        throw new NotImplementedException("glDrawArrays is only avaliable for OpenGL 11 and above");
    }

    default void glDrawBuffer(int mode) {
        throw new NotImplementedException("glDrawBuffer is only avaliable for OpenGL 11 and above");
    }

    default void glDrawElements(int mode, int indices_count, int type, long indices_buffer_offset) {
        throw new NotImplementedException("glDrawElements is only avaliable for OpenGL 11 and above");
    }

    default void glDrawElements(int mode, int count, int type, ByteBuffer indices) {
        throw new NotImplementedException("glDrawElements is only avaliable for OpenGL 11 and above");
    }

    default void glDrawElements(int mode, ByteBuffer indices) {
        throw new NotImplementedException("glDrawElements is only avaliable for OpenGL 11 and above");
    }

    default void glDrawElements(int mode, IntBuffer indices) {
        throw new NotImplementedException("glDrawElements is only avaliable for OpenGL 11 and above");
    }

    default void glDrawElements(int mode, ShortBuffer indices) {
        throw new NotImplementedException("glDrawElements is only avaliable for OpenGL 11 and above");
    }

    default void glDrawPixels(int width, int height, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glDrawPixels is only avaliable for OpenGL 11 and above");
    }

    default void glDrawPixels(int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glDrawPixels is only avaliable for OpenGL 11 and above");
    }

    default void glDrawPixels(int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glDrawPixels is only avaliable for OpenGL 11 and above");
    }

    default void glDrawPixels(int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glDrawPixels is only avaliable for OpenGL 11 and above");
    }

    default void glEdgeFlag(boolean flag) {
        throw new NotImplementedException("glEdgeFlag is only avaliable for OpenGL 11 and above");
    }

    default void glEdgeFlagPointer(int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glEdgeFlagPointer is only avaliable for OpenGL 11 and above");
    }

    default void glEdgeFlagPointer(int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glEdgeFlagPointer is only avaliable for OpenGL 11 and above");
    }

    default void glEnable(int cap) {
        throw new NotImplementedException("glEnable is only avaliable for OpenGL 11 and above");
    }

    default void glEnableClientState(int cap) {
        throw new NotImplementedException("glEnableClientState is only avaliable for OpenGL 11 and above");
    }

    default void glEnd() {
        throw new NotImplementedException("glEnd is only avaliable for OpenGL 11 and above");
    }

    default void glEndList() {
        throw new NotImplementedException("glEndList is only avaliable for OpenGL 11 and above");
    }

    default void glEvalCoord1d(double u) {
        throw new NotImplementedException("glEvalCoord1d is only avaliable for OpenGL 11 and above");
    }

    default void glEvalCoord1f(float u) {
        throw new NotImplementedException("glEvalCoord1f is only avaliable for OpenGL 11 and above");
    }

    default void glEvalCoord2d(double u, double v) {
        throw new NotImplementedException("glEvalCoord2d is only avaliable for OpenGL 11 and above");
    }

    default void glEvalCoord2f(float u, float v) {
        throw new NotImplementedException("glEvalCoord2f is only avaliable for OpenGL 11 and above");
    }

    default void glEvalMesh1(int mode, int i1, int i2) {
        throw new NotImplementedException("glEvalMesh1 is only avaliable for OpenGL 11 and above");
    }

    default void glEvalMesh2(int mode, int i1, int i2, int j1, int j2) {
        throw new NotImplementedException("glEvalMesh2 is only avaliable for OpenGL 11 and above");
    }

    default void glEvalPoint1(int i) {
        throw new NotImplementedException("glEvalPoint1 is only avaliable for OpenGL 11 and above");
    }

    default void glEvalPoint2(int i, int j) {
        throw new NotImplementedException("glEvalPoint2 is only avaliable for OpenGL 11 and above");
    }

    default void glFeedbackBuffer(int type, FloatBuffer buffer) {
        throw new NotImplementedException("glFeedbackBuffer is only avaliable for OpenGL 11 and above");
    }

    default void glFinish() {
        throw new NotImplementedException("glFinish is only avaliable for OpenGL 11 and above");
    }

    default void glFlush() {
        throw new NotImplementedException("glFlush is only avaliable for OpenGL 11 and above");
    }

    default void glFog(int pname, FloatBuffer params) {
        throw new NotImplementedException("glFog is only avaliable for OpenGL 11 and above");
    }

    default void glFog(int pname, IntBuffer params) {
        throw new NotImplementedException("glFog is only avaliable for OpenGL 11 and above");
    }

    default void glFogf(int pname, float param) {
        throw new NotImplementedException("glFogf is only avaliable for OpenGL 11 and above");
    }

    default void glFogi(int pname, int param) {
        throw new NotImplementedException("glFogi is only avaliable for OpenGL 11 and above");
    }

    default void glFrontFace(int mode) {
        throw new NotImplementedException("glFrontFace is only avaliable for OpenGL 11 and above");
    }

    default void glFrustum(double left, double right, double bottom, double top, double zNear, double zFar) {
        throw new NotImplementedException("glFrustum is only avaliable for OpenGL 11 and above");
    }

    default int glGenLists(int range) {
        throw new NotImplementedException("glGenLists is only avaliable for OpenGL 11 and above");
    }

    default int glGenTextures() {
        throw new NotImplementedException("glGenTextures is only avaliable for OpenGL 11 and above");
    }

    default void glGenTextures(IntBuffer textures) {
        throw new NotImplementedException("glGenTextures is only avaliable for OpenGL 11 and above");
    }

    default boolean glGetBoolean(int pname) {
        throw new NotImplementedException("glGetBoolean is only avaliable for OpenGL 11 and above");
    }

    default void glGetBoolean(int pname, ByteBuffer params) {
        throw new NotImplementedException("glGetBoolean is only avaliable for OpenGL 11 and above");
    }

    default void glGetClipPlane(int plane, DoubleBuffer equation) {
        throw new NotImplementedException("glGetClipPlane is only avaliable for OpenGL 11 and above");
    }

    default double glGetDouble(int pname) {
        throw new NotImplementedException("glGetDouble is only avaliable for OpenGL 11 and above");
    }

    default void glGetDouble(int pname, DoubleBuffer params) {
        throw new NotImplementedException("glGetDouble is only avaliable for OpenGL 11 and above");
    }

    default int glGetError() {
        throw new NotImplementedException("glGetError is only avaliable for OpenGL 11 and above");
    }

    default float glGetFloat(int pname) {
        throw new NotImplementedException("glGetFloat is only avaliable for OpenGL 11 and above");
    }

    default void glGetFloat(int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetFloat is only avaliable for OpenGL 11 and above");
    }

    default int glGetInteger(int pname) {
        throw new NotImplementedException("glGetInteger is only avaliable for OpenGL 11 and above");
    }

    default void glGetInteger(int pname, IntBuffer params) {
        throw new NotImplementedException("glGetInteger is only avaliable for OpenGL 11 and above");
    }

    default void glGetLight(int light, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetLight is only avaliable for OpenGL 11 and above");
    }

    default void glGetLight(int light, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetLight is only avaliable for OpenGL 11 and above");
    }

    default void glGetMap(int target, int query, DoubleBuffer v) {
        throw new NotImplementedException("glGetMap is only avaliable for OpenGL 11 and above");
    }

    default void glGetMap(int target, int query, FloatBuffer v) {
        throw new NotImplementedException("glGetMap is only avaliable for OpenGL 11 and above");
    }

    default void glGetMap(int target, int query, IntBuffer v) {
        throw new NotImplementedException("glGetMap is only avaliable for OpenGL 11 and above");
    }

    default void glGetMaterial(int face, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetMaterial is only avaliable for OpenGL 11 and above");
    }

    default void glGetMaterial(int face, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetMaterial is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMap(int map, FloatBuffer values) {
        throw new NotImplementedException("glGetPixelMap is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMapfv(int map, long values_buffer_offset) {
        throw new NotImplementedException("glGetPixelMapfv is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMapu(int map, IntBuffer values) {
        throw new NotImplementedException("glGetPixelMapu is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMapu(int map, ShortBuffer values) {
        throw new NotImplementedException("glGetPixelMapu is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMapuiv(int map, long values_buffer_offset) {
        throw new NotImplementedException("glGetPixelMapuiv is only avaliable for OpenGL 11 and above");
    }

    default void glGetPixelMapusv(int map, long values_buffer_offset) {
        throw new NotImplementedException("glGetPixelMapusv is only avaliable for OpenGL 11 and above");
    }

    default void glGetPolygonStipple(long mask_buffer_offset) {
        throw new NotImplementedException("glGetPolygonStipple is only avaliable for OpenGL 11 and above");
    }

    default void glGetPolygonStipple(ByteBuffer mask) {
        throw new NotImplementedException("glGetPolygonStipple is only avaliable for OpenGL 11 and above");
    }

    default String glGetString(int name) {
        throw new NotImplementedException("glGetString is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexEnv(int coord, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTexEnv is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexEnv(int coord, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTexEnv is only avaliable for OpenGL 11 and above");
    }

    default float glGetTexEnvf(int coord, int pname) {
        throw new NotImplementedException("glGetTexEnvf is only avaliable for OpenGL 11 and above");
    }

    default int glGetTexEnvi(int coord, int pname) {
        throw new NotImplementedException("glGetTexEnvi is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexGen(int coord, int pname, DoubleBuffer params) {
        throw new NotImplementedException("glGetTexGen is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexGen(int coord, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTexGen is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexGen(int coord, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTexGen is only avaliable for OpenGL 11 and above");
    }

    default double glGetTexGend(int coord, int pname) {
        throw new NotImplementedException("glGetTexGend is only avaliable for OpenGL 11 and above");
    }

    default float glGetTexGenf(int coord, int pname) {
        throw new NotImplementedException("glGetTexGenf is only avaliable for OpenGL 11 and above");
    }

    default int glGetTexGeni(int coord, int pname) {
        throw new NotImplementedException("glGetTexGeni is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexImage(int target, int level, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glGetTexImage is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexLevelParameter(int target, int level, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTexLevelParameter is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexLevelParameter(int target, int level, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTexLevelParameter is only avaliable for OpenGL 11 and above");
    }

    default float glGetTexLevelParameterf(int target, int level, int pname) {
        throw new NotImplementedException("glGetTexLevelParameterf is only avaliable for OpenGL 11 and above");
    }

    default int glGetTexLevelParameteri(int target, int level, int pname) {
        throw new NotImplementedException("glGetTexLevelParameteri is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexParameter(int target, int pname, FloatBuffer params) {
        throw new NotImplementedException("glGetTexParameter is only avaliable for OpenGL 11 and above");
    }

    default void glGetTexParameter(int target, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetTexParameter is only avaliable for OpenGL 11 and above");
    }

    default float glGetTexParameterf(int target, int pname) {
        throw new NotImplementedException("glGetTexParameterf is only avaliable for OpenGL 11 and above");
    }

    default int glGetTexParameteri(int target, int pname) {
        throw new NotImplementedException("glGetTexParameteri is only avaliable for OpenGL 11 and above");
    }

    default void glHint(int target, int mode) {
        throw new NotImplementedException("glHint is only avaliable for OpenGL 11 and above");
    }

    default void glInitNames() {
        throw new NotImplementedException("glInitNames is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, DoubleBuffer pointer) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, FloatBuffer pointer) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, IntBuffer pointer) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default void glInterleavedArrays(int format, int stride, ShortBuffer pointer) {
        throw new NotImplementedException("glInterleavedArrays is only avaliable for OpenGL 11 and above");
    }

    default boolean glIsEnabled(int cap) {
        throw new NotImplementedException("glIsEnabled is only avaliable for OpenGL 11 and above");
    }

    default boolean glIsList(int list) {
        throw new NotImplementedException("glIsList is only avaliable for OpenGL 11 and above");
    }

    default boolean glIsTexture(int texture) {
        throw new NotImplementedException("glIsTexture is only avaliable for OpenGL 11 and above");
    }

    default void glLight(int light, int pname, FloatBuffer params) {
        throw new NotImplementedException("glLight is only avaliable for OpenGL 11 and above");
    }

    default void glLight(int light, int pname, IntBuffer params) {
        throw new NotImplementedException("glLight is only avaliable for OpenGL 11 and above");
    }

    default void glLightModel(int pname, FloatBuffer params) {
        throw new NotImplementedException("glLightModel is only avaliable for OpenGL 11 and above");
    }

    default void glLightModel(int pname, IntBuffer params) {
        throw new NotImplementedException("glLightModel is only avaliable for OpenGL 11 and above");
    }

    default void glLightModelf(int pname, float param) {
        throw new NotImplementedException("glLightModelf is only avaliable for OpenGL 11 and above");
    }

    default void glLightModeli(int pname, int param) {
        throw new NotImplementedException("glLightModeli is only avaliable for OpenGL 11 and above");
    }

    default void glLightf(int light, int pname, float param) {
        throw new NotImplementedException("glLightf is only avaliable for OpenGL 11 and above");
    }

    default void glLighti(int light, int pname, int param) {
        throw new NotImplementedException("glLighti is only avaliable for OpenGL 11 and above");
    }

    default void glLineStipple(int factor, short pattern) {
        throw new NotImplementedException("glLineStipple is only avaliable for OpenGL 11 and above");
    }

    default void glLineWidth(float width) {
        throw new NotImplementedException("glLineWidth is only avaliable for OpenGL 11 and above");
    }

    default void glListBase(int base) {
        throw new NotImplementedException("glListBase is only avaliable for OpenGL 11 and above");
    }

    default void glLoadIdentity() {
        throw new NotImplementedException("glLoadIdentity is only avaliable for OpenGL 11 and above");
    }

    default void glLoadMatrix(DoubleBuffer m) {
        throw new NotImplementedException("glLoadMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glLoadMatrix(FloatBuffer m) {
        throw new NotImplementedException("glLoadMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glLoadName(int name) {
        throw new NotImplementedException("glLoadName is only avaliable for OpenGL 11 and above");
    }

    default void glLogicOp(int opcode) {
        throw new NotImplementedException("glLogicOp is only avaliable for OpenGL 11 and above");
    }

    default void glMap1d(int target, double u1, double u2, int stride, int order, DoubleBuffer points) {
        throw new NotImplementedException("glMap1d is only avaliable for OpenGL 11 and above");
    }

    default void glMap1f(int target, float u1, float u2, int stride, int order, FloatBuffer points) {
        throw new NotImplementedException("glMap1f is only avaliable for OpenGL 11 and above");
    }

    default void glMap2d(int target, double u1, double u2, int ustride, int uorder, double v1, double v2, int vstride, int vorder, DoubleBuffer points) {
        throw new NotImplementedException("glMap2d is only avaliable for OpenGL 11 and above");
    }

    default void glMap2f(int target, float u1, float u2, int ustride, int uorder, float v1, float v2, int vstride, int vorder, FloatBuffer points) {
        throw new NotImplementedException("glMap2f is only avaliable for OpenGL 11 and above");
    }

    default void glMapGrid1d(int un, double u1, double u2) {
        throw new NotImplementedException("glMapGrid1d is only avaliable for OpenGL 11 and above");
    }

    default void glMapGrid1f(int un, float u1, float u2) {
        throw new NotImplementedException("glMapGrid1f is only avaliable for OpenGL 11 and above");
    }

    default void glMapGrid2d(int un, double u1, double u2, int vn, double v1, double v2) {
        throw new NotImplementedException("glMapGrid2d is only avaliable for OpenGL 11 and above");
    }

    default void glMapGrid2f(int un, float u1, float u2, int vn, float v1, float v2) {
        throw new NotImplementedException("glMapGrid2f is only avaliable for OpenGL 11 and above");
    }

    default void glMaterial(int face, int pname, FloatBuffer params) {
        throw new NotImplementedException("glMaterial is only avaliable for OpenGL 11 and above");
    }

    default void glMaterial(int face, int pname, IntBuffer params) {
        throw new NotImplementedException("glMaterial is only avaliable for OpenGL 11 and above");
    }

    default void glMaterialf(int face, int pname, float param) {
        throw new NotImplementedException("glMaterialf is only avaliable for OpenGL 11 and above");
    }

    default void glMateriali(int face, int pname, int param) {
        throw new NotImplementedException("glMateriali is only avaliable for OpenGL 11 and above");
    }

    default void glMatrixMode(int mode) {
        throw new NotImplementedException("glMatrixMode is only avaliable for OpenGL 11 and above");
    }

    default void glMultMatrix(DoubleBuffer m) {
        throw new NotImplementedException("glMultMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glMultMatrix(FloatBuffer m) {
        throw new NotImplementedException("glMultMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glNewList(int list, int mode) {
        throw new NotImplementedException("glNewList is only avaliable for OpenGL 11 and above");
    }

    default void glNormal3b(byte nx, byte ny, byte nz) {
        throw new NotImplementedException("glNormal3b is only avaliable for OpenGL 11 and above");
    }

    default void glNormal3d(double nx, double ny, double nz) {
        throw new NotImplementedException("glNormal3d is only avaliable for OpenGL 11 and above");
    }

    default void glNormal3f(float nx, float ny, float nz) {
        throw new NotImplementedException("glNormal3f is only avaliable for OpenGL 11 and above");
    }

    default void glNormal3i(int nx, int ny, int nz) {
        throw new NotImplementedException("glNormal3i is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int type, int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int type, int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int stride, DoubleBuffer pointer) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int stride, FloatBuffer pointer) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glNormalPointer(int stride, IntBuffer pointer) {
        throw new NotImplementedException("glNormalPointer is only avaliable for OpenGL 11 and above");
    }

    default void glOrtho(double left, double right, double bottom, double top, double zNear, double zFar) {
        throw new NotImplementedException("glOrtho is only avaliable for OpenGL 11 and above");
    }

    default void glPassThrough(float token) {
        throw new NotImplementedException("glPassThrough is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMap(int map, FloatBuffer values) {
        throw new NotImplementedException("glPixelMap is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMapfv(int map, int values_mapsize, long values_buffer_offset) {
        throw new NotImplementedException("glPixelMapfv is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMapu(int map, IntBuffer values) {
        throw new NotImplementedException("glPixelMapu is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMapu(int map, ShortBuffer values) {
        throw new NotImplementedException("glPixelMapu is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMapuiv(int map, int values_mapsize, long values_buffer_offset) {
        throw new NotImplementedException("glPixelMapuiv is only avaliable for OpenGL 11 and above");
    }

    default void glPixelMapusv(int map, int values_mapsize, long values_buffer_offset) {
        throw new NotImplementedException("glPixelMapusv is only avaliable for OpenGL 11 and above");
    }

    default void glPixelStoref(int pname, float param) {
        throw new NotImplementedException("glPixelStoref is only avaliable for OpenGL 11 and above");
    }

    default void glPixelStorei(int pname, int param) {
        throw new NotImplementedException("glPixelStorei is only avaliable for OpenGL 11 and above");
    }

    default void glPixelTransferf(int pname, float param) {
        throw new NotImplementedException("glPixelTransferf is only avaliable for OpenGL 11 and above");
    }

    default void glPixelTransferi(int pname, int param) {
        throw new NotImplementedException("glPixelTransferi is only avaliable for OpenGL 11 and above");
    }

    default void glPixelZoom(float xfactor, float yfactor) {
        throw new NotImplementedException("glPixelZoom is only avaliable for OpenGL 11 and above");
    }

    default void glPointSize(float size) {
        throw new NotImplementedException("glPointSize is only avaliable for OpenGL 11 and above");
    }

    default void glPolygonMode(int face, int mode) {
        throw new NotImplementedException("glPolygonMode is only avaliable for OpenGL 11 and above");
    }

    default void glPolygonOffset(float factor, float units) {
        throw new NotImplementedException("glPolygonOffset is only avaliable for OpenGL 11 and above");
    }

    default void glPolygonStipple(long mask_buffer_offset) {
        throw new NotImplementedException("glPolygonStipple is only avaliable for OpenGL 11 and above");
    }

    default void glPolygonStipple(ByteBuffer mask) {
        throw new NotImplementedException("glPolygonStipple is only avaliable for OpenGL 11 and above");
    }

    default void glPopAttrib() {
        throw new NotImplementedException("glPopAttrib is only avaliable for OpenGL 11 and above");
    }

    default void glPopClientAttrib() {
        throw new NotImplementedException("glPopClientAttrib is only avaliable for OpenGL 11 and above");
    }

    default void glPopMatrix() {
        throw new NotImplementedException("glPopMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glPopName() {
        throw new NotImplementedException("glPopName is only avaliable for OpenGL 11 and above");
    }

    default void glPrioritizeTextures(IntBuffer textures, FloatBuffer priorities) {
        throw new NotImplementedException("glPrioritizeTextures is only avaliable for OpenGL 11 and above");
    }

    default void glPushAttrib(int mask) {
        throw new NotImplementedException("glPushAttrib is only avaliable for OpenGL 11 and above");
    }

    default void glPushClientAttrib(int mask) {
        throw new NotImplementedException("glPushClientAttrib is only avaliable for OpenGL 11 and above");
    }

    default void glPushMatrix() {
        throw new NotImplementedException("glPushMatrix is only avaliable for OpenGL 11 and above");
    }

    default void glPushName(int name) {
        throw new NotImplementedException("glPushName is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos2d(double x, double y) {
        throw new NotImplementedException("glRasterPos2d is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos2f(float x, float y) {
        throw new NotImplementedException("glRasterPos2f is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos2i(int x, int y) {
        throw new NotImplementedException("glRasterPos2i is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos3d(double x, double y, double z) {
        throw new NotImplementedException("glRasterPos3d is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos3f(float x, float y, float z) {
        throw new NotImplementedException("glRasterPos3f is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos3i(int x, int y, int z) {
        throw new NotImplementedException("glRasterPos3i is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos4d(double x, double y, double z, double w) {
        throw new NotImplementedException("glRasterPos4d is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos4f(float x, float y, float z, float w) {
        throw new NotImplementedException("glRasterPos4f is only avaliable for OpenGL 11 and above");
    }

    default void glRasterPos4i(int x, int y, int z, int w) {
        throw new NotImplementedException("glRasterPos4i is only avaliable for OpenGL 11 and above");
    }

    default void glReadBuffer(int mode) {
        throw new NotImplementedException("glReadBuffer is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glReadPixels(int x, int y, int width, int height, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glReadPixels is only avaliable for OpenGL 11 and above");
    }

    default void glRectd(double x1, double y1, double x2, double y2) {
        throw new NotImplementedException("glRectd is only avaliable for OpenGL 11 and above");
    }

    default void glRectf(float x1, float y1, float x2, float y2) {
        throw new NotImplementedException("glRectf is only avaliable for OpenGL 11 and above");
    }

    default void glRecti(int x1, int y1, int x2, int y2) {
        throw new NotImplementedException("glRecti is only avaliable for OpenGL 11 and above");
    }

    default int glRenderMode(int mode) {
        throw new NotImplementedException("glRenderMode is only avaliable for OpenGL 11 and above");
    }

    default void glRotated(double angle, double x, double y, double z) {
        throw new NotImplementedException("glRotated is only avaliable for OpenGL 11 and above");
    }

    default void glRotatef(float angle, float x, float y, float z) {
        throw new NotImplementedException("glRotatef is only avaliable for OpenGL 11 and above");
    }

    default void glScaled(double x, double y, double z) {
        throw new NotImplementedException("glScaled is only avaliable for OpenGL 11 and above");
    }

    default void glScalef(float x, float y, float z) {
        throw new NotImplementedException("glScalef is only avaliable for OpenGL 11 and above");
    }

    default void glScissor(int x, int y, int width, int height) {
        throw new NotImplementedException("glScissor is only avaliable for OpenGL 11 and above");
    }

    default void glSelectBuffer(IntBuffer buffer) {
        throw new NotImplementedException("glSelectBuffer is only avaliable for OpenGL 11 and above");
    }

    default void glShadeModel(int mode) {
        throw new NotImplementedException("glShadeModel is only avaliable for OpenGL 11 and above");
    }

    default void glStencilFunc(int func, int ref, int mask) {
        throw new NotImplementedException("glStencilFunc is only avaliable for OpenGL 11 and above");
    }

    default void glStencilMask(int mask) {
        throw new NotImplementedException("glStencilMask is only avaliable for OpenGL 11 and above");
    }

    default void glStencilOp(int fail, int zfail, int zpass) {
        throw new NotImplementedException("glStencilOp is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord1d(double s) {
        throw new NotImplementedException("glTexCoord1d is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord1f(float s) {
        throw new NotImplementedException("glTexCoord1f is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord2d(double s, double t) {
        throw new NotImplementedException("glTexCoord2d is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord2f(float s, float t) {
        throw new NotImplementedException("glTexCoord2f is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord3d(double s, double t, double r) {
        throw new NotImplementedException("glTexCoord3d is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord3f(float s, float t, float r) {
        throw new NotImplementedException("glTexCoord3f is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord4d(double s, double t, double r, double q) {
        throw new NotImplementedException("glTexCoord4d is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoord4f(float s, float t, float r, float q) {
        throw new NotImplementedException("glTexCoord4f is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int type, int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int type, int stride, ByteBuffer pointer) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int stride, DoubleBuffer pointer) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int stride, FloatBuffer pointer) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int stride, IntBuffer pointer) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexCoordPointer(int size, int stride, ShortBuffer pointer) {
        throw new NotImplementedException("glTexCoordPointer is only avaliable for OpenGL 11 and above");
    }

    default void glTexEnv(int target, int pname, FloatBuffer params) {
        throw new NotImplementedException("glTexEnv is only avaliable for OpenGL 11 and above");
    }

    default void glTexEnv(int target, int pname, IntBuffer params) {
        throw new NotImplementedException("glTexEnv is only avaliable for OpenGL 11 and above");
    }

    default void glTexEnvf(int target, int pname, float param) {
        throw new NotImplementedException("glTexEnvf is only avaliable for OpenGL 11 and above");
    }

    default void glTexEnvi(int target, int pname, int param) {
        throw new NotImplementedException("glTexEnvi is only avaliable for OpenGL 11 and above");
    }

    default void glTexGen(int coord, int pname, DoubleBuffer params) {
        throw new NotImplementedException("glTexGen is only avaliable for OpenGL 11 and above");
    }

    default void glTexGen(int coord, int pname, FloatBuffer params) {
        throw new NotImplementedException("glTexGen is only avaliable for OpenGL 11 and above");
    }

    default void glTexGen(int coord, int pname, IntBuffer params) {
        throw new NotImplementedException("glTexGen is only avaliable for OpenGL 11 and above");
    }

    default void glTexGend(int coord, int pname, double param) {
        throw new NotImplementedException("glTexGend is only avaliable for OpenGL 11 and above");
    }

    default void glTexGenf(int coord, int pname, float param) {
        throw new NotImplementedException("glTexGenf is only avaliable for OpenGL 11 and above");
    }

    default void glTexGeni(int coord, int pname, int param) {
        throw new NotImplementedException("glTexGeni is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, IntBuffer pixels) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage1D(int target, int level, int internalformat, int width, int border, int format, int type, ShortBuffer pixels) {
        throw new NotImplementedException("glTexImage1D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTexImage2D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        throw new NotImplementedException("glTexImage2D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, DoubleBuffer pixels) {
        throw new NotImplementedException("glTexImage2D is only avaliable for OpenGL 11 and above");
    }

    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, FloatBuffer pixels) {
        throw new NotImplementedException("glTexImage2D is only avaliable for OpenGL 11 and above");
    }
    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, java.nio.IntBuffer pixels) {
        throw new NotImplementedException("glTexImage2D(IntBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, java.nio.ShortBuffer pixels) {
        throw new NotImplementedException("glTexImage2D(ShortBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexParameter(int target, int pname, java.nio.FloatBuffer param) {
        throw new NotImplementedException("glTexParameter(FloatBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexParameter(int target, int pname, java.nio.IntBuffer param) {
        throw new NotImplementedException("glTexParameter(IntBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexParameterf(int target, int pname, float param) {
        throw new NotImplementedException("glTexParameterf is only available for OpenGL 11 and above");
    }

    default void glTexParameteri(int target, int pname, int param) {
        throw new NotImplementedException("glTexParameteri is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTexSubImage1D(buffer offset) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, java.nio.ByteBuffer pixels) {
        throw new NotImplementedException("glTexSubImage1D(ByteBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, java.nio.DoubleBuffer pixels) {
        throw new NotImplementedException("glTexSubImage1D(DoubleBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, java.nio.FloatBuffer pixels) {
        throw new NotImplementedException("glTexSubImage1D(FloatBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, java.nio.IntBuffer pixels) {
        throw new NotImplementedException("glTexSubImage1D(IntBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage1D(int target, int level, int xoffset, int width, int format, int type, java.nio.ShortBuffer pixels) {
        throw new NotImplementedException("glTexSubImage1D(ShortBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, long pixels_buffer_offset) {
        throw new NotImplementedException("glTexSubImage2D(buffer offset) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, java.nio.ByteBuffer pixels) {
        throw new NotImplementedException("glTexSubImage2D(ByteBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, java.nio.DoubleBuffer pixels) {
        throw new NotImplementedException("glTexSubImage2D(DoubleBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, java.nio.FloatBuffer pixels) {
        throw new NotImplementedException("glTexSubImage2D(FloatBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, java.nio.IntBuffer pixels) {
        throw new NotImplementedException("glTexSubImage2D(IntBuffer) is only available for OpenGL 11 and above");
    }

    default void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, java.nio.ShortBuffer pixels) {
        throw new NotImplementedException("glTexSubImage2D(ShortBuffer) is only available for OpenGL 11 and above");
    }

    default void glTranslated(double x, double y, double z) {
        throw new NotImplementedException("glTranslated is only available for OpenGL 11 and above");
    }

    default void glTranslatef(float x, float y, float z) {
        throw new NotImplementedException("glTranslatef is only available for OpenGL 11 and above");
    }

    default void glVertex2d(double x, double y) {
        throw new NotImplementedException("glVertex2d is only available for OpenGL 11 and above");
    }

    default void glVertex2f(float x, float y) {
        throw new NotImplementedException("glVertex2f is only available for OpenGL 11 and above");
    }

    default void glVertex2i(int x, int y) {
        throw new NotImplementedException("glVertex2i is only available for OpenGL 11 and above");
    }

    default void glVertex3d(double x, double y, double z) {
        throw new NotImplementedException("glVertex3d is only available for OpenGL 11 and above");
    }

    default void glVertex3f(float x, float y, float z) {
        throw new NotImplementedException("glVertex3f is only available for OpenGL 11 and above");
    }

    default void glVertex3i(int x, int y, int z) {
        throw new NotImplementedException("glVertex3i is only available for OpenGL 11 and above");
    }

    default void glVertex4d(double x, double y, double z, double w) {
        throw new NotImplementedException("glVertex4d is only available for OpenGL 11 and above");
    }

    default void glVertex4f(float x, float y, float z, float w) {
        throw new NotImplementedException("glVertex4f is only available for OpenGL 11 and above");
    }

    default void glVertex4i(int x, int y, int z, int w) {
        throw new NotImplementedException("glVertex4i is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int type, int stride, long pointer_buffer_offset) {
        throw new NotImplementedException("glVertexPointer(buffer offset) is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int type, int stride, java.nio.ByteBuffer pointer) {
        throw new NotImplementedException("glVertexPointer(ByteBuffer) is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int stride, java.nio.DoubleBuffer pointer) {
        throw new NotImplementedException("glVertexPointer(DoubleBuffer) is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int stride, java.nio.FloatBuffer pointer) {
        throw new NotImplementedException("glVertexPointer(FloatBuffer) is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int stride, java.nio.IntBuffer pointer) {
        throw new NotImplementedException("glVertexPointer(IntBuffer) is only available for OpenGL 11 and above");
    }

    default void glVertexPointer(int size, int stride, java.nio.ShortBuffer pointer) {
        throw new NotImplementedException("glVertexPointer(ShortBuffer) is only available for OpenGL 11 and above");
    }

    default void glViewport(int x, int y, int width, int height) {
        throw new NotImplementedException("glViewport is only available for OpenGL 11 and above");
    }
    // </editor-fold>
    // <editor-fold desc="OpenGL 12">
    default void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y, int width, int height)
    {
        throw new NotImplementedException("glCopyTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glDrawRangeElements(int mode, int start, int end, int indices_count, int type, long indices_buffer_offset)
    {
        throw new NotImplementedException("glDrawRangeElements is only avaliable for OpenGL 12 and above");
    }

    default void glDrawRangeElements(int mode, int start, int end, ByteBuffer indices)
    {
        throw new NotImplementedException("glDrawRangeElements is only avaliable for OpenGL 12 and above");
    }

    default void glDrawRangeElements(int mode, int start, int end, IntBuffer indices)
    {
        throw new NotImplementedException("glDrawRangeElements is only avaliable for OpenGL 12 and above");
    }

    default void glDrawRangeElements(int mode, int start, int end, ShortBuffer indices)
    {
        throw new NotImplementedException("glDrawRangeElements is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, long pixels_buffer_offset)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, ByteBuffer pixels)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, DoubleBuffer pixels)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, FloatBuffer pixels)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, IntBuffer pixels)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth, int border, int format, int type, ShortBuffer pixels)
    {
        throw new NotImplementedException("glTexImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ByteBuffer pixels)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, DoubleBuffer pixels)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, FloatBuffer pixels)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, IntBuffer pixels)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }

    default void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int type, ShortBuffer pixels)
    {
        throw new NotImplementedException("glTexSubImage3D is only avaliable for OpenGL 12 and above");
    }
    // </editor-fold>
    // <editor-fold desc="OpenGL 13">
    default void glActiveTexture(int texture)
    {
        throw new NotImplementedException("glActiveTexture is only avaliable for OpenGL 13 and above");
    }

    default void glClientActiveTexture(int texture)
    {
        throw new NotImplementedException("glClientActiveTexture is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexImage1D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage1D(int target, int level, int internalformat, int width, int border, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexImage1D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexImage2D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexImage2D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexImage3D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexImage3D(int target, int level, int internalformat, int width, int height, int depth, int border, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexImage3D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexSubImage1D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage1D(int target, int level, int xoffset, int width, int format, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexSubImage1D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexSubImage2D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexSubImage2D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, int data_imageSize, long data_buffer_offset)
    {
        throw new NotImplementedException("glCompressedTexSubImage3D is only avaliable for OpenGL 13 and above");
    }

    default void glCompressedTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth, int format, ByteBuffer data)
    {
        throw new NotImplementedException("glCompressedTexSubImage3D is only avaliable for OpenGL 13 and above");
    }

    default void glGetCompressedTexImage(int target, int lod, long img_buffer_offset)
    {
        throw new NotImplementedException("glGetCompressedTexImage is only avaliable for OpenGL 13 and above");
    }

    default void glGetCompressedTexImage(int target, int lod, ByteBuffer img)
    {
        throw new NotImplementedException("glGetCompressedTexImage is only avaliable for OpenGL 13 and above");
    }

    default void glGetCompressedTexImage(int target, int lod, IntBuffer img)
    {
        throw new NotImplementedException("glGetCompressedTexImage is only avaliable for OpenGL 13 and above");
    }

    default void glGetCompressedTexImage(int target, int lod, ShortBuffer img)
    {
        throw new NotImplementedException("glGetCompressedTexImage is only avaliable for OpenGL 13 and above");
    }

    default void glLoadTransposeMatrix(DoubleBuffer m)
    {
        throw new NotImplementedException("glLoadTransposeMatrix is only avaliable for OpenGL 13 and above");
    }

    default void glLoadTransposeMatrix(FloatBuffer m)
    {
        throw new NotImplementedException("glLoadTransposeMatrix is only avaliable for OpenGL 13 and above");
    }

    default void glMultTransposeMatrix(DoubleBuffer m)
    {
        throw new NotImplementedException("glMultTransposeMatrix is only avaliable for OpenGL 13 and above");
    }

    default void glMultTransposeMatrix(FloatBuffer m)
    {
        throw new NotImplementedException("glMultTransposeMatrix is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord1d(int target, double s)
    {
        throw new NotImplementedException("glMultiTexCoord1d is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord1f(int target, float s)
    {
        throw new NotImplementedException("glMultiTexCoord1f is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord2d(int target, double s, double t)
    {
        throw new NotImplementedException("glMultiTexCoord2d is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord2f(int target, float s, float t)
    {
        throw new NotImplementedException("glMultiTexCoord2f is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord3d(int target, double s, double t, double r)
    {
        throw new NotImplementedException("glMultiTexCoord3d is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord3f(int target, float s, float t, float r)
    {
        throw new NotImplementedException("glMultiTexCoord3f is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord4d(int target, double s, double t, double r, double q)
    {
        throw new NotImplementedException("glMultiTexCoord4d is only avaliable for OpenGL 13 and above");
    }

    default void glMultiTexCoord4f(int target, float s, float t, float r, float q)
    {
        throw new NotImplementedException("glMultiTexCoord4f is only avaliable for OpenGL 13 and above");
    }

    default void glSampleCoverage(float value, boolean invert)
    {
        throw new NotImplementedException("glSampleCoverage is only avaliable for OpenGL 13 and above");
    }
    // </editor-fold>
    // <editor-fold desc="OpenGL 14">
    default void glBlendColor(float red, float green, float blue, float alpha)
    {
        throw new NotImplementedException("glBlendColor is only avaliable for OpenGL 13 and above");
    }

    default void glBlendEquation(int mode)
    {
        throw new NotImplementedException("glBlendEquation is only avaliable for OpenGL 13 and above");
    }

    default void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha)
    {
        throw new NotImplementedException("glBlendFuncSeparate is only avaliable for OpenGL 13 and above");
    }

    default void glFogCoordPointer(int type, int stride, long data_buffer_offset)
    {
        throw new NotImplementedException("glFogCoordPointer is only avaliable for OpenGL 13 and above");
    }

    default void glFogCoordPointer(int stride, DoubleBuffer data)
    {
        throw new NotImplementedException("glFogCoordPointer is only avaliable for OpenGL 13 and above");
    }

    default void glFogCoordPointer(int stride, FloatBuffer data)
    {
        throw new NotImplementedException("glFogCoordPointer is only avaliable for OpenGL 13 and above");
    }

    default void glFogCoordd(double coord)
    {
        throw new NotImplementedException("glFogCoordd is only avaliable for OpenGL 13 and above");
    }

    default void glFogCoordf(float coord)
    {
        throw new NotImplementedException("glFogCoordf is only avaliable for OpenGL 13 and above");
    }

    default void glMultiDrawArrays(int mode, IntBuffer piFirst, IntBuffer piCount)
    {
        throw new NotImplementedException("glMultiDrawArrays is only avaliable for OpenGL 13 and above");
    }

    default void glPointParameter(int pname, FloatBuffer params)
    {
        throw new NotImplementedException("glPointParameter is only avaliable for OpenGL 13 and above");
    }

    default void glPointParameter(int pname, IntBuffer params)
    {
        throw new NotImplementedException("glPointParameter is only avaliable for OpenGL 13 and above");
    }

    default void glPointParameterf(int pname, float param)
    {
        throw new NotImplementedException("glPointParameterf is only avaliable for OpenGL 13 and above");
    }

    default void glPointParameteri(int pname, int param)
    {
        throw new NotImplementedException("glPointParameteri is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColor3b(byte red, byte green, byte blue)
    {
        throw new NotImplementedException("glSecondaryColor3b is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColor3d(double red, double green, double blue)
    {
        throw new NotImplementedException("glSecondaryColor3d is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColor3f(float red, float green, float blue)
    {
        throw new NotImplementedException("glSecondaryColor3f is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColor3ub(byte red, byte green, byte blue)
    {
        throw new NotImplementedException("glSecondaryColor3ub is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColorPointer(int size, int type, int stride, long data_buffer_offset)
    {
        throw new NotImplementedException("glSecondaryColorPointer is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColorPointer(int size, int stride, DoubleBuffer data)
    {
        throw new NotImplementedException("glSecondaryColorPointer is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColorPointer(int size, int stride, FloatBuffer data)
    {
        throw new NotImplementedException("glSecondaryColorPointer is only avaliable for OpenGL 13 and above");
    }

    default void glSecondaryColorPointer(int size, boolean unsigned, int stride, ByteBuffer data)
    {
        throw new NotImplementedException("glSecondaryColorPointer is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos2d(double x, double y)
    {
        throw new NotImplementedException("glWindowPos2d is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos2f(float x, float y)
    {
        throw new NotImplementedException("glWindowPos2f is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos2i(int x, int y)
    {
        throw new NotImplementedException("glWindowPos2i is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos3d(double x, double y, double z)
    {
        throw new NotImplementedException("glWindowPos3d is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos3f(float x, float y, float z)
    {
        throw new NotImplementedException("glWindowPos3f is only avaliable for OpenGL 13 and above");
    }

    default void glWindowPos3i(int x, int y, int z)
    {
        throw new NotImplementedException("glWindowPos3i is only avaliable for OpenGL 13 and above");
    }
    // </editor-fold>
    // <editor-fold desc="OpenGL 43">
    default void glBindVertexBuffer(int bindingindex, int buffer, long offset, int stride) {
        throw new NotImplementedException("glBindVertexBuffer is only avaliable for OpenGL 43 and above");
    }

    default void glClearBufferData(int target, int internalformat, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearBufferData is only avaliable for OpenGL 43 and above");
    }

    default void glClearBufferSubData(int target, int internalformat, long offset, long size, int format, int type, ByteBuffer data) {
        throw new NotImplementedException("glClearBufferSubData is only avaliable for OpenGL 43 and above");
    }

    default void glCopyImageSubData(int srcName, int srcTarget, int srcLevel, int srcX, int srcY, int srcZ, int dstName, int dstTarget, int dstLevel, int dstX, int dstY, int dstZ, int srcWidth, int srcHeight, int srcDepth) {
        throw new NotImplementedException("glCopyImageSubData is only avaliable for OpenGL 43 and above");
    }

    default void glDebugMessageCallback(KHRDebugCallback callback) {
        throw new NotImplementedException("glDebugMessageCallback is only avaliable for OpenGL 43 and above");
    }

    default void glDebugMessageControl(int source, int type, int severity, IntBuffer ids, boolean enabled) {
        throw new NotImplementedException("glDebugMessageControl is only avaliable for OpenGL 43 and above");
    }

    default void glDebugMessageInsert(int source, int type, int id, int severity, CharSequence buf) {
        throw new NotImplementedException("glDebugMessageInsert is only avaliable for OpenGL 43 and above");
    }

    default void glDebugMessageInsert(int source, int type, int id, int severity, ByteBuffer buf) {
        throw new NotImplementedException("glDebugMessageInsert is only avaliable for OpenGL 43 and above");
    }

    default void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        throw new NotImplementedException("glDispatchCompute is only avaliable for OpenGL 43 and above");
    }

    default void glDispatchComputeIndirect(long indirect) {
        throw new NotImplementedException("glDispatchComputeIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glFramebufferParameteri(int target, int pname, int param) {
        throw new NotImplementedException("glFramebufferParameteri is only avaliable for OpenGL 43 and above");
    }

    default int glGetDebugMessageLog(int count, IntBuffer sources, IntBuffer types, IntBuffer ids, IntBuffer severities, IntBuffer lengths, ByteBuffer messageLog) {
        throw new NotImplementedException("glGetDebugMessageLog is only avaliable for OpenGL 43 and above");
    }

    default void glGetFramebufferParameter(int target, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetFramebufferParameter is only avaliable for OpenGL 43 and above");
    }

    default int glGetFramebufferParameteri(int target, int pname) {
        throw new NotImplementedException("glGetFramebufferParameteri is only avaliable for OpenGL 43 and above");
    }

    default void glGetInternalformat(int target, int internalformat, int pname, LongBuffer params) {
        throw new NotImplementedException("glGetInternalformat is only avaliable for OpenGL 43 and above");
    }

    default long glGetInternalformati64(int target, int internalformat, int pname) {
        throw new NotImplementedException("glGetInternalformati64 is only avaliable for OpenGL 43 and above");
    }

    default String glGetObjectLabel(int identifier, int name, int bufSize) {
        throw new NotImplementedException("glGetObjectLabel is only avaliable for OpenGL 43 and above");
    }

    default void glGetObjectLabel(int identifier, int name, IntBuffer length, ByteBuffer label) {
        throw new NotImplementedException("glGetObjectLabel is only avaliable for OpenGL 43 and above");
    }

    default void glGetProgramInterface(int program, int programInterface, int pname, IntBuffer params) {
        throw new NotImplementedException("glGetProgramInterface is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramInterfacei(int program, int programInterface, int pname) {
        throw new NotImplementedException("glGetProgramInterfacei is only avaliable for OpenGL 43 and above");
    }

    default void glGetProgramResource(int program, int programInterface, int index, IntBuffer props, IntBuffer length, IntBuffer params) {
        throw new NotImplementedException("glGetProgramResource is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceIndex(int program, int programInterface, CharSequence name) {
        throw new NotImplementedException("glGetProgramResourceIndex is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceIndex(int program, int programInterface, ByteBuffer name) {
        throw new NotImplementedException("glGetProgramResourceIndex is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceLocation(int program, int programInterface, CharSequence name) {
        throw new NotImplementedException("glGetProgramResourceLocation is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceLocation(int program, int programInterface, ByteBuffer name) {
        throw new NotImplementedException("glGetProgramResourceLocation is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceLocationIndex(int program, int programInterface, CharSequence name) {
        throw new NotImplementedException("glGetProgramResourceLocationIndex is only avaliable for OpenGL 43 and above");
    }

    default int glGetProgramResourceLocationIndex(int program, int programInterface, ByteBuffer name) {
        throw new NotImplementedException("glGetProgramResourceLocationIndex is only avaliable for OpenGL 43 and above");
    }

    default String glGetProgramResourceName(int program, int programInterface, int index, int bufSize) {
        throw new NotImplementedException("glGetProgramResourceName is only avaliable for OpenGL 43 and above");
    }

    default void glGetProgramResourceName(int program, int programInterface, int index, IntBuffer length, ByteBuffer name) {
        throw new NotImplementedException("glGetProgramResourceName is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateBufferData(int buffer) {
        throw new NotImplementedException("glInvalidateBufferData is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateBufferSubData(int buffer, long offset, long length) {
        throw new NotImplementedException("glInvalidateBufferSubData is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateFramebuffer(int target, IntBuffer attachments) {
        throw new NotImplementedException("glInvalidateFramebuffer is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateSubFramebuffer(int target, IntBuffer attachments, int x, int y, int width, int height) {
        throw new NotImplementedException("glInvalidateSubFramebuffer is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateTexImage(int texture, int level) {
        throw new NotImplementedException("glInvalidateTexImage is only avaliable for OpenGL 43 and above");
    }

    default void glInvalidateTexSubImage(int texture, int level, int xoffset, int yoffset, int zoffset, int width, int height, int depth) {
        throw new NotImplementedException("glInvalidateTexSubImage is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawArraysIndirect(int mode, long indirect_buffer_offset, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawArraysIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawArraysIndirect(int mode, ByteBuffer indirect, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawArraysIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawArraysIndirect(int mode, IntBuffer indirect, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawArraysIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawElementsIndirect(int mode, int type, long indirect_buffer_offset, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawElementsIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawElementsIndirect(int mode, int type, ByteBuffer indirect, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawElementsIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glMultiDrawElementsIndirect(int mode, int type, IntBuffer indirect, int primcount, int stride) {
        throw new NotImplementedException("glMultiDrawElementsIndirect is only avaliable for OpenGL 43 and above");
    }

    default void glObjectLabel(int identifier, int name, CharSequence label) {
        throw new NotImplementedException("glObjectLabel is only avaliable for OpenGL 43 and above");
    }

    default void glObjectLabel(int identifier, int name, ByteBuffer label) {
        throw new NotImplementedException("glObjectLabel is only avaliable for OpenGL 43 and above");
    }

    default void glPopDebugGroup() {
        throw new NotImplementedException("glPopDebugGroup is only avaliable for OpenGL 43 and above");
    }

    default void glPushDebugGroup(int source, int id, CharSequence message) {
        throw new NotImplementedException("glPushDebugGroup is only avaliable for OpenGL 43 and above");
    }

    default void glPushDebugGroup(int source, int id, ByteBuffer message) {
        throw new NotImplementedException("glPushDebugGroup is only avaliable for OpenGL 43 and above");
    }

    default void glShaderStorageBlockBinding(int program, int storageBlockIndex, int storageBlockBinding) {
        throw new NotImplementedException("glShaderStorageBlockBinding is only avaliable for OpenGL 43 and above");
    }

    default void glTexBufferRange(int target, int internalformat, int buffer, long offset, long size) {
        throw new NotImplementedException("glTexBufferRange is only avaliable for OpenGL 43 and above");
    }

    default void glTexStorage2DMultisample(int target, int samples, int internalformat, int width, int height, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTexStorage2DMultisample is only avaliable for OpenGL 43 and above");
    }

    default void glTexStorage3DMultisample(int target, int samples, int internalformat, int width, int height, int depth, boolean fixedsamplelocations) {
        throw new NotImplementedException("glTexStorage3DMultisample is only avaliable for OpenGL 43 and above");
    }

    default void glTextureView(int texture, int target, int origtexture, int internalformat, int minlevel, int numlevels, int minlayer, int numlayers) {
        throw new NotImplementedException("glTextureView is only avaliable for OpenGL 43 and above");
    }

    default void glVertexAttribBinding(int attribindex, int bindingindex) {
        throw new NotImplementedException("glVertexAttribBinding is only avaliable for OpenGL 43 and above");
    }

    default void glVertexAttribFormat(int attribindex, int size, int type, boolean normalized, int relativeoffset) {
        throw new NotImplementedException("glVertexAttribFormat is only avaliable for OpenGL 43 and above");
    }

    default void glVertexAttribIFormat(int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexAttribIFormat is only avaliable for OpenGL 43 and above");
    }

    default void glVertexAttribLFormat(int attribindex, int size, int type, int relativeoffset) {
        throw new NotImplementedException("glVertexAttribLFormat is only avaliable for OpenGL 43 and above");
    }

    default void glVertexBindingDivisor(int bindingindex, int divisor) {
        throw new NotImplementedException("glVertexBindingDivisor is only avaliable for OpenGL 43 and above");
    }
    // </editor-fold>
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
