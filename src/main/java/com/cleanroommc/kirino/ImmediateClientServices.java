package com.cleanroommc.kirino;

import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.font.McTtfFontManager;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.TimeUnit;

/**
 * This class must be accessed on the GL thread, and {@link #initAndWarmUp(IReloadableResourceManager)}
 * must be the first reference to it that triggers class loading.
 * Specifically, never access it earlier than Minecraft IResourceManager constructor call which
 * is right before the start of the Splash process.
 */
public final class ImmediateClientServices {

    private static final Logger LOGGER = LogManager.getLogger("Kirino ICS");

    private static final ImmediateClientServices instance = new ImmediateClientServices();

    @SuppressWarnings("deprecation")
    private static final class McFontReloadListener implements IResourceManagerReloadListener {

        private final boolean isSimpleReloadableResourceManager;
        private boolean firstCall = true;

        private final ImmediateClientServices instance;

        private McFontReloadListener(
                ImmediateClientServices instance,
                IReloadableResourceManager resourceManager) {

            this.instance = instance;
            isSimpleReloadableResourceManager = resourceManager instanceof SimpleReloadableResourceManager;
        }

        @Override
        public void onResourceManagerReload(IResourceManager var1) {
            // SimpleReloadableResourceManager fires the reload listener immediately on register
            if (isSimpleReloadableResourceManager && firstCall) {
                firstCall = false;
                return;
            }

            // todo
        }
    }

    /**
     * It initializes and warms up the ICS instance.
     *
     * <p>Note: Must only call it on the GL thread.</p>
     * <p>Note: This is accessed by hooks. Must not be called by clients!</p>
     * <p>Note: It'll be called right after the Minecraft IResourceManager constructor call.</p>
     * <p>Note: This is supposed to be and must be the first {@link ImmediateClientServices} call and
     * must be the call that triggers class loading.</p>
     */
    public static void initAndWarmUp(
            @NonNull IReloadableResourceManager resourceManager) {

        Preconditions.checkNotNull(resourceManager,
                "Minecraft resource manager is supposed to be ready.");
        Preconditions.checkState(resourceManager == Minecraft.getMinecraft().getResourceManager());

        StopWatch stopWatch = StopWatch.createStarted();

        instance.mcFontManager.enableResourcePackAssetSource();

        // it'll be the call that triggers the actual font loading
        SimpleTextRuntime[] out = new SimpleTextRuntime[1];
        boolean textVanillaAvailable = instance.tryLoadTextRuntimeVanilla(false, out);

        instance.mcFontManager.enableResourceManagerAssetSource();

        // register reload listener only if text vanilla is available
        if (textVanillaAvailable) {
            resourceManager.registerReloadListener(new McFontReloadListener(
                    instance,
                    resourceManager));
        }

        LOGGER.info("Module \"text\" availability: {}", instance.textAvailable() ? "TRUE" : "FALSE");
        LOGGER.info("Module \"gui\" availability: {}", instance.guiAvailable() ? "TRUE" : "FALSE");
        LOGGER.info("Module \"dummyVao\" availability: {}", instance.dummyVaoAvailable() ? "TRUE" : "FALSE");
        LOGGER.info("Module \"textVanilla\" availability: {}", textVanillaAvailable ? "TRUE" : "FALSE");

        stopWatch.stop();
        LOGGER.info("Finished initializing ICS. Time taken: {}ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * <p>Note: Must only use it on the GL thread.</p>
     */
    @NonNull
    public static ImmediateClientServices instance() {
        return instance;
    }

    private final ImmediateShaderAccess shaderAccess;
    private final FreeTypeManager freeTypeManager;
    private final @Nullable SimpleTextRuntime textRuntime;
    private final @Nullable SimpleGuiRuntime guiRuntime;
    private final @Nullable VAO dummyVao;

    private @Nullable SimpleTextRuntime textRuntimeVanilla = null;
    private final McTtfFontManager mcFontManager;

    private void dispose() {
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
        if (textRuntimeVanilla != null) {
            try {
                textRuntimeVanilla.close();
            } catch (Exception ignored) {
            }
        }
        mcFontManager.destroy();
        freeTypeManager.destroy();
    }

    private ImmediateClientServices() {
        LOGGER.info("Starts constructing ImmediateClientServices.");

        shaderAccess = new ImmediateShaderAccess();
        freeTypeManager = MethodHolder.newFreeTypeManager();
        freeTypeManager.init();

        mcFontManager = new McTtfFontManager(
                Minecraft.getMinecraft().getResourceManager(),
                Minecraft.getMinecraft().defaultResourcePack,
                freeTypeManager);

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
                        Preconditions.checkNotNull(rl);

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

        ShutdownManager.register(this::dispose);

        LOGGER.info("Finished constructing ImmediateClientServices.");
    }

    /**
     * Call {@link #tryLoadTextRuntimeVanilla(boolean, SimpleTextRuntime[])} first.
     * Once it returns <code>true</code>, this function is safe to access directly
     * for the rest of the program lifetime.
     *
     * <p>Note: Never cache the result since backend instance might be replaced by reloading.</p>
     *
     * <p>Note: <i><b>This is a borrowed runtime. Must not <code>close</code>!</b></i></p>
     */
    @NonNull
    public SimpleTextRuntime textVanilla() {
        Preconditions.checkState(textRuntimeVanilla != null,
                "Text runtime vanilla unavailable!");

        return textRuntimeVanilla;
    }

    /**
     * <p>Note: Never cache the result since <code>reload</code> replaces the backend instance.
     * Accessing this function directly is relatively cheap even for hot paths.
     * However, the call that actually triggers loading takes very long.</p>
     *
     * <p>Note: <i><b>The out parameter is a borrowed runtime. Must not <code>close</code>!</b></i></p>
     * <p>Note: {@link #mcFontManager} is an implicit dependency here.</p>
     *
     * @return <code>false</code> means the text runtime is unavailable for the entire program lifetime,
     *         and <code>reload</code> cannot make it available.<br>
     *         Once <code>true</code> is returned, all subsequent calls that complete normally
     *         will also return <code>true</code>, including calls with <code>reload</code>.
     *
     * @see #textVanilla()
     */
    public boolean tryLoadTextRuntimeVanilla(
            boolean reload,
            @Nullable SimpleTextRuntime @NonNull [] outTextRuntime) {

        Preconditions.checkNotNull(outTextRuntime);
        Preconditions.checkArgument(outTextRuntime.length == 1,
                "Argument \"outTextRuntime\"'s length must be one.");

        if (!reload && textRuntimeVanilla != null) {
            outTextRuntime[0] = textRuntimeVanilla;
            return true;
        }

        if (!KirinoClientCore.GL_DEVICE_INFO.isVersionAtLeast(4, 6)) {
            outTextRuntime[0] = null;
            return false;
        }

        Preconditions.checkNotNull(dummyVao);

        if (reload) {
            if (textRuntimeVanilla != null) {
                try {
                    textRuntimeVanilla.close();
                } catch (Exception ignored) {
                } finally {
                    textRuntimeVanilla = null;
                }
            }
            mcFontManager.invalidate();
        }

        ST_Config config = new ST_Config(
                ST_FontBackendType.FREE_TYPE,
                48,
                16,
                12,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

        textRuntimeVanilla = new SimpleTextRuntime(
                (rl, cfg) -> {
                    FT_Face face = mcFontManager.loadDefaultFace(cfg.pixelSize());
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
                null);

        outTextRuntime[0] = textRuntimeVanilla;
        return true;
    }

    /**
     * It checks if all conditioned modules here are available, which requires at least GL46.
     * Feel free to access services without concerns about availability after this call.
     *
     * <p>Specifically, it checks availability for:</p>
     * <ul>
     *     <li>{@link #text()}</li>
     *     <li>{@link #gui()}</li>
     *     <li>{@link #dummyVao()}</li>
     * </ul>
     *
     * <p>Note: It doesn't include {@link #textVanilla()}.</p>
     */
    public void assertFullAvailability() {
        Preconditions.checkState(KirinoClientCore.GL_DEVICE_INFO.isVersionAtLeast(4, 6)
                && textRuntime != null
                && guiRuntime != null
                && dummyVao != null);
    }

    //<editor-fold desc="accessors">

    /**
     * <p>Note: <i><b>This is a borrowed runtime.</b></i></p>
     */
    @NonNull
    public ImmediateShaderAccess shader() {
        return shaderAccess;
    }

    /**
     * <p>Note: <i><b>This is a borrowed runtime.</b></i></p>
     */
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
     *
     * <p>Note: <i><b>This is a borrowed runtime. Must not <code>close</code>!</b></i></p>
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
     *
     * <p>Note: <i><b>This is a borrowed runtime. Must not <code>close</code>!</b></i></p>
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
     *
     * <p>Note: <i><b>This is a borrowed runtime.</b></i></p>
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
