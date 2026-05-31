package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.backend.DebugTextRenderer;
import com.cleanroommc.kirino.ui.simpletext.backend.FreeTypeFontHandle;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextProducer;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.kirino.ui.simpletext.sdf.SDFGeneratorBruteForceImpl;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

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

        ST_Config config = new ST_Config(
                ST_FontBackendType.FREE_TYPE,
                48,
                16,
                12,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);
        textRuntime = new SimpleTextRuntime(
                (rl, cfg) -> {
                    FT_Face face = freeTypeManager.load(rl, 0, cfg.pixelSize());
                    return new FreeTypeFontHandle(face);
                },
                (context) -> {
                    return new DebugTextRenderer(
                            context,
                            new SDFGeneratorBruteForceImpl(context.getConfig().sdfPadding(), context.getConfig().sdfSpread()),
                            new Tex2DGlyphAtlas(1024, 1024),
                            context.getShaderAccess());
                },
                (context) -> {
                    return new DefaultTextProducer(context, context.getConfig().pixelSize());
                },
                shaderAccess,
                config,
                new ResourceLocation("forge:fonts/jetbrains/jetbrains_mono_nl_regular.ttf"));

        AttributeLayout dummyLayout = new AttributeLayout();
        dummyLayout.push(new Stride(0));
        dummyVao = new VAO(dummyLayout, null, (VBOView[]) null);

        ShutdownManager.register(freeTypeManager::destroy);
    }

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final SimpleTextRuntime textRuntime;
    private final VAO dummyVao;

    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }

    public FreeTypeManager freetype() {
        return freeTypeManager;
    }

    public SimpleTextRuntime text() {
        return textRuntime;
    }

    public VAO dummyVao() {
        return dummyVao;
    }

    private static final class MethodHolder {
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
