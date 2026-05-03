package com.cleanroommc.kirino.simpletext;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.shader.Shader;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.*;
import org.lwjgl.util.freetype.FT_Face;

/**
 * This is a simple text renderer that is highly coupled with freetype.
 */
public class SimpleTextRenderer {

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final FT_Face face;

    public FT_Face getFreeTypeFace() {
        return face;
    }

    public SimpleTextRenderer(ImmediateShaderAccess shaderAccess, FreeTypeManager freeTypeManager, ResourceLocation rl) {
        this.shaderAccess = shaderAccess;
        this.freeTypeManager = freeTypeManager;
        face = freeTypeManager.load(rl, 0, 32);

        Shader shader = shaderAccess.makeShader(new ResourceLocation("forge:shaders/font.vert"));
        shaderAccess.submitToGL(shader);
    }
}