package com.cleanroommc.test.kirino.gl;

import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderDebugInjection;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderDebugSnippet;
import com.cleanroommc.kirino.engine.render.core.shader.compile.ShaderRemapHelper;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.meta.MapBufferAccessBit;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.shader.ShaderType;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(GLTestExtension.class)
public class ShaderDebugInfraTest {

    private static final String SHADER_SRC = """
            #version 430
            
            layout(local_size_x = 1) in;
            
            void main()
            {
            #ifdef KIRINO_DEBUG
                uint index = KirinoDebug_beginVec3Stream();
                KirinoDebug_putVec3(index, vec3(1.0, 1.0, 1.0));
                KirinoDebug_putVec3(index, vec3(2.0, 2.0, 2.0));
                KirinoDebug_putVec3(index, vec3(3.0, 3.0, 3.0));
                index = KirinoDebug_beginVec3Stream();
                KirinoDebug_putVec3(index, vec3(4.0, 4.0, 4.0));
                KirinoDebug_putVec3(index, vec3(5.0, 5.0, 5.0));
            #endif
            }
            """;

    @Test
    public void testVec3f() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            String source = ShaderDebugInjection.inject(
                    SHADER_SRC,
                    MinecraftResourceUtils.readText(new ResourceLocation("forge:shaders/debug/highlevel/temp_kirino_debug_vec3f.glsl"), MinecraftResourceUtils.NewLineType.BACK_SLASH_N));

            Set<String> remapFields = new HashSet<>();
            source = ShaderDebugInjection.injectDebugInfra(
                    source,
                    List.of(ShaderDebugInjection.Type.VEC3F_DEBUG),
                    remapFields);

            var remap = ShaderDebugInjection.resolveDebugRemap(remapFields);
            source = ShaderRemapHelper.remap(source, remap);

            GLTestExtension.logger().info(source);

            int shaderID = GL20.glCreateShader(ShaderType.COMPUTE.glValue);
            GL20.glShaderSource(shaderID, source);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);

            int programID = GL20.glCreateProgram();
            GL20.glAttachShader(programID, shaderID);
            GL20.glLinkProgram(programID);

            assertTrue(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_FALSE);

            String counterBinding = remap.get(ShaderDebugSnippet.REMAP_FIELD_COUNTER_BINDING);
            String vec3fBinding = remap.get(ShaderDebugSnippet.REMAP_FIELD_VEC3F_RECORD_BINDING);

            assertNotNull(counterBinding);
            assertNotNull(vec3fBinding);

            int b0 = Integer.parseInt(counterBinding);
            int b1 = Integer.parseInt(vec3fBinding);

            GLTestExtension.logger().info("counter binding: " + b0);
            GLTestExtension.logger().info("vec3f binding: " + b1);

            SSBOView ssbo0 = new SSBOView(new GLBuffer());
            SSBOView ssbo1 = new SSBOView(new GLBuffer());

            ssbo0.bind();
            ssbo0.allocPersistent(32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            ssbo0.clearUint0();
            ssbo0.mapPersistent(0, 32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

            ssbo1.bind();
            ssbo1.allocPersistent(32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            ssbo1.clearUint0();
            ssbo1.mapPersistent(0, 32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

            ssbo1.bind(0);

            // make sure clear is visible
            GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

            GL30.glBindBufferBase(ssbo0.target(), b0, ssbo0.bufferID);
            GL30.glBindBufferBase(ssbo1.target(), b1, ssbo1.bufferID);

            GL20.glUseProgram(programID);

            GL43.glDispatchCompute(1, 1, 1);
            GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

            long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

            int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000L);
            assumeTrue(waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED);

            ByteBuffer byteBuffer = ssbo0.getPersistentMappedBuffer().orElseThrow();

            assertEquals(2, byteBuffer.getInt());

            byteBuffer = ssbo1.getPersistentMappedBuffer().orElseThrow();

            assertEquals(1, byteBuffer.getFloat());
            assertEquals(1, byteBuffer.getFloat());
            assertEquals(1, byteBuffer.getFloat());
            assertEquals(0, Float.floatToIntBits(byteBuffer.getFloat()));

            assertEquals(2, byteBuffer.getFloat());
            assertEquals(2, byteBuffer.getFloat());
            assertEquals(2, byteBuffer.getFloat());
            assertEquals(0, Float.floatToIntBits(byteBuffer.getFloat()));

            assertEquals(3, byteBuffer.getFloat());
            assertEquals(3, byteBuffer.getFloat());
            assertEquals(3, byteBuffer.getFloat());
            assertEquals(0, Float.floatToIntBits(byteBuffer.getFloat()));

            assertEquals(4, byteBuffer.getFloat());
            assertEquals(4, byteBuffer.getFloat());
            assertEquals(4, byteBuffer.getFloat());
            assertEquals(1, Float.floatToIntBits(byteBuffer.getFloat()));

            assertEquals(5, byteBuffer.getFloat());
            assertEquals(5, byteBuffer.getFloat());
            assertEquals(5, byteBuffer.getFloat());
            assertEquals(1, Float.floatToIntBits(byteBuffer.getFloat()));
        }).join();
    }

    private static final String SHADER_SRC2 = """
            #version 430
            
            layout(local_size_x = 1) in;
            
            void main()
            {
            #ifdef KIRINO_DEBUG
                uint dispatchIndex = gl_GlobalInvocationID.x;
                uint index = KirinoDebug_beginVec3Stream();
                KirinoDebug_putVec3(index, vec3(dispatchIndex, 0.0, 0.0));
            #endif
            }
            """;

    @Test
    public void testVec3f_2() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.submit(() -> {
            GLTestExtension.assumeGL46();

            String source = ShaderDebugInjection.inject(
                    SHADER_SRC2,
                    MinecraftResourceUtils.readText(new ResourceLocation("forge:shaders/debug/highlevel/temp_kirino_debug_vec3f.glsl"), MinecraftResourceUtils.NewLineType.BACK_SLASH_N));

            Set<String> remapFields = new HashSet<>();
            source = ShaderDebugInjection.injectDebugInfra(
                    source,
                    List.of(ShaderDebugInjection.Type.VEC3F_DEBUG),
                    remapFields);

            var remap = ShaderDebugInjection.resolveDebugRemap(remapFields);
            source = ShaderRemapHelper.remap(source, remap);

            GLTestExtension.logger().info(source);

            int shaderID = GL20.glCreateShader(ShaderType.COMPUTE.glValue);
            GL20.glShaderSource(shaderID, source);
            GL20.glCompileShader(shaderID);

            assertTrue(GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) != GL11.GL_FALSE);

            int programID = GL20.glCreateProgram();
            GL20.glAttachShader(programID, shaderID);
            GL20.glLinkProgram(programID);

            assertTrue(GL20.glGetProgrami(programID, GL20.GL_LINK_STATUS) != GL11.GL_FALSE);

            String counterBinding = remap.get(ShaderDebugSnippet.REMAP_FIELD_COUNTER_BINDING);
            String vec3fBinding = remap.get(ShaderDebugSnippet.REMAP_FIELD_VEC3F_RECORD_BINDING);

            assertNotNull(counterBinding);
            assertNotNull(vec3fBinding);

            int b0 = Integer.parseInt(counterBinding);
            int b1 = Integer.parseInt(vec3fBinding);

            GLTestExtension.logger().info("counter binding: " + b0);
            GLTestExtension.logger().info("vec3f binding: " + b1);

            SSBOView ssbo0 = new SSBOView(new GLBuffer());
            SSBOView ssbo1 = new SSBOView(new GLBuffer());

            ssbo0.bind();
            ssbo0.allocPersistent(32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            ssbo0.clearUint0();
            ssbo0.mapPersistent(0, 32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

            ssbo1.bind();
            ssbo1.allocPersistent(32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);
            ssbo1.clearUint0();
            ssbo1.mapPersistent(0, 32 * 4, MapBufferAccessBit.READ_BIT, MapBufferAccessBit.WRITE_BIT, MapBufferAccessBit.MAP_PERSISTENT_BIT, MapBufferAccessBit.MAP_COHERENT_BIT);

            ssbo1.bind(0);

            // make sure clear is visible
            GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

            GL30.glBindBufferBase(ssbo0.target(), b0, ssbo0.bufferID);
            GL30.glBindBufferBase(ssbo1.target(), b1, ssbo1.bufferID);

            GL20.glUseProgram(programID);

            GL43.glDispatchCompute(2, 1, 1);
            GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

            long fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);

            int waitReturn = GL32C.glClientWaitSync(fence, GL32.GL_SYNC_FLUSH_COMMANDS_BIT, 1_000_000L);
            assumeTrue(waitReturn == GL32.GL_ALREADY_SIGNALED || waitReturn == GL32.GL_CONDITION_SATISFIED);

            ByteBuffer byteBuffer = ssbo0.getPersistentMappedBuffer().orElseThrow();

            assertEquals(2, byteBuffer.getInt());

            byteBuffer = ssbo1.getPersistentMappedBuffer().orElseThrow();

            GLTestExtension.logger().info("invo index 0: " + (int) byteBuffer.getFloat());
            byteBuffer.getFloat(); byteBuffer.getFloat();
            GLTestExtension.logger().info("header 0: " + Float.floatToIntBits(byteBuffer.getFloat()));

            GLTestExtension.logger().info("invo index 1: " + (int) byteBuffer.getFloat());
            byteBuffer.getFloat(); byteBuffer.getFloat();
            GLTestExtension.logger().info("header 1: " + Float.floatToIntBits(byteBuffer.getFloat()));
        }).join();
    }
}
