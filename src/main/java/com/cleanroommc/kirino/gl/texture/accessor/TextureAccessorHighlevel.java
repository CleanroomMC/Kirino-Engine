package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import org.jspecify.annotations.NonNull;

import java.nio.ByteBuffer;

public interface TextureAccessorHighlevel {

    interface HighlevelOperator {

        //<editor-fold desc="convenient allocation overloads">

        // implement two of them based on the texture type

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width, int height) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width, int height, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width, int height, int depthOrLayers) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAllocEmpty(int width, int height, int depthOrLayers, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        // implement two of them based on the texture type

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, @NonNull ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, int height, @NonNull ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, int height, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * If the previously specified texture format is <code>null</code>,
         * then allocate with {@link TextureFormat#RGBA8_UNORM}. Otherwise, allocate with the
         * previously specified texture format.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code></p>
         */
        default void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        // implement both; texture type agnostic

        /**
         * Allocates with {@link TextureFormat#RGBA8_UNORM}.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         *
         * @param mutable Must not call resize on immutable storage! (must not reallocate)
         */
        default void alloc(boolean mutable, @NonNull ByteBuffer byteBuffer) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         *
         * @param mutable Must not call resize on immutable storage! (must not reallocate)
         */
        default void alloc(boolean mutable, @NonNull ByteBuffer byteBuffer, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }

        // implement both; texture type agnostic

        /**
         * Allocates with {@link TextureFormat#RGBA8_UNORM}.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         *
         * @param mutable Must not call resize on immutable storage! (must not reallocate)
         */
        default void allocEmpty(boolean mutable) {
            throw new UnsupportedOperationException("\"allocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: Mipmap count defaults to <code>1</code>.</p>
         *
         * @param mutable Must not call resize on immutable storage! (must not reallocate)
         */
        default void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"allocEmpty\" is not implemented.");
        }
        //</editor-fold>

        //<editor-fold desc="canonical allocation methods">

        // implement one of them based on the texture type

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         */
        default void resizeAndAllocEmpty(int width, @NonNull StorageOptions options, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         */
        default void resizeAndAllocEmpty(int width, int height, @NonNull StorageOptions options, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         */
        default void resizeAndAllocEmpty(int width, int height, int depthOrLayers, @NonNull StorageOptions options, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"resizeAndAllocEmpty\" is not implemented.");
        }

        // implement one of them based on the texture type

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         * @param generateMipmaps Whether immediately generates mipmaps altogether
         */
        default void resizeAndAlloc(int width, @NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         * @param generateMipmaps Whether immediately generates mipmaps altogether
         */
        default void resizeAndAlloc(int width, int height, @NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         * <p>Note: Must not call resize on an immutable storage! (must not reallocate)</p>
         * <p><b>Resize Path:</b> <code>unallocated</code>/<code>mutable</code> -> <code>mutable</code>/<code>immutable</code></p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         * @param generateMipmaps Whether immediately generates mipmaps altogether
         */
        default void resizeAndAlloc(int width, int height, int depthOrLayers, @NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            throw new UnsupportedOperationException("\"resizeAndAlloc\" is not implemented.");
        }

        // must implement

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         * @param generateMipmaps Whether immediately generates mipmaps altogether
         */
        default void alloc(@NonNull StorageOptions options, @NonNull ByteBuffer data, @NonNull TextureFormat format, boolean generateMipmaps) {
            throw new UnsupportedOperationException("\"alloc\" is not implemented.");
        }

        /**
         * Allocates with the given <code>format</code>.
         *
         * <p>Note: Every allocation specifies the texture format, so does it.</p>
         * <p>Note: For <code>mutable</code> mipmap reallocations, it won't
         * delete the old mipmaps but only override. And, <code>immutable</code> storage
         * simply doesn't allow reallocations.</p>
         *
         * @param options Must not call resize on immutable storage! (must not reallocate)
         */
        default void allocEmpty(@NonNull StorageOptions options, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"allocEmpty\" is not implemented.");
        }
        //</editor-fold>

        /**
         * Uploads with the previously specified texture format which must be non-null!
         *
         * <p>Note: It doesn't modify/specificy texture format!</p>
         */
        default void uploadLevel(int level, @NonNull ByteBuffer data) {
            throw new UnsupportedOperationException("\"uploadLevel\" is not implemented.");
        }

        /**
         * Uploads with the given <code>format</code>.
         *
         * <p>Note: It doesn't modify/specificy texture format!</p>
         * <p>Note: Must not mismatch the base level format!</p>
         */
        default void uploadLevel(int level, @NonNull ByteBuffer data, @NonNull TextureFormat format) {
            throw new UnsupportedOperationException("\"uploadLevel\" is not implemented.");
        }

        /**
         * Automatically generates every mipmap level.
         * Existing contents may be replaced.
         */
        default void generateMipmaps() {
            throw new UnsupportedOperationException("\"generateMipmaps\" is not implemented.");
        }

        /**
         * It sets the range to <code>0 .. levels-1</code>.
         * It only modifies the global GL states to guide {@link #generateMipmaps()} behavior.
         * Don't expect any shadow states to be maintained internally.
         */
        default void setMipRange(int levels) {
            throw new UnsupportedOperationException("\"setMipRange\" is not implemented.");
        }
    }

    @NonNull
    HighlevelOperator highlevel();
}
