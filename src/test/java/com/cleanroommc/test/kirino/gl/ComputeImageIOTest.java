package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.accessor.Texture1DAccessor;
import com.cleanroommc.kirino.gl.texture.meta.TextureFormat;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GLTestExtension.class)
public class ComputeImageIOTest {

    private static final String SHADER_SRC = """
            #version 430
            
            layout(local_size_x = 1) in;
            
            layout(binding = 3, r32ui) uniform uimage1D counter;
            
            void main()
            {
                uint value = 123u;
                imageStore(counter, 0, uvec4(value, 0, 0, 0));
            }
            """;

    @Test
    public void testComputeIO() {
        GLTestExtension.submit(() -> {
            int shaderID = GL20.glCreateShader(ShaderType.COMPUTE.glValue);
            GL20.glShaderSource(shaderID, SHADER_SRC);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);

            int programID = GL20.glCreateProgram();
            GL20.glAttachShader(programID, shaderID);
            GL20.glLinkProgram(programID);

            assertTrue(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_FALSE);

            Texture1DAccessor texture1DAccessor = new Texture1DAccessor(false, GLTexture.newTex1D(false, false, 1));
            texture1DAccessor.bind();
            texture1DAccessor.highlevel().alloc(
                    true,
                    BufferUtils.createByteBuffer(4).putInt(0).flip(),
                    TextureFormat.R32UI);

            GL42.glBindImageTexture(
                    3,
                    texture1DAccessor.textureID(),
                    0,
                    false,
                    0,
                    GL15.GL_READ_WRITE,
                    TextureFormat.R32UI.internalFormat);

            GL20.glUseProgram(programID);

            GL43.glDispatchCompute(1, 1, 1);
            GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

            long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

            int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000L);
            assertTrue(waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED);

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
    public void testDsaComputeIO() {
        GLTestExtension.submit(() -> {
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
                    3,
                    texture1DAccessor.textureID(),
                    0,
                    false,
                    0,
                    GL15.GL_READ_WRITE,
                    TextureFormat.R32UI.internalFormat);

            GL20.glUseProgram(programID);

            GL43.glDispatchCompute(1, 1, 1);
            GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

            long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

            int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000L);
            assertTrue(waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED);

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
