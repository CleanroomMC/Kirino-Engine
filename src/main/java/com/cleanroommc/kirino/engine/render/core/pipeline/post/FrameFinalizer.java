package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.pipeline.PassDescriptor;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.framebuffer.ColorAttachment;
import com.cleanroommc.kirino.gl.framebuffer.DepthStencilAttachment;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

public class FrameFinalizer {

    public final boolean enableHDR;
    public final boolean enablePostProcessing;

    private final PostProcessingManager postProcessingManager;
    private final PassDescriptor toneMappingPassDesc;
    private final PassDescriptor upscalingPassDesc;
    private final PassDescriptor downscalingPassDesc;

    private boolean isResourcesInit = false;
    private FramebufferStore framebufferStore = null;

    @NonNull
    public FramebufferStore acquireFramebufferStore() {
        Preconditions.checkState(isResourcesInit,
                "\"FrameFinalizer#initResources\" must be finished before the acquire call.");

        return Preconditions.checkNotNull(framebufferStore);
    }

    public FrameFinalizer(
            @NonNull PostProcessingManager postProcessingManager,
            @NonNull PassDescriptor toneMappingPassDesc,
            @NonNull PassDescriptor upscalingPassDesc,
            @NonNull PassDescriptor downscalingPassDesc,
            boolean enableHDR,
            boolean enablePostProcessing) {

        Preconditions.checkNotNull(postProcessingManager);
        Preconditions.checkNotNull(toneMappingPassDesc);
        Preconditions.checkNotNull(upscalingPassDesc);
        Preconditions.checkNotNull(downscalingPassDesc);

        this.enableHDR = enableHDR;
        this.enablePostProcessing = enablePostProcessing;
        this.postProcessingManager = postProcessingManager;
        this.toneMappingPassDesc = toneMappingPassDesc;
        this.upscalingPassDesc = upscalingPassDesc;
        this.downscalingPassDesc = downscalingPassDesc;
    }

    /**
     * It initializes {@link #framebufferStore} based on the given {@link #enableHDR}, {@link #enablePostProcessing},
     * <code>postProcessingSchedule</code>.
     * <p>
     * This late initialization logic totally relies on <code>postProcessingSchedule</code>.
     * And, the resources allocated with the given configuration will be immutable during runtime.
     *
     * <p>Note: It modifies GL framebuffer binding, clear hint, and viewport.
     * Restore these OpenGL states on your own after this call or not.</p>
     *
     * <p>Note: <code>postProcessingSchedule</code> must contain the tone mapping process when
     * <code>{@link #enableHDR} == true</code>.</p>
     *
     * @param minecraftFramebuffer The Minecraft's framebuffer
     */
    public void initResources(
            @NonNull Logger logger,
            net.minecraft.client.shader.@NonNull Framebuffer minecraftFramebuffer,
            @NonNull PostProcessingSchedule postProcessingSchedule) {

        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(minecraftFramebuffer);
        Preconditions.checkNotNull(postProcessingSchedule);

        framebufferStore = new FramebufferStore();
        framebufferStore.allocate(
                logger,
                minecraftFramebuffer,
                enableHDR,
                enablePostProcessing,
                postProcessingSchedule);

        isResourcesInit = true;
    }

    public void updateResolution() {
        FramebufferStore store = acquireFramebufferStore();

        if (store.getMainFramebuffer().isScheduledToResize()) {
            store.getResolution().synchronize();
            store.getMainFramebuffer().finishResize();
        }
        store.getResolution().update();
    }

    public void bindMainFramebuffer(boolean viewport) {
        FramebufferStore store = acquireFramebufferStore();

        store.getMainFramebuffer().framebuffer.bind();
        if (viewport) {
            GL11.glViewport(0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height());
        }
    }

    public void bindMinecraftFramebuffer(boolean viewport) {
        FramebufferStore store = acquireFramebufferStore();

        Framebuffer.bind(store.getMinecraftFramebuffer().framebufferObject);
        if (viewport) {
            GL11.glViewport(0, 0, store.getMinecraftFramebuffer().framebufferWidth, store.getMinecraftFramebuffer().framebufferHeight);
        }
    }

    // todo: fix potential pipeline stall
    /**
     * <p><b>Read Framebuffer</b>: {@link FramebufferStore#getMainFramebuffer()}</p>
     * <p><b>Draw Framebuffer</b>: {@link FramebufferStore#getMinecraftFramebuffer()}</p>
     * <br>
     * It scales the input if necessary and then post-processes it if necessary, and also handles HDR tone mapping.
     * Whichever combination it is, the result will be drawn to Minecraft framebuffer.
     * <br>
     * Framebuffer binding will be chaotic after this final pass. Bind framebuffer on your own after this pass.
     * You most likely want to bind Minecraft framebuffer after this call.
     */
    public void finalizeFramebuffer(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge) {

        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(glKnowledge);

        FramebufferStore store = acquireFramebufferStore();

        // todo: (FIX) blit depth from D24S8 to D32 is invalid. must confirm Minecraft fbo behavior when stencil==false
//        Preconditions.checkState(store.getMinecraftFramebuffer().isStencilEnabled());
        if (!store.getMinecraftFramebuffer().isStencilEnabled()) {
            store.getMinecraftFramebuffer().enableStencil();
        }

        //<editor-fold desc="blit depth to minecraft framebuffer">
        if (store.getMainFramebuffer().getRatio() == 1f) {
            // just in case the size and format of the main framebuffer and minecraft framebuffer mismatch
            if (store.getMainFramebuffer().framebuffer.width() != store.getMinecraftFramebuffer().framebufferTextureWidth ||
                    store.getMainFramebuffer().framebuffer.height() != store.getMinecraftFramebuffer().framebufferTextureHeight ||
                    !store.getMinecraftFramebuffer().isStencilEnabled()) {

                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
                GL30.glBlitFramebuffer(
                        0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                        0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                        store.getMinecraftFramebuffer().isStencilEnabled() ? GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT : GL11.GL_DEPTH_BUFFER_BIT,
                        GL11.GL_NEAREST);
            } else {
                DepthStencilAttachment depthAttachmentSrc = ((DepthStencilAttachment) store.getMainFramebuffer().framebuffer.getDepthAttachment());
                Preconditions.checkNotNull(depthAttachmentSrc);

                GL43.glCopyImageSubData(
                        depthAttachmentSrc.texture2D.texture.textureID,
                        depthAttachmentSrc.texture2D.target(),
                        0, 0, 0, 0,
                        store.getMinecraftFramebuffer().depthBuffer,
                        GL30.GL_RENDERBUFFER,
                        0, 0, 0, 0,
                        depthAttachmentSrc.texture2D.texture.extentX(),
                        depthAttachmentSrc.texture2D.texture.extentY(),
                        1);
            }
        } else if (store.getMainFramebuffer().getRatio() < 1f) {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
            GL30.glBlitFramebuffer(
                    0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                    0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                    store.getMinecraftFramebuffer().isStencilEnabled() ? GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT : GL11.GL_DEPTH_BUFFER_BIT,
                    GL11.GL_NEAREST);
        } else if (store.getMainFramebuffer().getRatio() > 1f) {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
            GL30.glBlitFramebuffer(
                    0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                    0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                    store.getMinecraftFramebuffer().isStencilEnabled() ? GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT : GL11.GL_DEPTH_BUFFER_BIT,
                    GL11.GL_NEAREST);
        }
        //</editor-fold>

        // main framebuffer -> Minecraft framebuffer
        //<editor-fold desc="no hdr & no post-processing">
        if (!enableHDR && !enablePostProcessing) {
            if (store.getMainFramebuffer().getRatio() == 1f) {
                // just in case the size of the main framebuffer and minecraft framebuffer mismatches
                if (store.getMainFramebuffer().framebuffer.width() != store.getMinecraftFramebuffer().framebufferTextureWidth ||
                        store.getMainFramebuffer().framebuffer.height() != store.getMinecraftFramebuffer().framebufferTextureHeight) {

                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                            0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
                } else {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) store.getMainFramebuffer().framebuffer.getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            store.getMinecraftFramebuffer().framebufferTexture,
                            GL11.GL_TEXTURE_2D,
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.extentX(),
                            colorAttachmentSrc.texture2D.texture.extentY(),
                            1);
                }
            } else if (store.getMainFramebuffer().getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                        0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            } else if (store.getMainFramebuffer().getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getMinecraftFramebuffer().framebufferObject);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                        0, 0, store.getMinecraftFramebuffer().framebufferTextureWidth, store.getMinecraftFramebuffer().framebufferTextureHeight,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            }
        }
        //</editor-fold>

        // main framebuffer -(skipped when no scaling)-> intermediate framebuffer -> Minecraft framebuffer
        //<editor-fold desc="hdr & no post-processing">
        if (enableHDR && !enablePostProcessing) {
            if (store.getMainFramebuffer().getRatio() == 1f) {
                Framebuffer.bind(store.getMinecraftFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, store.getMinecraftFramebuffer().framebufferWidth, store.getMinecraftFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPassDesc.acquire().render(storage, glKnowledge, null, null, null, new Object[]{store.getMainFramebuffer().framebuffer});
            } else if (store.getMainFramebuffer().getRatio() < 1f) {
                // todo: upscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getIntermediateFramebuffer().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                        0, 0, store.getIntermediateFramebuffer().width(), store.getIntermediateFramebuffer().height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                Framebuffer.bind(store.getMinecraftFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, store.getMinecraftFramebuffer().framebufferWidth, store.getMinecraftFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPassDesc.acquire().render(storage, glKnowledge, null, null, null, new Object[]{store.getIntermediateFramebuffer()});
            } else if (store.getMainFramebuffer().getRatio() > 1f) {
                // todo: downscale impl
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getIntermediateFramebuffer().fboID);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL30.glBlitFramebuffer(
                        0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                        0, 0, store.getIntermediateFramebuffer().width(), store.getIntermediateFramebuffer().height(),
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                Framebuffer.bind(store.getMinecraftFramebuffer().framebufferObject);
                GL11.glViewport(0, 0, store.getMinecraftFramebuffer().framebufferWidth, store.getMinecraftFramebuffer().framebufferHeight);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                toneMappingPassDesc.acquire().render(storage, glKnowledge, null, null, null, new Object[]{store.getIntermediateFramebuffer()});
            }
        }
        //</editor-fold>

        // main framebuffer -> intermediate framebuffer / ping-pong framebuffer A -> post-process -> Minecraft framebuffer
        //<editor-fold desc="hdr ANY & post-processing">
        if (enablePostProcessing) {
            if (store.getMainFramebuffer().getRatio() == 1f) {
                if (store.getChainMode() == FramebufferChainMode.INTERMEDIATE) {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) store.getMainFramebuffer().framebuffer.getColorAttachment(0));
                    ColorAttachment colorAttachmentDest = ((ColorAttachment) store.getIntermediateFramebuffer().getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentDest.texture2D.texture.textureID,
                            colorAttachmentDest.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.extentX(),
                            colorAttachmentSrc.texture2D.texture.extentY(),
                            1);

                    postProcessingManager.postProcess(storage, glKnowledge);
                } else if (store.getChainMode() == FramebufferChainMode.PING_PONG) {
                    ColorAttachment colorAttachmentSrc = ((ColorAttachment) store.getMainFramebuffer().framebuffer.getColorAttachment(0));
                    ColorAttachment colorAttachmentDest = ((ColorAttachment) store.getPingPongFramebuffer().framebufferA().getColorAttachment(0));
                    GL43.glCopyImageSubData(
                            colorAttachmentSrc.texture2D.texture.textureID,
                            colorAttachmentSrc.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentDest.texture2D.texture.textureID,
                            colorAttachmentDest.texture2D.target(),
                            0, 0, 0, 0,
                            colorAttachmentSrc.texture2D.texture.extentX(),
                            colorAttachmentSrc.texture2D.texture.extentY(),
                            1);

                    postProcessingManager.postProcess(storage, glKnowledge);
                }
            } else if (store.getMainFramebuffer().getRatio() < 1f) {
                if (store.getChainMode() == FramebufferChainMode.INTERMEDIATE) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getIntermediateFramebuffer().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                            0, 0, store.getIntermediateFramebuffer().width(), store.getIntermediateFramebuffer().height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(storage, glKnowledge);
                } else if (store.getChainMode() == FramebufferChainMode.PING_PONG) {
                    // todo: upscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getPingPongFramebuffer().framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                            0, 0, store.getPingPongFramebuffer().width(), store.getPingPongFramebuffer().height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(storage, glKnowledge);
                }
            } else if (store.getMainFramebuffer().getRatio() > 1f) {
                if (store.getChainMode() == FramebufferChainMode.INTERMEDIATE) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getIntermediateFramebuffer().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                            0, 0, store.getIntermediateFramebuffer().width(), store.getIntermediateFramebuffer().height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(storage, glKnowledge);
                } else if (store.getChainMode() == FramebufferChainMode.PING_PONG) {
                    // todo: downscale impl
                    GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, store.getMainFramebuffer().framebuffer.fboID);
                    GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, store.getPingPongFramebuffer().framebufferA().fboID);
                    GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                    GL30.glBlitFramebuffer(
                            0, 0, store.getMainFramebuffer().framebuffer.width(), store.getMainFramebuffer().framebuffer.height(),
                            0, 0, store.getPingPongFramebuffer().width(), store.getPingPongFramebuffer().height(),
                            GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

                    postProcessingManager.postProcess(storage, glKnowledge);
                }
            }
        }
        //</editor-fold>
    }
}
