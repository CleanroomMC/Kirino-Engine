package com.cleanroommc.kirino.engine.render.usage;

import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.engine.ShutdownManager;
import com.cleanroommc.kirino.engine.render.core.GraphicsRuntimeBundle;
import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.usage.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.utils.ForkJoinPoolUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ForkJoinPool;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.McSceneViewStateInit
 */
public final class McSceneViewState {

    public final MinecraftCamera camera;
    public final MinecraftScene scene;
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

        camera = new MinecraftCamera();

        ForkJoinPool systemFlowPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoMinecraftSystemFlow");
        ForkJoinPool systemPool = ForkJoinPoolUtils.newWorkStealingPool("KirinoMinecraftSystem");

        ShutdownManager.registerAsync(() -> {
            ForkJoinPoolUtils.shutdownPool(systemFlowPool, 5);
            ForkJoinPoolUtils.shutdownPool(systemPool, 5);
        });

        meshletGpuRegistry = resourceLayout.slot(MeshletGpuRegistry.class, "meshlet_gpu_registry");
        meshletComputeSystem = resourceLayout.slot(MeshletComputeSystem.class, "meshlet_compute_system");

        scene = new MinecraftScene(
                storage,
                ecsRuntime.entityManager,
                ecsRuntime.jobScheduler,
                mcIntegrationBundle.blockMeshGenerator,
                graphicsRuntimeBundle.gizmosManager,
                camera,
                meshletGpuRegistry,
                meshletComputeSystem,
                systemFlowPool,
                systemPool);
    }
}
