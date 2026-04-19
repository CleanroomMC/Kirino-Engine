package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;

public final class ImmediateServices {

    private static final ImmediateServices instance = new ImmediateServices();

    public static ImmediateServices instance() {
        return instance;
    }

    private ImmediateServices() {
    }

    private final ImmediateShaderAccess shaderAccess = new ImmediateShaderAccess();

    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }
}
