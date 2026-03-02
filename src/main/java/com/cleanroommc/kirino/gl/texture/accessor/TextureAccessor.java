package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.TextureType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public interface TextureAccessor {

    int textureID();
    int target();
    int bindingTarget();

    @NonNull
    TextureType type();

    default void bind(int textureID) {
        GL11.glBindTexture(target(), textureID);
    }

    default void bind() {
        bind(textureID());
    }

    default int fetchCurrentBoundBufferID() {
        return GL11.glGetInteger(bindingTarget());
    }

    default void unit(int unit) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
    }

    default void clearTexImage(
            int level,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"clearTexImage\" is not implemented.");
    }

    default void clearTexSubImage(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int width,
            int height,
            int depthOrLayers,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"clearTexSubImage\" is not implemented.");
    }

    default void getTexImage(int level, int format, int type, @NonNull ByteBuffer data) {
        Preconditions.checkNotNull(data);

        GL11.glGetTexImage(target(), level, format, type, data);
    }

    default void getCompressedTexImage(int level, @NonNull ByteBuffer data) {
        Preconditions.checkNotNull(data);

        GL13.glGetCompressedTexImage(target(), level, data);
    }

    default void texParamI(int pname, int param) {
        GL11.glTexParameteri(target(), pname, param);
    }

    default void texParamF(int pname, float param) {
        GL11.glTexParameterf(target(), pname, param);
    }

    default void texParamIv(int pname, int[] params) {
        GL11C.glTexParameteriv(target(), pname, params);
    }

    default void texParamFv(int pname, float[] params) {
        GL11C.glTexParameterfv(target(), pname, params);
    }

    default void texParamIiv(int pname, int[] params) {
        GL30C.glTexParameterIiv(target(), pname, params);
    }

    default void texParamIuiv(int pname, int[] params) {
        GL30C.glTexParameterIuiv(target(), pname, params);
    }

    default int fetchTexLevelParamI(int level, int pname) {
        return GL11.glGetTexLevelParameteri(target(), level, pname);
    }

    default float fetchTexLevelParamF(int level, int pname) {
        return GL11.glGetTexLevelParameterf(target(), level, pname);
    }

    default void genMipmap() {
        GL30.glGenerateMipmap(target());
    }

    default void texStorage1D(int levels, int internalFormat, int width) {
        throw new UnsupportedOperationException("\"texStorage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void texStorage2D(int levels, int internalFormat, int width, int height) {
        throw new UnsupportedOperationException("\"texStorage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void texStorage3D(int levels, int internalFormat, int width, int height, int depthOrLayers) {
        throw new UnsupportedOperationException("\"texStorage3D\" not supported by this texture type " + type().name() + ".");
    }

    default void texImage1D(
            int level,
            int internalFormat,
            int width,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texImage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void texImage2D(
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void texImage3D(
            int level,
            int internalFormat,
            int width,
            int height,
            int depthOrLayers,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texImage3D\" not supported by this texture type " + type().name() + ".");
    }

    default void texSubImage1D(
            int level,
            int xOffset,
            int width,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texSubImage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void texSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texSubImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void texSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int width,
            int height,
            int depthOrLayerCount,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"texSubImage3D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexImage1D(
            int level,
            int internalFormat,
            int width,
            int border,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexImage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexImage2D(
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexImage3D(
            int level,
            int internalFormat,
            int width,
            int height,
            int depthOrLayers,
            int border,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexImage3D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexSubImage1D(
            int level,
            int xOffset,
            int width,
            int format,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexSubImage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexSubImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedTexSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int width,
            int height,
            int depthOrLayerCount,
            int format,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedTexSubImage3D\" not supported by this texture type " + type().name() + ".");
    }

    default void copyTexSubImage1D(
            int level,
            int xOffset,
            int x,
            int y,
            int width) {

        throw new UnsupportedOperationException("\"copyTexSubImage1D\" not supported by this texture type " + type().name() + ".");
    }

    default void copyTexSubImage2D(
            int level,
            int xOffset,
            int yOffset,
            int x,
            int y,
            int width,
            int height) {

        throw new UnsupportedOperationException("\"copyTexSubImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void copyTexSubImage3D(
            int level,
            int xOffset,
            int yOffset,
            int zOffset,
            int x,
            int y,
            int width,
            int height) {

        throw new UnsupportedOperationException("\"copyTexSubImage3D\" not supported by this texture type " + type().name() + ".");
    }

    enum CubeFace {
        POS_X(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X),
        NEG_X(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X),
        POS_Y(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y),
        NEG_Y(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y),
        POS_Z(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z),
        NEG_Z(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z);

        public final int glValue;

        CubeFace(int glValue) {
            this.glValue = glValue;
        }
    }

    default void cubeTexImage2D(
            @NonNull CubeFace face,
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"cubeTexImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void cubeTexSubImage2D(
            @NonNull CubeFace face,
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            int type,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"cubeTexSubImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedCubeTexImage2D(
            @NonNull CubeFace face,
            int level,
            int internalFormat,
            int width,
            int height,
            int border,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedCubeTexImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void compressedCubeTexSubImage2D(
            @NonNull CubeFace face,
            int level,
            int xOffset,
            int yOffset,
            int width,
            int height,
            int format,
            @Nullable ByteBuffer data) {

        throw new UnsupportedOperationException("\"compressedCubeTexSubImage2D\" not supported by this texture type " + type().name() + ".");
    }

    default void texImage2DMultisample(
            int samples,
            int internalFormat,
            int width,
            int height,
            boolean fixedSampleLocations) {

        throw new UnsupportedOperationException("\"texImage2DMultisample\" not supported by this texture type " + type().name() + ".");
    }

    default void texImage3DMultisample(
            int samples,
            int internalFormat,
            int width,
            int height,
            int layers,
            boolean fixedSampleLocations) {

        throw new UnsupportedOperationException("\"texImage3DMultisample\" not supported by this texture type " + type().name() + ".");
    }

    default void texBuffer(int internalFormat, int bufferID) {
        throw new UnsupportedOperationException("\"texBuffer\" not supported by this texture type " + type().name() + ".");
    }

    default void texBufferRange(int internalFormat, int bufferID, long offset, long size) {
        throw new UnsupportedOperationException("\"texBufferRange\" not supported by this texture type " + type().name() + ".");
    }
}
