package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture1DAccessor;
import com.cleanroommc.kirino.gl.texture.accessor.TextureBufferAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(GLTestExtension.class)
public class TBOTest {

    @Order(1)
    @Test
    public void testIO() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            TextureBufferAccessor tbo = new TextureBufferAccessor(true, GLTexture.newDsaTexBuffer());

            VBOView vbo = new VBOView(new GLBuffer());
            vbo.bind();
            vbo.uploadDirectly(BufferUtils
                    .createByteBuffer(16)
                    .putInt(0)
                    .putInt(123)
                    .putInt(456)
                    .putInt(0)
                    .flip());

            tbo.texBufferRange(TextureFormat.R32UI.internalFormat, vbo.bufferID, 0, 16);

            ByteBuffer result = BufferUtils.createByteBuffer(16);

            GL15.glBindBuffer(tbo.target(), vbo.bufferID);
            GL15.glGetBufferSubData(tbo.target(), 0, result);

            assertEquals(123, result.getInt(4));
            assertEquals(456, result.getInt(8));

            assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
        }).join();
    }

    @Order(2)
    @Test
    public void testAlignment() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            int alignment = GL11.glGetInteger(GL43.GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
            assumeTrue(alignment == 16);

            TextureBufferAccessor tbo = new TextureBufferAccessor(true, GLTexture.newDsaTexBuffer());

            VBOView vbo = new VBOView(new GLBuffer());
            vbo.bind();
            vbo.uploadDirectly(BufferUtils
                    .createByteBuffer(16)
                    .putInt(0)
                    .putInt(123)
                    .putInt(456)
                    .putInt(0)
                    .flip());

            tbo.texBufferRange(TextureFormat.R32UI.internalFormat, vbo.bufferID, 4, 12);

            assertNotEquals(GL11.GL_NO_ERROR, GL11.glGetError());

            vbo.bind();
            vbo.uploadDirectly(BufferUtils
                    .createByteBuffer(32)
                    .putInt(0)
                    .putInt(0)
                    .putInt(0)
                    .putInt(0)
                    .putInt(0)
                    .putInt(123)
                    .putInt(456)
                    .putInt(0)
                    .flip());

            tbo.texBufferRange(TextureFormat.R32UI.internalFormat, vbo.bufferID, 16, 16);

            assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
        }).join();
    }

    private static final String SHADER_SRC = """
            #version 430
            
            layout(local_size_x = 1) in;
            
            uniform usamplerBuffer inputBuffer;
            layout(binding = 0, r32ui) uniform uimage1D outputImage;
            
            void main()
            {
                uint value = texelFetch(inputBuffer, 0).r;
                imageStore(outputImage, 0, uvec4(value, 0, 0, 0));
            }
            """;

    @Order(3)
    @Test
    public void testComputeTBO() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            int shaderID = GL20.glCreateShader(ShaderType.COMPUTE.glValue);
            GL20.glShaderSource(shaderID, SHADER_SRC);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);

            int programID = GL20.glCreateProgram();
            GL20.glAttachShader(programID, shaderID);
            GL20.glLinkProgram(programID);

            assertTrue(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_FALSE);

            Texture1DAccessor texture1DAccessor = new Texture1DAccessor(true, GLTexture.newDsaTex1D(1));
            texture1DAccessor.highlevel().alloc(
                    false,
                    BufferUtils.createByteBuffer(4).putInt(0).flip(),
                    TextureFormat.R32UI);

            GL42.glBindImageTexture(
                    0,
                    texture1DAccessor.textureID(),
                    0,
                    false,
                    0,
                    GL15.GL_READ_WRITE,
                    TextureFormat.R32UI.internalFormat);

            TextureBufferAccessor tbo = new TextureBufferAccessor(true, GLTexture.newDsaTexBuffer());

            VBOView vbo = new VBOView(new GLBuffer());
            vbo.bind();
            vbo.uploadDirectly(BufferUtils
                    .createByteBuffer(4)
                    .putInt(123456)
                    .flip());

            tbo.texBuffer(TextureFormat.R32UI.internalFormat, vbo.bufferID);
            tbo.unit(0);

            int loc = GL20.glGetUniformLocation(programID, "inputBuffer");
            GL20.glUniform1i(loc, 0);

            GL20.glUseProgram(programID);

            GL43.glDispatchCompute(1, 1, 1);
            GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

            long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

            int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000L);
            assumeTrue(waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED);

            ByteBuffer result = BufferUtils.createByteBuffer(4);
            texture1DAccessor.getTexImage(
                    0,
                    TextureFormat.R32UI.format,
                    TextureFormat.R32UI.type,
                    result);

            assertEquals(123456, result.getInt(0));
        }).join();
    }
}
