package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeStorageCleanupTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void terminalCleanup_releasesPayloadsCallbacksPoolsQueryAndWorkloadCaches(boolean reset) throws Exception {
        CleanComponent[] markers = {new QueryCacheTest.M0(), new QueryCacheTest.M1(), new QueryCacheTest.M2(),
            new QueryCacheTest.M3(), new QueryCacheTest.M4(), new QueryCacheTest.M5(), new QueryCacheTest.M6()};
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        for (int i = 0; i < markers.length; i++) {
            registry.registerComponent("m" + i, markers[i].getClass(), new MemberLayout());
        }
        registry.lock();
        EntityManager manager = new EntityManager(registry);
        for (int mask = 0; mask < 64; mask++) {
            List<CleanComponent> values = new ArrayList<>();
            values.add(markers[0]);
            for (int bit = 0; bit < 6; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    values.add(markers[bit + 1]);
                }
            }
            manager.createEntity(context -> {}, null, values.toArray(CleanComponent[]::new));
        }
        manager.flush();
        assertEquals(64, manager.selectPools(EntityQuery.query()).length);
        assertNotNull(field(manager, "queryCache"));
        manager.createEntity(context -> fail("Uncommitted destroy callback must not run"),
            context -> fail("Uncommitted create callback must not run"), markers[0]);
        Object[] slots = (Object[]) field(manager, "slots");
        JobExecutor executor = new JobExecutor(new JobRegistry(registry), manager);
        JobWorkloadCache cache = (JobWorkloadCache) field(executor, "workloads");
        JobWorkload retained = cache.acquire();
        retained.estimates = new int[128];
        cache.release(retained);

        manager.lifecycle.finish(reset);

        assertNull(field(manager, "queryCache"));
        assertTrue(((java.util.Map<?, ?>) field(manager, "archetypes")).isEmpty());
        assertTrue(((java.util.List<?>) field(manager, "commands")).isEmpty());
        for (Object slot : slots) {
            if (slot != null) {
                assertNull(field(slot, "committed"));
                assertNull(field(slot, "projected"));
                assertNull(field(slot, "createCallback"));
                assertNull(field(slot, "destroyCallback"));
            }
        }
        JobWorkload replacement = cache.acquire();
        assertNotSame(retained, replacement);
        assertEquals(0, replacement.estimates.length);
        if (reset) {
            assertEquals(0, manager.selectPools(EntityQuery.query()).length);
        } else {
            assertThrows(IllegalStateException.class, () -> manager.selectPools(EntityQuery.query()));
        }
    }

    @Test
    void exhaustedEpoch_closesRuntimeInsteadOfMakingOldGraphsValidAgain() throws Exception {
        EntityManager manager = new EntityManager(new ComponentRegistry(new FieldRegistry(new StructRegistry())));
        var epoch = RuntimeLifecycle.class.getDeclaredField("epoch");
        epoch.setAccessible(true);
        epoch.setLong(manager.lifecycle, Long.MAX_VALUE);
        assertThrows(IllegalStateException.class, () -> manager.lifecycle.finish(true));
        assertEquals(CleanECSRuntime.State.CLOSED, manager.lifecycle.state());
    }

    private static Object field(Object target, String name) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
