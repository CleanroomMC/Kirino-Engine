package com.cleanroommc.kirino.engine.render.pipeline.draw;

import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;
import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Optional;

public class IndirectDrawBufferGenerator {
    private final ByteBuffer buildingAreaByteBuffer;
    private final IDBView idbView;
    public final int bufferSize;

    public final static int IDB_STRIDE_BYTE = 5 * Integer.BYTES;

    private int offset = 0;

    public void reset() {
        offset = 0;
    }

    public IndirectDrawBufferGenerator(int bufferSize) {
        this.bufferSize = bufferSize;
        buildingAreaByteBuffer = BufferUtils.createByteBuffer(bufferSize);
        idbView = new IDBView(new GLBuffer());
        idbView.bind();
        idbView.allocPersistent(bufferSize, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        idbView.mapPersistent(0, bufferSize, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        idbView.bind(0);
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>units</code> are all <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> typed commands</li>
     *     <li><code>units</code> all share the same <code>vao</code>, <code>mode</code> and <code>elementType</code>, which are the parameters of this method call</li>
     * </ul>
     *
     * Combines units and generates a <code>MULTI_ELEMENTS_INDIRECT</code> typed command, and this is an OpenGL operation free method call.
     * By the way, <code>units</code> will be recycled automatically here.
     *
     * @param units Low-level <code>MULTI_ELEMENTS_INDIRECT_UNIT</code> typed commands
     * @return <code>MULTI_ELEMENTS_INDIRECT</code> typed command
     */
    public LowLevelDC generate(List<LowLevelDC> units, int vao, int mode, int elementType) {
        int idbBufferSize = units.size() * IDB_STRIDE_BYTE;

        Preconditions.checkArgument(offset + idbBufferSize <= bufferSize,
                "Too many commands (%s) being passed, resulting in overflow. Current offset: %s; Input size: %s; Buffer size: %s.", units.size(), offset, idbBufferSize, bufferSize);

        buildingAreaByteBuffer.clear();
        IntBuffer intView = buildingAreaByteBuffer.asIntBuffer();
        for (LowLevelDC lowLevelDC : units) {
            intView.put(lowLevelDC.idIndicesCount);
            intView.put(lowLevelDC.idInstanceCount);
            intView.put(lowLevelDC.idEboFirstIndex);
            intView.put(lowLevelDC.idBaseVertex);
            intView.put(lowLevelDC.idBaseInstance);
        }

        buildingAreaByteBuffer.position(0);
        buildingAreaByteBuffer.limit(idbBufferSize);

        Optional<ByteBuffer> optional = idbView.getPersistentMappedBuffer();
        Preconditions.checkState(optional.isPresent()); // impossible to throw

        ByteBuffer persistent = optional.get();
        persistent.position(offset);
        persistent.limit(offset + idbBufferSize);
        persistent.put(buildingAreaByteBuffer);

        LowLevelDC lowLevelDC = LowLevelDC.acquire().fillMultiElementIndirect(
                vao,
                idbView.bufferID,
                mode,
                elementType,
                offset,
                IndirectDrawBufferGenerator.IDB_STRIDE_BYTE,
                units.size());

        offset += idbBufferSize;

        for (LowLevelDC unit : units) {
            unit.recycle();
        }

        return lowLevelDC;
    }
}
