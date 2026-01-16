package com.cleanroommc.kirino.engine.render.platform;

import com.cleanroommc.kirino.engine.render.core.camera.MinecraftCamera;
import com.cleanroommc.kirino.engine.render.platform.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;

public class SceneViewState {
    public final MinecraftCamera camera;
    public final MinecraftScene scene;
    public final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

    public SceneViewState(
            MinecraftCamera camera,
            MinecraftScene scene,
            ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {

        this.camera = camera;
        this.scene = scene;
        this.meshletGpuRegistry = meshletGpuRegistry;
    }
}
