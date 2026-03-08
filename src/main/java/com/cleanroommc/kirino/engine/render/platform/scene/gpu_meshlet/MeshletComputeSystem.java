package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture1DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class MeshletComputeSystem {

    private final ResourceSlot<ShaderProgram> computeShader;
    private boolean shaderRunning = false;
    private long fence;
    private Texture1DAccessor counterTex; // vertexCounter & indexCounter
    private ByteBuffer texTempBuffer;
    private int rawVertexCount = 0;
    private int rawIndexCount = 0;

    /**
     * Be aware of the phase when calling this method.
     */
    public long getUintVertexCount() {
        return Integer.toUnsignedLong(rawVertexCount);
    }

    /**
     * Be aware of the phase when calling this method.
     */
    public long getUintIndexCount() {
        return Integer.toUnsignedLong(rawIndexCount);
    }

    public boolean isShaderRunning() {
        return shaderRunning;
    }

    public MeshletComputeSystem(ResourceSlot<ShaderProgram> computeShader) {
        this.computeShader = computeShader;
    }

    public void lateInit() {
        counterTex = new Texture1DAccessor(true, GLTexture.newDsaTex1D(2));
        counterTex.highlevel().alloc(
                false,
                BufferUtils.createByteBuffer(8).putInt(0).putInt(0).flip(),
                TextureFormat.R32UI);

        texTempBuffer = BufferUtils.createByteBuffer(8);
    }

    public void startDispatch(ResourceStorage storage, MeshletGpuRegistry meshletGpuRegistry) {
        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        shaderRunning = true;
        ShaderProgram program = storage.get(computeShader);

        // todo: abstract shader setup
        GL30.glBindBufferBase(meshletGpuRegistry.getConsumeTarget().target(), 0, meshletGpuRegistry.getConsumeTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getVertexWriteTarget().target(), 1, meshletGpuRegistry.getVertexWriteTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getIndexWriteTarget().target(), 2, meshletGpuRegistry.getIndexWriteTarget().bufferID);
        GL42.glBindImageTexture(3, counterTex.textureID(), 0, false, 0, GL15.GL_READ_WRITE, TextureFormat.R32UI.internalFormat);

        program.use();

        GL43.glDispatchCompute(meshletGpuRegistry.getMeshletCount(), 1, 1);
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT); // make image write visible to subsequent read

        fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public boolean tryPullResult() {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 0L);
        if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
            GL32C.glDeleteSync(fence);

            texTempBuffer.clear();
            counterTex.getTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    texTempBuffer);

            rawVertexCount = texTempBuffer.getInt(0);
            rawIndexCount = texTempBuffer.getInt(4);

            counterTex.clearTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    null);

            shaderRunning = false;
            return true;
        }

        return false;
    }
}
