package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.debug.shader.ShaderDebugResource;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer.MeshletConstants;
import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer.MeshletBufferCapacity;
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
    static final class InternalBuffers {

        private int maxDirtyListBytes;
        private int maxRangeBytes;
        private int currentRangeBytes;
        private int maxDispatchCount;

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
            maxRangeBytes = MeshletBufferCapacity.maximumBytes(GL32.glGetInteger64(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE));
            maxDirtyListBytes = MeshletBufferCapacity.maximumBytes((long) GL11.glGetInteger(GL31.GL_MAX_TEXTURE_BUFFER_SIZE) * 4);
            maxDispatchCount = GL30.glGetIntegeri(GL43.GL_MAX_COMPUTE_WORK_GROUP_COUNT, 0);
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

            prepareRanges(MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS);
        }

        void prepareRanges(int meshletCount) {
            int required = MeshletBufferCapacity.checkedBytes("Meshlet ranges", meshletCount, 12, 0, maxRangeBytes);
            if (required <= currentRangeBytes) return;
            SSBOView replacement = new SSBOView(new GLBuffer());
            int previous = replacement.fetchCurrentBoundBufferID();
            try {
                replacement.bind();
                replacement.allocImmutable(required, 0);
                if (replacement.fetchBufferSize64() != required) {
                    throw new IllegalStateException("Failed to allocate meshlet ranges: requiredBytes=" + required);
                }
                replacement.clearUint0();
                if (rangeSsbo != null) {
                    rangeSsbo.copyTo(replacement, 0, 0, currentRangeBytes);
                }
                GL42.glMemoryBarrier(GL42.GL_BUFFER_UPDATE_BARRIER_BIT | GL43.GL_SHADER_STORAGE_BARRIER_BIT);
            } catch (Throwable failure) {
                replacement.buffer.disposeManually();
                throw failure;
            } finally {
                replacement.bind(previous);
            }
            if (rangeSsbo != null) {
                rangeSsbo.buffer.disposeManually();
            }
            rangeSsbo = replacement;
            currentRangeBytes = required;
        }

        void resetForWorld() {
            if (counterSsbo != null) {
                int previous = counterSsbo.fetchCurrentBoundBufferID();
                counterSsbo.bind();
                counterSsbo.clearUint0();
                counterSsbo.bind(previous);
            }
            if (rangeSsbo != null) {
                int previous = rangeSsbo.fetchCurrentBoundBufferID();
                rangeSsbo.bind();
                rangeSsbo.clearUint0();
                rangeSsbo.bind(previous);
            }
        }

        /**
         * @return The compute dispatch count
         */
        int prepareTbo(List<Integer> dirtyList) {
            int required = MeshletBufferCapacity.checkedBytes("Meshlet dirty list", dirtyList.size(), 4, 8, maxDirtyListBytes);
            if (required > currentTboWorkspaceSize) {
                ByteBuffer replacement = BufferUtils.createByteBuffer(required);

                int prevID = tboWorkspace.fetchCurrentBoundBufferID();
                tboWorkspace.bind();
                tboWorkspace.alloc(required, BufferUploadHint.STATIC_DRAW);
                tboWorkspace.bind(prevID);
                tboTempByteBuffer = replacement;
                currentTboWorkspaceSize = required;
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
                    required);

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

    public void resetForWorld() {
        if (fence != 0) {
            GL32C.glDeleteSync(fence);
            fence = 0;
        }
        buffers.resetForWorld();
        vertexCount = 0;
        indexCount = 0;
        shaderRunning = false;
    }

    public void startDispatch(
            ResourceStorage storage,
            MeshletGpuRegistry meshletGpuRegistry,
            int meshletCount) {

        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        ShaderProgram vertexGenProgram = storage.get(vertexGenCompute);
        ShaderProgram drawIndexGenProgram = storage.get(drawIndexGenCompute);

        buffers.prepareRanges(meshletCount);
        int dispatchCount = buffers.prepareTbo(meshletGpuRegistry.getDirtySlots());

        shaderRunning = true;

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
        buffers.dirtyListTbo.bindUnit(4); // no one is using 4 atm; todo: refactor

        ShaderDebugResource.RESOURCE.setDispatchCount(dispatchCount);
        KirinoCommonCore.LOGGER.info("dispatch " + dispatchCount);

        int dirtyOffset = GL20.glGetUniformLocation(vertexGenProgram.getProgramID(), "dirtyOffset");
        for (int offset = 0; offset < dispatchCount;) {
            int batch = Math.min(dispatchCount - offset, buffers.maxDispatchCount);
            GL30.glUniform1ui(dirtyOffset, offset);
            GL43.glDispatchCompute(batch, 1, 1);
            offset += batch;
        }
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        drawIndexGenProgram.use();

        GL30.glUniform1ui(GL20.glGetUniformLocation(drawIndexGenProgram.getProgramID(), "meshletCap"), meshletCount);

        GL43.glDispatchCompute(1, 1, 1);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        Preconditions.checkState(fence != 0, "Failed to create the meshlet compute fence.");
    }

    public boolean tryPullResult() {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 0L);
        if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
            GL32C.glDeleteSync(fence);
            fence = 0;

            ByteBuffer byteBuffer = buffers.counterSsbo.getPersistentMappedBuffer().orElseThrow();

            vertexCount = byteBuffer.getInt(0);
            indexCount = byteBuffer.getInt(4);

//            ShaderDebugResource.RESOURCE.readAndPrint();

            shaderRunning = false;
            return true;
        }

        Preconditions.checkState(waitReturn == GL32.GL_TIMEOUT_EXPIRED, "Failed to wait for the meshlet compute fence.");
        return false;
    }
}
