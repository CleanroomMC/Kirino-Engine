package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet.buffer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class MeshletBufferCapacityTest {
    @ParameterizedTest
    @CsvSource({"0, 24576, 4, 4", "21845, 24576, 4, 536862724",
        "21846, 24576, 4, 536887300", "25000, 24576, 4, 614400004",
        "25000, 12, 0, 300000", "25000, 4, 8, 100008",
        "2147483647, 24576, 4, 52776558108676"})
    void requiredBytes_largeCountsAndFormerLimits_preservesFullSize(int count, int stride, int padding, long expected) {
        assertEquals(expected, MeshletBufferCapacity.requiredBytes(count, stride, padding));
    }

    @Test
    void checkedBytes_respectsDriverLimitAndReportsTheRequest() {
        int maximum = MeshletBufferCapacity.maximumBytes(Integer.MAX_VALUE);
        assertEquals(614400004, MeshletBufferCapacity.checkedBytes("vertices", 25000, 24576, 4, maximum));
        var failure = assertThrows(IllegalStateException.class,
            () -> MeshletBufferCapacity.checkedBytes("vertices", 25000, 24576, 4, 512 * 1024 * 1024));
        assertTrue(failure.getMessage().contains("meshlets=25000"));
        assertTrue(failure.getMessage().contains("requiredBytes=614400004"));
        assertTrue(failure.getMessage().contains("maxBytes=536870912"));
        assertEquals(128 * 1024 * 1024, MeshletBufferCapacity.maximumBytes(128 * 1024 * 1024));
        assertEquals(2147483636, maximum);
        assertEquals(maximum, MeshletBufferCapacity.maximumBytes(Long.MAX_VALUE));
    }

    @Test
    void outputGrowth_overflowingRequests_rejectWithoutTouchingEitherBuffer() {
        VertexOutputDoubleBuffer vertices = new VertexOutputDoubleBuffer();
        DrawIndexOutputDoubleBuffer drawIndices = new DrawIndexOutputDoubleBuffer();
        for (int buffer = 0; buffer < 2; buffer++) {
            int vertexSize = vertices.getVertexSize();
            int indexSize = vertices.getIndexSize();
            int drawSize = drawIndices.getIndexSize();
            assertFalse(vertices.growVertex(Integer.MAX_VALUE));
            assertFalse(vertices.growIndex(Integer.MAX_VALUE));
            assertFalse(drawIndices.growIndex(Integer.MAX_VALUE));
            assertThrows(IllegalArgumentException.class, () -> vertices.growVertex(-1));
            assertThrows(IllegalArgumentException.class, () -> vertices.growIndex(-1));
            assertThrows(IllegalArgumentException.class, () -> drawIndices.growIndex(-1));
            assertEquals(vertexSize, vertices.getVertexSize());
            assertEquals(indexSize, vertices.getIndexSize());
            assertEquals(drawSize, drawIndices.getIndexSize());
            vertices.swap();
            drawIndices.swap();
        }
    }

    @Test
    void requiredBytes_invalidLayoutOrCount_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> MeshletBufferCapacity.requiredBytes(-1, 4, 0));
        assertThrows(IllegalArgumentException.class, () -> MeshletBufferCapacity.requiredBytes(1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> MeshletBufferCapacity.requiredBytes(1, 4, -1));
        assertThrows(IllegalArgumentException.class, () -> MeshletBufferCapacity.maximumBytes(0));
    }
}
