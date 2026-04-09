package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer;

import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;

public class DrawIndexOutputDoubleBuffer {

    private SSBOView indexSsbo0 = null;
    private SSBOView indexSsbo1 = null;
    private int indexSsboSize0;
    private int indexSsboSize1;

    private int calcWorstCaseIndexBytes(int meshletCount) {
        return meshletCount * MeshletConstants.WORST_CASE_MESHLET_INDEX_BYTES + 4; // padding: 4
    }

    private final static int INITIAL_INDEX_SSBO_SIZE = 1024 * 1024 * 16; // 16MB

    private final static int MAX_SSBO_SIZE = 1024 * 1024 * 512; // 512MB

    public DrawIndexOutputDoubleBuffer() {
        indexSsboSize0 = INITIAL_INDEX_SSBO_SIZE;
        indexSsboSize1 = INITIAL_INDEX_SSBO_SIZE;
    }

    public void lateInit() {
        indexSsbo0 = new SSBOView(new GLBuffer());
        indexSsbo1 = new SSBOView(new GLBuffer());

        indexSsbo0.bind();
        indexSsbo0.allocPersistent(indexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo0.clearUint0();
        indexSsbo0.mapPersistent(0, indexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo1.bind();
        indexSsbo1.allocPersistent(indexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo1.clearUint0();
        indexSsbo1.mapPersistent(0, indexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo1.bind(0);
    }

    //<editor-fold desc="grow utils">
    /**
     * Make sure that indexSsbo0 isn't being used by gpu at the moment.
     * Must only grow the current write target.
     *
     * @return Whether successfully grew the buffer
     */
    private boolean growIndexSsbo0(int meshletCount) {
        int targetSize = calcWorstCaseIndexBytes(meshletCount);
        if (indexSsboSize0 >= targetSize) {
            return true;
        }
        if (targetSize > MAX_SSBO_SIZE) {
            return false;
        }

        indexSsboSize0 = targetSize;
        resizeIndexSsbo0(indexSsboSize0);

        return true;
    }

    /**
     * Make sure that indexSsbo1 isn't being used by gpu at the moment.
     * Must only grow the current write target.
     *
     * @return Whether successfully grew the buffer
     */
    private boolean growIndexSsbo1(int meshletCount) {
        int targetSize = calcWorstCaseIndexBytes(meshletCount);
        if (indexSsboSize1 >= targetSize) {
            return true;
        }
        if (targetSize > MAX_SSBO_SIZE) {
            return false;
        }

        indexSsboSize1 = targetSize;
        resizeIndexSsbo1(indexSsboSize1);

        return true;
    }
    //</editor-fold>

    //<editor-fold desc="resize utils">
    /**
     * Make sure that indexSsbo0 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeIndexSsbo0(int size) {
        int prevID = indexSsbo0.fetchCurrentBoundBufferID();

        indexSsbo0.bind();
        indexSsbo0.unmapPersistent();
        GLResourceManager.disposeEarly(indexSsbo0.buffer);

        indexSsbo0 = new SSBOView(new GLBuffer());

        // compute will overwrite contents. no need to clear
        indexSsbo0.bind();
        indexSsbo0.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo0.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo0.bind(prevID);
    }

    /**
     * Make sure that indexSsbo1 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeIndexSsbo1(int size) {
        int prevID = indexSsbo1.fetchCurrentBoundBufferID();

        indexSsbo1.bind();
        indexSsbo1.unmapPersistent();
        GLResourceManager.disposeEarly(indexSsbo1.buffer);

        indexSsbo1 = new SSBOView(new GLBuffer());

        // compute will overwrite contents. no need to clear
        indexSsbo1.bind();
        indexSsbo1.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo1.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo1.bind(prevID);
    }
    //</editor-fold>

    private int index = 0;

    /**
     * Swap the current write target and consume target.
     */
    public void swap() {
        index = index == 0 ? 1 : 0;
    }

    // not being consumed by draw commands atm; going to be written by compute
    public SSBOView getIndexWriteTarget() {
        return index == 0 ? indexSsbo0 : indexSsbo1;
    }

    // just finished computing; going to be passed to draw commands
    public SSBOView getIndexConsumeTarget() {
        return index == 0 ? indexSsbo1 : indexSsbo0;
    }

    /**
     * @return The index size of the current write target
     */
    public int getIndexSize() {
        if (index == 0) {
            return indexSsboSize0;
        }
        if (index == 1) {
            return indexSsboSize1;
        }

        throw new RuntimeException("No such index (expected 0 or 1). Index=" + index);
    }

    /**
     * Grow the current vertex write target.
     *
     * @return Whether successfully grew the buffer
     */
    public boolean growIndex(int meshletCount) {
        if (index == 0) {
            return growIndexSsbo0(meshletCount);
        }
        if (index == 1) {
            return growIndexSsbo1(meshletCount);
        }

        throw new RuntimeException("No such index (expected 0 or 1). Index=" + index);
    }
}
