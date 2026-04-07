package com.cleanroommc.kirino.gl.texture.accessor;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.*;

public class TextureBufferAccessor extends TextureAccessorExt implements TextureAccessorHighlevel {

    public final GLTexture texture;

    public TextureBufferAccessor(boolean dsa, GLTexture texture) {
        super(dsa);
        Preconditions.checkState(texture.type == TextureType.TEX_BUFFER,
                "Texture type must be TEX_BUFFER.");

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
        return TextureType.TEX_BUFFER;
    }

    @Override
    public void texBuffer(int internalFormat, int bufferID) {
        if (dsa) {
            GL45.glTextureBuffer(textureID(), internalFormat, bufferID);
        } else {
            GL31.glTexBuffer(target(), internalFormat, bufferID);
        }
    }

    @Override
    public void texBufferRange(int internalFormat, int bufferID, long offset, long size) {
        if (dsa) {
            GL45.glTextureBufferRange(textureID(), internalFormat, bufferID, offset, size);
        } else {
            GL43.glTexBufferRange(target(), internalFormat, bufferID, offset, size);
        }
    }

    @NonNull
    @Override
    public HighlevelOperator highlevel() {
        throw new UnsupportedOperationException("\"TextureBufferAccessor\" doesn't provide any high level API.");
    }
}