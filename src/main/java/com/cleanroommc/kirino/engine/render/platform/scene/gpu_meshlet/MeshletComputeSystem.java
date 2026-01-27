package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;

public class MeshletComputeSystem {

    private final ResourceSlot<ShaderProgram> computeShader;

    public MeshletComputeSystem(ResourceSlot<ShaderProgram> computeShader) {
        this.computeShader = computeShader;
    }
}
