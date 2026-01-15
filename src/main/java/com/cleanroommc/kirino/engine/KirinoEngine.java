package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.analysis.install.AnalyticalWorldInstaller;
import com.cleanroommc.kirino.engine.gl.install.GLWorldInstaller;
import com.cleanroommc.kirino.engine.render.*;
import com.cleanroommc.kirino.engine.render.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.debug.hud.InGameDebugHUDManager;
import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftCulling;
import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftEntityRendering;
import com.cleanroommc.kirino.engine.render.minecraft.patch.MinecraftTESRRendering;
import com.cleanroommc.kirino.engine.render.minecraft.utils.BlockMeshGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.GLStateBackup;
import com.cleanroommc.kirino.engine.render.pipeline.Renderer;
import com.cleanroommc.kirino.engine.render.pipeline.draw.IndirectDrawBufferGenerator;
import com.cleanroommc.kirino.engine.render.pipeline.post.FrameFinalizer;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.shader.ShaderRegistry;
import com.cleanroommc.kirino.engine.render.staging.StagingBufferManager;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.WorldRunner;
import com.cleanroommc.kirino.engine.world.type.GL;
import com.cleanroommc.kirino.engine.world.type.Headless;
import com.cleanroommc.kirino.engine.analysis.view.AnalyticalWorldViewImpl;
import com.cleanroommc.kirino.engine.gl.view.GLWorldViewImpl;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.shader.analysis.DefaultShaderAnalyzer;
import com.cleanroommc.kirino.gl.shader.schema.GLSLRegistry;
import com.cleanroommc.kirino.gl.vao.VAO;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;

public class KirinoEngine {
    public final BootstrapResources bootstrapResources;
    public final GraphicsRuntimeServices graphicsRuntimeServices;
    public final SceneViewState sceneViewState;
    public final MinecraftIntegration minecraftIntegration;
    public final MinecraftAssetProviders minecraftAssetProviders;
    public final ShaderIntrospection shaderIntrospection;
    public final RenderStructure renderStructure;
    public final RenderExtensions renderExtensions;

    public final ResourceStorage resourceStorage;

    private final WorldRunner<GL> glWorld;
    private final WorldRunner<Headless> headlessWorld;

    private KirinoEngine(
            EventBus eventBus,
            Logger logger,
            CleanECSRuntime ecsRuntime,
            boolean enableHDR,
            boolean enablePostProcessing) {

        ResourceLayout resourceLayout = new ResourceLayout();
        resourceStorage = new ResourceStorage();

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

        glWorld = WorldRunner.of(
                new GLWorldViewImpl(
                        ecsRuntime,
                        renderStructure,
                        renderExtensions,
                        eventBus,
                        logger,
                        resourceStorage,
                        bootstrapResources,
                        graphicsRuntimeServices,
                        minecraftIntegration,
                        minecraftAssetProviders,
                        sceneViewState,
                        shaderIntrospection),
                new GLWorldInstaller());

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

    public void run(@NonNull FramePhase phase) {
        headlessWorld.run(phase);
        glWorld.run(phase);
    }
}
