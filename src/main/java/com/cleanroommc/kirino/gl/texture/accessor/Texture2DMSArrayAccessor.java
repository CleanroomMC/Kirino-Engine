package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL45;

/**
 * <p>Note: (<i>This is not derived from the GL spec but a stricter enforcement</i>)
 * <b>Mutable texture storage allocation is not supported in DSA mode.</b></p>
 *
 * <p>Available raw GL operations:</p>
 * <ul>
 *     <li><code>texImage3DMultisample</code></li>
 * </ul>
 */
public class Texture2DMSArrayAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public Texture2DMSArrayAccessor(boolean dsa, @NonNull GLTexture texture) {
        super(dsa);
        Preconditions.checkNotNull(texture);
        Preconditions.checkState(texture.type == TextureType.TEX_2D_MS_ARRAY,
                "Texture type must be TEX_2D_MS_ARRAY.");

        this.texture = texture;
    }

    @Override
    public int textureID() {
        return texture.textureID;
    }

    @Override
    public int target() {
        return type().glValue;
    }

    @Override
    public int bindingTarget() {
        return type().bindingTarget();
    }

    @NonNull
    @Override
    public TextureType type() {
        return TextureType.TEX_2D_MS_ARRAY;
    }

    @Override
    public void texImage3DMultisample(
            int samples,
            int internalFormat,
            int width,
            int height,
            int layers,
            boolean fixedSampleLocations) {

        if (dsa) {
            GL45.glTextureStorage3DMultisample(
                    textureID(),
                    samples,
                    internalFormat,
                    width,
                    height,
                    layers,
                    fixedSampleLocations);
        } else {
            GL32.glTexImage3DMultisample(
                    target(),
                    samples,
                    internalFormat,
                    width,
                    height,
                    layers,
                    fixedSampleLocations);
        }
    }

    private static final class HighlevelOperatorImpl implements HighlevelOperator {

        private final Texture2DMSArrayAccessor accessor;

        private HighlevelOperatorImpl(Texture2DMSArrayAccessor accessor) {
            this.accessor = accessor;
        }

        //<editor-fold desc="convenient allocation overloads">
        @Override
        public void resizeAndAllocEmpty(int width, int height, int depthOrLayers) {
            TextureFormat format = accessor.texture.currentFormat();
            if (format == null) {
                allocEmpty(width, height, depthOrLayers, !accessor.dsa, TextureFormat.RGBA8_UNORM);
            } else {
                allocEmpty(width, height, depthOrLayers, !accessor.dsa, format);
            }

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setLayers(accessor.texture, depthOrLayers);
        }

        @Override
        public void resizeAndAllocEmpty(
                int width,
                int height,
                int depthOrLayers,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(format);

            allocEmpty(width, height, depthOrLayers, !accessor.dsa, format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setLayers(accessor.texture, depthOrLayers);
        }

        @Override
        public void allocEmpty(boolean mutable) {
            allocEmpty(mutable, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            allocEmpty(accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.layers(), mutable, format);
        }

        private void allocEmpty(
                int width,
                int height,
                int depthOrLayers,
                boolean mutable,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(format);

            allocEmpty(width, height, depthOrLayers, new StorageOptions(mutable, 1), format);
        }
        //</editor-fold>

        //<editor-fold desc="canonical allocation methods">
        @Override
        public void resizeAndAllocEmpty(
                int width,
                int height,
                int depthOrLayers,
                @NonNull StorageOptions options,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            allocEmpty(width, height, depthOrLayers, options, format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
            MethodHolder.setLayers(accessor.texture, depthOrLayers);
        }

        @Override
        public void allocEmpty(@NonNull StorageOptions options, @NonNull TextureFormat format) {
            allocEmpty(accessor.texture.extentX(), accessor.texture.extentY(), accessor.texture.layers(), options, format);
        }

        private void allocEmpty(
                int width,
                int height,
                int depthOrLayers,
                @NonNull StorageOptions options,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            int layers = depthOrLayers;
            int samples = accessor.texture.samples();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(layers > 0);
            Preconditions.checkState(samples > 0);
            Preconditions.checkArgument(options.levels() == 1,
                    "Multisample textures must have exactly one mipmap level.");
            Preconditions.checkArgument(options.mutable() != accessor.dsa,
                    "DSA multisample storage must be immutable and non-DSA multisample storage must be mutable.");

            accessor.texImage3DMultisample(samples, format.internalFormat, width, height, layers, true);

            MethodHolder.setCurrentFormat(accessor.texture, format);
        }
        //</editor-fold>
    }

    private HighlevelOperatorImpl highlevelOperator = null;

    @NonNull
    @Override
    public HighlevelOperator highlevel() {
        if (highlevelOperator == null) {
            highlevelOperator = new HighlevelOperatorImpl(this);
        }
        return highlevelOperator;
    }
}
