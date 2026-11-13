package com.cleanroommc.kirino.engine.render.usage.scene.gpu_meshlet;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataHandle;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.usage.scene.scheduler.RendererTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeshletWorldResetTest {
    @TempDir Path home;
    private DebugDataHandle<?> previous;
    @BeforeEach void setup() throws Throwable {
        RendererTestSupport.initializeConfig(home);
        previous = RendererTestSupport.installInactiveTimeline();
    }
    @AfterEach void restore() throws Throwable { RendererTestSupport.setTimeline(previous); }

    @Test
    void registryReset_discardsPartialWritingIdsAndDirtySlotsWithoutReplacingBuffers() throws Exception {
        MeshletGpuRegistry registry = new MeshletGpuRegistry();
        MeshletComponent first = new MeshletComponent();
        MeshletComponent next = new MeshletComponent();
        registry.allocateMeshletID(first);
        registry.allocateMeshletID(next);
        registry.meshletBufferSlotAllocator.syncMeshletIdAddition(List.of(first.meshletId, next.meshletId));
        registry.disposeMeshletID(first.meshletId);
        set(registry, "writing", true);
        registry.finishWriting();
        Object buffer = registry.meshletInputBuffer;
        set(registry, "writing", true);
        set(registry, "computing", true);
        registry.resetForWorld();
        assertFalse(registry.isWriting());
        assertFalse(registry.isFinishedWritingOnce());
        assertFalse(registry.hasMeshletChanges());
        assertEquals(0, registry.getMeshletCount());
        assertTrue(registry.getDirtySlots().isEmpty());
        assertSame(buffer, registry.meshletInputBuffer);
        assertThrows(IllegalArgumentException.class, () -> registry.meshletBufferSlotAllocator.getSlotForMeshletId(next.meshletId));
        MeshletComponent replacement = new MeshletComponent();
        registry.allocateMeshletID(replacement);
        assertEquals(0, replacement.meshletId);
        assertTrue(registry.hasMeshletChanges());
        registry.resetForWorld();
        assertFalse(registry.hasMeshletChanges());
    }

    @Test
    void computeReset_discardsOldCountsAndFailedDispatchStateBeforeReuse() throws Exception {
        MeshletComputeSystem compute = new MeshletComputeSystem(null, null);
        set(compute, "shaderRunning", true);
        set(compute, "vertexCount", 100);
        set(compute, "indexCount", 200);
        compute.resetForWorld();
        assertFalse(compute.isShaderRunning());
        assertEquals(0, compute.getVertexCount());
        assertEquals(0, compute.getIndexCount());
        compute.resetForWorld();
    }

    private static void set(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
