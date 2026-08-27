package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.simplegui.SimpleGuiRuntime;
import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextRenderer;
import com.cleanroommc.kirino.ui.simpletext.backend.FreeTypeFontHandle;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextProducer;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.lang.invoke.MethodHandle;

public final class ImmediateClientServices {

    private static final ImmediateClientServices instance = new ImmediateClientServices();

    public static ImmediateClientServices instance() {
        return instance;
    }

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final @Nullable SimpleTextRuntime textRuntime;
    private final @Nullable SimpleGuiRuntime guiRuntime;
    private final @Nullable VAO dummyVao;

    private void dispose() {
        freeTypeManager.destroy();
        if (textRuntime != null) {
            try {
                textRuntime.close();
            } catch (Exception ignored) {
            }
        }
        if (guiRuntime != null) {
            try {
                guiRuntime.close();
            } catch (Exception ignored) {
            }
        }
    }

    private ImmediateClientServices() {
        Preconditions.checkState(FMLCommonHandler.instance().getSide().isClient(),
                "ImmediateClientServices is for client side only.");

        ShutdownManager.register(this::dispose);

        shaderAccess = new ImmediateShaderAccess();
        freeTypeManager = MethodHolder.newFreeTypeManager();
        freeTypeManager.init();

        if (KirinoClientCore.GL_DEVICE_INFO.isVersionAtLeast(3, 0)) {
            AttributeLayout dummyLayout = new AttributeLayout();
            dummyLayout.push(new Stride(0));
            dummyVao = new VAO(dummyLayout, null, (VBOView[]) null);
        } else {
            dummyVao = null;
        }

        if (KirinoClientCore.GL_DEVICE_INFO.isVersionAtLeast(4, 6)) {
            Preconditions.checkNotNull(dummyVao);

            guiRuntime = new SimpleGuiRuntime(shaderAccess, dummyVao);

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
                    (context) -> new DefaultTextRenderer(
                            context,
                            new Tex2DArrayGlyphAtlas(1024, 1024),
                            context.getShaderAccess(),
                            1024),
                    (context) -> new DefaultTextProducer(context, context.getConfig().pixelSize()),
                    shaderAccess,
                    new SimpleGuiRuntime(shaderAccess, dummyVao),
                    new SimpleGuiRuntime(shaderAccess, dummyVao),
                    config,
                    new ResourceLocation("kirino:fonts/source_han_sans/source_han_sans_hw_vf.ttf"));
        } else {
            guiRuntime = null;
            textRuntime = null;
        }
    }

    /**
     * It checks if everything here is available, which requires at least GL46.
     * Feel free to access services without concerns about availability after this call.
     */
    public void assertFullAvailability() {
        Preconditions.checkState(KirinoClientCore.GL_DEVICE_INFO.isVersionAtLeast(4, 6)
                && textRuntime != null
                && guiRuntime != null
                && dummyVao != null);
    }

    //<editor-fold desc="accessors">
    @NonNull
    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }

    @NonNull
    public FreeTypeManager freetype() {
        return freeTypeManager;
    }

    public boolean textAvailable() {
        return textRuntime != null;
    }

    /**
     * It requires at least GL46. Check {@link #textAvailable()} before accessing
     * OR simply {@link #assertFullAvailability()} once.
     */
    @NonNull
    public SimpleTextRuntime text() {
        Preconditions.checkState(textRuntime != null,
                "SimpleTextRuntime requires at least GL46 to initialize. Current context: GL%s",
                KirinoClientCore.GL_DEVICE_INFO.getVersionMajor() + "" + KirinoClientCore.GL_DEVICE_INFO.getVersionMinor());

        return textRuntime;
    }

    public boolean guiAvailable() {
        return guiRuntime != null;
    }

    /**
     * It requires at least GL46. Check {@link #guiAvailable()} before accessing
     * OR simply {@link #assertFullAvailability()} once.
     */
    @NonNull
    public SimpleGuiRuntime gui() {
        Preconditions.checkState(guiRuntime != null,
                "SimpleGuiRuntime requires at least GL46 to initialize. Current context: GL%s",
                KirinoClientCore.GL_DEVICE_INFO.getVersionMajor() + "" + KirinoClientCore.GL_DEVICE_INFO.getVersionMinor());

        return guiRuntime;
    }

    public boolean dummyVaoAvailable() {
        return dummyVao != null;
    }

    /**
     * It requires at least GL30. Check {@link #dummyVaoAvailable()} before accessing
     * OR simply {@link #assertFullAvailability()} once.
     */
    @NonNull
    public VAO dummyVao() {
        Preconditions.checkState(dummyVao != null,
                "VAO requires at least GL30 to initialize. Current context: GL%s",
                KirinoClientCore.GL_DEVICE_INFO.getVersionMajor() + "" + KirinoClientCore.GL_DEVICE_INFO.getVersionMinor());

        return dummyVao;
    }
    //</editor-fold>

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
