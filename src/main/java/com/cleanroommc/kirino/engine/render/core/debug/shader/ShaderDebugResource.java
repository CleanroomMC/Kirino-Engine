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
    private SSBOView ssboTemp;

    public SSBOView getSsboVec3() {
        return ssboVec3;
    }

    public SSBOView getSsboCounter() {
        return ssboCounter;
    }

    public SSBOView getSsboTemp() {
        return ssboTemp;
    }

    public void lateInit() {
        ssboVec3 = new SSBOView(new GLBuffer());
        ssboCounter = new SSBOView(new GLBuffer());
        ssboTemp = new SSBOView(new GLBuffer());

        ssboVec3.bind();
        ssboVec3.allocPersistent(4096 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssboVec3.clearUint0();
        ssboVec3.mapPersistent(0, 4096 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssboCounter.bind();
        ssboCounter.allocPersistent(1024 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssboCounter.clearUint0();
        ssboCounter.mapPersistent(0, 1024 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssboTemp.bind();
        ssboTemp.allocPersistent(4096 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
        ssboTemp.clearUint0();
        ssboTemp.mapPersistent(0, 4096 * 16, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

        ssboTemp.bind(0);
    }

    private int dispatchCount = 0;

    public void setDispatchCount(int count) {
        dispatchCount = count;
    }

    public void readAndPrint() {
        ByteBuffer byteBuffer = ssboTemp.getPersistentMappedBuffer().orElseThrow()
                .duplicate()
                .order(ByteOrder.nativeOrder());
        byteBuffer.position(0);

        KirinoCommonCore.LOGGER.info("---------------------------------");

        for (int i = 0; i < dispatchCount; i++) {
            float f00 = byteBuffer.getFloat();
            float f01 = byteBuffer.getFloat();
            float f02 = byteBuffer.getFloat();
            float f03 = byteBuffer.getFloat();

            float f10 = byteBuffer.getFloat();
            float f11 = byteBuffer.getFloat();
            float f12 = byteBuffer.getFloat();
            float f13 = byteBuffer.getFloat();

            float f20 = byteBuffer.getFloat();
            float f21 = byteBuffer.getFloat();
            float f22 = byteBuffer.getFloat();
            float f23 = byteBuffer.getFloat();

            float f30 = byteBuffer.getFloat();
            float f31 = byteBuffer.getFloat();
            float f32 = byteBuffer.getFloat();
            float f33 = byteBuffer.getFloat();

            KirinoCommonCore.LOGGER.info("f00 (dirty index): " + (int) f00);
            KirinoCommonCore.LOGGER.info("f01 (old index count): " + (int) f01);
            KirinoCommonCore.LOGGER.info("f02 (old vertex count): " + (int) f02);
            KirinoCommonCore.LOGGER.info("f10 (first index): " + (int) f10);
            KirinoCommonCore.LOGGER.info("f11 (index count): " + (int) f11);
            KirinoCommonCore.LOGGER.info("f12 (vertex count): " + (int) f12);
            KirinoCommonCore.LOGGER.info("f20 (couters 0: vertex): " + (int) f20);
            KirinoCommonCore.LOGGER.info("f21 (couters 1: index): " + (int) f21);
            KirinoCommonCore.LOGGER.info("f22 (meshlet block count): " + (int) f22);
            KirinoCommonCore.LOGGER.info("f30 (chunk pos x): " + (int) f30);
            KirinoCommonCore.LOGGER.info("f31 (chunk pos y): " + (int) f31);
            KirinoCommonCore.LOGGER.info("f32 (chun pos z): " + (int) f32);
        }

        KirinoCommonCore.LOGGER.info("---------------------------------");
    }
}
