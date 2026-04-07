package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.ByteBuffer;

public interface TextureAccessorHighlevel {

    interface HighlevelOperator {

        default void resizeAndAllocEmpty(int width) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }
        default void resizeAndAllocEmpty(int width, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }
        default void resizeAndAllocEmpty(int width, int height) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }
        default void resizeAndAllocEmpty(int width, int height, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }
        default void resizeAndAllocEmpty(int width, int height, int depthOrLayers) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }
        default void resizeAndAllocEmpty(int width, int height, int depthOrLayers, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        default void resizeAndAlloc(int width, @Nullable ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }
        default void resizeAndAlloc(int width, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }
        default void resizeAndAlloc(int width, int height, @Nullable ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }
        default void resizeAndAlloc(int width, int height, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }
        default void resizeAndAlloc(int width, int height, int depthOrLayers, @Nullable ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }
        default void resizeAndAlloc(int width, int height, int depthOrLayers, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        default void alloc(boolean mutable, @Nullable ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }
        default void alloc(boolean mutable, @Nullable ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }

        default void allocEmpty(boolean mutable) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }
        default void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }

        // todo: provide mipmap related utils
    }

    @NonNull
    HighlevelOperator highlevel();
}
