package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.analysis.install.AnalyticalWorldInstaller;
import com.cleanroommc.kirino.engine.graphics.install.GraphicsWorldInstaller;
import com.cleanroommc.kirino.engine.graphics.view.GraphicsWorldViewImpl;
import com.cleanroommc.kirino.engine.render.core.*;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.core.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.platform.MinecraftAssetProviders;
import com.cleanroommc.kirino.engine.render.platform.MinecraftIntegration;
import com.cleanroommc.kirino.engine.render.platform.SceneViewState;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.platform.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.render.platform.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.core.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.core.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.core.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.core.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.platform.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.core.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.core.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.WorldRunner;
import com.cleanroommc.kirino.engine.world.type.Graphics;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.analysis.view.AnalyticalWorldViewImpl;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.ForkJoinPool;

public class KirinoEngine {
    private final BootstrapResources bootstrapResources;
    private final GraphicsRuntimeServices graphicsRuntimeServices;
    private final SceneViewState sceneViewState;
    private final MinecraftIntegration minecraftIntegration;
    private final MinecraftAssetProviders minecraftAssetProviders;
    private final ShaderIntrospection shaderIntrospection;
    private final RenderStructure renderStructure;
    private final RenderExtensions renderExtensions;

    private final ResourceStorage storage;

    @Nullable
    public ResourceStorage getStorage() {
        if (storage.isSealed()) {
            return storage;
        } else {
            return null;
        }
    }

    private final WorldRunner<Graphics> graphicsWorld;
    private final WorldRunner<Headless> headlessWorld;

    /**
     * Side-effect free.
     */
    private KirinoEngine(
            EventBus eventBus,
            Logger logger,
            CleanECSRuntime ecsRuntime,
            boolean enableHDR,
            boolean enablePostProcessing) {

        ResourceLayout resourceLayout = MethodHolder.constructResourceLayout();
        storage = MethodHolder.constructResourceStorage();

        ResourceSlot<GLStateBackup> stateBackup = resourceLayout.slot(GLStateBackup.class);
        ResourceSlot<Renderer> renderer = resourceLayout.slot(Renderer.class);
        ResourceSlot<GraphicResourceManager> graphicResourceManager = resourceLayout.slot(GraphicResourceManager.class);
        ResourceSlot<IndirectDrawBufferGenerator> idbGenerator = resourceLayout.slot(IndirectDrawBufferGenerator.class);
        ResourceSlot<GizmosManager> gizmosManager = resourceLayout.slot(GizmosManager.class);
        ResourceSlot<VAO> fullscreenTriangleVao = resourceLayout.slot(VAO.class);
        ResourceSlot<ShaderRegistry> shaderRegistry = resourceLayout.slot(ShaderRegistry.class);
        ResourceSlot<FrameFinalizer> frameFinalizer = resourceLayout.slot(FrameFinalizer.class);
        ResourceSlot<VAO> dummyVao = resourceLayout.slot(VAO.class);
        ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry = resourceLayout.slot(MeshletGpuRegistry.class);
        ResourceSlot<BlockMeshGenerator> blockMeshGenerator = resourceLayout.slot(BlockMeshGenerator.class);
        ResourceSlot<StagingBufferManager> stagingBufferManager = resourceLayout.slot(StagingBufferManager.class);
        ResourceSlot<InGameDebugHUDManager> debugHudManager = resourceLayout.slot(InGameDebugHUDManager.class);

        ResourceSlot<ShaderProgram> terrainGpuPassProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> chunkCpuPassProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> gizmosPassProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> postProcessingDefaultProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> toneMappingPassProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> upscalingPassProgram = resourceLayout.slot(ShaderProgram.class);
        ResourceSlot<ShaderProgram> downscalingPassProgram = resourceLayout.slot(ShaderProgram.class);

        bootstrapResources = new BootstrapResources(
                frameFinalizer,
                idbGenerator,
                fullscreenTriangleVao,
                dummyVao);

        graphicsRuntimeServices = new GraphicsRuntimeServices(
                stateBackup,
                renderer,
                stagingBufferManager,
                graphicResourceManager,
                gizmosManager,
                debugHudManager,
                shaderRegistry);

        MinecraftCamera camera = new MinecraftCamera();
        MinecraftScene scene = new MinecraftScene(
                storage,
                ecsRuntime.entityManager,
                ecsRuntime.jobScheduler,
                blockMeshGenerator,
                gizmosManager,
                camera,
                meshletGpuRegistry,
                ForkJoinPool.commonPool(),
                ForkJoinPool.commonPool());

        sceneViewState = new SceneViewState(
                camera,
                scene,
                meshletGpuRegistry);

        MinecraftCulling cullingPatch = new MinecraftCulling();
        minecraftIntegration = new MinecraftIntegration(
                cullingPatch,
                new MinecraftEntityRendering(cullingPatch),
                new MinecraftTESRRendering(cullingPatch));

        minecraftAssetProviders = new MinecraftAssetProviders(blockMeshGenerator);

        shaderIntrospection = new ShaderIntrospection(
                new GLSLRegistry(),
                new DefaultShaderAnalyzer());

        renderStructure = new RenderStructure(
                enableHDR,
                enablePostProcessing,
                renderer,
                graphicResourceManager,
                idbGenerator,
                gizmosManager,
                fullscreenTriangleVao,
                terrainGpuPassProgram,
                chunkCpuPassProgram,
                gizmosPassProgram,
                toneMappingPassProgram,
                upscalingPassProgram,
                downscalingPassProgram);

        renderExtensions = new RenderExtensions(
                renderer,
                graphicResourceManager,
                idbGenerator,
                fullscreenTriangleVao,
                postProcessingDefaultProgram,
                terrainGpuPassProgram,
                chunkCpuPassProgram,
                gizmosPassProgram,
                toneMappingPassProgram,
                upscalingPassProgram,
                downscalingPassProgram);

        graphicsWorld = WorldRunner.of(
                new GraphicsWorldViewImpl(
                        ecsRuntime,
                        renderStructure,
                        renderExtensions,
                        eventBus,
                        logger,
                        storage,
                        bootstrapResources,
                        graphicsRuntimeServices,
                        minecraftIntegration,
                        minecraftAssetProviders,
                        sceneViewState,
                        shaderIntrospection),
                new GraphicsWorldInstaller());

        headlessWorld = WorldRunner.of(
                new AnalyticalWorldViewImpl(
                        ecsRuntime,
                        renderStructure,
                        renderExtensions,
                        eventBus,
                        logger,
                        shaderIntrospection),
                new AnalyticalWorldInstaller());
    }

    private boolean modeChosen = false;
    private boolean headlessMode = false;

    private final FramePhaseFSM framePhaseFsm = new FramePhaseFSM();

    public FramePhase nextExpectedPhase() {
        return framePhaseFsm.getState();
    }

    /**
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public void run(@NonNull FramePhase phase) {
        Preconditions.checkState(!modeChosen || !headlessMode,
                "The engine was running headlessly and it's not allowed to switch mode during runtime.");

        if (!modeChosen) {
            modeChosen = true;
            headlessMode = false;
        }

        Preconditions.checkState(framePhaseFsm.getState() == phase,
                "Expect to run \"%s\" but got \"%s\".", framePhaseFsm.getState(), phase);

        framePhaseFsm.next();

        headlessWorld.run(phase);
        graphicsWorld.run(phase);

        if (phase == FramePhase.PREPARE && !storage.isSealed()) {
            MethodHolder.sealResourceStorage(storage);
        }
    }

    /**
     * <p>Note: <b>must never be called manually by clients!</b></p>
     */
    public void runHeadlessly(@NonNull FramePhase phase) {
        Preconditions.checkState(!modeChosen || headlessMode,
                "The engine wasn't running headlessly and it's not allowed to switch mode during runtime.");

        if (!modeChosen) {
            modeChosen = true;
            headlessMode = true;
        }

        Preconditions.checkState(framePhaseFsm.getState() == phase,
                "Expect to run \"%s\" but got \"%s\".", framePhaseFsm.getState(), phase);

        framePhaseFsm.next();

        headlessWorld.run(phase);

        if (phase == FramePhase.PREPARE && !storage.isSealed()) {
            MethodHolder.sealResourceStorage(storage);
        }
    }

    private static class MethodHolder {
        static final Delegate DELEGATE;

        static {
            DELEGATE = new Delegate(
                    ReflectionUtils.getConstructor(ResourceLayout.class),
                    ReflectionUtils.getConstructor(ResourceStorage.class),
                    ReflectionUtils.getMethod(ResourceStorage.class, "seal", void.class));

            Preconditions.checkNotNull(DELEGATE.resourceLayoutCtor);
            Preconditions.checkNotNull(DELEGATE.resourceStorageCtor);
            Preconditions.checkNotNull(DELEGATE.resourceStorageSeal);
        }

        static ResourceLayout constructResourceLayout() {
            ResourceLayout result;
            try {
                result = (ResourceLayout) DELEGATE.resourceLayoutCtor.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static ResourceStorage constructResourceStorage() {
            ResourceStorage result;
            try {
                result = (ResourceStorage) DELEGATE.resourceStorageCtor.invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        static void sealResourceStorage(ResourceStorage storage) {
            try {
                DELEGATE.resourceStorageSeal.invokeExact(storage);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        record Delegate(
                MethodHandle resourceLayoutCtor,
                MethodHandle resourceStorageCtor,
                MethodHandle resourceStorageSeal) {
        }
    }
}
