package com.cleanroommc.kirino.engine.render.platform.scene.callback;

import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import org.jspecify.annotations.NonNull;

public class MeshletCreateCallback implements EntityCreateCallback {

    private final ResourceStorage storage;
    private final ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry;

    public MeshletCreateCallback(ResourceStorage storage, ResourceSlot<MeshletGpuRegistry> meshletGpuRegistry) {
        this.storage = storage;
        this.meshletGpuRegistry = meshletGpuRegistry;
    }

    @Override
    public void beforeCreate(@NonNull EntityCreateContext createContext) {
        MeshletComponent meshletComponent = (MeshletComponent) createContext.getComponent(MeshletComponent.class);
        storage.get(meshletGpuRegistry).allocateMeshletID(meshletComponent);
    }
}
