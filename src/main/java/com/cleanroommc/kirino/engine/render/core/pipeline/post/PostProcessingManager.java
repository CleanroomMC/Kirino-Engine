package com.cleanroommc.kirino.engine.render.core.pipeline.post;

import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.core.framebuffer.PingPongFramebuffer;
import com.cleanroommc.kirino.engine.render.core.gl.semantic.GLKnowledgeKeys;
import com.cleanroommc.kirino.engine.render.core.pipeline.PSOPresets;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.*;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.semantic.ClaimedScopeHandle;
import com.cleanroommc.kirino.engine.semantic.KnowledgeRuntime;
import com.cleanroommc.kirino.gl.framebuffer.Framebuffer;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.Logger;
import org.joml.Vector4i;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/**
 * This class should be treated as part of {@link FrameFinalizer},
 * and the post-processing process is fully guided by {@link FrameFinalizer#finalizeFramebuffer(ResourceStorage, KnowledgeRuntime)}.
 * <p>
 * It's designed to be highly coupled with {@link FrameFinalizer}. For example, it assumes that the
 * framebuffer structure is immutable, which is guaranteed by {@link FrameFinalizer}.
 */
public class PostProcessingManager {

    private static final class SubpassScopeProviderImpl implements SubpassScopeProvider {

        private final FramebufferStore framebufferStore;

        SubpassScopeProviderImpl(FramebufferStore framebufferStore) {
            this.framebufferStore = framebufferStore;
        }

        @NonNull
        @Override
        public ClaimedScopeHandle provide(
                int index,
                int subpassCount,
                @NonNull String subpassName,
                @NonNull ResourceStorage storage,
                @NonNull KnowledgeRuntime glKnowledge,
                @Nullable Camera camera,
                @Nullable Object payload) {

            if (index == 0) {
                if (framebufferStore.getChainMode() == FramebufferChainMode.INTERMEDIATE) {
                    // directly render to Minecraft framebuffer
                    // chain: (start) intermediate -> Minecraft
                    final net.minecraft.client.shader.Framebuffer minecraftFramebuffer = framebufferStore.getMinecraftFramebuffer();

                    Framebuffer.bind(minecraftFramebuffer.framebufferObject);
                    GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                    glKnowledge.commit(cp -> {
                        cp.know(GLKnowledgeKeys.FBO_READ, minecraftFramebuffer.framebufferObject);
                        cp.know(GLKnowledgeKeys.FBO_DRAW, minecraftFramebuffer.framebufferObject);
                        cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight));
                    });
                } else if (framebufferStore.getChainMode() == FramebufferChainMode.PING_PONG) {
                    // render to post-processing framebuffer B to start the ping-pong process
                    // chain: (start) ping-pong A -> ping-pong B
                    final PingPongFramebuffer pingPongFramebuffer = framebufferStore.getPingPongFramebuffer();

                    pingPongFramebuffer.framebufferB().bind();
                    GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                    glKnowledge.commit(cp -> {
                        cp.know(GLKnowledgeKeys.FBO_READ, pingPongFramebuffer.framebufferB().fboID);
                        cp.know(GLKnowledgeKeys.FBO_DRAW, pingPongFramebuffer.framebufferB().fboID);
                        cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height()));
                    });
                }
            } else {
                // when there are more than one subpass, the chain mode is definitely ping-pong
                Preconditions.checkState(framebufferStore.getChainMode() == FramebufferChainMode.PING_PONG);

                // last subpass
                if (index == subpassCount - 1) {
                    // must render to Minecraft framebuffer at the end
                    final net.minecraft.client.shader.Framebuffer minecraftFramebuffer = framebufferStore.getMinecraftFramebuffer();

                    Framebuffer.bind(minecraftFramebuffer.framebufferObject);
                    GL11.glViewport(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                    glKnowledge.commit(cp -> {
                        cp.know(GLKnowledgeKeys.FBO_READ, minecraftFramebuffer.framebufferObject);
                        cp.know(GLKnowledgeKeys.FBO_DRAW, minecraftFramebuffer.framebufferObject);
                        cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, minecraftFramebuffer.framebufferWidth, minecraftFramebuffer.framebufferHeight));
                    });

                // before last subpass
                } else if (index < subpassCount - 1) {
                    final PingPongFramebuffer pingPongFramebuffer = framebufferStore.getPingPongFramebuffer();

                    pingPongFramebuffer.swap();
                    pingPongFramebuffer.framebufferB().bind();
                    GL11.glViewport(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height());
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                    glKnowledge.commit(cp -> {
                        cp.know(GLKnowledgeKeys.FBO_READ, pingPongFramebuffer.framebufferB().fboID);
                        cp.know(GLKnowledgeKeys.FBO_DRAW, pingPongFramebuffer.framebufferB().fboID);
                        cp.know(GLKnowledgeKeys.VIEWPORT, new Vector4i(0, 0, pingPongFramebuffer.width(), pingPongFramebuffer.height()));
                    });
                }
            }

            return glKnowledge.claim(GLKnowledgeKeys.FBO_DRAW, GLKnowledgeKeys.VIEWPORT);
        }
    }

    private final RenderPass placeholderPass;
    private final ResourceSlot<Renderer> renderer;
    private final ResourceSlot<VAO> fullscreenTriangleVao;

    private boolean registrationPeriod = false;
    private boolean isInitialized = false;

    private void openRegister() {
        registrationPeriod = true;
    }

    private void closeRegister() {
        registrationPeriod = false;
    }

    private FramebufferStore framebufferStore;
    private SubpassScopeProvider subpassScopeProvider = null;
    private Object[] subpassPayloads = null;

    /**
     * It'll only be called when post-processing is enabled.
     *
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li><code>schedule</code> must have at least one subpass to work as expected. Otherwise, disable post-processing instead</li>
     *     <li>Must exit the registration period already (i.e. cannot modify subpasses)</li>
     *     <li>Must be called after {@link FrameFinalizer#initResources(Logger, net.minecraft.client.shader.Framebuffer, PostProcessingSchedule)},
     *     since the <code>store</code> is allocated with that call.</li>
     *     <li><code>schedule</code> must be identical to the one passed to {@link FrameFinalizer#initResources(Logger, net.minecraft.client.shader.Framebuffer, PostProcessingSchedule)}.</li>
     * </ul>
     *
     * <p>Note: Both <code>store</code> and <code>schedule</code> will get recorded. Immutability must be guaranteed.</p>
     */
    public void lateInit(@NonNull FramebufferStore store, @NonNull PostProcessingSchedule schedule) {
        Preconditions.checkNotNull(store);
        Preconditions.checkNotNull(schedule);
        Preconditions.checkArgument(schedule.getSubpassCount() >= 1,
                "Argument \"schedule\" must contain at least one subpass.");
        Preconditions.checkState(!registrationPeriod,
                "Registration period must already have ended.");
        Preconditions.checkState(store.getChainMode() != FramebufferChainMode.DIRECT,
                "\"FramebufferStore#getChainMode()\" is not expected to be DIRECT since post-processing is enabled.");

        for (int i = 0; i < schedule.getSubpassCount(); i++) {
            PostProcessingSchedule.ScheduledPass scheduledPass = schedule.getSubpass(i);
            Preconditions.checkState(placeholderPass.hasSubpass(scheduledPass.getRegisteredName()),
                    "Scheduled subpass \"%s\" from \"schedule\" doesn't exist in PostProcessingManager during runtime.",
                    scheduledPass.getRegisteredName());
        }

        Map<String, Subpass> subpasses = new HashMap<>();
        for (int i = 0; i < schedule.getSubpassCount(); i++) {
            PostProcessingSchedule.ScheduledPass scheduledPass = schedule.getSubpass(i);
            Subpass subpass = placeholderPass.removeSubpass(scheduledPass.getRegisteredName());
            Preconditions.checkNotNull(subpass);

            subpasses.put(scheduledPass.getRegisteredName(), subpass);
        }

        // re-order placeholderPass
        placeholderPass.clearSubpasses();
        for (int i = 0; i < schedule.getSubpassCount(); i++) {
            PostProcessingSchedule.ScheduledPass scheduledPass = schedule.getSubpass(i);
            placeholderPass.addSubpass(scheduledPass.getRegisteredName(), subpasses.get(scheduledPass.getRegisteredName()));
        }

        framebufferStore = store;
        subpassScopeProvider = new SubpassScopeProviderImpl(store);

        if (store.getChainMode() == FramebufferChainMode.INTERMEDIATE) {
            subpassPayloads = new Object[1];
            subpassPayloads[0] = store.getIntermediateFramebuffer();

        } else if (store.getChainMode() == FramebufferChainMode.PING_PONG) {
            final int subpassCount = schedule.getSubpassCount();
            Preconditions.checkState(subpassCount >= 2,
                    "There are at least two subpasses from the given schedule.");

            final PingPongFramebuffer pingPongFramebuffer = store.getPingPongFramebuffer();

            subpassPayloads = new Object[subpassCount]; // A B A B and so on
            pingPongFramebuffer.reset();
            for (int i = 0; i < subpassCount; i++) {
                if (i % 2 == 0) {
                    subpassPayloads[i] = pingPongFramebuffer.framebufferA();
                } else {
                    subpassPayloads[i] = pingPongFramebuffer.framebufferB();
                }
            }
        }

        isInitialized = true;
    }

    public PostProcessingManager(
            @NonNull ResourceSlot<GraphicResourceManager> graphicResourceManager,
            @NonNull ResourceSlot<IndirectDrawBufferGenerator> idbGenerator,
            @NonNull ResourceSlot<Renderer> renderer,
            @NonNull ResourceSlot<VAO> fullscreenTriangleVao) {

        Preconditions.checkNotNull(graphicResourceManager);
        Preconditions.checkNotNull(idbGenerator);
        Preconditions.checkNotNull(renderer);
        Preconditions.checkNotNull(fullscreenTriangleVao);

        this.placeholderPass = new RenderPass("Post-Processing", graphicResourceManager, idbGenerator);
        this.renderer = renderer;
        this.fullscreenTriangleVao = fullscreenTriangleVao;
    }

    /**
     * <p>Note: This is part of the engine initialization lifecycle. Must not be called by clients.</p>
     */
    @NonNull
    public ResourceSlot<ShaderProgram> addSubpass(@NonNull PostProcessingEntry entry) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkState(registrationPeriod,
                "Only call this method during the registration period.");

        ResourceSlot<ShaderProgram> shaderProgram = entry.getShaderProgram();

        AbstractPostProcessingPass subpass = entry.ctor.construct(
                renderer,
                PSOPresets.createScreenOverwritePSO(shaderProgram),
                fullscreenTriangleVao);

        placeholderPass.addSubpass(entry.subpassName, subpass);

        return shaderProgram;
    }

    /**
     * <p>Note: This is part of the engine initialization lifecycle. Must not be called by clients.</p>
     */
    public void attachSubpassDecorator(@NonNull String subpassName, @NonNull SubpassDecorator decorator) {
        Preconditions.checkNotNull(subpassName);
        Preconditions.checkNotNull(decorator);
        Preconditions.checkState(registrationPeriod,
                "Only call this method during the registration period.");

        placeholderPass.attachSubpassDecorator(subpassName, decorator);
    }

    /**
     * <p>Note: This information is only reliable after the late initialization.</p>
     */
    public boolean hasSubpass(@NonNull String subpassName) {
        Preconditions.checkNotNull(subpassName);
        Preconditions.checkState(isInitialized,
                "Only call this method after the late initialization.");

        return placeholderPass.hasSubpass(subpassName);
    }

    /**
     * <p>Note: This call is only reliable after the late initialization.</p>
     */
    public void postProcess(
            @NonNull ResourceStorage storage,
            @NonNull KnowledgeRuntime glKnowledge) {

        Preconditions.checkState(isInitialized,
                "Only call this method after the late initialization.");
        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(glKnowledge);

        placeholderPass.render(
                storage,
                glKnowledge,
                null,
                null,
                subpassScopeProvider,
                subpassPayloads);

        if (framebufferStore.getChainMode() == FramebufferChainMode.PING_PONG) {
            framebufferStore.getPingPongFramebuffer().reset();
        }
    }
}
