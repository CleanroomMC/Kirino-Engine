package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.FreeTypeManager;
import com.cleanroommc.kirino.simpletext.SimpleTextRenderer;
import net.minecraft.util.ResourceLocation;

public final class ImmediateClientServices {

    private static final ImmediateClientServices instance = new ImmediateClientServices();

    public static ImmediateClientServices instance() {
        return instance;
    }

    private ImmediateClientServices() {
        shaderAccess = new ImmediateShaderAccess();
        freeTypeManager = new FreeTypeManager();
        freeTypeManager.init();
        textRenderer = new SimpleTextRenderer(freeTypeManager,
                new ResourceLocation("forge:fonts/jetbrains/jetbrains_mono_nl_regular.ttf"));
        freeTypeManager.destroy();
    }

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final SimpleTextRenderer textRenderer;

    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }

    public FreeTypeManager freetype() {
        return freeTypeManager;
    }

    public SimpleTextRenderer text() {
        return textRenderer;
    }
}
