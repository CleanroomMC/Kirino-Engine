package com.cleanroommc.kirino;

import com.cleanroommc.kirino.config.KirinoConfigHub;
import com.cleanroommc.kirino.config.event.KirinoOneTimeConfigEvent;
import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.platform.task.job.*;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class KirinoCommonCore {
    private KirinoCommonCore() {
    }

    public static final Logger LOGGER;
    public static final EventBus KIRINO_EVENT_BUS;
    public static final KirinoConfigHub KIRINO_CONFIG_HUB;
    private static CleanECSRuntime ECS_RUNTIME;
    public static KirinoEngine KIRINO_ENGINE;

    //<editor-fold desc="static init">
    static {
        LOGGER = LogManager.getLogger("Kirino Core");
        KIRINO_EVENT_BUS = new EventBus();

        Constructor<KirinoConfigHub> configHubCtor;
        try {
            configHubCtor = KirinoConfigHub.class.getDeclaredConstructor();
            configHubCtor.setAccessible(true);
            KIRINO_CONFIG_HUB = configHubCtor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    //</editor-fold>

    // last chance to modify the config
    public static void configEvent() {
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onKirinoOneTimeConfig = KirinoCommonCore.class.getDeclaredMethod("onKirinoOneTimeConfig", KirinoOneTimeConfigEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, KirinoOneTimeConfigEvent.class, KirinoCommonCore.class, onKirinoOneTimeConfig, Loader.instance().getMinecraftModContainer());
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register the Kirino one time config event listener.", throwable);
        }

        KIRINO_EVENT_BUS.post(new KirinoOneTimeConfigEvent());
    }

    public static void identifyMods(List<ModContainer> mods) {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        mods.add(new InjectedModContainer(new KirinoEngineModContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new KirinoECSModContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new KirinoGLModContainer(), FMLSanityChecker.fmlLocation));
    }

    public static void init() {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        LOGGER.info("---------- Kirino Common Initialization ----------");

        //<editor-fold desc="event listeners">
        // register default event listeners
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onShaderRegister = KirinoCommonCore.class.getDeclaredMethod("onShaderRegister", ShaderRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ShaderRegistrationEvent.class, KirinoCommonCore.class, onShaderRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ShaderRegistrationEvent listener.");

            Method onPostProcessingRegister = KirinoCommonCore.class.getDeclaredMethod("onPostProcessingRegister", PostProcessingRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, PostProcessingRegistrationEvent.class, KirinoCommonCore.class, onPostProcessingRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default PostProcessingRegistrationEvent listener.");
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register default event listeners.", throwable);
        }
        //</editor-fold>

        //<editor-fold desc="ecs runtime">
        LOGGER.info("Initializing ECS Runtime.");
        StopWatch stopWatch = StopWatch.createStarted();

        try {
            MethodHandle ctor = ReflectionUtils.getConstructor(CleanECSRuntime.class, EventBus.class, Logger.class);
            Preconditions.checkNotNull(ctor);

            ECS_RUNTIME = (CleanECSRuntime) ctor.invokeExact(KIRINO_EVENT_BUS, LOGGER);
        } catch (Throwable throwable) {
            throw new RuntimeException("ECS Runtime failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("ECS Runtime Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        //</editor-fold>

        //<editor-fold desc="kirino engine">
        LOGGER.info("Initializing Kirino Engine.");
        stopWatch = StopWatch.createStarted();

        try {
            MethodHandle ctor = ReflectionUtils.getConstructor(KirinoEngine.class,
                    EventBus.class,
                    Logger.class,
                    CleanECSRuntime.class,
                    boolean.class,
                    boolean.class);
            Preconditions.checkNotNull(ctor);

            KIRINO_ENGINE = (KirinoEngine) ctor.invokeExact(KIRINO_EVENT_BUS, LOGGER, ECS_RUNTIME, KIRINO_CONFIG_HUB.isEnableHDR(), KIRINO_CONFIG_HUB.isEnablePostProcessing());
        } catch (Throwable throwable) {
            throw new RuntimeException("Kirino Engine failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        //</editor-fold>
    }

    public static void postInit() {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        LOGGER.info("---------- Kirino Common Post-Initialization ----------");
    }

    @SubscribeEvent
    public static void onShaderRegister(ShaderRegistrationEvent event) {
        event.register(new ResourceLocation("forge:shaders/test.vert"));
        event.register(new ResourceLocation("forge:shaders/gizmos.vert"));
        event.register(new ResourceLocation("forge:shaders/gizmos.frag"));
        event.register(new ResourceLocation("forge:shaders/post_processing.vert"));
        event.register(new ResourceLocation("forge:shaders/pp_default.frag"));
        event.register(new ResourceLocation("forge:shaders/pp_tone_mapping.frag"));
        event.register(new ResourceLocation("forge:shaders/meshlets2vertices.comp"));
        event.register(new ResourceLocation("forge:shaders/opaque_terrain.vert"));
        event.register(new ResourceLocation("forge:shaders/opaque_terrain.frag"));
    }

    // todo: abstraction
    @SubscribeEvent
    public static void onPostProcessingRegister(PostProcessingRegistrationEvent event) {
//        event.register(
//                "Tone Mapping Pass",
//                event.newShaderProgram("forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"),
//                DefaultPostProcessingPass::new);
    }

    @SubscribeEvent
    public static void onKirinoOneTimeConfig(KirinoOneTimeConfigEvent event) {
        event.getOneTimeConfig().enableRenderDelegate = true;
    }
}
