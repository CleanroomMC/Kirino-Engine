package com.cleanroommc.kirino.gl.texture.accessor;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL45;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;

public abstract class TextureDSAAccessor implements TextureAccessor {

    protected final boolean dsa;

    TextureDSAAccessor(boolean dsa) {
        this.dsa = dsa;
    }

    public final boolean isDsaOn() {
        return dsa;
    }

    @Override
    public void genMipmap() {
        if (dsa) {
            GL45.glGenerateTextureMipmap(textureID());
        } else {
            TextureAccessor.super.genMipmap();
        }
    }

    @Override
    public void texParamI(int pname, int param) {
        if (dsa) {
            GL45.glTextureParameteri(textureID(), pname, param);
        } else {
            TextureAccessor.super.texParamI(pname, param);
        }
    }

    @Override
    public void texParamF(int pname, float param) {
        if (dsa) {
            GL45.glTextureParameterf(textureID(), pname, param);
        } else {
            TextureAccessor.super.texParamF(pname, param);
        }
    }

    @Override
    public void texParamIv(int pname, int[] params) {
        if (dsa) {
            GL45C.glTextureParameteriv(textureID(), pname, params);
        } else {
            TextureAccessor.super.texParamIv(pname, params);
        }
    }

    @Override
    public void texParamFv(int pname, float[] params) {
        if (dsa) {
            GL45C.glTextureParameterfv(textureID(), pname, params);
        } else {
            TextureAccessor.super.texParamFv(pname, params);
        }
    }

    @Override
    public void texParamIiv(int pname, int[] params) {
        if (dsa) {
            GL45C.glTextureParameterIiv(textureID(), pname, params);
        } else {
            TextureAccessor.super.texParamIiv(pname, params);
        }
    }

    @Override
    public void texParamIuiv(int pname, int[] params) {
        if (dsa) {
            GL45C.glTextureParameterIuiv(textureID(), pname, params);
        } else {
            TextureAccessor.super.texParamIuiv(pname, params);
        }
    }

    @Override
    public int fetchTexLevelParamI(int level, int pname) {
        if (dsa) {
            return GL45.glGetTextureLevelParameteri(textureID(), level, pname);
        } else {
            return TextureAccessor.super.fetchTexLevelParamI(level, pname);
        }
    }

    @Override
    public float fetchTexLevelParamF(int level, int pname) {
        if (dsa) {
            return GL45.glGetTextureLevelParameterf(textureID(), level, pname);
        } else {
            return TextureAccessor.super.fetchTexLevelParamF(level, pname);
        }
    }

    @Override
    public void unit(int unit) {
        if (dsa) {
            GL45C.glBindTextureUnit(unit, textureID());
        } else {
            TextureAccessor.super.unit(unit);
        }
    }

    @Override
    public void clearTexImage(int level, int format, int type, @Nullable ByteBuffer data) {
        Preconditions.checkState(dsa, "Non-DSA \"clearTexImage\" is not implemented.");

        GL45C.glClearTexImage(textureID(), level, format, type, data);
    }

    @Override
    public void clearTexSubImage(
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

        Preconditions.checkState(dsa, "Non-DSA \"clearTexSubImage\" is not implemented.");

        GL45C.glClearTexSubImage(textureID(), level, xOffset, yOffset, zOffset, width, height, depthOrLayers, format, type, data);
    }

    @Override
    public void getTexImage(int level, int format, int type, @NonNull ByteBuffer data) {
        if (dsa) {
            Preconditions.checkNotNull(data);

            GL45C.glGetTextureImage(textureID(), level, format, type, data);
        } else {
            TextureAccessor.super.getTexImage(level, format, type, data);
        }
    }

    @Override
    public void getCompressedTexImage(int level, @NonNull ByteBuffer data) {
        if (dsa) {
            Preconditions.checkNotNull(data);

            GL45C.glGetCompressedTextureImage(textureID(), level, data);
        } else {
            TextureAccessor.super.getCompressedTexImage(level, data);
        }
    }

    // todo: move to tbo accessor
//    @Override
//    public void texBuffer(int internalFormat, int bufferID) {
//        if (dsa) {
//            GL45C.glTextureBuffer(textureID(), internalFormat, bufferID);
//        } else {
//            TextureAccessor.super.texBuffer(internalFormat, bufferID);
//        }
//    }
//
//    @Override
//    public void texBufferRange(int internalFormat, int bufferID, long offset, long size) {
//        if (dsa) {
//            GL45C.glTextureBufferRange(textureID(), internalFormat, bufferID, offset, size);
//        } else {
//            TextureAccessor.super.texBufferRange(internalFormat, bufferID, offset, size);
//        }
//    }
}
