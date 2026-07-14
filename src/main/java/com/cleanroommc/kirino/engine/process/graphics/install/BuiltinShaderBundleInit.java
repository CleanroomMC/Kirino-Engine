package com.cleanroommc.kirino.engine.process.graphics.install;

import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.engine.world.context.GraphicsWorldView;

/**
 * @see com.cleanroommc.kirino.engine.render.core.BuiltinShaderBundle
 */
public final class BuiltinShaderBundleInit {

    static void init(GraphicsWorldView context) {
        ResourceStorage storage = context.storage();

        storage.put(context.shaderb().postProcessingDefaultProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.shaderb().terrainGpuPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/opaque_terrain.vert", "forge:shaders/opaque_terrain.frag"));

        storage.put(context.shaderb().chunkCpuPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.shaderb().gizmosPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/gizmos.vert", "forge:shaders/gizmos.frag"));

        storage.put(context.shaderb().toneMappingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.shaderb().upscalingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.shaderb().downscalingPassProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/post_processing.vert", "forge:shaders/pp_default.frag"));

        storage.put(context.shaderb().meshletVertexGenComputeProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/meshlets2vertices.comp"));

        storage.put(context.shaderb().meshletDrawIndexGenComputeProgram,
                storage.get(context.graphicsb().shaderRegistry).newShaderProgram(
                        "forge:shaders/meshlet_draw_index_gen.comp"));

        storage.sealResource(context.shaderb().postProcessingDefaultProgram);
        storage.sealResource(context.shaderb().terrainGpuPassProgram);
        storage.sealResource(context.shaderb().chunkCpuPassProgram);
        storage.sealResource(context.shaderb().gizmosPassProgram);
        storage.sealResource(context.shaderb().toneMappingPassProgram);
        storage.sealResource(context.shaderb().upscalingPassProgram);
        storage.sealResource(context.shaderb().downscalingPassProgram);
        storage.sealResource(context.shaderb().meshletVertexGenComputeProgram);
        storage.sealResource(context.shaderb().meshletDrawIndexGenComputeProgram);
    }
}
