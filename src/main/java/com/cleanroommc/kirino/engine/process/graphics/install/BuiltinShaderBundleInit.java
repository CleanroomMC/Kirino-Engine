package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;

/**
 * @see com.cleanroommc.kirino.engine.render.core.BuiltinShaderBundle
 */
public final class BuiltinShaderBundleInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        storage.put(context.shaderbb().postProcessingDefaultProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/post_processing.vert", "kirino:shaders/pp_default.frag"));

        storage.put(context.shaderbb().terrainGpuPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/opaque_terrain.vert", "kirino:shaders/opaque_terrain.frag"));

        storage.put(context.shaderbb().chunkCpuPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/gizmos.vert", "kirino:shaders/gizmos.frag"));

        storage.put(context.shaderbb().gizmosPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/gizmos.vert", "kirino:shaders/gizmos.frag"));

        storage.put(context.shaderbb().toneMappingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/post_processing.vert", "kirino:shaders/pp_default.frag"));

        storage.put(context.shaderbb().upscalingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/post_processing.vert", "kirino:shaders/pp_default.frag"));

        storage.put(context.shaderbb().downscalingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/post_processing.vert", "kirino:shaders/pp_default.frag"));

        storage.put(context.shaderbb().meshletVertexGenComputeProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/meshlets2vertices.comp"));

        storage.put(context.shaderbb().meshletDrawIndexGenComputeProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "kirino:shaders/meshlet_draw_index_gen.comp"));

        storage.sealResource(context.shaderbb().postProcessingDefaultProgram);
        storage.sealResource(context.shaderbb().terrainGpuPassProgram);
        storage.sealResource(context.shaderbb().chunkCpuPassProgram);
        storage.sealResource(context.shaderbb().gizmosPassProgram);
        storage.sealResource(context.shaderbb().toneMappingPassProgram);
        storage.sealResource(context.shaderbb().upscalingPassProgram);
        storage.sealResource(context.shaderbb().downscalingPassProgram);
        storage.sealResource(context.shaderbb().meshletVertexGenComputeProgram);
        storage.sealResource(context.shaderbb().meshletDrawIndexGenComputeProgram);
    }
}
