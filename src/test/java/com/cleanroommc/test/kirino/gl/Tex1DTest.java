package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture1DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GLTestExtension.class)
public class Tex1DTest {

    @Test
    public void testRead() {
        GLTestExtension.submit(() -> {
            Texture1DAccessor texture1DAccessor = new Texture1DAccessor(false, GLTexture.newTex1D(false, false, 1));
            texture1DAccessor.bind();
            texture1DAccessor.highlevel().alloc(
                    true,
                    BufferUtils.createByteBuffer(4).putInt(123).flip(),
                    TextureFormat.R32UI);

            ByteBuffer result = BufferUtils.createByteBuffer(4);
            texture1DAccessor.getTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    result);

            assertEquals(123, result.getInt(0));
        }).join();
    }

    @Test
    public void testDsaRead() {
        GLTestExtension.submit(() -> {
            Texture1DAccessor texture1DAccessor = new Texture1DAccessor(true, GLTexture.newDsaTex1D(1));
            texture1DAccessor.highlevel().alloc(
                    false,
                    BufferUtils.createByteBuffer(4).putInt(123).flip(),
                    TextureFormat.R32UI);

            ByteBuffer result = BufferUtils.createByteBuffer(4);
            texture1DAccessor.getTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    result);

            assertEquals(123, result.getInt(0));
        }).join();
    }
}
