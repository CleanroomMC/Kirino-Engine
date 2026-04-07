package com.cleanroommc.kirino.gl.texture;

import org.lwjgl.opengl.*;

public enum TextureType {
    TEX_1D(GL11.GL_TEXTURE_1D),
    TEX_2D(GL11.GL_TEXTURE_2D),
    TEX_3D(GL12.GL_TEXTURE_3D),
    TEX_1D_ARRAY(GL30.GL_TEXTURE_1D_ARRAY),
    TEX_2D_ARRAY(GL30.GL_TEXTURE_2D_ARRAY),
    CUBEMAP(GL13.GL_TEXTURE_CUBE_MAP),
    CUBEMAP_ARRAY(GL40.GL_TEXTURE_CUBE_MAP_ARRAY),
    TEX_2D_MS(GL32.GL_TEXTURE_2D_MULTISAMPLE),
    TEX_2D_MS_ARRAY(GL32.GL_TEXTURE_2D_MULTISAMPLE_ARRAY),
    TEX_BUFFER(GL31.GL_TEXTURE_BUFFER);

    public final int glValue;

    TextureType(int glValue) {
        this.glValue = glValue;
    }

    public int bindingTarget() {
        return switch (this) {
            case TEX_1D -> GL11.GL_TEXTURE_BINDING_1D;
            case TEX_2D -> GL11.GL_TEXTURE_BINDING_2D;
            case TEX_3D -> GL12.GL_TEXTURE_BINDING_3D;
            case TEX_1D_ARRAY -> GL30.GL_TEXTURE_BINDING_1D_ARRAY;
            case TEX_2D_ARRAY -> GL30.GL_TEXTURE_BINDING_2D_ARRAY;
            case CUBEMAP -> GL13.GL_TEXTURE_BINDING_CUBE_MAP;
            case CUBEMAP_ARRAY -> GL40.GL_TEXTURE_BINDING_CUBE_MAP_ARRAY;
            case TEX_2D_MS -> GL32.GL_TEXTURE_BINDING_2D_MULTISAMPLE;
            case TEX_2D_MS_ARRAY -> GL32.GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY;
            case TEX_BUFFER -> GL31.GL_TEXTURE_BINDING_BUFFER;
        };
    }
}
