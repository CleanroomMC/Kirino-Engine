package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.*;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GLTestExtension.class)
public class TextureAccessorCoverageTest {

    @Test
    public void testDsaAllocations() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            Texture1DArrayAccessor texture1DArray = new Texture1DArrayAccessor(true, GLTexture.newDsaTex1DArray(8, 3));
            texture1DArray.highlevel().allocEmpty(false, TextureFormat.R8_UNORM);
            assertEquals(8, texture1DArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(3, texture1DArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(TextureFormat.R8_UNORM, texture1DArray.texture.currentFormat());

            TextureCubemapAccessor cubemap = new TextureCubemapAccessor(true, GLTexture.newDsaCubemap(4));
            cubemap.highlevel().allocEmpty(false, TextureFormat.R8_UNORM);
            assertEquals(4, cubemap.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, cubemap.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(4, cubemap.fetchCubeTexLevelParamI(TextureAccessor.CubeFace.POS_X, 0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(TextureFormat.R8_UNORM, cubemap.texture.currentFormat());

            ByteBuffer cubemapFace = BufferUtils.createByteBuffer(16);
            cubemapFace.put(0, (byte) 7);
            cubemap.highlevel().uploadSubImage(0, 0, 0, 4, 4, cubemapFace);
            for (TextureAccessor.CubeFace face : TextureAccessor.CubeFace.values()) {
                ByteBuffer faceData = BufferUtils.createByteBuffer(16);
                faceData.put(0, (byte) (face.layer + 1));
                cubemap.cubeTexSubImage2D(
                        face,
                        0,
                        0,
                        0,
                        4,
                        4,
                        TextureFormat.R8_UNORM.format,
                        TextureFormat.R8_UNORM.type,
                        faceData);
            }

            ByteBuffer cubemapData = BufferUtils.createByteBuffer(96);
            cubemap.highlevel().downloadLevel(0, cubemapData);
            for (int face = 0; face < 6; face++) {
                assertEquals(face + 1, cubemapData.get(face * 16));
            }

            ByteBuffer cubemapFaceResult = BufferUtils.createByteBuffer(16);
            cubemap.getCubeTexImage(
                    TextureAccessor.CubeFace.NEG_Z,
                    0,
                    TextureFormat.R8_UNORM.format,
                    TextureFormat.R8_UNORM.type,
                    cubemapFaceResult);
            assertEquals(6, cubemapFaceResult.get(0));

            TextureCubemapArrayAccessor cubemapArray = new TextureCubemapArrayAccessor(true, GLTexture.newDsaCubemapArray(4, 2));
            cubemapArray.highlevel().allocEmpty(false, TextureFormat.RGBA8_UNORM);
            assertEquals(4, cubemapArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, cubemapArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(12, cubemapArray.fetchTexLevelParamI(0, GL12.GL_TEXTURE_DEPTH));
            assertEquals(TextureFormat.RGBA8_UNORM, cubemapArray.texture.currentFormat());

            Texture2DMSAccessor texture2DMS = new Texture2DMSAccessor(true, GLTexture.newDsaTex2DMS(8, 4, 4));
            texture2DMS.highlevel().allocEmpty(false, TextureFormat.RGBA8_UNORM);
            assertEquals(8, texture2DMS.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, texture2DMS.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(4, texture2DMS.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));
            assertEquals(TextureFormat.RGBA8_UNORM, texture2DMS.texture.currentFormat());

            Texture2DMSArrayAccessor texture2DMSArray = new Texture2DMSArrayAccessor(true, GLTexture.newDsaTex2DMSArray(8, 4, 3, 4));
            texture2DMSArray.highlevel().allocEmpty(false, TextureFormat.RGBA8_UNORM);
            assertEquals(8, texture2DMSArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, texture2DMSArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(3, texture2DMSArray.fetchTexLevelParamI(0, GL12.GL_TEXTURE_DEPTH));
            assertEquals(4, texture2DMSArray.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));
            assertEquals(TextureFormat.RGBA8_UNORM, texture2DMSArray.texture.currentFormat());
        }).join();
    }

    @Test
    public void testLegacyAllocations() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            Texture1DArrayAccessor texture1DArray = new Texture1DArrayAccessor(false, GLTexture.newTex1DArray(false, false, 8, 3));
            texture1DArray.bind();
            texture1DArray.highlevel().allocEmpty(true, TextureFormat.R8_UNORM);
            assertEquals(8, texture1DArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(3, texture1DArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));

            TextureCubemapAccessor cubemap = new TextureCubemapAccessor(false, GLTexture.newCubemap(false, false, 4));
            cubemap.bind();
            cubemap.highlevel().allocEmpty(true, TextureFormat.R8_UNORM);
            assertEquals(4, cubemap.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, cubemap.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(4, cubemap.fetchCubeTexLevelParamI(TextureAccessor.CubeFace.NEG_Z, 0, GL11.GL_TEXTURE_WIDTH));

            ByteBuffer cubemapFace = BufferUtils.createByteBuffer(16);
            cubemapFace.put(0, (byte) 7);
            cubemap.highlevel().uploadSubImage(0, 0, 0, 4, 4, cubemapFace);
            for (TextureAccessor.CubeFace face : TextureAccessor.CubeFace.values()) {
                ByteBuffer faceData = BufferUtils.createByteBuffer(16);
                faceData.put(0, (byte) (face.layer + 1));
                cubemap.cubeTexSubImage2D(
                        face,
                        0,
                        0,
                        0,
                        4,
                        4,
                        TextureFormat.R8_UNORM.format,
                        TextureFormat.R8_UNORM.type,
                        faceData);
            }

            ByteBuffer cubemapData = BufferUtils.createByteBuffer(96);
            cubemap.highlevel().downloadLevel(0, cubemapData);
            for (int face = 0; face < 6; face++) {
                assertEquals(face + 1, cubemapData.get(face * 16));
            }

            ByteBuffer cubemapFaceResult = BufferUtils.createByteBuffer(16);
            cubemap.getCubeTexImage(
                    TextureAccessor.CubeFace.NEG_Z,
                    0,
                    TextureFormat.R8_UNORM.format,
                    TextureFormat.R8_UNORM.type,
                    cubemapFaceResult);
            assertEquals(6, cubemapFaceResult.get(0));

            TextureCubemapArrayAccessor cubemapArray = new TextureCubemapArrayAccessor(false, GLTexture.newCubemapArray(false, false, 4, 2));
            cubemapArray.bind();
            cubemapArray.highlevel().allocEmpty(true, TextureFormat.RGBA8_UNORM);
            assertEquals(4, cubemapArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, cubemapArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(12, cubemapArray.fetchTexLevelParamI(0, GL12.GL_TEXTURE_DEPTH));

            Texture2DMSAccessor texture2DMS = new Texture2DMSAccessor(false, GLTexture.newTex2DMS(false, false, 8, 4, 4));
            texture2DMS.bind();
            texture2DMS.highlevel().allocEmpty(true, TextureFormat.RGBA8_UNORM);
            assertEquals(8, texture2DMS.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, texture2DMS.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(4, texture2DMS.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));

            Texture2DMSArrayAccessor texture2DMSArray = new Texture2DMSArrayAccessor(false, GLTexture.newTex2DMSArray(false, false, 8, 4, 3, 4));
            texture2DMSArray.bind();
            texture2DMSArray.highlevel().allocEmpty(true, TextureFormat.RGBA8_UNORM);
            assertEquals(8, texture2DMSArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
            assertEquals(4, texture2DMSArray.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
            assertEquals(3, texture2DMSArray.fetchTexLevelParamI(0, GL12.GL_TEXTURE_DEPTH));
            assertEquals(4, texture2DMSArray.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));
        }).join();
    }
}
