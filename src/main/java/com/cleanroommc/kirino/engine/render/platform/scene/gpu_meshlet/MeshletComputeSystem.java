package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.google.common.base.Preconditions;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

public class MeshletComputeSystem {

    private final ResourceSlot<ShaderProgram> computeShader;
    private boolean shaderRunning = false;
    private long fence;
    private SSBOView counterSsbo; // vertexCounter & indexCounter
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
        counterSsbo = new SSBOView(new GLBuffer());

        int size = 8; // two integers

        counterSsbo.bind();
        counterSsbo.allocPersistent(size, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        counterSsbo.mapPersistent(0, size, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        counterSsbo.bind(0);

        ByteBuffer byteBuffer = counterSsbo
                .getPersistentMappedBuffer()
                .orElseThrow(() -> new IllegalStateException("SSBO not mapped."));

        byteBuffer.putInt(0, 0);
        byteBuffer.putInt(4, 0);
    }

    public void startDispatch(ResourceStorage storage, MeshletGpuRegistry meshletGpuRegistry) {
        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        shaderRunning = true;
        ShaderProgram program = storage.get(computeShader);

        GL30.glBindBufferBase(meshletGpuRegistry.getConsumeTarget().target(), 0, meshletGpuRegistry.getConsumeTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getVertexWriteTarget().target(), 1, meshletGpuRegistry.getVertexWriteTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getIndexWriteTarget().target(), 2, meshletGpuRegistry.getIndexWriteTarget().bufferID);
        GL30.glBindBufferBase(counterSsbo.target(), 3, counterSsbo.bufferID);

        program.use();

        GL42.glMemoryBarrier(GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT); // make persistently mapped buffer visible (just finished the writing task)
        GL43.glDispatchCompute(meshletGpuRegistry.getMeshletCount(), 1, 1);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT); // needed for subsequent ssbo reading in shaders

        fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public boolean tryPullResult() {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 0L);
        if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
            GL32C.glDeleteSync(fence);
            GL42.glMemoryBarrier(GL44.GL_CLIENT_MAPPED_BUFFER_BARRIER_BIT);

            KirinoCommonCore.LOGGER.info("finished compute");

            ByteBuffer byteBuffer = counterSsbo
                    .getPersistentMappedBuffer()
                    .orElseThrow(() -> new IllegalStateException("SSBO not mapped."));

            rawVertexCount = byteBuffer.getInt(0);
            rawIndexCount = byteBuffer.getInt(4);

            byteBuffer.putInt(0, 0);
            byteBuffer.putInt(4, 0);

            shaderRunning = false;
            return true;
        }

        return false;
    }
}
