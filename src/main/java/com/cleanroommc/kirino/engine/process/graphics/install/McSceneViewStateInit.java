package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletComputeSystem;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.MeshletGpuRegistry;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;

/**
 * @see com.cleanroommc.kirino.engine.render.usage.McSceneViewState
 */
public final class McSceneViewStateInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        MeshletGpuRegistry meshletGpuRegistry = new MeshletGpuRegistry();
        meshletGpuRegistry.lateInit();

        MeshletComputeSystem meshletComputeSystem = new MeshletComputeSystem(
                context.shaderb().meshletVertexGenComputeProgram,
                context.shaderb().meshletDrawIndexGenComputeProgram);
        meshletComputeSystem.lateInit();

        storage.put(context.mcscene().meshletGpuRegistry, meshletGpuRegistry);
        storage.put(context.mcscene().meshletComputeSystem, meshletComputeSystem);

        storage.sealResource(context.mcscene().meshletGpuRegistry);
        storage.sealResource(context.mcscene().meshletComputeSystem);
    }
}
