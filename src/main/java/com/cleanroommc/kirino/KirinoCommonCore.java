package com.cleanroommc.kirino;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.EngineInitParams;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.builtin.DefaultPostProcessingPass;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderDebugInjection;
import com.cleanroommc.kirino.engine.render.core.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.mod.KirinoECSModContainer;
import com.cleanroommc.kirino.mod.KirinoEngineModContainer;
import com.cleanroommc.kirino.mod.KirinoGLModContainer;
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class KirinoCommonCore {

    private KirinoCommonCore() {
    }

    public static final Logger LOGGER;
    public static final EventBus KIRINO_EVENT_BUS;
    private static CleanECSRuntime ECS_RUNTIME;
    public static KirinoEngine KIRINO_ENGINE;

    //<editor-fold desc="static init">
    static {
        LOGGER = LogManager.getLogger("Kirino Core");
        KIRINO_EVENT_BUS = new EventBus();
    }
    //</editor-fold>

    public static void identifyMods(List<ModContainer> mods) {
        if (!KirinoConfig.isEnabled()) {
            return;
        }

        mods.add(new InjectedModContainer(new KirinoEngineModContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new KirinoECSModContainer(), FMLSanityChecker.fmlLocation));
        mods.add(new InjectedModContainer(new KirinoGLModContainer(), FMLSanityChecker.fmlLocation));
    }

    public static void init() {
        if (!KirinoConfig.isEnabled()) {
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
            Constructor<CleanECSRuntime> ctor = CleanECSRuntime.class.getDeclaredConstructor(EventBus.class, Logger.class);
            Preconditions.checkNotNull(ctor);

            ctor.setAccessible(true);

            ECS_RUNTIME = ctor.newInstance(KIRINO_EVENT_BUS, LOGGER);
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
            Constructor<KirinoEngine> ctor = KirinoEngine.class.getDeclaredConstructor(
                    EventBus.class,
                    Logger.class,
                    CleanECSRuntime.class,
                    EngineInitParams.class);
            Preconditions.checkNotNull(ctor);

            ctor.setAccessible(true);

            EngineInitParams params = new EngineInitParams(
                    KirinoConfig.NEEDS_RESTART.enableHDR,
                    KirinoConfig.NEEDS_RESTART.enablePostProcessing,
                    KirinoConfig.NEEDS_RESTART.enableKhrDebug,
                    KirinoConfig.NEEDS_RESTART.enableShaderDebug,
                    KirinoConfig.NEEDS_RESTART.postProcessingSchedule);

            KIRINO_ENGINE = ctor.newInstance(KIRINO_EVENT_BUS, LOGGER, ECS_RUNTIME, params);
        } catch (Throwable throwable) {
            throw new RuntimeException("Kirino Engine failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        //</editor-fold>
    }

    public static void postInit() {
        if (!KirinoConfig.isEnabled()) {
            return;
        }

        LOGGER.info("---------- Kirino Common Post-Initialization ----------");
    }

    @SubscribeEvent
    public static void onShaderRegister(ShaderRegistrationEvent event) {
        event.register(new ResourceLocation("kirino:shaders/test.vert"));
        event.register(new ResourceLocation("kirino:shaders/gizmos.vert"));
        event.register(new ResourceLocation("kirino:shaders/gizmos.frag"));
        event.register(new ResourceLocation("kirino:shaders/post_processing.vert"));
        event.register(new ResourceLocation("kirino:shaders/pp_default.frag"));
        event.register(new ResourceLocation("kirino:shaders/pp_tone_mapping.frag"));
        event.register(new ResourceLocation("kirino:shaders/meshlets2vertices.comp"), ShaderDebugInjection.VEC3F_DEBUG);
        event.register(new ResourceLocation("kirino:shaders/meshlet_draw_index_gen.comp"));
        event.register(new ResourceLocation("kirino:shaders/opaque_terrain.vert"), ShaderDebugInjection.VEC3F_DEBUG);
        event.register(new ResourceLocation("kirino:shaders/opaque_terrain.frag"));
    }

    @SubscribeEvent
    public static void onPostProcessingRegister(PostProcessingRegistrationEvent event) {
        event.register(
                "Blit Pass",
                new String[]{"kirino:shaders/post_processing.vert", "kirino:shaders/pp_default.frag"},
                DefaultPostProcessingPass::new);
        event.register(
                "Tone Mapping",
                new String[]{"kirino:shaders/post_processing.vert", "kirino:shaders/pp_tone_mapping.frag"},
                DefaultPostProcessingPass::new);
    }

}
