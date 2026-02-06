package com.cleanroommc.kirino.engine.render.platform.scene.gpu_meshlet.buffer;

import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;

public class VertexOutputDoubleBuffer {

    private SSBOView vertexSsbo0 = null;
    private SSBOView vertexSsbo1 = null;
    private int vertexSsboSize0;
    private int vertexSsboSize1;

    private SSBOView indexSsbo0 = null;
    private SSBOView indexSsbo1 = null;
    private int indexSsboSize0;
    private int indexSsboSize1;

    private final static int INITIAL_VERTEX_SSBO_SIZE = 1024 * 1024 * 16; // 16MB
    private final static int INITIAL_INDEX_SSBO_SIZE = 1024 * 1024 * 16; // 16MB

    public VertexOutputDoubleBuffer() {
        vertexSsboSize0 = INITIAL_VERTEX_SSBO_SIZE;
        vertexSsboSize1 = INITIAL_VERTEX_SSBO_SIZE;
        indexSsboSize0 = INITIAL_INDEX_SSBO_SIZE;
        indexSsboSize1 = INITIAL_INDEX_SSBO_SIZE;
    }

    public void lateInit() {
        vertexSsbo0 = new SSBOView(new GLBuffer());
        vertexSsbo1 = new SSBOView(new GLBuffer());
        indexSsbo0 = new SSBOView(new GLBuffer());
        indexSsbo1 = new SSBOView(new GLBuffer());

        vertexSsbo0.bind();
        vertexSsbo0.allocPersistent(vertexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        vertexSsbo0.mapPersistent(0, vertexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        vertexSsbo1.bind();
        vertexSsbo1.allocPersistent(vertexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        vertexSsbo1.mapPersistent(0, vertexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo0.bind();
        indexSsbo0.allocPersistent(indexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo0.mapPersistent(0, indexSsboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo1.bind();
        indexSsbo1.allocPersistent(indexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        indexSsbo1.mapPersistent(0, indexSsboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        indexSsbo1.bind(0);
    }

    //<editor-fold desc="resize utils">
    /**
     * Make sure that vertexSsbo0 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeVertexSsbo0(int size) {
        int prevID = vertexSsbo0.fetchCurrentBoundBufferID();

        vertexSsbo0.bind();
        vertexSsbo0.unmapPersistent();
        GLResourceManager.disposeEarly(vertexSsbo0.buffer);

        vertexSsbo0 = new SSBOView(new GLBuffer());

        vertexSsbo0.bind();
        vertexSsbo0.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        vertexSsbo0.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        vertexSsbo0.bind(prevID);
    }

    /**
     * Make sure that vertexSsbo1 isn't being used by gpu at the moment.
     * Must only resize the current write target.
     */
    private void resizeVertexSsbo1(int size) {
        int prevID = vertexSsbo1.fetchCurrentBoundBufferID();

        vertexSsbo1.bind();
        vertexSsbo1.unmapPersistent();
        GLResourceManager.disposeEarly(vertexSsbo1.buffer);

        vertexSsbo1 = new SSBOView(new GLBuffer());

        vertexSsbo1.bind();
        vertexSsbo1.allocPersistent(size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        vertexSsbo1.mapPersistent(0, size, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        vertexSsbo1.bind(prevID);
    }

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
    public SSBOView getVertexWriteTarget() {
        return index == 0 ? vertexSsbo0 : vertexSsbo1;
    }

    // not being consumed by draw commands atm; going to be written by compute
    public SSBOView getIndexWriteTarget() {
        return index == 0 ? indexSsbo0 : indexSsbo1;
    }

    // just finished computing; going to be passed to draw commands
    public SSBOView getVertexConsumeTarget() {
        return index == 0 ? vertexSsbo1 : vertexSsbo0;
    }

    // just finished computing; going to be passed to draw commands
    public SSBOView getIndexConsumeTarget() {
        return index == 0 ? indexSsbo1 : indexSsbo0;
    }

    /**
     * @return The vertex size of the current write target
     */
    public int getVertexSize() {
        if (index == 0) {
            return vertexSsboSize0;
        }
        if (index == 1) {
            return vertexSsboSize1;
        }

        return -1; // impossible
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

        return -1; // impossible
    }
}
