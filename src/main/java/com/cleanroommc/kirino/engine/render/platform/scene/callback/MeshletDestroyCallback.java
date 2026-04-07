package com.cleanroommc.kirino.engine.render.platform.scene.callback;

import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import org.jspecify.annotations.NonNull;

public class MeshletDestroyCallback implements EntityDestroyCallback {

    private final ResourceStorage storage;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

    public MeshletDestroyCallback(ResourceStorage storage, ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {
        this.storage = storage;
        this.meshletGpuRegistry = meshletGpuRegistry;
    }

    @Override
    public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
        MeshletComponent meshletComponent = (MeshletComponent) destroyContext.getComponent(MeshletComponent.class);
        storage.get(meshletGpuRegistry).disposeMeshletID(meshletComponent.meshletId);
    }
}
