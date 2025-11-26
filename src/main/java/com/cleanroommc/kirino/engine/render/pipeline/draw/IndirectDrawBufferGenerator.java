package com.cleanroommc.kirino.engine.render.pipeline.draw;

import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.IDBView;
import com.google.common.base.Preconditions;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class IndirectDrawBufferGenerator {
    private final IDBView idbView;
    public final int bufferSize;

    public final static int IDB_STRIDE_BYTE = 5 * Integer.BYTES;
    public final static int MEMORY_STACK_LIMIT_BYTE = 1024 * 256; // 0.25MB

    private int offset = 0;

    public void reset() {
        offset = 0;
    }

    public IndirectDrawBufferGenerator(int bufferSize) {
        this.bufferSize = bufferSize;
        this.idbView = new IDBView(new GLBuffer());
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

        if (idbBufferSize < MEMORY_STACK_LIMIT_BYTE) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer byteBuffer = stack.malloc(idbBufferSize);

                IntBuffer intView = byteBuffer.asIntBuffer();
                for (LowLevelDC lowLevelDC : units) {
                    intView.put(lowLevelDC.idIndicesCount);
                    intView.put(lowLevelDC.idInstanceCount);
                    intView.put(lowLevelDC.idEboFirstIndex);
                    intView.put(lowLevelDC.idBaseVertex);
                    intView.put(lowLevelDC.idBaseInstance);
                }

                byteBuffer.position(0);
                byteBuffer.limit(idbBufferSize);

                ByteBuffer persistent = idbView.getPersistentMappedBuffer();
                persistent.position(offset);
                persistent.limit(offset + idbBufferSize);
                persistent.put(byteBuffer);
            }
        } else {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(idbBufferSize);

            IntBuffer intView = byteBuffer.asIntBuffer();
            for (LowLevelDC lowLevelDC : units) {
                intView.put(lowLevelDC.idIndicesCount);
                intView.put(lowLevelDC.idInstanceCount);
                intView.put(lowLevelDC.idEboFirstIndex);
                intView.put(lowLevelDC.idBaseVertex);
                intView.put(lowLevelDC.idBaseInstance);
            }

            byteBuffer.position(0);
            byteBuffer.limit(idbBufferSize);

            ByteBuffer persistent = idbView.getPersistentMappedBuffer();
            persistent.position(offset);
            persistent.limit(offset + idbBufferSize);
            persistent.put(byteBuffer);

            MemoryUtil.memFree(byteBuffer);
        }

        LowLevelDC lowLevelDC = LowLevelDC.get().fillMultiElementIndirect(
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
