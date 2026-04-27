package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.ImmediateFontRenderer;

public final class ImmediateServices {

    private static final ImmediateServices instance = new ImmediateServices();

    public static ImmediateServices instance() {
        return instance;
    }

    private ImmediateServices() {
    }

    private final ImmediateShaderAccess shaderAccess = new ImmediateShaderAccess();
    private final ImmediateFontRenderer fontRenderer = new ImmediateFontRenderer("");

    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }

    public ImmediateFontRenderer font() {
        return fontRenderer;
    }
}
