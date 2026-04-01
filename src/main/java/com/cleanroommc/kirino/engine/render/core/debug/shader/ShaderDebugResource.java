package com.cleanroommc.kirino.engine.render.core.debug.shader;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class ShaderDebugResource {

    public static final ShaderDebugResource RESOURCE;

    // todo: temp; refactor
    static {
        RESOURCE = new ShaderDebugResource();
        RESOURCE.lateInit();
    }

    private SSBOView ssboVec3;
    private SSBOView ssboCounter;

    public SSBOView getSsboVec3() {
        return ssboVec3;
    }

    public SSBOView getSsboCounter() {
        return ssboCounter;
    }

    public void lateInit() {
        ssboVec3 = new SSBOView(new GLBuffer());
        ssboCounter = new SSBOView(new GLBuffer());

        ssboVec3.bind();
        ssboVec3.allocPersistent(4096 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssboVec3.clearUint0();
        ssboVec3.mapPersistent(0, 1024 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssboCounter.bind();
        ssboCounter.allocPersistent(1024 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssboCounter.clearUint0();
        ssboCounter.mapPersistent(0, 1024 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssboCounter.bind(0);
    }

    // todo: fix wrong read logic
    public void readAndPrint() {
        ByteBuffer byteBufferCounter = ssboCounter.getPersistentMappedBuffer().orElseThrow()
                .duplicate()
                .order(ByteOrder.nativeOrder());
        byteBufferCounter.position(0);

        int counter0 = byteBufferCounter.getInt();
        int counter1 = byteBufferCounter.getInt();

        ByteBuffer byteBufferVec3 = ssboVec3.getPersistentMappedBuffer().orElseThrow()
                .duplicate()
                .order(ByteOrder.nativeOrder());
        byteBufferVec3.position(0);

        KirinoCommonCore.LOGGER.info("---------------------------------");

        KirinoCommonCore.LOGGER.info("counter0 (dispatch count): " + counter0);
        KirinoCommonCore.LOGGER.info("counter1 (vec3 data count): " + counter1);

        for (int i = 0; i < counter0; i++) {
            float f00 = byteBufferVec3.getFloat();
            float f01 = byteBufferVec3.getFloat();
            float f02 = byteBufferVec3.getFloat();
            byteBufferVec3.getFloat();

            float f10 = byteBufferVec3.getFloat();
            float f11 = byteBufferVec3.getFloat();
            float f12 = byteBufferVec3.getFloat();
            byteBufferVec3.getFloat();

            float f20 = byteBufferVec3.getFloat();
            float f21 = byteBufferVec3.getFloat();
            float f22 = byteBufferVec3.getFloat();
            byteBufferVec3.getFloat();

            KirinoCommonCore.LOGGER.info("f00 (dirty index): " + (int) f00);
            KirinoCommonCore.LOGGER.info("f01 (old index count): " + (int) f01);
            KirinoCommonCore.LOGGER.info("f02 (old vertex count): " + (int) f02);
            KirinoCommonCore.LOGGER.info("f10 (first index): " + (int) f10);
            KirinoCommonCore.LOGGER.info("f11 (index count): " + (int) f11);
            KirinoCommonCore.LOGGER.info("f12 (vertex count): " + (int) f12);
            KirinoCommonCore.LOGGER.info("f20 (couters 0: vertex): " + (int) f20);
            KirinoCommonCore.LOGGER.info("f21 (couters 1: index): " + (int) f21);
            KirinoCommonCore.LOGGER.info("f22 (meshlet block count): " + (int) f22);
        }

        KirinoCommonCore.LOGGER.info("---------------------------------");
    }

    public void reset() {
        ByteBuffer byteBufferCounter = ssboCounter.getPersistentMappedBuffer().orElseThrow();
        byteBufferCounter.putInt(0, 0);
        byteBufferCounter.putInt(4, 0);
    }
}
