package com.cleanroommc.kirino;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.render.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.shader.event.ShaderRegistrationEvent;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;
import com.cleanroommc.kirino.engine.render.task.job.ChunkPrioritizationJob;
import com.cleanroommc.kirino.gl.GLTest;
import com.cleanroommc.kirino.gl.debug.*;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class KirinoCore {
    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();
    public static final Logger LOGGER = LogManager.getLogger("Kirino Core");
    public static final EventBus KIRINO_EVENT_BUS = new EventBus();
    public static final KirinoConfigHub KIRINO_CONFIG_HUB = new KirinoConfigHub();
    private static CleanECSRuntime ECS_RUNTIME;
    private static KirinoEngine KIRINO_ENGINE;
    private static boolean UNSUPPORTED = false;
    private static boolean FULLY_INITIALIZED = false;

    public static boolean isEnableRenderDelegate() {
        return KIRINO_CONFIG_HUB.enableRenderDelegate && !UNSUPPORTED;
    }

    //<editor-fold desc="hooks">
    /**
     * Block update hook.
     *
     * <hr>
     * <p><b><code>RenderGlobal</code> Patch</b>:</p>
     * <code>
     * public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)<br>
     * {<br>
     * &emsp;...<br>
     * &emsp;com.cleanroommc.kirino.KirinoCore.RenderGlobal$notifyBlockUpdate(k1, l1, i2, oldState, newState);<br>
     * }<br>
     * </code>
     */
    public static void RenderGlobal$notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {
        if (!FULLY_INITIALIZED) {
            return;
        }

        KIRINO_ENGINE.renderingCoordinator.scene.notifyBlockUpdate(x, y, z, oldState, newState);
    }

    /**
     * Light update hook.
     *
     * <hr>
     * <p><b><code>RenderGlobal</code> Patch</b>:</p>
     * <code>
     * public void notifyLightSet(BlockPos pos)<br>
     * {<br>
     * &emsp;...<br>
     * &emsp;com.cleanroommc.kirino.KirinoCore.RenderGlobal$notifyLightUpdate(pos.getX(), pos.getY(), pos.getZ());<br>
     * }<br>
     * </code>
     */
    public static void RenderGlobal$notifyLightUpdate(int x, int y, int z) {
        if (!FULLY_INITIALIZED) {
            return;
        }

        KIRINO_ENGINE.renderingCoordinator.scene.notifyLightUpdate(x, y, z);
    }
    //</editor-fold>

    /**
     * This method is a direct replacement of {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)}.
     * Specifically, <code>anaglyph</code> logic is removed and all other functions remain the same.
     * <code>anaglyph</code> can be easily added back via post-processing by the way.
     *
     * <hr>
     * <p><b><code>EntityRenderer</code> Patch</b>:</p>
     * <code>
     * public void updateCameraAndRender(float partialTicks, long nanoTime)<br>
     * {<br>
     * &emsp;...<br>
     * &emsp;if (com.cleanroommc.kirino.KirinoCore.isEnableRenderDelegate())<br>
     * &emsp;{<br>
     * &emsp;&emsp;com.cleanroommc.kirino.KirinoCore.EntityRenderer$renderWorld(System.nanoTime() + l);<br>
     * &emsp;}<br>
     * &emsp;else<br>
     * &emsp;{<br>
     * &emsp;&emsp;this.renderWorld(partialTicks, System.nanoTime() + l);<br>
     * &emsp;}<br>
     * &emsp;...<br>
     * }<br>
     * </code>
     */
    public static void EntityRenderer$renderWorld(long finishTimeNano) {
        if (!FULLY_INITIALIZED) {
            return;
        }

        KIRINO_ENGINE.renderingCoordinator.preUpdate();

        //<editor-fold desc="vanilla logic">
        KIRINO_ENGINE.renderingCoordinator.camera.getProjectionBuffer().clear();
        KIRINO_ENGINE.renderingCoordinator.camera.getViewRotationBuffer().clear();
        float partialTicks = (float) KIRINO_ENGINE.renderingCoordinator.camera.getPartialTicks();
        MethodHolder.updateLightmap(MINECRAFT.entityRenderer, partialTicks);
        if (MINECRAFT.getRenderViewEntity() == null) {
            MINECRAFT.setRenderViewEntity(Minecraft.getMinecraft().player);
        }
        MINECRAFT.entityRenderer.getMouseOver(partialTicks);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.5F);
        GlStateManager.enableCull();

        // ========== clear ==========
        // note: update fog color; bottom part of the sky
        MINECRAFT.profiler.startSection("clear");
        MethodHolder.updateFogColor(MINECRAFT.entityRenderer, partialTicks);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // ========== camera ==========
        MINECRAFT.profiler.endStartSection("camera");
        MethodHolder.setupCameraTransform(MINECRAFT.entityRenderer, partialTicks, 2);
        ActiveRenderInfo.updateRenderInfo(MINECRAFT.getRenderViewEntity(), MINECRAFT.gameSettings.thirdPersonView == 2);

        // ========== frustum ==========
        MINECRAFT.profiler.endStartSection("frustum");
        ClippingHelperImpl.getInstance();

        // ========== culling ==========
        MINECRAFT.profiler.endStartSection("culling");
        Entity renderViewEntity = MINECRAFT.getRenderViewEntity();
        double d0 = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * (double) partialTicks;
        double d1 = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * (double) partialTicks;
        double d2 = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * (double) partialTicks;
        ICamera cameraFrustum = new Frustum();
        cameraFrustum.setPosition(d0, d1, d2);

        // ========== sky ==========
        MINECRAFT.profiler.endStartSection("sky");
        // note: sun and moon etc.
        if (MINECRAFT.gameSettings.renderDistanceChunks >= 4) {
            MethodHolder.setupFog(MINECRAFT.entityRenderer, -1, partialTicks);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float fovModifier = MethodHolder.getFOVModifier(MINECRAFT.entityRenderer, partialTicks, true);
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, MethodHolder.getFarPlaneDistance(MINECRAFT.entityRenderer) * 2.0F);
            GlStateManager.matrixMode(5888);
            MINECRAFT.renderGlobal.renderSky(partialTicks, 2);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, MethodHolder.getFarPlaneDistance(MINECRAFT.entityRenderer) * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);
        }

        // note: cloud
        MethodHolder.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);
        GlStateManager.shadeModel(7425);
        if (MINECRAFT.getRenderViewEntity().posY + (double) MINECRAFT.getRenderViewEntity().getEyeHeight() < 128.0D) {
            MethodHolder.renderCloudsCheck(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
        }
        MINECRAFT.profiler.endSection();

        // note: skybox and basic stuff are done
        //</editor-fold>

        KIRINO_ENGINE.renderingCoordinator.update();
        KIRINO_ENGINE.renderingCoordinator.updateWorld(MINECRAFT.world);
//        KIRINO_ENGINE.renderingCoordinator.runChunkPass();

        //<editor-fold desc="vanilla logic">
        KIRINO_ENGINE.renderingCoordinator.cullingPatch.collectEntitiesInView(
                renderViewEntity,
                cameraFrustum,
                MINECRAFT.world.getChunkProvider(),
                partialTicks);

        boolean flag = MethodHolder.isDrawBlockOutline(MINECRAFT.entityRenderer);

        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        // note: default value of debugView == false
        if (!MethodHolder.isDebugView(MINECRAFT.entityRenderer)) {
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(0);
            KIRINO_ENGINE.renderingCoordinator.entityRenderingPatch.renderEntities(
                    MINECRAFT.getRenderViewEntity(),
                    MINECRAFT.pointedEntity,
                    MINECRAFT.player,
                    cameraFrustum,
                    MINECRAFT.gameSettings,
                    MINECRAFT.world,
                    MINECRAFT.fontRenderer,
                    MINECRAFT.getRenderManager(),
                    MINECRAFT.entityRenderer,
                    partialTicks,
                    MinecraftForgeClient.getRenderPass());
            KIRINO_ENGINE.renderingCoordinator.tesrRenderingPatch.renderTESRs(
                    MINECRAFT.getRenderViewEntity(),
                    cameraFrustum,
                    MINECRAFT.world,
                    MINECRAFT.fontRenderer,
                    MINECRAFT.entityRenderer,
                    MINECRAFT.getTextureManager(),
                    MINECRAFT.objectMouseOver,
                    MINECRAFT.renderGlobal,
                    partialTicks,
                    MinecraftForgeClient.getRenderPass());
            ForgeHooksClient.setRenderPass(0);
            RenderHelper.disableStandardItemLighting();
            MINECRAFT.entityRenderer.disableLightmap();
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
        }
        GlStateManager.matrixMode(5888);

        // ========== outline ==========
        MINECRAFT.profiler.endStartSection("outline");
        // note: block select box; on by default
        if (flag && MINECRAFT.objectMouseOver != null && !renderViewEntity.isInsideOfMaterial(Material.WATER)) {
            EntityPlayer entityplayer = (EntityPlayer) renderViewEntity;
            GlStateManager.disableAlpha();
            if (!ForgeHooksClient.onDrawBlockHighlight(MINECRAFT.renderGlobal, entityplayer, MINECRAFT.objectMouseOver, 0, partialTicks)) {
                MINECRAFT.renderGlobal.drawSelectionBox(entityplayer, MINECRAFT.objectMouseOver, 0, partialTicks);
            }
            GlStateManager.enableAlpha();
        }
        // note: debug visuals; off by default
        if (MINECRAFT.debugRenderer.shouldRender()) {
            MINECRAFT.debugRenderer.renderDebug(partialTicks, finishTimeNano);
        }

        // ========== destroyProgress ==========
        MINECRAFT.profiler.endStartSection("destroyProgress");
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        MINECRAFT.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
        MINECRAFT.renderGlobal.drawBlockDamageTexture(Tessellator.getInstance(), Tessellator.getInstance().getBuffer(), renderViewEntity, partialTicks);
        MINECRAFT.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        GlStateManager.disableBlend();
        MINECRAFT.profiler.endSection();

        // note: default value of debugView == false
        if (!MethodHolder.isDebugView(MINECRAFT.entityRenderer)) {
            // ========== litParticles ==========
            MINECRAFT.profiler.startSection("litParticles");
            MINECRAFT.entityRenderer.enableLightmap();
            MINECRAFT.effectRenderer.renderLitParticles(renderViewEntity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            MethodHolder.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);

            // ========== particles ==========
            MINECRAFT.profiler.endStartSection("particles");
            MINECRAFT.effectRenderer.renderParticles(renderViewEntity, partialTicks);
            MINECRAFT.entityRenderer.disableLightmap();
            MINECRAFT.profiler.endSection();
        }

        // ========== weather ==========
        MINECRAFT.profiler.startSection("weather");
        // note: weather like rain etc.
        GlStateManager.depthMask(false);
        GlStateManager.enableCull();
        MethodHolder.renderRainSnow(MINECRAFT.entityRenderer, partialTicks);
        GlStateManager.depthMask(true);
        MINECRAFT.renderGlobal.renderWorldBorder(renderViewEntity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        MethodHolder.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        MINECRAFT.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        MINECRAFT.profiler.endSection();
        //</editor-fold>

//        KIRINO_ENGINE.renderingCoordinator.renderWorldTransparent();

        KIRINO_ENGINE.renderingCoordinator.runGizmosPass();

        //<editor-fold desc="vanilla logic">
        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        // note: default value of debugView == false
        if (!MethodHolder.isDebugView(MINECRAFT.entityRenderer)) {
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(1);
            KIRINO_ENGINE.renderingCoordinator.entityRenderingPatch.renderEntities(
                    MINECRAFT.getRenderViewEntity(),
                    MINECRAFT.pointedEntity,
                    MINECRAFT.player,
                    cameraFrustum,
                    MINECRAFT.gameSettings,
                    MINECRAFT.world,
                    MINECRAFT.fontRenderer,
                    MINECRAFT.getRenderManager(),
                    MINECRAFT.entityRenderer,
                    partialTicks,
                    MinecraftForgeClient.getRenderPass());
            KIRINO_ENGINE.renderingCoordinator.tesrRenderingPatch.renderTESRs(
                    MINECRAFT.getRenderViewEntity(),
                    cameraFrustum,
                    MINECRAFT.world,
                    MINECRAFT.fontRenderer,
                    MINECRAFT.entityRenderer,
                    MINECRAFT.getTextureManager(),
                    MINECRAFT.objectMouseOver,
                    MINECRAFT.renderGlobal,
                    partialTicks,
                    MinecraftForgeClient.getRenderPass());
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            ForgeHooksClient.setRenderPass(-1);
            RenderHelper.disableStandardItemLighting();
        }
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();

        // ========== aboveClouds ==========
        MINECRAFT.profiler.endStartSection("aboveClouds");
        if (renderViewEntity.posY + (double) renderViewEntity.getEyeHeight() >= 128.0D) {
            MethodHolder.renderCloudsCheck(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
        }

        // ========== forge_render_last ==========
        MINECRAFT.profiler.endStartSection("forge_render_last");
        ForgeHooksClient.dispatchRenderLast(MINECRAFT.renderGlobal, partialTicks);

        // ========== hand ==========
        MINECRAFT.profiler.endStartSection("hand");
        if (MethodHolder.isRenderHand(MINECRAFT.entityRenderer)) {
            GlStateManager.clear(256);
            MethodHolder.renderHand(MINECRAFT.entityRenderer, partialTicks, 2);
        }
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        KIRINO_ENGINE.renderingCoordinator.postUpdate();
    }

    public static void init() {
        if (!KIRINO_CONFIG_HUB.enable) {
            KIRINO_CONFIG_HUB.enableRenderDelegate = false;
            return;
        }

        LOGGER.info("KirinoCore Initialization Stage");

        //<editor-fold desc="gl version check">
        String rawGLVersion = GL11.glGetString(GL11.GL_VERSION);
        int majorGLVersion = -1;
        int minorGLVersion = -1;

        if (rawGLVersion != null) {
            String[] parts = rawGLVersion.split("\\s+")[0].split("\\.");
            if (parts.length >= 2) {
                try {
                    majorGLVersion = Integer.parseInt(parts[0]);
                    minorGLVersion = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        } else {
            rawGLVersion = "";
        }

        LOGGER.info("OpenGL version: {}", rawGLVersion);

        if (rawGLVersion.isEmpty() || majorGLVersion == -1 || minorGLVersion == -1) {
            UNSUPPORTED = true;
            KIRINO_CONFIG_HUB.enable = false;
            KIRINO_CONFIG_HUB.enableRenderDelegate = false;
            LOGGER.warn("Failed to parse the OpenGL version. Aborting Kirino initialization.");
            return;
        }

        if (!(majorGLVersion == 4 && minorGLVersion == 6)) {
            UNSUPPORTED = true;
            KIRINO_CONFIG_HUB.enable = false;
            KIRINO_CONFIG_HUB.enableRenderDelegate = false;
            LOGGER.warn("OpenGL 4.6 not supported. Aborting Kirino initialization.");
            return;
        }
        //</editor-fold>

        KHRDebug.enable(LOGGER, List.of(
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.ERROR, DebugMsgSeverity.ANY),
                new DebugMessageFilter(DebugMsgSource.ANY, DebugMsgType.MARKER, DebugMsgSeverity.ANY)));

        GLTest.test();

        //<editor-fold desc="event listeners">
        // register default event listeners
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onStructScan = KirinoCore.class.getDeclaredMethod("onStructScan", StructScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, StructScanningEvent.class, KirinoCore.class, onStructScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default StructScanningEvent listener.");

            Method onComponentScan = KirinoCore.class.getDeclaredMethod("onComponentScan", ComponentScanningEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ComponentScanningEvent.class, KirinoCore.class, onComponentScan, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ComponentScanningEvent listener.");

            Method onShaderRegister = KirinoCore.class.getDeclaredMethod("onShaderRegister", ShaderRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, ShaderRegistrationEvent.class, KirinoCore.class, onShaderRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default ShaderRegistrationEvent listener.");

            Method onJobRegister = KirinoCore.class.getDeclaredMethod("onJobRegister", JobRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, JobRegistrationEvent.class, KirinoCore.class, onJobRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default JobRegistrationEvent listener.");

            Method onPostProcessingRegister = KirinoCore.class.getDeclaredMethod("onPostProcessingRegister", PostProcessingRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, PostProcessingRegistrationEvent.class, KirinoCore.class, onPostProcessingRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default PostProcessingRegistrationEvent listener.");
        } catch (Throwable throwable) {
            throw new RuntimeException("Failed to register default event listeners.", throwable);
        }
        //</editor-fold>

        //<editor-fold desc="ecs runtime">
        LOGGER.info("---------------");
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
        LOGGER.info("---------------");
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

            KIRINO_ENGINE = (KirinoEngine) ctor.invokeExact(KIRINO_EVENT_BUS, LOGGER, ECS_RUNTIME, KIRINO_CONFIG_HUB.enableHDR, KIRINO_CONFIG_HUB.enablePostProcessing);
        } catch (Throwable throwable) {
            throw new RuntimeException("Kirino Engine failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        LOGGER.info("---------------");
        //</editor-fold>
    }

    public static void postInit() {
        if (!KIRINO_CONFIG_HUB.enable) {
            return;
        }

        LOGGER.info("KirinoCore Post-Initialization Stage");

        //<editor-fold desc="kirino engine">
        LOGGER.info("---------------");
        LOGGER.info("Post-Initializing Kirino Engine.");
        StopWatch stopWatch = StopWatch.createStarted();

        KIRINO_ENGINE.renderingCoordinator.deferredInit();

        stopWatch.stop();
        LOGGER.info("Kirino Engine Post-Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        LOGGER.info("---------------");
        //</editor-fold>

        FULLY_INITIALIZED = true;
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.geometry");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.geometry.component");
    }

    @SubscribeEvent
    public static void onJobRegister(JobRegistrationEvent event) {
        event.register(ChunkMeshletGenJob.class);
        event.register(ChunkPrioritizationJob.class);
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
    }

    @SubscribeEvent
    public static void onPostProcessingRegister(PostProcessingRegistrationEvent event) {
//        event.register(
//                "Tone Mapping Pass",
//                event.newShaderProgram("forge:shaders/post_processing.vert", "forge:shaders/pp_tone_mapping.frag"),
//                DefaultPostProcessingPass::new);
    }

    //<editor-fold desc="reflection">
    /**
     * Holder class to initialize-on-demand necessary method handles.
     */
    private static class MethodHolder {
        static final EntityRendererDelegate DELEGATE;

        static {
            DELEGATE = new EntityRendererDelegate(
                    ReflectionUtils.getMethod(EntityRenderer.class, "setupCameraTransform", "func_78479_a(FI)V", void.class, float.class, int.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "updateFogColor", "func_78466_h(F)V", void.class, float.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "setupFog", "func_78468_a(IF)V", void.class, int.class, float.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "getFOVModifier", "func_78481_a(FZ)F", float.class, float.class, boolean.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "renderCloudsCheck", "func_180437_a(Lnet/minecraft/client/renderer/RenderGlobal,FIDDD)V", void.class, RenderGlobal.class, float.class, int.class, double.class, double.class, double.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "isDrawBlockOutline", "func_175070_n()Z", boolean.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "updateLightmap", "func_78472_g(F)V", void.class, float.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "renderRainSnow", "func_78474_d(F)V", void.class, float.class),
                    ReflectionUtils.getMethod(EntityRenderer.class, "renderHand", "func_78476_b(FI)V", void.class, float.class, int.class),
                    ReflectionUtils.getFieldGetter(EntityRenderer.class, "farPlaneDistance", "field_78530_s", float.class),
                    ReflectionUtils.getFieldGetter(EntityRenderer.class, "debugView", "field_175078_W", boolean.class),
                    ReflectionUtils.getFieldGetter(EntityRenderer.class, "renderHand", "field_175074_C", boolean.class));

            Preconditions.checkNotNull(DELEGATE.setupCameraTransform);
            Preconditions.checkNotNull(DELEGATE.updateFogColor);
            Preconditions.checkNotNull(DELEGATE.setupFog);
            Preconditions.checkNotNull(DELEGATE.getFOVModifier);
            Preconditions.checkNotNull(DELEGATE.renderCloudsCheck);
            Preconditions.checkNotNull(DELEGATE.isDrawBlockOutline);
            Preconditions.checkNotNull(DELEGATE.updateLightmap);
            Preconditions.checkNotNull(DELEGATE.renderRainSnow);
            Preconditions.checkNotNull(DELEGATE.renderHand);
            Preconditions.checkNotNull(DELEGATE.farPlaneDistance);
            Preconditions.checkNotNull(DELEGATE.debugView);
            Preconditions.checkNotNull(DELEGATE.isRenderHand);
        }

        /**
         * @see EntityRenderer#setupCameraTransform(float, int)
         */
        @SuppressWarnings("SameParameterValue")
        static void setupCameraTransform(EntityRenderer instance, float partialTicks, int pass) {
            try {
                DELEGATE.setupCameraTransform().invokeExact(instance, partialTicks, pass);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#updateFogColor(float)
         */
        static void updateFogColor(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.updateFogColor().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#setupFog(int, float)
         */
        static void setupFog(EntityRenderer instance, int startCoords, float partialTicks) {
            try {
                DELEGATE.setupFog().invokeExact(instance, startCoords, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#getFOVModifier(float, boolean)
         */
        @SuppressWarnings("SameParameterValue")
        static float getFOVModifier(EntityRenderer instance, float partialTicks, boolean useFOVSetting) {
            try {
                return (float) DELEGATE.getFOVModifier().invokeExact(instance, partialTicks, useFOVSetting);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#renderCloudsCheck(RenderGlobal, float, int, double, double, double)
         */
        @SuppressWarnings("SameParameterValue")
        static void renderCloudsCheck(EntityRenderer instance, RenderGlobal renderGlobalIn, float partialTicks, int pass, double x, double y, double z) {
            try {
                DELEGATE.renderCloudsCheck().invokeExact(instance, renderGlobalIn, partialTicks, pass, x, y, z);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#isDrawBlockOutline()
         */
        static boolean isDrawBlockOutline(EntityRenderer instance) {
            try {
                return (boolean) DELEGATE.isDrawBlockOutline().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#updateLightmap(float)
         */
        static void updateLightmap(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.updateLightmap().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#renderRainSnow(float)
         */
        static void renderRainSnow(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.renderRainSnow().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#renderHand(float, int)
         */
        @SuppressWarnings("SameParameterValue")
        static void renderHand(EntityRenderer instance, float partialTicks, int pass) {
            try {
                DELEGATE.renderHand().invokeExact(instance, partialTicks, pass);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#farPlaneDistance
         */
        static float getFarPlaneDistance(EntityRenderer instance) {
            try {
                return (float) DELEGATE.farPlaneDistance().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#debugView
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        static boolean isDebugView(EntityRenderer instance) {
            try {
                return (boolean) DELEGATE.debugView().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * @see EntityRenderer#renderHand
         */
        static boolean isRenderHand(EntityRenderer instance) {
            try {
                return (boolean) DELEGATE.isRenderHand().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Holds necessary handles for EntityRenderer methods.
         */
        record EntityRendererDelegate(
                MethodHandle setupCameraTransform,
                MethodHandle updateFogColor,
                MethodHandle setupFog,
                MethodHandle getFOVModifier,
                MethodHandle renderCloudsCheck,
                MethodHandle isDrawBlockOutline,
                MethodHandle updateLightmap,
                MethodHandle renderRainSnow,
                MethodHandle renderHand,
                MethodHandle farPlaneDistance,
                MethodHandle debugView,
                MethodHandle isRenderHand) {
        }
    }
    //</editor-fold>
}
