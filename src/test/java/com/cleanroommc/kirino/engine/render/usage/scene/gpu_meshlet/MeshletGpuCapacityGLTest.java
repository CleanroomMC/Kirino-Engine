package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer.VertexOutputDoubleBuffer;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.view.SSBOView;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.texture.accessor.TextureBufferAccessor;
import com.cleanroommc.test.kirino.gl.ext.GLTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(GLTestExtension.class)
class MeshletGpuCapacityGLTest {
    @Test
    void vertexGrowth_above512MiB_usesTheDeviceLimit() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.assumeGL46();
        long limit = GLTestExtension.submit(() -> GL32.glGetInteger64(GL43.GL_MAX_SHADER_STORAGE_BLOCK_SIZE)).join();
        assumeTrue(limit >= 536887300L);
        GLTestExtension.submit(() -> {
            try (var scope = GLResourceManager.openScope()) {
                VertexOutputDoubleBuffer output = new VertexOutputDoubleBuffer();
                output.lateInit();
                assertTrue(output.growVertex(21846));
                assertEquals(536887300, output.getVertexSize());
                output.getVertexWriteTarget().bind();
                assertEquals(536887300L, output.getVertexWriteTarget().fetchBufferSize64());
                assertTrue(output.getVertexWriteTarget().getPersistentMappedBuffer().isPresent());
                assertTrue(output.growVertex(21846));
                assertFalse(output.growVertex(Integer.MAX_VALUE));
                assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
            }
        }).join();
    }

    @Test
    void computeTables_above16384_preserveOldRangesAndClearNewEntries() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.assumeGL46();
        GLTestExtension.submit(() -> {
            try (var scope = GLResourceManager.openScope()) {
                MeshletComputeSystem.InternalBuffers buffers = new MeshletComputeSystem.InternalBuffers();
                buffers.lateInit();
                SSBOView before = field(buffers, "rangeSsbo", SSBOView.class);
                before.bind();
                before.clearUint(713);
                buffers.prepareRanges(25000);
                SSBOView after = field(buffers, "rangeSsbo", SSBOView.class);
                assertNotSame(before, after);
                after.bind();
                assertEquals(300000L, after.fetchBufferSize64());
                ByteBuffer values = BufferUtils.createByteBuffer(300000);
                after.downloadBySubData(0, values);
                for (int offset = 0; offset < values.capacity(); offset += 4) {
                    assertEquals(offset < 16384 * 12 ? 713 : 0, values.getInt(offset), "offset=" + offset);
                }
                buffers.prepareRanges(1);
                assertSame(after, field(buffers, "rangeSsbo", SSBOView.class));
                assertThrows(IllegalStateException.class, () -> buffers.prepareRanges(Integer.MAX_VALUE));
                assertSame(after, field(buffers, "rangeSsbo", SSBOView.class));

                List<Integer> dirty = new ArrayList<>();
                for (int i = 0; i < 25000; i++) dirty.add(i);
                assertEquals(25000, buffers.prepareTbo(dirty));
                VBOView workspace = field(buffers, "tboWorkspace", VBOView.class);
                workspace.bind();
                assertEquals(100008L, workspace.fetchBufferSize64());
                ByteBuffer uploaded = BufferUtils.createByteBuffer(100004);
                workspace.downloadBySubData(0, uploaded);
                assertEquals(25000, uploaded.getInt(0));
                for (int i = 0; i < dirty.size(); i++) assertEquals(i, uploaded.getInt(4 + i * 4));

                buffers.resetForWorld();
                after.bind();
                after.downloadBySubData(0, values);
                for (int offset = 0; offset < values.capacity(); offset += 4) assertEquals(0, values.getInt(offset));
                assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
            }
        }).join();
    }

    @Test
    void vertexShader_nonzeroDirtyOffset_onlyProcessesTheRequestedEntry() {
        GLTestExtension.assumeInitialized();
        GLTestExtension.assumeGL46();
        GLTestExtension.submit(() -> {
            try (var scope = GLResourceManager.openScope()) {
                MeshletComputeSystem.InternalBuffers buffers = new MeshletComputeSystem.InternalBuffers();
                buffers.lateInit();
                buffers.prepareTbo(List.of(0, 1, 2));
                SSBOView ranges = field(buffers, "rangeSsbo", SSBOView.class);
                ranges.bind();
                ranges.clearUint(42);
                bindZeroBuffer(3 * 3616, 0);
                bindZeroBuffer(32, 1);
                bindZeroBuffer(4, 2);
                SSBOView counter = field(buffers, "counterSsbo", SSBOView.class);
                GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 3, counter.bufferID);
                GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 4, ranges.bufferID);
                field(buffers, "dirtyListTbo", TextureBufferAccessor.class).bindUnit(4);
                int shader = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
                int program = GL20.glCreateProgram();
                try {
                    GL20.glShaderSource(shader, shaderSource());
                    GL20.glCompileShader(shader);
                    assertEquals(GL11.GL_TRUE, GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS), GL20.glGetShaderInfoLog(shader));
                    GL20.glAttachShader(program, shader);
                    GL20.glLinkProgram(program);
                    assertEquals(GL11.GL_TRUE, GL20.glGetProgrami(program, GL20.GL_LINK_STATUS), GL20.glGetProgramInfoLog(program));
                    GL20.glUseProgram(program);
                    GL20.glUniform1i(GL20.glGetUniformLocation(program, "dirtyList"), 4);
                    GL30.glUniform1ui(GL20.glGetUniformLocation(program, "dirtyOffset"), 2);
                    GL43.glDispatchCompute(1, 1, 1);
                    GL42.glMemoryBarrier(GL42.GL_BUFFER_UPDATE_BARRIER_BIT | GL43.GL_SHADER_STORAGE_BARRIER_BIT);
                    ByteBuffer values = BufferUtils.createByteBuffer(36);
                    ranges.bind();
                    ranges.downloadBySubData(0, values);
                    for (int offset = 0; offset < 24; offset += 4) assertEquals(42, values.getInt(offset));
                    assertEquals(2304, values.getInt(24));
                    assertEquals(0, values.getInt(28));
                    assertEquals(0, values.getInt(32));
                    assertEquals(GL11.GL_NO_ERROR, GL11.glGetError());
                } finally {
                    GL20.glUseProgram(0);
                    GL20.glDeleteProgram(program);
                    GL20.glDeleteShader(shader);
                }
            }
        }).join();
    }

    private static void bindZeroBuffer(int bytes, int binding) {
        SSBOView view = new SSBOView(new GLBuffer());
        view.bind();
        view.allocImmutable(bytes, 0);
        view.clearUint0();
        GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, binding, view.bufferID);
    }

    private static String shaderSource() {
        try (var source = MeshletGpuCapacityGLTest.class.getResourceAsStream("/assets/kirino/shaders/meshlets2vertices.comp")) {
            assertNotNull(source);
            return new String(source.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static <T> T field(Object target, String name, Class<T> type) {
        try {
            var field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (ReflectiveOperationException failure) {
            throw new AssertionError(failure);
        }
    }
}
