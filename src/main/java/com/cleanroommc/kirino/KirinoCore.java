package com.cleanroommc.kirino;

import com.cleanroommc.kirino.config.KirinoConfigHub;
import com.cleanroommc.kirino.config.event.KirinoOneTimeConfigEvent;
import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.KirinoEngine;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.core.debug.data.impl.FpsHistory;
import com.cleanroommc.kirino.engine.render.core.debug.data.impl.RenderStatsFrame;
import com.cleanroommc.kirino.engine.render.core.debug.hud.impl.FpsHUD;
import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataServiceLocator;
import com.cleanroommc.kirino.engine.render.core.debug.hud.event.DebugHUDRegistrationEvent;
import com.cleanroommc.kirino.engine.render.core.debug.hud.impl.CommonStatsHUD;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.event.PostProcessingRegistrationEvent;
import com.cleanroommc.kirino.engine.render.platform.MinecraftAssetProviders;
import com.cleanroommc.kirino.engine.render.platform.MinecraftIntegration;
import com.cleanroommc.kirino.engine.render.platform.SceneViewState;
import com.cleanroommc.kirino.engine.render.platform.task.job.*;
import com.cleanroommc.kirino.engine.render.core.shader.event.ShaderRegistrationEvent;
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
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class KirinoCore {
    private KirinoCore() {
    }

    private static final Minecraft MINECRAFT;
    public static final DebugDataServiceLocator DEBUG_SERVICE;
    public static final Logger LOGGER;
    public static final EventBus KIRINO_EVENT_BUS;
    public static final KirinoConfigHub KIRINO_CONFIG_HUB;
    private static CleanECSRuntime ECS_RUNTIME;
    public static KirinoEngine KIRINO_ENGINE;
    private static boolean RENDER_UNSUPPORTED;

    //<editor-fold desc="static init">
    static {
        MINECRAFT = Minecraft.getMinecraft();

        Constructor<DebugDataServiceLocator> debugServiceCtor;
        try {
            debugServiceCtor = DebugDataServiceLocator.class.getDeclaredConstructor();
            debugServiceCtor.setAccessible(true);
            DEBUG_SERVICE = debugServiceCtor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

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

        RENDER_UNSUPPORTED = false;
    }
    //</editor-fold>

    public static boolean isRenderUnsupported() {
        return RENDER_UNSUPPORTED;
    }

    //<editor-fold desc="vanilla source related patches">
    /**
     * Block update hook.
     *
     * <hr>
     * <p><b><code>RenderGlobal</code> Patch</b>:</p>
     * <code>
     * public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags)<br>
     * {<br>
     * &emsp;...<br>
     * &emsp;com.cleanroommc.kirino.KirinoCore.RenderGlobal$notifyBlockUpdate(i, j, k, oldState, newState);<br>
     * }<br>
     * </code>
     *
     * <br>
     * <hr>
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public static void RenderGlobal$notifyBlockUpdate(int x, int y, int z, IBlockState oldState, IBlockState newState) {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }
        if (!KIRINO_CONFIG_HUB.isEnableRenderDelegate()) {
            return;
        }
        if (RENDER_UNSUPPORTED) {
            return;
        }

        MethodHolder2.getSceneViewState(KIRINO_ENGINE).scene.notifyBlockUpdate(x, y, z, oldState, newState);
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
     *
     * <br>
     * <hr>
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public static void RenderGlobal$notifyLightUpdate(int x, int y, int z) {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }
        if (!KIRINO_CONFIG_HUB.isEnableRenderDelegate()) {
            return;
        }
        if (RENDER_UNSUPPORTED) {
            return;
        }

        MethodHolder2.getSceneViewState(KIRINO_ENGINE).scene.notifyLightUpdate(x, y, z);
    }

    /**
     * This method is an alternative of our {@link #EntityRenderer$renderWorld(long)}.
     * When the render delegate is disabled or the rendering is unsupported,
     * vanilla {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)} will take place
     * instead of our {@link #EntityRenderer$renderWorld(long)}, and this method will be injected
     * to several places of vanilla {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)}
     * to run the full engine cycle headlessly.
     *
     * <br>
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public static void runHeadlessly(FramePhase phase) {
        KIRINO_ENGINE.runHeadlessly(phase);
    }

    /**
     * This method is a direct replacement of vanilla {@link net.minecraft.client.renderer.EntityRenderer#renderWorld(float, long)}
     * <i>when the render delegate is enabled and the rendering is supported</i>.
     * Specifically, <code>anaglyph</code> logic is removed and all other functions remain the same.
     * <code>anaglyph</code> can be easily added back via post-processing by the way.
     *
     * <hr>
     * <p><b><code>EntityRenderer</code> Patch</b>:</p>
     * <code>
     * public void updateCameraAndRender(float partialTicks, long nanoTime)<br>
     * {<br>
     * &emsp;...<br>
     * &emsp;if (com.cleanroommc.kirino.KirinoCore.KIRINO_CONFIG_HUB.isEnable()<br>
     * &emsp;&emsp;&emsp;&& com.cleanroommc.kirino.KirinoCore.KIRINO_CONFIG_HUB.isEnableRenderDelegate()<br>
     * &emsp;&emsp;&emsp;&& !com.cleanroommc.kirino.KirinoCore.isRenderUnsupported())<br>
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
     *
     * <br>
     * <hr>
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public static void EntityRenderer$renderWorld(long finishTimeNano) {
        KirinoDebug.recordFps(Minecraft.getDebugFPS());
        KirinoDebug.resetDrawCalls();

        if (KIRINO_ENGINE.nextExpectedPhase() == FramePhase.PREPARE) {
            KIRINO_ENGINE.run(FramePhase.PREPARE);
        }

        KIRINO_ENGINE.run(FramePhase.PRE_UPDATE);

        //<editor-fold desc="vanilla logic">
        MethodHolder2.getSceneViewState(KIRINO_ENGINE).camera.getProjectionBuffer().clear();
        MethodHolder2.getSceneViewState(KIRINO_ENGINE).camera.getViewRotationBuffer().clear();
        float partialTicks = (float) MethodHolder2.getSceneViewState(KIRINO_ENGINE).camera.getPartialTicks();
        MethodHolder1.updateLightmap(MINECRAFT.entityRenderer, partialTicks);
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
        MethodHolder1.updateFogColor(MINECRAFT.entityRenderer, partialTicks);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // ========== camera ==========
        MINECRAFT.profiler.endStartSection("camera");
        MethodHolder1.setupCameraTransform(MINECRAFT.entityRenderer, partialTicks, 2);
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
            MethodHolder1.setupFog(MINECRAFT.entityRenderer, -1, partialTicks);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            float fovModifier = MethodHolder1.getFOVModifier(MINECRAFT.entityRenderer, partialTicks, true);
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, MethodHolder1.getFarPlaneDistance(MINECRAFT.entityRenderer) * 2.0F);
            GlStateManager.matrixMode(5888);
            MINECRAFT.renderGlobal.renderSky(partialTicks, 2);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(fovModifier, (float) MINECRAFT.displayWidth / (float) MINECRAFT.displayHeight, 0.05F, MethodHolder1.getFarPlaneDistance(MINECRAFT.entityRenderer) * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);
        }

        // note: cloud
        MethodHolder1.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);
        GlStateManager.shadeModel(7425);
        if (MINECRAFT.getRenderViewEntity().posY + (double) MINECRAFT.getRenderViewEntity().getEyeHeight() < 128.0D) {
            MethodHolder1.renderCloudsCheck(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
        }
        MINECRAFT.profiler.endSection();

        // note: skybox and basic stuff are done
        //</editor-fold>

        KIRINO_ENGINE.run(FramePhase.UPDATE);
        KIRINO_ENGINE.run(FramePhase.RENDER_OPAQUE);

        //<editor-fold desc="vanilla logic">
        MethodHolder2.getMinecraftIntegration(KIRINO_ENGINE).cullingPatch.collectEntitiesInView(
                renderViewEntity,
                cameraFrustum,
                MINECRAFT.world.getChunkProvider(),
                partialTicks);

        boolean flag = MethodHolder1.isDrawBlockOutline(MINECRAFT.entityRenderer);

        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        // note: default value of debugView == false
        if (!MethodHolder1.isDebugView(MINECRAFT.entityRenderer)) {
            GlStateManager.matrixMode(5888);
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(0);
            MethodHolder2.getMinecraftIntegration(KIRINO_ENGINE).entityRenderingPatch.renderEntities(
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
            MethodHolder2.getMinecraftIntegration(KIRINO_ENGINE).tesrRenderingPatch.renderTESRs(
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
        if (!MethodHolder1.isDebugView(MINECRAFT.entityRenderer)) {
            // ========== litParticles ==========
            MINECRAFT.profiler.startSection("litParticles");
            MINECRAFT.entityRenderer.enableLightmap();
            MINECRAFT.effectRenderer.renderLitParticles(renderViewEntity, partialTicks);
            RenderHelper.disableStandardItemLighting();
            MethodHolder1.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);

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
        MethodHolder1.renderRainSnow(MINECRAFT.entityRenderer, partialTicks);
        GlStateManager.depthMask(true);
        MINECRAFT.renderGlobal.renderWorldBorder(renderViewEntity, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.alphaFunc(516, 0.1F);
        MethodHolder1.setupFog(MINECRAFT.entityRenderer, 0, partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        MINECRAFT.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.shadeModel(7425);
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        KIRINO_ENGINE.run(FramePhase.RENDER_TRANSPARENT);

        //<editor-fold desc="vanilla logic">
        // ========== entities ==========
        MINECRAFT.profiler.startSection("entities");
        // note: default value of debugView == false
        if (!MethodHolder1.isDebugView(MINECRAFT.entityRenderer)) {
            RenderHelper.enableStandardItemLighting();
            ForgeHooksClient.setRenderPass(1);
            MethodHolder2.getMinecraftIntegration(KIRINO_ENGINE).entityRenderingPatch.renderEntities(
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
            MethodHolder2.getMinecraftIntegration(KIRINO_ENGINE).tesrRenderingPatch.renderTESRs(
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
            MethodHolder1.renderCloudsCheck(MINECRAFT.entityRenderer, MINECRAFT.renderGlobal, partialTicks, 2, d0, d1, d2);
        }

        // ========== forge_render_last ==========
        MINECRAFT.profiler.endStartSection("forge_render_last");
        ForgeHooksClient.dispatchRenderLast(MINECRAFT.renderGlobal, partialTicks);

        // ========== hand ==========
        MINECRAFT.profiler.endStartSection("hand");
        if (MethodHolder1.isRenderHand(MINECRAFT.entityRenderer)) {
            GlStateManager.clear(256);
            MethodHolder1.renderHand(MINECRAFT.entityRenderer, partialTicks, 2);
        }
        MINECRAFT.profiler.endSection();
        //</editor-fold>

        KIRINO_ENGINE.run(FramePhase.POST_UPDATE);
        KIRINO_ENGINE.run(FramePhase.RENDER_OVERLAY);
    }
    //</editor-fold>

    // last chance to modify the config
    public static void configEvent() {
        try {
            Method registerMethod = KIRINO_EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
            registerMethod.setAccessible(true);

            Method onKirinoOneTimeConfig = KirinoCore.class.getDeclaredMethod("onKirinoOneTimeConfig", KirinoOneTimeConfigEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, KirinoOneTimeConfigEvent.class, KirinoCore.class, onKirinoOneTimeConfig, Loader.instance().getMinecraftModContainer());
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

        LOGGER.info("KirinoCore Initialization Stage");

        //<editor-fold desc="gl version fetch">
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
            throw new RuntimeException("Failed to parse the OpenGL version.");
        }
        //</editor-fold>

        if (!(majorGLVersion == 4 && minorGLVersion == 6)) {
            RENDER_UNSUPPORTED = true;
            LOGGER.warn("OpenGL 4.6 not supported. Marking \"RENDER_UNSUPPORTED\"=true.");
        }

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

            Method onDebugHudRegister = KirinoCore.class.getDeclaredMethod("onDebugHudRegister", DebugHUDRegistrationEvent.class);
            registerMethod.invoke(KIRINO_EVENT_BUS, DebugHUDRegistrationEvent.class, KirinoCore.class, onDebugHudRegister, Loader.instance().getMinecraftModContainer());
            LOGGER.info("Registered the default DebugHUDRegistrationEvent listener.");
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

            KIRINO_ENGINE = (KirinoEngine) ctor.invokeExact(KIRINO_EVENT_BUS, LOGGER, ECS_RUNTIME, KIRINO_CONFIG_HUB.isEnableHDR(), KIRINO_CONFIG_HUB.isEnablePostProcessing());
        } catch (Throwable throwable) {
            throw new RuntimeException("Kirino Engine failed to initialize.", throwable);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        LOGGER.info("---------------");
        //</editor-fold>

        DEBUG_SERVICE.register(RenderStatsFrame.class, new RenderStatsFrame(MethodHolder2.getGraphicsRuntimeServices(KIRINO_ENGINE).debugHudManager));
        DEBUG_SERVICE.register(FpsHistory.class, new FpsHistory());
    }

    public static void postInit() {
        if (!KIRINO_CONFIG_HUB.isEnable()) {
            return;
        }

        LOGGER.info("KirinoCore Post-Initialization Stage");

        //<editor-fold desc="kirino engine">
        LOGGER.info("---------------");
        LOGGER.info("Post-Initializing Kirino Engine.");
        StopWatch stopWatch = StopWatch.createStarted();

        if (KIRINO_CONFIG_HUB.isEnableRenderDelegate() && !RENDER_UNSUPPORTED) {
            KIRINO_ENGINE.run(FramePhase.PREPARE);
        } else {
            KIRINO_ENGINE.runHeadlessly(FramePhase.PREPARE);
        }

        stopWatch.stop();
        LOGGER.info("Kirino Engine Post-Initialized. Time taken: {} ms", stopWatch.getTime(TimeUnit.MILLISECONDS));
        LOGGER.info("---------------");
        //</editor-fold>
    }

    @SubscribeEvent
    public static void onStructScan(StructScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.platform.ecs.struct");
    }

    @SubscribeEvent
    public static void onComponentScan(ComponentScanningEvent event) {
        event.register("com.cleanroommc.kirino.engine.render.platform.ecs.component");
    }

    @SubscribeEvent
    public static void onJobRegister(JobRegistrationEvent event) {
        event.register(ChunkMeshletGenJob.class);
        event.register(ChunkPrioritizationJob.class);
        event.register(MeshletDestroyJob.class);
        event.register(MeshletDebugJob.class);
        event.register(MeshletBufferWriteJob.class);
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
    public static void onDebugHudRegister(DebugHUDRegistrationEvent event) {
        event.register(new FpsHUD());
        event.register(new CommonStatsHUD());
    }

    @SubscribeEvent
    public static void onKirinoOneTimeConfig(KirinoOneTimeConfigEvent event) {
        event.getOneTimeConfig().enableRenderDelegate = false;
    }

    //<editor-fold desc="reflection">
    /**
     * Holder class to initialize-on-demand necessary method handles.
     */
    private static class MethodHolder1 {
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
         * See <code>EntityRenderer#setupCameraTransform(float, int)</code>
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
         * See <code>EntityRenderer#updateFogColor(float)</code>
         */
        static void updateFogColor(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.updateFogColor().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#setupFog(int, float)</code>
         */
        static void setupFog(EntityRenderer instance, int startCoords, float partialTicks) {
            try {
                DELEGATE.setupFog().invokeExact(instance, startCoords, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#getFOVModifier(float, boolean)</code>
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
         * See <code>EntityRenderer#renderCloudsCheck(RenderGlobal, float, int, double, double, double)</code>
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
         * See <code>EntityRenderer#isDrawBlockOutline()</code>
         */
        static boolean isDrawBlockOutline(EntityRenderer instance) {
            try {
                return (boolean) DELEGATE.isDrawBlockOutline().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#updateLightmap(float)</code>
         */
        static void updateLightmap(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.updateLightmap().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#renderRainSnow(float)</code>
         */
        static void renderRainSnow(EntityRenderer instance, float partialTicks) {
            try {
                DELEGATE.renderRainSnow().invokeExact(instance, partialTicks);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#renderHand(float, int)</code>
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
         * See <code>EntityRenderer#farPlaneDistance</code>
         */
        static float getFarPlaneDistance(EntityRenderer instance) {
            try {
                return (float) DELEGATE.farPlaneDistance().invokeExact(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * See <code>EntityRenderer#debugView</code>
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
         * See <code>EntityRenderer#renderHand</code>
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

    /**
     * Holder class to initialize-on-demand necessary method handles.
     */
    private static class MethodHolder2 {
        static final KirinoEngineDelegate DELEGATE;

        static {
            DELEGATE = new KirinoEngineDelegate(
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "bootstrapResources", BootstrapResources.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "graphicsRuntimeServices", GraphicsRuntimeServices.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "sceneViewState", SceneViewState.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "minecraftIntegration", MinecraftIntegration.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "minecraftAssetProviders", MinecraftAssetProviders.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "shaderIntrospection", ShaderIntrospection.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "renderStructure", RenderStructure.class),
                    ReflectionUtils.getFieldGetter(KirinoEngine.class, "renderExtensions", RenderExtensions.class));

            Preconditions.checkNotNull(DELEGATE.bootstrapResourcesGetter);
            Preconditions.checkNotNull(DELEGATE.graphicsRuntimeServicesGetter);
            Preconditions.checkNotNull(DELEGATE.sceneViewStateGetter);
            Preconditions.checkNotNull(DELEGATE.minecraftIntegrationGetter);
            Preconditions.checkNotNull(DELEGATE.minecraftAssetProvidersGetter);
            Preconditions.checkNotNull(DELEGATE.shaderIntrospectionGetter);
            Preconditions.checkNotNull(DELEGATE.renderStructureGetter);
            Preconditions.checkNotNull(DELEGATE.renderExtensionsGetter);
        }

        static BootstrapResources getBootstrapResources(KirinoEngine engine) {
            BootstrapResources result;
            try {
                result = (BootstrapResources) DELEGATE.bootstrapResourcesGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static GraphicsRuntimeServices getGraphicsRuntimeServices(KirinoEngine engine) {
            GraphicsRuntimeServices result;
            try {
                result = (GraphicsRuntimeServices) DELEGATE.graphicsRuntimeServicesGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static SceneViewState getSceneViewState(KirinoEngine engine) {
            SceneViewState result;
            try {
                result = (SceneViewState) DELEGATE.sceneViewStateGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static MinecraftIntegration getMinecraftIntegration(KirinoEngine engine) {
            MinecraftIntegration result;
            try {
                result = (MinecraftIntegration) DELEGATE.minecraftIntegrationGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static MinecraftAssetProviders getMinecraftAssetProviders(KirinoEngine engine) {
            MinecraftAssetProviders result;
            try {
                result = (MinecraftAssetProviders) DELEGATE.minecraftAssetProvidersGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static ShaderIntrospection getShaderIntrospection(KirinoEngine engine) {
            ShaderIntrospection result;
            try {
                result = (ShaderIntrospection) DELEGATE.shaderIntrospectionGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static RenderStructure getRenderStructure(KirinoEngine engine) {
            RenderStructure result;
            try {
                result = (RenderStructure) DELEGATE.renderStructureGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static RenderExtensions getRenderExtensions(KirinoEngine engine) {
            RenderExtensions result;
            try {
                result = (RenderExtensions) DELEGATE.renderExtensionsGetter.invokeExact(engine);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        /**
         * Holds handles for KirinoEngine private fields.
         */
        record KirinoEngineDelegate(
                MethodHandle bootstrapResourcesGetter,
                MethodHandle graphicsRuntimeServicesGetter,
                MethodHandle sceneViewStateGetter,
                MethodHandle minecraftIntegrationGetter,
                MethodHandle minecraftAssetProvidersGetter,
                MethodHandle shaderIntrospectionGetter,
                MethodHandle renderStructureGetter,
                MethodHandle renderExtensionsGetter) {
        }
    }
    //</editor-fold>
}
