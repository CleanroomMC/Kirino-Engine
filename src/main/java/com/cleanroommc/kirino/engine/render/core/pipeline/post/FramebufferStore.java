package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.framebuffer.PingPongFramebuffer;
import com.cleanroommc.kirino.engine.render.core.framebuffer.ResolutionContainer;
import com.cleanroommc.kirino.engine.render.core.framebuffer.ScalableFramebuffer;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.DepthStencilAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture2DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.FilterMode;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.kirino.gl.texture.meta.WrapMode;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;

/**
 * This is a component possessed by {@link FrameFinalizer}.
 *
 * @see FrameFinalizer
 */
public class FramebufferStore {

    private static final Minecraft MINECRAFT = Minecraft.getMinecraft();

    private FramebufferChainMode chainMode;
    private ResolutionContainer resolution;
    private ScalableFramebuffer mainFramebuffer;
    private PingPongFramebuffer pingPongFramebuffer;
    private Framebuffer intermediateFramebuffer;
    private net.minecraft.client.shader.Framebuffer minecraftFramebuffer;

    /**
     * <code>chainMode</code> can be seen as a corollary of the subpass count, which implicitly relies on
     * the immutability of the {@link PostProcessingSchedule} owned by the engine runtime.
     * Nevertheless, the {@link PostProcessingSchedule} is designed to be immutable.
     *
     * @see #allocate(Logger, net.minecraft.client.shader.Framebuffer, boolean, boolean, PostProcessingSchedule)
     */
    @NonNull
    public FramebufferChainMode getChainMode() {
        return chainMode;
    }

    @NonNull
    public ResolutionContainer getResolution() {
        return resolution;
    }

    @NonNull
    public ScalableFramebuffer getMainFramebuffer() {
        return mainFramebuffer;
    }

    @NonNull
    public PingPongFramebuffer getPingPongFramebuffer() {
        Preconditions.checkState(chainMode == FramebufferChainMode.PING_PONG,
                "Ping-pong framebuffer is only available when \"chainMode\"=%s equals %s.",
                chainMode,
                FramebufferChainMode.PING_PONG);

        return pingPongFramebuffer;
    }

    @NonNull
    public Framebuffer getIntermediateFramebuffer() {
        Preconditions.checkState(chainMode == FramebufferChainMode.INTERMEDIATE,
                "Intermediate framebuffer is only available when \"chainMode\"=%s equals %s.",
                chainMode,
                FramebufferChainMode.INTERMEDIATE);

        return intermediateFramebuffer;
    }

    public net.minecraft.client.shader.@NonNull Framebuffer getMinecraftFramebuffer() {
        return minecraftFramebuffer;
    }

    FramebufferStore() {
    }

    private boolean allocated = false;

    /**
     * Every input parameter must stay immutable during the runtime after this allocation call.
     */
    void allocate(
            @NonNull Logger logger,
            net.minecraft.client.shader.@NonNull Framebuffer minecraftFramebuffer,
            boolean enableHDR,
            boolean enablePostProcessing,
            @NonNull PostProcessingSchedule postProcessingSchedule) {

        Preconditions.checkState(!allocated, "Must not be allocated already.");
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(minecraftFramebuffer);
        Preconditions.checkNotNull(postProcessingSchedule);

        this.minecraftFramebuffer = minecraftFramebuffer;

        int postProcessingPassCount = postProcessingSchedule.getSubpassCount();

        logger.info("Framebuffer HDR: {}", enableHDR ? "ON" : "OFF");
        logger.info("Framebuffer Post-processing: {}; Pass Count: {}", enablePostProcessing ? "ON" : "OFF", postProcessingPassCount);

        Preconditions.checkState(!enablePostProcessing || postProcessingPassCount >= 1,
                "PostProcessingSchedule must not be empty when post-processing is enabled.");

        mainFramebuffer = new ScalableFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight, 1f);
        logger.info("Initiated the main framebuffer: {}, {}", mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());

        // these two are mutually exclusive
        final boolean useIntermediate = (enableHDR && !enablePostProcessing) || (enablePostProcessing && postProcessingPassCount == 1);
        final boolean usePingPong = enablePostProcessing && postProcessingPassCount >= 2;

        if (useIntermediate) {
            intermediateFramebuffer = new Framebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
            logger.info("Initiated the intermediate framebuffer: {}, {}", intermediateFramebuffer.width(), intermediateFramebuffer.height());
        } else {
            intermediateFramebuffer = null;
        }

        if (usePingPong) {
            pingPongFramebuffer = new PingPongFramebuffer(MINECRAFT.displayWidth, MINECRAFT.displayHeight);
            logger.info("Initiated the ping-pong framebuffer: {}, {}", pingPongFramebuffer.width(), pingPongFramebuffer.height());
        } else {
            pingPongFramebuffer = null;
        }

        //<editor-fold desc="resolution and callbacks">
        resolution = new ResolutionContainer((width, height) -> {

            // display resized callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getRatio()),
                    (int) (height * mainFramebuffer.getRatio()));

            if (useIntermediate) {
                intermediateFramebuffer.resize(width, height);
            }
            if (usePingPong) {
                pingPongFramebuffer.resize(width, height);
            }

            logger.info("Display size updated. Current display width & height: {}, {}", width, height);
            logger.info("[Display Resized] Main framebuffer resized: width={}, height={}, ratio={}", mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(), mainFramebuffer.getRatio());

            if (useIntermediate) {
                logger.info("[Display Resized] Intermediate framebuffer resized: width={}, height={}", intermediateFramebuffer.width(), intermediateFramebuffer.height());
            }
            if (usePingPong) {
                logger.info("[Display Resized] Ping-pong framebuffer resized: width={}, height={}", pingPongFramebuffer.width(), pingPongFramebuffer.height());
            }

        }, (width, height) -> {

            // ratio changed callback
            mainFramebuffer.framebuffer.resize(
                    (int) (width * mainFramebuffer.getTargetRatio()),
                    (int) (height * mainFramebuffer.getTargetRatio()));

            if (useIntermediate) {
                intermediateFramebuffer.resize(width, height);
            }
            if (usePingPong) {
                pingPongFramebuffer.resize(width, height);
            }

            logger.info("Main framebuffer ratio changed: {} -> {}", mainFramebuffer.getRatio(), mainFramebuffer.getTargetRatio());
            logger.info("[Ratio Changed] Main framebuffer resized: width={}, height={}, ratio={}", mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height(), mainFramebuffer.getTargetRatio());

            if (useIntermediate) {
                logger.info("[Ratio Changed] Intermediate framebuffer resized: width={}, height={}", intermediateFramebuffer.width(), intermediateFramebuffer.height());
            }
            if (usePingPong) {
                logger.info("[Ratio Changed] Ping-pong framebuffer resized: width={}, height={}", pingPongFramebuffer.width(), pingPongFramebuffer.height());
            }

        });
        //</editor-fold>

        //<editor-fold desc="main framebuffer initialization">
        {
            mainFramebuffer.framebuffer.bind();

            Texture2DAccessor color0Tex = new Texture2DAccessor(false, GLTexture.newTex2D(false, false, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height()));
            color0Tex.bind();
            color0Tex.highlevel().allocEmpty(true, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.setCommonParams(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            mainFramebuffer.framebuffer.attach(new ColorAttachment(0, color0Tex));

            Texture2DAccessor depthTex = new Texture2DAccessor(false, GLTexture.newTex2D(false, false, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height()));
            depthTex.bind();
            depthTex.highlevel().allocEmpty(true, TextureFormat.D24S8);
            depthTex.setCommonParams(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            depthTex.bind(0);
            mainFramebuffer.framebuffer.attach(new DepthStencilAttachment(depthTex));

            mainFramebuffer.framebuffer.check();

            GL11.glViewport(0, 0, mainFramebuffer.framebuffer.width(), mainFramebuffer.framebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClearDepth(1);
            GL11.glClearStencil(0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

            logger.info("Main framebuffer created. ID: {}", mainFramebuffer.framebuffer.fboID);
        }
        //</editor-fold>

        //<editor-fold desc="intermediate framebuffer initialization">
        if (useIntermediate) {
            intermediateFramebuffer.bind();

            Texture2DAccessor color0Tex = new Texture2DAccessor(false, GLTexture.newTex2D(false, false, intermediateFramebuffer.width(), intermediateFramebuffer.height()));
            color0Tex.bind();
            color0Tex.highlevel().allocEmpty(true, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.setCommonParams(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            intermediateFramebuffer.attach(new ColorAttachment(0, color0Tex));

            intermediateFramebuffer.check();

            GL11.glViewport(0, 0, intermediateFramebuffer.width(), intermediateFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            logger.info("Intermediate framebuffer created. ID: {}", intermediateFramebuffer.fboID);
        }
        //</editor-fold>

        //<editor-fold desc="ping-pong framebuffer A initialization">
        if (usePingPong) {
            pingPongFramebuffer.framebufferA().bind();

            Texture2DAccessor color0Tex = new Texture2DAccessor(false, GLTexture.newTex2D(false, false, pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            color0Tex.bind();
            color0Tex.highlevel().allocEmpty(true, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.setCommonParams(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            pingPongFramebuffer.framebufferA().attach(new ColorAttachment(0, color0Tex));

            pingPongFramebuffer.framebufferA().check();

            GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            logger.info("Ping-pong framebuffer A created. ID: {}", pingPongFramebuffer.framebufferA().fboID);
        }
        //</editor-fold>

        //<editor-fold desc="ping-pong framebuffer B initialization">
        if (usePingPong) {
            pingPongFramebuffer.framebufferB().bind();

            Texture2DAccessor color0Tex = new Texture2DAccessor(false, GLTexture.newTex2D(false, false, pingPongFramebuffer.width(), pingPongFramebuffer.height()));
            color0Tex.bind();
            color0Tex.highlevel().allocEmpty(true, enableHDR ? TextureFormat.RGBA16F : TextureFormat.RGBA8_UNORM);
            color0Tex.setCommonParams(FilterMode.NEAREST, FilterMode.NEAREST, WrapMode.CLAMP, WrapMode.CLAMP);
            color0Tex.bind(0);
            pingPongFramebuffer.framebufferB().attach(new ColorAttachment(0, color0Tex));

            pingPongFramebuffer.framebufferB().check();

            GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            logger.info("Ping-pong framebuffer B created. ID: {}", pingPongFramebuffer.framebufferB().fboID);
        }
        //</editor-fold>

        if (useIntermediate) {
            chainMode = FramebufferChainMode.INTERMEDIATE;
        } else if (usePingPong) {
            chainMode = FramebufferChainMode.PING_PONG;
        } else {
            chainMode = FramebufferChainMode.DIRECT;
        }

        allocated = true;
    }
}
