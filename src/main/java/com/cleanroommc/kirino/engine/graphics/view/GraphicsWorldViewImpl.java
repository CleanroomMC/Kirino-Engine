package com.cleanroommc.kirino.engine.graphics.view;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.FramePhase;
import com.cleanroommc.kirino.engine.FramePhaseTiming;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.core.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.platform.MinecraftAssetProviders;
import com.cleanroommc.kirino.engine.render.platform.MinecraftIntegration;
import com.cleanroommc.kirino.engine.render.platform.SceneViewState;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;
import com.cleanroommc.kirino.engine.world.context.WorldContext;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.util.Map;
import java.util.function.Consumer;

public class GraphicsWorldViewImpl implements GraphicsWorldView {
    private final CleanECSRuntime ecs;
    private final RenderStructure render;
    private final RenderExtensions extensions;
    private final EventBus eventBus;
    private final Logger logger;

    public final ResourceStorage storage;

    public final BootstrapResources bootstrapResources;
    public final GraphicsRuntimeServices graphicsRuntimeServices;
    public final MinecraftIntegration minecraftIntegration;
    public final MinecraftAssetProviders minecraftAssetProviders;
    public final SceneViewState sceneViewState;

    public final ShaderIntrospection shaderIntrospection;

    private final Map<FramePhase, Map<FramePhaseTiming, Consumer<WorldContext<Graphics>>>> callbacks = new Object2ObjectOpenHashMap<>();

    public GraphicsWorldViewImpl(
            CleanECSRuntime ecs,
            RenderStructure render,
            RenderExtensions extensions,
            EventBus eventBus,
            Logger logger,
            ResourceStorage storage,
            BootstrapResources bootstrapResources,
            GraphicsRuntimeServices graphicsRuntimeServices,
            MinecraftIntegration minecraftIntegration,
            MinecraftAssetProviders minecraftAssetProviders,
            SceneViewState sceneViewState,
            ShaderIntrospection shaderIntrospection) {
        this.ecs = ecs;
        this.render = render;
        this.extensions = extensions;
        this.eventBus = eventBus;
        this.logger = logger;

        this.storage = storage;
        this.bootstrapResources = bootstrapResources;
        this.graphicsRuntimeServices = graphicsRuntimeServices;
        this.minecraftIntegration = minecraftIntegration;
        this.minecraftAssetProviders = minecraftAssetProviders;
        this.sceneViewState = sceneViewState;
        this.shaderIntrospection = shaderIntrospection;
    }

    @NonNull
    @Override
    public CleanECSRuntime ecs() {
        return ecs;
    }

    @NonNull
    @Override
    public RenderStructure rs() {
        return render;
    }

    @NonNull
    @Override
    public RenderExtensions ext() {
        return extensions;
    }

    @NonNull
    @Override
    public Logger logger() {
        return logger;
    }

    @NonNull
    @Override
    public EventBus bus() {
        return eventBus;
    }

    @Override
    public void run(@NonNull FramePhase phase) {
        Map<FramePhaseTiming, Consumer<WorldContext<Graphics>>> map = callbacks.get(phase);
        if (map != null) {
            Consumer<WorldContext<Graphics>> consumer = map.get(FramePhaseTiming.BEFORE);
            if (consumer != null) {
                consumer.accept(this);
            }
        }

        switch (phase) {
            case PRE_UPDATE -> {
                // only read states once to prevent huge amount of pipeline stalls
                GLStateBackup stateBackup = storage.get(graphicsRuntimeServices.stateBackup);
                if (!stateBackup.isStored()) {
                    stateBackup.storeStates();
                }

                FrameFinalizer frameFinalizer = storage.get(bootstrapResources.frameFinalizer);
                frameFinalizer.updateResolution();

                // current render target: main framebuffer
                frameFinalizer.bindMainFramebuffer(true);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);
            }
            case UPDATE -> {
                storage.get(graphicsRuntimeServices.graphicResourceManager).runStaging();
                sceneViewState.scene.tryUpdateWorld(Minecraft.getMinecraft().world);
                sceneViewState.scene.update();
            }
            case POST_UPDATE -> {
                GLStateBackup stateBackup = storage.get(graphicsRuntimeServices.stateBackup);
                FrameFinalizer frameFinalizer = storage.get(bootstrapResources.frameFinalizer);

                frameFinalizer.finalizeFramebuffer(storage);

                // current render target: minecraft framebuffer
                frameFinalizer.bindMinecraftFramebuffer(true);

                // reset everything to prevent any unexpected behavior
                stateBackup.restoreStates();
                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

                HighLevelDC.nextGen();
                LowLevelDC.nextGen();

                // set up fixed-func overlay rendering
                ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0, resolution.getScaledWidth_double(), resolution.getScaledHeight_double(), 0, -1, 1);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();
            }
            case RENDER_OPAQUE -> {
                if (!debug) {
                    return;
                }

                // test
                glStateBackup.storeStates();
                int vbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

                rs().terrainGpuPass.render(storage, sceneViewState.camera);
//                rs().chunkCpuPass.render(sceneViewState.camera);

                glStateBackup.restoreStates();
                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
            case RENDER_TRANSPARENT -> {
                // test
                glStateBackup.storeStates();
                int vbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);

                rs().gizmosPass.render(storage, sceneViewState.camera);

                glStateBackup.restoreStates();
                GL30.glBindVertexArray(0);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
            case RENDER_OVERLAY -> {
                InGameDebugHUDManager debugHudManager = storage.get(graphicsRuntimeServices.debugHudManager);
                debugHudManager.updateAndRenderIfNeeded();
            }
        }

        if (map != null) {
            Consumer<WorldContext<Graphics>> consumer = map.get(FramePhaseTiming.AFTER);
            if (consumer != null) {
                consumer.accept(this);
            }
        }
    }

    // test
    private GLStateBackup glStateBackup = new GLStateBackup();

    // test, temp
    public static boolean debug = false;

    @Override
    public void on(@NonNull FramePhase phase, @NonNull FramePhaseTiming timing, @NonNull Consumer<WorldContext<Graphics>> consumer) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkNotNull(timing);
        Preconditions.checkNotNull(consumer);

        Map<FramePhaseTiming, Consumer<WorldContext<Graphics>>> map = callbacks.computeIfAbsent(phase, k -> new Object2ObjectOpenHashMap<>());
        map.put(timing, consumer);
    }
}
