package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer;

import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;

public class MeshletInputDoubleBuffer {
    public final static int MESHLET_STRIDE_BYTE = 3616;

    private SSBOView ssbo0 = null;
    private SSBOView ssbo1 = null;
    private int ssboSize0;
    private int ssboSize1;

    private final static int INITIAL_SSBO_SIZE = 1024 * 1024 * 16; // 16MB
    private final static int MAX_SSBO_SIZE = 1024 * 1024 * 512; // 512MB

    public MeshletInputDoubleBuffer() {
        ssboSize0 = INITIAL_SSBO_SIZE;
        ssboSize1 = INITIAL_SSBO_SIZE;
    }

    public void lateInit() {
        ssbo0 = new SSBOView(new GLBuffer());
        ssbo1 = new SSBOView(new GLBuffer());

        ssbo0.bind();
        ssbo0.allocPersistent(ssboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo0.mapPersistent(0, ssboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo1.bind();
        ssbo1.allocPersistent(ssboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo1.mapPersistent(0, ssboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo1.bind(0);
    }

    //<editor-fold desc="grow utils">
    /**
     * Make sure that ssbo0 isn't being used by gpu at the moment.
     * Must only grow the current write target.
     *
     * @return Whether successfully grew the buffer
     */
    private boolean growSsbo0() {
        int oldSize0 = ssboSize0;
        ssboSize0 = Math.min(MAX_SSBO_SIZE, ssboSize0 * 2);

        if (oldSize0 == ssboSize0) {
            return false;
        }

        resizeSsbo0(ssboSize0);

        return true;
    }

    /**
     * Make sure that ssbo1 isn't being used by gpu at the moment.
     * Must only grow the current write target.
     *
     * @return Whether successfully grew the buffer
     */
    private boolean growSsbo1() {
        int oldSize1 = ssboSize1;
        ssboSize1 = Math.min(MAX_SSBO_SIZE, ssboSize1 * 2);

        if (oldSize1 == ssboSize1) {
            return false;
        }

        resizeSsbo1(ssboSize1);

        return true;
    }
    //</editor-fold>

    //<editor-fold desc="resize utils">
    /**
     * Make sure that ssbo0 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeSsbo0(int size) {
        int prevID = ssbo0.fetchCurrentBoundBufferID();

        ssbo0.bind();
        ssbo0.unmapPersistent();
        GLResourceManager.disposeEarly(ssbo0.buffer);

        ssbo0 = new SSBOView(new GLBuffer());

        ssbo0.bind();
        ssbo0.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo0.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo0.bind(prevID);
    }

    /**
     * Make sure that ssbo1 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeSsbo1(int size) {
        int prevID = ssbo1.fetchCurrentBoundBufferID();

        ssbo1.bind();
        ssbo1.unmapPersistent();
        GLResourceManager.disposeEarly(ssbo1.buffer);

        ssbo1 = new SSBOView(new GLBuffer());

        ssbo1.bind();
        ssbo1.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo1.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo1.bind(prevID);
    }
    //</editor-fold>

    private int index = 0;

    /**
     * @return The max meshlet input count of the current write target
     */
    public int getMaxMeshletInputCount() {
        return getSize() / MESHLET_STRIDE_BYTE;
    }

    /**
     * Swap the current write target and consume target.
     */
    public void swap() {
        index = index == 0 ? 1 : 0;
    }

    // not being used by compute atm; going to be written on cpu side
    public SSBOView getWriteTarget() {
        return index == 0 ? ssbo0 : ssbo1;
    }

    // just finished writing on cpu side; going to be passed to compute
    public SSBOView getConsumeTarget() {
        return index == 0 ? ssbo1 : ssbo0;
    }

    /**
     * @return The size of the current write target
     */
    public int getSize() {
        if (index == 0) {
            return ssboSize0;
        }
        if (index == 1) {
            return ssboSize1;
        }

        throw new RuntimeException("No such index (expected 0 or 1). Index=" + index);
    }

    /**
     * Grow the current write target.
     *
     * @return Whether successfully grew the buffer
     */
    public boolean grow() {
        if (index == 0) {
            return growSsbo0();
        }
        if (index == 1) {
            return growSsbo1();
        }

        throw new RuntimeException("No such index (expected 0 or 1). Index=" + index);
    }

    /**
     * Grow the current write target if it's smaller than the consume target.
     */
    public void growToMatchSize() {
        int consumeSize = index == 0 ? ssboSize1 : ssboSize0;
        if (getSize() < consumeSize) {
            if (index == 0) {
                resizeSsbo0(consumeSize);
            } else if (index == 1) {
                resizeSsbo1(consumeSize);
            } else {
                throw new RuntimeException("No such index (expected 0 or 1). Index=" + index);
            }
        }
    }
}
