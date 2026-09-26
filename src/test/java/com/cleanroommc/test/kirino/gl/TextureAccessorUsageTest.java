package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.*;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(GLTestExtension.class)
public class TextureAccessorUsageTest {

    private static final TextureFormat FORMAT = TextureFormat.R8_UNORM;

    @Test
    public void testEveryDsaTextureAccessor() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            useTexture1D();
            useTexture1DArray();
            useTexture2D();
            useTexture2DArray();
            useTexture3D();
            useTextureCubemap();
            useTextureCubemapArray();
            useTexture2DMultisample();
            useTexture2DMultisampleArray();
            useTextureBuffer();

            assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
        }).join();
    }

    private static ByteBuffer filledBytes(int size, int value) {
        ByteBuffer data = BufferUtils.createByteBuffer(size);
        for (int i = 0; i < size; i++) {
            data.put(i, (byte) value);
        }
        return data;
    }

    private static int unsignedByte(ByteBuffer data, int index) {
        return Byte.toUnsignedInt(data.get(index));
    }

    private static void useTexture1D() {
        int width = 8;
        GLTexture texture = GLTexture.newDsaTex1D(width);
        Texture1DAccessor accessor = new Texture1DAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(width, 1));
        textureOps.uploadSubImage(0, 2, 3, filledBytes(3, 7));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(width);
        textureOps.downloadLevel(0, downloaded);

        assertEquals(1, unsignedByte(downloaded, 1));
        assertEquals(7, unsignedByte(downloaded, 2));
        assertEquals(7, unsignedByte(downloaded, 4));
        assertEquals(1, unsignedByte(downloaded, 5));
    }

    private static void useTexture1DArray() {
        int width = 4;
        int layers = 3;
        GLTexture texture = GLTexture.newDsaTex1DArray(width, layers);
        Texture1DArrayAccessor accessor = new Texture1DArrayAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(width * layers, 2));

        int layer = 1;
        textureOps.uploadSubImage(0, 1, layer, 2, 1, filledBytes(2, 8));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(width * layers);
        textureOps.downloadLevel(0, downloaded);

        int changedTexel = layer * width + 1;
        assertEquals(2, unsignedByte(downloaded, changedTexel - 1));
        assertEquals(8, unsignedByte(downloaded, changedTexel));
        assertEquals(8, unsignedByte(downloaded, changedTexel + 1));
        assertEquals(2, unsignedByte(downloaded, changedTexel + 2));
    }

    private static void useTexture2D() {
        int width = 4;
        int height = 4;
        GLTexture texture = GLTexture.newDsaTex2D(width, height);
        Texture2DAccessor accessor = new Texture2DAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(width * height, 3));
        textureOps.uploadSubImage(0, 1, 2, 2, 1, filledBytes(2, 9));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(width * height);
        textureOps.downloadLevel(0, downloaded);

        int changedTexel = 2 * width + 1;
        assertEquals(3, unsignedByte(downloaded, changedTexel - 1));
        assertEquals(9, unsignedByte(downloaded, changedTexel));
        assertEquals(9, unsignedByte(downloaded, changedTexel + 1));
        assertEquals(3, unsignedByte(downloaded, changedTexel + 2));
    }
        // Texture-buffer readback is performed through the attached buffer object.

    private static void useTexture2DArray() {
        int width = 4;
        int height = 4;
        int layers = 2;
        GLTexture texture = GLTexture.newDsaTex2DArray(width, height, layers);
        Texture2DArrayAccessor accessor = new Texture2DArrayAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(width * height * layers, 4));

        int layer = 1;
        textureOps.uploadSubImage(0, 1, 2, layer, 2, 1, 1, filledBytes(2, 10));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(width * height * layers);
        textureOps.downloadLevel(0, downloaded);

        int changedTexel = layer * width * height + 2 * width + 1;
        assertEquals(4, unsignedByte(downloaded, changedTexel - 1));
        assertEquals(10, unsignedByte(downloaded, changedTexel));
        assertEquals(10, unsignedByte(downloaded, changedTexel + 1));
        assertEquals(4, unsignedByte(downloaded, changedTexel + 2));
    }

    private static void useTexture3D() {
        int width = 4;
        int height = 4;
        int depth = 2;
        GLTexture texture = GLTexture.newDsaTex3D(width, height, depth);
        Texture3DAccessor accessor = new Texture3DAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(width * height * depth, 5));
        textureOps.uploadSubImage(0, 1, 2, 1, 2, 1, 1, filledBytes(2, 11));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(width * height * depth);
        textureOps.downloadLevel(0, downloaded);

        int changedTexel = width * height + 2 * width + 1;
        assertEquals(5, unsignedByte(downloaded, changedTexel - 1));
        assertEquals(11, unsignedByte(downloaded, changedTexel));
        assertEquals(11, unsignedByte(downloaded, changedTexel + 1));
        assertEquals(5, unsignedByte(downloaded, changedTexel + 2));
    }

    private static void useTextureCubemap() {
        int extent = 4;
        int faceTexels = extent * extent;
        GLTexture texture = GLTexture.newDsaCubemap(extent);
        TextureCubemapAccessor accessor = new TextureCubemapAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(faceTexels, 6));
        textureOps.uploadSubImage(0, 0, 0, extent, extent, filledBytes(faceTexels, 12));

        TextureAccessor.CubeFace face = TextureAccessor.CubeFace.NEG_Z;
        accessor.cubeTexSubImage2D(
                face,
                0,
                1,
                2,
                1,
                1,
                FORMAT.format,
                FORMAT.type,
                filledBytes(1, 13));

        ByteBuffer downloadedFace = BufferUtils.createByteBuffer(faceTexels);
        accessor.getCubeTexImage(face, 0, FORMAT.format, FORMAT.type, downloadedFace);
        assertEquals(13, unsignedByte(downloadedFace, 2 * extent + 1));

        ByteBuffer downloadedCubemap = BufferUtils.createByteBuffer(faceTexels * 6);
        textureOps.downloadLevel(0, downloadedCubemap);
        int changedTexel = face.layer * faceTexels + 2 * extent + 1;
        assertEquals(13, unsignedByte(downloadedCubemap, changedTexel));
    }

    private static void useTextureCubemapArray() {
        int extent = 4;
        int cubeCount = 2;
        int faceCount = cubeCount * 6;
        GLTexture texture = GLTexture.newDsaCubemapArray(extent, cubeCount);
        TextureCubemapArrayAccessor accessor = new TextureCubemapArrayAccessor(true, texture);
        TextureAccessorHighlevel.HighlevelOperator textureOps = accessor.highlevel();

        textureOps.allocEmpty(false, FORMAT);
        textureOps.uploadLevel(0, filledBytes(extent * extent * faceCount, 14));

        int cubeIndex = 1;
        int faceLayer = cubeIndex * 6 + TextureAccessor.CubeFace.POS_Y.layer;
        textureOps.uploadSubImage(0, 1, 2, faceLayer, 1, 1, 1, filledBytes(1, 15));

        ByteBuffer downloaded = BufferUtils.createByteBuffer(extent * extent * faceCount);
        textureOps.downloadLevel(0, downloaded);

        int changedTexel = faceLayer * extent * extent + 2 * extent + 1;
        assertEquals(15, unsignedByte(downloaded, changedTexel));
    }

    private static void useTexture2DMultisample() {
        GLTexture texture = GLTexture.newDsaTex2DMS(8, 4, 4);
        Texture2DMSAccessor accessor = new Texture2DMSAccessor(true, texture);

        accessor.highlevel().allocEmpty(false, TextureFormat.RGBA8_UNORM);

        assertEquals(8, accessor.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
        assertEquals(4, accessor.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
        assertEquals(4, accessor.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));
    }

    private static void useTexture2DMultisampleArray() {
        GLTexture texture = GLTexture.newDsaTex2DMSArray(8, 4, 3, 4);
        Texture2DMSArrayAccessor accessor = new Texture2DMSArrayAccessor(true, texture);

        accessor.highlevel().allocEmpty(false, TextureFormat.RGBA8_UNORM);

        assertEquals(8, accessor.fetchTexLevelParamI(0, GL11.GL_TEXTURE_WIDTH));
        assertEquals(4, accessor.fetchTexLevelParamI(0, GL11.GL_TEXTURE_HEIGHT));
        assertEquals(3, accessor.fetchTexLevelParamI(0, GL12.GL_TEXTURE_DEPTH));
        assertEquals(4, accessor.fetchTexLevelParamI(0, GL32.GL_TEXTURE_SAMPLES));
    }

    private static void useTextureBuffer() {
        GLBuffer backingBuffer = new GLBuffer(true);
        ByteBuffer initialData = BufferUtils.createByteBuffer(4 * Integer.BYTES)
                .putInt(10)
                .putInt(20)
                .putInt(30)
                .putInt(40)
                .flip();
        GL45.glNamedBufferData(backingBuffer.bufferID, initialData, GL15.GL_STATIC_DRAW);

        GLTexture texture = GLTexture.newDsaTexBuffer();
        TextureBufferAccessor accessor = new TextureBufferAccessor(true, texture);

        accessor.texBuffer(TextureFormat.R32UI.internalFormat, backingBuffer.bufferID);
        accessor.texBufferRange(
                TextureFormat.R32UI.internalFormat,
                backingBuffer.bufferID,
                0,
                4L * Integer.BYTES);

        ByteBuffer replacement = BufferUtils.createByteBuffer(Integer.BYTES).putInt(99).flip();
        GL45.glNamedBufferSubData(backingBuffer.bufferID, Integer.BYTES, replacement);

        ByteBuffer downloaded = BufferUtils.createByteBuffer(4 * Integer.BYTES);
        GL45.glGetNamedBufferSubData(backingBuffer.bufferID, 0, downloaded);
        assertEquals(10, downloaded.getInt(0));
        assertEquals(99, downloaded.getInt(Integer.BYTES));
        assertEquals(30, downloaded.getInt(2 * Integer.BYTES));
        assertEquals(40, downloaded.getInt(3 * Integer.BYTES));
    }
}
