package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.simpletext.freetype.FreeTypeManager;
import com.cleanroommc.kirino.simpletext.SimpleTextRenderer;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;

import java.lang.invoke.MethodHandle;

public final class ImmediateClientServices {

    private static final ImmediateClientServices instance = new ImmediateClientServices();

    public static ImmediateClientServices instance() {
        return instance;
    }

    private ImmediateClientServices() {
        shaderAccess = new ImmediateShaderAccess();
        freeTypeManager = MethodHolder.newFreeTypeManager();
        freeTypeManager.init();
        textRenderer = new SimpleTextRenderer(
                shaderAccess,
                freeTypeManager,
                new ResourceLocation("forge:fonts/jetbrains/jetbrains_mono_nl_regular.ttf"));
//        freeTypeManager.destroy();
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

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(ReflectionUtils.getConstructor(FreeTypeManager.class));

            Preconditions.checkNotNull(DELEGATE.freeTypeManagerCtor);
        }

        static FreeTypeManager newFreeTypeManager() {
            try {
                return (FreeTypeManager) DELEGATE.freeTypeManagerCtor.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record Delegate(MethodHandle freeTypeManagerCtor) {
        }
    }
}
