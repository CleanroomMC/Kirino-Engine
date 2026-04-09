package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.shader.ShaderDebugResource;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer.MeshletConstants;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.TextureBufferAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.List;

public class MeshletComputeSystem {

    // these buffers won't face RAW/WAR hazards. no need to do double buffering
    private static final class InternalBuffers {

        private final static int MAX_DIRTY_LIST_BYTES = 4 * (MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS + 2);
        private final static int MAX_RANGE_BYTES = 12 * MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS;

        // record global vertex/index count
        private SSBOView counterSsbo;

        // record dirty meshlet slot indices
        private TextureBufferAccessor dirtyListTbo;
        private VBOView tboWorkspace;
        private ByteBuffer tboTempByteBuffer;
        private int currentTboWorkspaceSize;

        // record meshlet ranges of output vertex/index
        private SSBOView rangeSsbo;

        void lateInit() {
            counterSsbo = new SSBOView(new GLBuffer());
            counterSsbo.bind();
            counterSsbo.allocPersistent(8, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            counterSsbo.clearUint0(); // must clear since compute reads and writes
            counterSsbo.mapPersistent(0, 8, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            counterSsbo.bind(0);

            currentTboWorkspaceSize = 1024 * 4; // 1024 ints
            dirtyListTbo = new TextureBufferAccessor(true, GLTexture.newDsaTexBuffer());
            tboTempByteBuffer = BufferUtils.createByteBuffer(currentTboWorkspaceSize);
            tboWorkspace = new VBOView(new GLBuffer());
            tboWorkspace.bind();
            tboWorkspace.alloc(currentTboWorkspaceSize, BufferUploadHint.STATIC_DRAW);
            tboWorkspace.bind(0);

            rangeSsbo = new SSBOView(new GLBuffer());
            rangeSsbo.bind();
            rangeSsbo.allocPersistent(MAX_RANGE_BYTES, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            rangeSsbo.clearUint0(); // must clear since compute reads and writes
            rangeSsbo.mapPersistent(0, MAX_RANGE_BYTES, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            rangeSsbo.bind(0);
        }

        /**
         * @return The compute dispatch count
         */
        int prepareTbo(List<Integer> dirtyList) {
            int padding = 1;
            int size = dirtyList.size() + 1 + padding; // (header=1 int) + extra padding
            if (size * 4 > currentTboWorkspaceSize) {
                currentTboWorkspaceSize = size * 4;
                Preconditions.checkState(currentTboWorkspaceSize <= MAX_DIRTY_LIST_BYTES,
                        "Dirty list TBO workspace overflow. %s bytes exceeds MAX_DIRTY_LIST_BYTES=%s.", currentTboWorkspaceSize, MAX_DIRTY_LIST_BYTES);

                tboTempByteBuffer = BufferUtils.createByteBuffer(currentTboWorkspaceSize);

                int prevID = tboWorkspace.fetchCurrentBoundBufferID();
                tboWorkspace.bind();
                tboWorkspace.alloc(currentTboWorkspaceSize, BufferUploadHint.STATIC_DRAW);
                tboWorkspace.bind(prevID);
            }

            tboTempByteBuffer.clear();
            tboTempByteBuffer.putInt(dirtyList.size());
            for (int dirtyIndex : dirtyList) {
                tboTempByteBuffer.putInt(dirtyIndex);
            }
            tboTempByteBuffer.flip();

            int prevID = tboWorkspace.fetchCurrentBoundBufferID();
            tboWorkspace.bind();
            tboWorkspace.uploadBySubData(0, tboTempByteBuffer);
            tboWorkspace.bind(prevID);

            // note: offset 0 is always safe for any alignment
            dirtyListTbo.texBufferRange(
                    TextureFormat.R32UI.internalFormat,
                    tboWorkspace.bufferID,
                    0,
                    size * 4L);

            return dirtyList.size();
        }
    }

    private final ResourceSlot<ShaderProgram> vertexGenCompute;
    private final ResourceSlot<ShaderProgram> drawIndexGenCompute;

    private boolean shaderRunning = false;
    private long fence;
    private final InternalBuffers buffers;

    // integer is big enough for vertex and index count
    // see MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS
    private int vertexCount = 0;
    private int indexCount = 0;

    /**
     * Must only be accessed right after <code>{@link #tryPullResult()} == true</code>.
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Must only be accessed right after <code>{@link #tryPullResult()} == true</code>.
     */
    public int getIndexCount() {
        return indexCount;
    }

    public boolean isShaderRunning() {
        return shaderRunning;
    }

    public MeshletComputeSystem(
            ResourceSlot<ShaderProgram> vertexGenCompute,
            ResourceSlot<ShaderProgram> drawIndexGenCompute) {
        this.vertexGenCompute = vertexGenCompute;
        this.drawIndexGenCompute = drawIndexGenCompute;
        buffers = new InternalBuffers();
    }

    public void lateInit() {
        buffers.lateInit();
    }

    public void startDispatch(
            ResourceStorage storage,
            MeshletGpuRegistry meshletGpuRegistry,
            int meshletCount) {

        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        shaderRunning = true;

        ShaderProgram vertexGenProgram = storage.get(vertexGenCompute);
        ShaderProgram drawIndexGenProgram = storage.get(drawIndexGenCompute);

        int dispatchCount = buffers.prepareTbo(meshletGpuRegistry.getDirtySlots());

        // todo: abstract shader setup
        GL30.glBindBufferBase(meshletGpuRegistry.getConsumeTarget().target(), 0, meshletGpuRegistry.getConsumeTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getVertexWriteTarget().target(), 1, meshletGpuRegistry.getVertexWriteTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getIndexWriteTarget().target(), 2, meshletGpuRegistry.getIndexWriteTarget().bufferID);
        GL30.glBindBufferBase(buffers.counterSsbo.target(), 3, buffers.counterSsbo.bufferID);
        GL30.glBindBufferBase(buffers.rangeSsbo.target(), 4, buffers.rangeSsbo.bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getDrawIndexWriteTarget().target(), 5, meshletGpuRegistry.getDrawIndexWriteTarget().bufferID);

        GL30.glBindBufferBase(ShaderDebugResource.RESOURCE.getSsboCounter().target(), 15, ShaderDebugResource.RESOURCE.getSsboCounter().bufferID);
        GL30.glBindBufferBase(ShaderDebugResource.RESOURCE.getSsboVec3().target(), 14, ShaderDebugResource.RESOURCE.getSsboVec3().bufferID);
        GL30.glBindBufferBase(ShaderDebugResource.RESOURCE.getSsboTemp().target(), 13, ShaderDebugResource.RESOURCE.getSsboTemp().bufferID);

        vertexGenProgram.use();

        GL20.glUniform1i(GL20.glGetUniformLocation(vertexGenProgram.getProgramID(), "dirtyList"), 4);
        buffers.dirtyListTbo.unit(4); // no one is using 4 atm; todo: refactor

        ShaderDebugResource.RESOURCE.setDispatchCount(dispatchCount);
        KirinoCommonCore.LOGGER.info("dispatch " + dispatchCount);

        GL43.glDispatchCompute(dispatchCount, 1, 1);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        drawIndexGenProgram.use();

        GL30.glUniform1ui(GL20.glGetUniformLocation(drawIndexGenProgram.getProgramID(), "meshletCap"), meshletCount);

        GL43.glDispatchCompute(1, 1, 1);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public boolean tryPullResult() {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 0L);
        if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
            GL32C.glDeleteSync(fence);

            ByteBuffer byteBuffer = buffers.counterSsbo.getPersistentMappedBuffer().orElseThrow();

            vertexCount = byteBuffer.getInt(0);
            indexCount = byteBuffer.getInt(4);

//            ShaderDebugResource.RESOURCE.readAndPrint();

            shaderRunning = false;
            return true;
        }

        return false;
    }
}
