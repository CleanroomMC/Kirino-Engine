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
 *     <li><code>texImage2DMultisample</code></li>
 * </ul>
 */
public class Texture2DMSAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public Texture2DMSAccessor(boolean dsa, @NonNull GLTexture texture) {
        super(dsa);
        Preconditions.checkNotNull(texture);
        Preconditions.checkState(texture.type == TextureType.TEX_2D_MS,
                "Texture type must be TEX_2D_MS.");

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
        return TextureType.TEX_2D_MS;
    }

    @Override
    public void texImage2DMultisample(
            int samples,
            int internalFormat,
            int width,
            int height,
            boolean fixedSampleLocations) {

        if (dsa) {
            GL45.glTextureStorage2DMultisample(
                    textureID(),
                    samples,
                    internalFormat,
                    width,
                    height,
                    fixedSampleLocations);
        } else {
            GL32.glTexImage2DMultisample(
                    target(),
                    samples,
                    internalFormat,
                    width,
                    height,
                    fixedSampleLocations);
        }
    }

    private static final class HighlevelOperatorImpl implements HighlevelOperator {

        private final Texture2DMSAccessor accessor;

        private HighlevelOperatorImpl(Texture2DMSAccessor accessor) {
            this.accessor = accessor;
        }

        //<editor-fold desc="convenient allocation overloads">
        @Override
        public void resizeAndAllocEmpty(int width, int height) {
            TextureFormat format = accessor.texture.currentFormat();
            if (format == null) {
                allocEmpty(width, height, !accessor.dsa, TextureFormat.RGBA8_UNORM);
            } else {
                allocEmpty(width, height, !accessor.dsa, format);
            }

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
        }

        @Override
        public void resizeAndAllocEmpty(int width, int height, @NonNull TextureFormat format) {
            Preconditions.checkNotNull(format);

            allocEmpty(width, height, !accessor.dsa, format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
        }

        @Override
        public void allocEmpty(boolean mutable) {
            allocEmpty(mutable, TextureFormat.RGBA8_UNORM);
        }

        @Override
        public void allocEmpty(boolean mutable, @NonNull TextureFormat format) {
            allocEmpty(accessor.texture.extentX(), accessor.texture.extentY(), mutable, format);
        }

        private void allocEmpty(
                int width,
                int height,
                boolean mutable,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(format);

            allocEmpty(width, height, new StorageOptions(mutable, 1), format);
        }
        //</editor-fold>

        //<editor-fold desc="canonical allocation methods">
        @Override
        public void resizeAndAllocEmpty(
                int width,
                int height,
                @NonNull StorageOptions options,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            allocEmpty(width, height, options, format);

            MethodHolder.setExtentX(accessor.texture, width);
            MethodHolder.setExtentY(accessor.texture, height);
        }

        @Override
        public void allocEmpty(@NonNull StorageOptions options, @NonNull TextureFormat format) {
            allocEmpty(accessor.texture.extentX(), accessor.texture.extentY(), options, format);
        }

        private void allocEmpty(
                int width,
                int height,
                @NonNull StorageOptions options,
                @NonNull TextureFormat format) {

            Preconditions.checkNotNull(options);
            Preconditions.checkNotNull(format);

            int samples = accessor.texture.samples();

            Preconditions.checkState(width > 0);
            Preconditions.checkState(height > 0);
            Preconditions.checkState(samples > 0);
            Preconditions.checkArgument(options.levels() == 1,
                    "Multisample textures must have exactly one mipmap level.");
            Preconditions.checkArgument(options.mutable() != accessor.dsa,
                    "DSA multisample storage must be immutable and non-DSA multisample storage must be mutable.");

            accessor.texImage2DMultisample(samples, format.internalFormat, width, height, true);

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
