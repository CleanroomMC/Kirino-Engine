package com.cleanroommc.kirino.engine.render.core;

import com.cleanroommc.kirino.engine.resource.ResourceLayout;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

/**
 * @see com.cleanroommc.kirino.engine.process.graphics.install.BuiltinShaderBundleInit
 */
public final class BuiltinShaderBundle {

    public final ResourceSlot<ShaderProgram> postProcessingDefaultProgram;
    public final ResourceSlot<ShaderProgram> terrainGpuPassProgram;
    public final ResourceSlot<ShaderProgram> chunkCpuPassProgram;
    public final ResourceSlot<ShaderProgram> gizmosPassProgram;
    public final ResourceSlot<ShaderProgram> toneMappingPassProgram;
    public final ResourceSlot<ShaderProgram> upscalingPassProgram;
    public final ResourceSlot<ShaderProgram> downscalingPassProgram;
    public final ResourceSlot<ShaderProgram> meshletVertexGenComputeProgram;
    public final ResourceSlot<ShaderProgram> meshletDrawIndexGenComputeProgram;

    public BuiltinShaderBundle(@NonNull ResourceLayout resourceLayout) {
        Preconditions.checkNotNull(resourceLayout);

        postProcessingDefaultProgram = resourceLayout.slot(ShaderProgram.class, "post_process_default_shader");
        terrainGpuPassProgram = resourceLayout.slot(ShaderProgram.class, "terrain_gpu_pass_shader");
        chunkCpuPassProgram = resourceLayout.slot(ShaderProgram.class, "chunk_cpu_pass_shader");
        gizmosPassProgram = resourceLayout.slot(ShaderProgram.class, "gizmos_pass_shader");
        toneMappingPassProgram = resourceLayout.slot(ShaderProgram.class, "tone_mapping_pass_shader");
        upscalingPassProgram = resourceLayout.slot(ShaderProgram.class, "upscaling_pass_shader");
        downscalingPassProgram = resourceLayout.slot(ShaderProgram.class, "downscaling_pass_program");
        meshletVertexGenComputeProgram = resourceLayout.slot(ShaderProgram.class, "meshlet_vertex_gen_compute_shader");
        meshletDrawIndexGenComputeProgram = resourceLayout.slot(ShaderProgram.class, "meshlet_draw_index_gen_compute_shader");
    }
}
