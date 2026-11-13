package com.cleanroommc.kirino.engine.render.usage;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.GraphicsRuntimeBundle;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.usage.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletRenderPayload;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.utils.ForkJoinPoolUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.MinecraftForge;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.McSceneViewStateInit
 */
public final class McSceneViewState {

    public final MinecraftCamera camera;
    private MinecraftScene scene;
    private final WorldSceneLifecycle<WorldClient> lifecycle;
    public final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;
    public final ResourceSlot<MeshletComputeSystem> meshletComputeSystem;

    public McSceneViewState(
        @NonNull ResourceStorage storage,
        @NonNull ResourceLayout resourceLayout,
        @NonNull CleanECSRuntime ecsRuntime,
        @NonNull GraphicsRuntimeBundle graphicsRuntimeBundle,
        @NonNull McIntegrationBundle mcIntegrationBundle) {

        Preconditions.checkNotNull(storage);
        Preconditions.checkNotNull(resourceLayout);
        Preconditions.checkNotNull(ecsRuntime);
        Preconditions.checkNotNull(graphicsRuntimeBundle);
        Preconditions.checkNotNull(mcIntegrationBundle);
        CleanECSRuntime sceneRuntime = ecsRuntime.newSession();

        camera = new MinecraftCamera();

        ForkJoinPool systemFlowPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoMinecraftSystemFlow");
        ForkJoinPool systemPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoMinecraftSystem");

        Runnable workerShutdown = () -> {
            sceneRuntime.quiesce().handle((unused, failure) -> null).join();
            ForkJoinPoolUtils.shutdownPool(systemFlowPool, 5);
            ForkJoinPoolUtils.shutdownPool(systemPool, 5);
        };
        ShutdownManager.registerAsync(workerShutdown);

        meshletGpuRegistry = resourceLayout.slot(MeshletGpuRegistry.class, "meshlet_gpu_registry");
        meshletComputeSystem = resourceLayout.slot(MeshletComputeSystem.class, "meshlet_compute_system");

        Supplier<MinecraftScene> createScene = () -> new MinecraftScene(
            storage,
            sceneRuntime.entityManager,
            sceneRuntime.jobScheduler,
            mcIntegrationBundle.blockMeshGenerator,
            graphicsRuntimeBundle.gizmosManager,
            camera,
            meshletGpuRegistry,
            meshletComputeSystem,
            systemFlowPool,
            systemPool);
        scene = createScene.get();
        lifecycle = new WorldSceneLifecycle<>(new WorldSceneLifecycle.Backend<>() {
            private final SceneGpuFence gpuFence = new SceneGpuFence();

            public void attach(WorldClient world) {
                if (scene == null) {
                    scene = createScene.get();
                }
                scene.tryUpdateWorld(world);
            }

            public void detach() {
                if (scene != null) {
                    scene.detachWorld();
                }
            }

            public java.util.concurrent.CompletableFuture<Void> quiesce() {
                return sceneRuntime.quiesce();
            }

            public void beginGpuDrain() {
                if (storage.has(meshletComputeSystem)) {
                    gpuFence.begin();
                }
            }

            public boolean gpuReady() {
                return gpuFence.ready();
            }

            public void awaitGpu() {
                gpuFence.await();
            }

            public void reset() {
                try {
                    sceneRuntime.reset();
                } catch (Throwable cause) {
                    if (sceneRuntime.state() != CleanECSRuntime.State.RUNNING) {
                        throw cause;
                    }
                    KirinoCommonCore.LOGGER.error("Previous scene stopped with execution errors.", cause);
                }
                releaseScene();
            }

            public void close() {
                try {
                    sceneRuntime.close();
                } catch (Throwable cause) {
                    if (sceneRuntime.state() != CleanECSRuntime.State.CLOSED) {
                        throw cause;
                    }
                    KirinoCommonCore.LOGGER.error("ECS shutdown completed with errors.", cause);
                }
                releaseScene();
            }

            private void releaseScene() {
                if (scene != null) {
                    scene.releaseWorldReferences();
                    scene = null;
                }
                if (storage.has(meshletComputeSystem)) {
                    storage.get(meshletComputeSystem).resetForWorld();
                }
                if (storage.has(meshletGpuRegistry)) {
                    storage.get(meshletGpuRegistry).resetForWorld();
                }
                if (storage.has(graphicsRuntimeBundle.gizmosManager)) {
                    storage.get(graphicsRuntimeBundle.gizmosManager).clearMeshlets();
                }
            }

            public void update() {
                scene.update();
            }
        });

        SceneMenuTicker menuTicker = new SceneMenuTicker(() -> Minecraft.getMinecraft().world == null,
            () -> lifecycle.update(null));
        MinecraftForge.EVENT_BUS.register(menuTicker);

        ShutdownManager.registerBeforeResources(() -> {
            lifecycle.close();
            MinecraftForge.EVENT_BUS.unregister(menuTicker);
            ForkJoinPoolUtils.shutdownPool(systemFlowPool, 5);
            ForkJoinPoolUtils.shutdownPool(systemPool, 5);
            ShutdownManager.unregisterAsync(workerShutdown);
        });
    }

    public void updateWorld(WorldClient world) {
        lifecycle.update(world);
    }

    public void notifyBlockUpdate(int x, int y, int z, IBlockState previous, IBlockState next) {
        if (scene != null) {
            scene.notifyBlockUpdate(x, y, z, previous, next);
        }
    }

    public void notifyLightUpdate(int x, int y, int z) {
        if (scene != null) {
            scene.notifyLightUpdate(x, y, z);
        }
    }

    public boolean isMeshletRenderReady() {
        return scene != null && scene.isMeshletRenderReady();
    }

    public MeshletRenderPayload getMeshletRenderPayload() {
        return scene == null ? new MeshletRenderPayload(0, 0) : scene.getMeshletRenderPayload();
    }
}
