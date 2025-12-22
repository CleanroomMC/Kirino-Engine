package com.cleanroommc.kirino.engine.render.scene.gpu_meshlet;

import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;

public class MeshletInputDoubleBuffer {
    private SSBOView ssbo0 = null;
    private SSBOView ssbo1 = null;
    private int ssboSize0;
    private int ssboSize1;

    private final static int INITIAL_SSBO_SIZE = 1024 * 1024 * 16; // 16MB
    private final static int MAX_SSBO_SIZE = 1024 * 1024 * 1024; // 1GB

    public MeshletInputDoubleBuffer() {
        ssboSize0 = INITIAL_SSBO_SIZE;
        ssboSize1 = INITIAL_SSBO_SIZE;
    }

    /**
     * Make sure that ssbo0 isn't being used at the moment.
     */
    private boolean growSsbo0() {
        int oldSize0 = ssboSize0;
        ssboSize0 = Math.min(MAX_SSBO_SIZE, ssboSize0 * 2);

        if (oldSize0 == ssboSize0) {
            return false;
        }

        int prevID = ssbo0.fetchCurrentBoundBufferID();

        ssbo0.bind();
        ssbo0.unmapPersistent();
        GLResourceManager.disposeEarly(ssbo0.buffer);

        ssbo0 = new SSBOView(new GLBuffer());

        ssbo0.bind();
        ssbo0.allocPersistent(ssboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo0.mapPersistent(0, ssboSize0, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo0.bind(prevID);

        return true;
    }

    /**
     * Make sure that ssbo1 isn't being used at the moment.
     */
    private boolean growSsbo1() {
        int oldSize1 = ssboSize1;
        ssboSize1 = Math.min(MAX_SSBO_SIZE, ssboSize1 * 2);

        if (oldSize1 == ssboSize1) {
            return false;
        }

        int prevID = ssbo1.fetchCurrentBoundBufferID();

        ssbo1.bind();
        ssbo1.unmapPersistent();
        GLResourceManager.disposeEarly(ssbo1.buffer);

        ssbo1 = new SSBOView(new GLBuffer());

        ssbo1.bind();
        ssbo1.allocPersistent(ssboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssbo1.mapPersistent(0, ssboSize1, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssbo1.bind(prevID);

        return true;
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
}
