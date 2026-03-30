package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer.MeshletConstants;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.BufferUploadHint;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.ShaderProgram;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture1DAccessor;
import com.cleanroommc.kirino.gl.texture.accessor.TextureBufferAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.List;

public class MeshletComputeSystem {

    private final ResourceSlot<ShaderProgram> computeShader;
    private boolean shaderRunning = false;
    private long fence;

    // record global vertex/index count
    private Texture1DAccessor counterTex; // vertexCounter & indexCounter
    private ByteBuffer texTempByteBuffer;

    // record dirty meshlet slot indices
    private TextureBufferAccessor dirtyListTbo;
    private VBOView tboWorkspace;
    private ByteBuffer tboTempByteBuffer;
    private int currentTboWorkspaceSize = 1024 * 4; // 1024 ints

    // record meshlet ranges of output vertex/index
    private SSBOView rangeSsbo;

    public SSBOView getRangeSsbo() {
        return rangeSsbo;
    }

    private final static int MAX_DIRTY_LIST_BYTES = 4 * MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS;
    private final static int MAX_RANGE_BYTES = 12 * MeshletConstants.WORST_CASE_MESHLET_COUNT_IN_R8_16CUBIC_CHUNKS;

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

        texTempByteBuffer = BufferUtils.createByteBuffer(8);

        dirtyListTbo = new TextureBufferAccessor(true, GLTexture.newDsaTexBuffer());
        tboTempByteBuffer = BufferUtils.createByteBuffer(MAX_DIRTY_LIST_BYTES);
        tboWorkspace = new VBOView(new GLBuffer());
        tboWorkspace.bind();
        tboWorkspace.alloc(currentTboWorkspaceSize, BufferUploadHint.STATIC_DRAW);
        tboWorkspace.clearUint0();
        tboWorkspace.bind(0);

        rangeSsbo = new SSBOView(new GLBuffer());
        rangeSsbo.bind();
        rangeSsbo.allocPersistent(MAX_RANGE_BYTES, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        rangeSsbo.clearUint0();
        rangeSsbo.mapPersistent(0, MAX_RANGE_BYTES, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        rangeSsbo.bind(0);
    }

    /**
     * @return The compute dispatch count
     */
    private int prepareTbo(List<Integer> dirtyList) {
        int padding = 1;
        int size = dirtyList.size() + 1 + padding; // header=1 int + extra padding
        if (size * 4 > currentTboWorkspaceSize) {
            currentTboWorkspaceSize = size * 4;
            Preconditions.checkState(currentTboWorkspaceSize <= MAX_DIRTY_LIST_BYTES,
                    "Dirty list TBO workspace overflow. %s bytes exceeds 512KB.", currentTboWorkspaceSize);

            int prevID = tboWorkspace.fetchCurrentBoundBufferID();
            tboWorkspace.bind();
            tboWorkspace.alloc(currentTboWorkspaceSize, BufferUploadHint.STATIC_DRAW);
            tboWorkspace.clearUint0();
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

        // note: 0 = 0 (mod n) for all n=alignment
        dirtyListTbo.texBufferRange(
                TextureFormat.R32UI.internalFormat,
                tboWorkspace.bufferID,
                0,
                size * 4L);

        return dirtyList.size();
    }

    public void startDispatch(ResourceStorage storage, MeshletGpuRegistry meshletGpuRegistry) {
        Preconditions.checkState(!shaderRunning, "Compute shader must not be running already.");

        shaderRunning = true;
        ShaderProgram program = storage.get(computeShader);

        int dispatchCount = prepareTbo(meshletGpuRegistry.getDirtySlots());

        // todo: abstract shader setup
        GL30.glBindBufferBase(meshletGpuRegistry.getConsumeTarget().target(), 0, meshletGpuRegistry.getConsumeTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getVertexWriteTarget().target(), 1, meshletGpuRegistry.getVertexWriteTarget().bufferID);
        GL30.glBindBufferBase(meshletGpuRegistry.getIndexWriteTarget().target(), 2, meshletGpuRegistry.getIndexWriteTarget().bufferID);
        GL42.glBindImageTexture(3, counterTex.textureID(), 0, false, 0, GL15.GL_READ_WRITE, TextureFormat.R32UI.internalFormat);
        GL30.glBindBufferBase(rangeSsbo.target(), 4, rangeSsbo.bufferID);

        program.use();

        GL20.glUniform1i(GL20.glGetUniformLocation(program.getProgramID(), "dirtyList"), 4);
        dirtyListTbo.unit(4); // no one is using 4 atm; temp

        GL43.glDispatchCompute(dispatchCount, 1, 1);
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT | GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
    }

    public boolean tryPullResult() {
        Preconditions.checkState(shaderRunning, "Compute shader must be running.");

        int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 0L);
        if (waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED) {
            GL32C.glDeleteSync(fence);

            texTempByteBuffer.clear();
            counterTex.getTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    texTempByteBuffer);

            rawVertexCount = texTempByteBuffer.getInt(0);
            rawIndexCount = texTempByteBuffer.getInt(4);

            KirinoCommonCore.LOGGER.info("global vertex count: " + rawVertexCount);
            KirinoCommonCore.LOGGER.info("global index count: " + rawIndexCount);

            shaderRunning = false;
            return true;
        }

        return false;
    }
}
