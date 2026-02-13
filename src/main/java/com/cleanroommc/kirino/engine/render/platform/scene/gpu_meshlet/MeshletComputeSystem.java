package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;

public class MeshletComputeSystem {

    private final ResourceSlot<ShaderProgram> computeShader;
    private boolean shaderRunning = false;

    public boolean isShaderRunning() {
        return shaderRunning;
    }

    public MeshletComputeSystem(ResourceSlot<ShaderProgram> computeShader) {
        this.computeShader = computeShader;
    }

    public void startDispatch(ResourceStorage storage) {
        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        shaderRunning = true;
        ShaderProgram program = storage.get(computeShader);
    }

    public boolean tryPullResult(ResourceStorage storage) {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        ShaderProgram program = storage.get(computeShader);

        if (false) {
            shaderRunning = false;
            return true;
        }

        return false;
    }
}
