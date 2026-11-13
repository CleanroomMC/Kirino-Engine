package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class QueryCacheTest {
    @TempDir
    static Path home;

    @BeforeAll
    static void initializeEnvironment() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var field = FMLInjectionData.class.getDeclaredField("minecraftHome");
        field.setAccessible(true);
        Object previous = field.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            field.set(null, home.toFile());
            Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            field.set(null, previous);
            Launch.minecraftHome = previousLaunch;
        }
    }

    @Test
    void equivalentConditions_ignoreOrderAndDuplicatesAndSnapshotMutableBuilder() {
        Fixture fixture = fixture();
        EntityQuery original = EntityQuery.query().with(M0.class).with(M1.class).without(M2.class);
        ArchetypeDataPool[] first = fixture.manager.selectPools(original);
        assertEquals(16, first.length);
        assertSame(first, fixture.manager.selectPools(EntityQuery.query().with(M1.class).with(M0.class)
            .with(M1.class).without(M2.class).without(M2.class)));

        original.without(M1.class);
        assertEquals(0, fixture.manager.selectPools(original).length);
        assertEquals(16, first.length);
        for (ArchetypeDataPool pool : first) {
            assertTrue(pool.components.contains(M1.class));
            assertFalse(pool.components.contains(M2.class));
        }
        assertEquals(16, fixture.manager.selectPools(
            EntityQuery.query().with(M0.class).with(M1.class).without(M2.class)).length);
    }

    @Test
    void pendingCreate_doesNotPublishPoolAndCommitInvalidatesCachedEmptyResult() {
        Fixture fixture = fixture();
        EntityQuery query = EntityQuery.query().with(NewMarker.class);
        ArchetypeDataPool[] before = fixture.manager.selectPools(query);
        assertEquals(0, before.length);
        CleanEntityHandle created = fixture.manager.createEntity(new M0(), new NewMarker());
        assertSame(before, fixture.manager.selectPools(query));
        assertFalse(created.isAlive());

        fixture.manager.flush();

        ArchetypeDataPool[] after = fixture.manager.selectPools(query);
        assertEquals(1, after.length);
        assertTrue(after[0].containsEntity(created.index));
        assertEquals(0, before.length);
        assertSame(after, fixture.manager.selectPools(query));
    }

    @Test
    void migrations_newPoolInvalidatesAndExistingPoolsStayLiveAfterDestroyAndReuse() {
        Fixture fixture = fixture();
        CleanEntityHandle entity = fixture.handles[0];
        EntityQuery query = EntityQuery.query().with(NewMarker.class);
        ArchetypeDataPool[] initial = fixture.manager.selectPools(query);
        assertTrue(entity.tryAddComponent(new NewMarker()));
        assertSame(initial, fixture.manager.selectPools(query));
        fixture.manager.flush();
        ArchetypeDataPool[] selected = fixture.manager.selectPools(query);
        assertEquals(1, selected.length);
        assertTrue(selected[0].containsEntity(entity.index));
        assertTrue(entity.tryRemoveComponent(NewMarker.class));
        fixture.manager.flush();
        assertSame(selected, fixture.manager.selectPools(query));
        assertFalse(selected[0].containsEntity(entity.index));

        CleanEntityHandle replacement = fixture.manager.createEntity(new NewMarker(), new M0());
        fixture.manager.flush();
        assertSame(selected, fixture.manager.selectPools(query));
        assertTrue(selected[0].containsEntity(replacement.index));
        assertTrue(replacement.tryDestroy());
        fixture.manager.flush();
        assertSame(selected, fixture.manager.selectPools(query));
        assertFalse(selected[0].containsEntity(replacement.index));
    }

    @Test
    void emptyAndContradictoryConditions_matchUncachedScanAcrossManyQueries() {
        Fixture fixture = fixture();
        for (int required = 0; required < 64; required++) {
            for (int excluded = 0; excluded < 8; excluded++) {
                EntityQuery query = EntityQuery.query();
                for (int bit = 0; bit < 6; bit++) {
                    if ((required & (1 << bit)) != 0) {
                        query.with(fixture.markers[bit + 1].getClass());
                    }
                    if ((excluded & (1 << bit)) != 0) {
                        query.without(fixture.markers[bit + 1].getClass());
                    }
                }
                assertSelection(fixture.manager, query);
                assertSelection(fixture.manager, query);
            }
        }
        assertEquals(64, fixture.manager.selectPools(EntityQuery.query()).length);
        assertEquals(0, fixture.manager.selectPools(EntityQuery.query().with(M0.class).without(M0.class)).length);
    }

    @Test
    void collidingEntries_verifyExactConditionsAndClearDropsAllEntries() {
        QueryCache cache = new QueryCache();
        ReferenceSet<Class<? extends CleanComponent>> required = set(M0.class, M1.class);
        ReferenceSet<Class<? extends CleanComponent>> excluded = set(M2.class);
        ArchetypeDataPool[] first = new ArchetypeDataPool[0];
        cache.store(7, required, excluded, first);
        assertSame(first, cache.find(7, set(M1.class, M0.class), set(M2.class)));
        assertNull(cache.find(7, set(M0.class), excluded));
        assertNull(cache.find(7, required, set(M3.class)));
        ArchetypeDataPool[] second = new ArchetypeDataPool[0];
        cache.store(7, set(M3.class), excluded, second);
        assertSame(first, cache.find(7, required, excluded));
        assertSame(second, cache.find(7, set(M3.class), excluded));
        cache.store(7, set(M4.class), excluded, new ArchetypeDataPool[0]);
        cache.store(7, set(M5.class), excluded, new ArchetypeDataPool[0]);
        cache.store(7, set(M6.class), excluded, new ArchetypeDataPool[0]);
        assertNull(cache.find(7, required, excluded));
        assertSame(second, cache.find(7, set(M3.class), excluded));
        assertNotNull(cache.find(7, set(M6.class), excluded));
        cache.clear();
        assertNull(cache.find(7, set(M3.class), excluded));
    }

    @Test
    void retentionLimits_bypassOversizedArraysAndConditionSets() throws Exception {
        QueryCache cache = new QueryCache();
        ReferenceSet<Class<? extends CleanComponent>> required = set(M0.class);
        ReferenceSet<Class<? extends CleanComponent>> excluded = set();
        ArchetypeDataPool[] maximum = new ArchetypeDataPool[QueryCache.MAX_POOLS_PER_ENTRY];
        cache.store(1, required, excluded, maximum);
        assertSame(maximum, cache.find(1, required, excluded));
        cache.store(2, required, excluded, new ArchetypeDataPool[QueryCache.MAX_POOLS_PER_ENTRY + 1]);
        assertNull(cache.find(2, required, excluded));

        ReferenceSet<Class<? extends CleanComponent>> tooWide = new ReferenceArraySet<>();
        byte[] bytes;
        try (var input = M0.class.getResourceAsStream("QueryCacheTest$M0.class")) {
            bytes = input.readAllBytes();
        }
        class Loader extends ClassLoader {
            Loader() { super(M0.class.getClassLoader()); }
            Class<? extends CleanComponent> copy() {
                return defineClass(M0.class.getName(), bytes, 0, bytes.length).asSubclass(CleanComponent.class);
            }
        }
        for (int i = 0; i <= QueryCache.MAX_CONDITIONS; i++) {
            tooWide.add(new Loader().copy());
        }
        cache.store(3, tooWide, excluded, new ArchetypeDataPool[0]);
        assertNull(cache.find(3, tooWide, excluded));
        for (int i = 0; i < QueryCache.CAPACITY * 4; i++) {
            cache.store(i, required, excluded, new ArchetypeDataPool[0]);
        }
        for (int i = 0; i < QueryCache.CAPACITY * 3; i++) {
            assertNull(cache.find(i, required, excluded));
        }
        for (int i = QueryCache.CAPACITY * 3; i < QueryCache.CAPACITY * 4; i++) {
            assertNotNull(cache.find(i, required, excluded));
        }
    }

    @Test
    void sameNameTypesFromDifferentLoaders_areDifferentConditionKeys() throws Exception {
        byte[] bytes;
        try (var input = M0.class.getResourceAsStream("QueryCacheTest$M0.class")) {
            bytes = input.readAllBytes();
        }
        class Loader extends ClassLoader {
            Loader() { super(M0.class.getClassLoader()); }
            Class<? extends CleanComponent> copy() {
                return defineClass(M0.class.getName(), bytes, 0, bytes.length).asSubclass(CleanComponent.class);
            }
        }
        Class<? extends CleanComponent> first = new Loader().copy();
        Class<? extends CleanComponent> second = new Loader().copy();
        QueryCache cache = new QueryCache();
        cache.store(0, set(first), set(), new ArchetypeDataPool[0]);
        assertNotNull(cache.find(0, set(first), set()));
        assertNull(cache.find(0, set(second), set()));
    }

    @Test
    void managerOwnership_andTwoCachedScopes_keepArraysAndGuardsIndependent() {
        Fixture first = fixture();
        Fixture second = fixture();
        EntityQuery query = EntityQuery.query().with(M0.class);
        ArchetypeDataPool[] pools = first.manager.selectPools(query);
        ArchetypeDataPool[] other = second.manager.selectPools(query);
        assertNotSame(pools, other);
        assertFalse(Arrays.asList(pools).contains(other[0]));
        var one = first.manager.beginExecution(query);
        var two = first.manager.beginExecution(EntityQuery.query().with(M0.class));
        try {
            assertSame(pools, one.pools());
            assertSame(pools, two.pools());
            assertThrows(IllegalStateException.class, first.manager::flush);
            one.close();
            assertThrows(IllegalStateException.class, first.manager::flush);
            assertThrows(IllegalStateException.class, () -> pools[0].removeEntity(first.handles[0].index));
        } finally {
            one.close();
            two.close();
        }
        first.manager.flush();
        second.manager.flush();
    }

    @Test
    void jobQuery_isInvokedEachLaunchAndDynamicConditionsChangeSelectedPools() {
        Fixture fixture = fixture();
        JobRegistry registry = new JobRegistry(fixture.components);
        registry.registerParallelJob(DynamicJob.class);
        JobExecutor executor = new JobExecutor(registry);
        DynamicJob.queryCalls = 0;
        for (int i = 0; i < 20; i++) {
            DynamicJob.include = i % 2 == 0;
            DynamicJob.visits = 0;
            var result = executor.executeParallelJob(fixture.manager, DynamicJob.class, Map.of(), Runnable::run);
            result.future().join();
            assertEquals(32, DynamicJob.visits);
            assertEquals(i + 1, DynamicJob.queryCalls);
            assertEquals(32, result.totalThreadCount());
        }
        fixture.manager.flush();
    }

    @Test
    void cachedSelection_afterCommitFailure_stillRejectsUnavailableManager() {
        Fixture fixture = fixture();
        EntityQuery query = EntityQuery.query().with(M0.class);
        fixture.manager.selectPools(query);
        fixture.manager.createEntity(null, context -> {
            assertThrows(IllegalStateException.class, () -> fixture.manager.selectPools(query));
            throw new IllegalStateException("expected callback failure");
        }, new M0());

        assertThrows(IllegalStateException.class, fixture.manager::flush);
        assertThrows(IllegalStateException.class, () -> fixture.manager.selectPools(query));
        assertThrows(IllegalStateException.class, () -> fixture.manager.beginExecution(query));
    }

    @Test
    void concurrentSelectionsAndPendingArchetype_changesStayWithinManagerProtocol() throws Exception {
        Fixture fixture = fixture();
        fixture.manager.selectPools(EntityQuery.query().with(M0.class));
        CountDownLatch start = new CountDownLatch(1);
        try (var callers = Executors.newFixedThreadPool(2)) {
            var select = callers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                for (int i = 0; i < 100; i++) {
                    try (var scope = fixture.manager.beginExecution(EntityQuery.query().with(M0.class))) {
                        assertEquals(64, scope.pools().length);
                    }
                }
                return null;
            });
            var create = callers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                return fixture.manager.createEntity(new M0(), new NewMarker());
            });
            start.countDown();
            select.get(5, TimeUnit.SECONDS);
            CleanEntityHandle pending = create.get(5, TimeUnit.SECONDS);
            fixture.manager.flush();
            ArchetypeDataPool[] after = fixture.manager.selectPools(EntityQuery.query().with(M0.class));
            assertEquals(65, after.length);
            assertTrue(Arrays.stream(after).anyMatch(pool -> pool.containsEntity(pending.index)));
        }
    }

    private static void assertSelection(EntityManager manager, EntityQuery query) {
        List<ArchetypeDataPool> expected = manager.startQuery(query);
        List<ArchetypeDataPool> actual = Arrays.asList(manager.selectPools(query));
        assertEquals(expected.size(), actual.size());
        assertTrue(actual.containsAll(expected));
    }

    @SafeVarargs
    private static ReferenceSet<Class<? extends CleanComponent>> set(Class<? extends CleanComponent>... values) {
        return new ReferenceArraySet<>(values);
    }

    private static Fixture fixture() {
        CleanComponent[] markers = {new M0(), new M1(), new M2(), new M3(), new M4(), new M5(), new M6()};
        ComponentRegistry components = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        for (int i = 0; i < markers.length; i++) {
            components.registerComponent("marker" + i, markers[i].getClass(), new MemberLayout());
        }
        components.registerComponent("new", NewMarker.class, new MemberLayout());
        components.lock();
        EntityManager manager = new EntityManager(components);
        CleanEntityHandle[] handles = new CleanEntityHandle[64];
        for (int mask = 0; mask < handles.length; mask++) {
            List<CleanComponent> values = new ArrayList<>();
            values.add(markers[0]);
            for (int bit = 0; bit < 6; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    values.add(markers[bit + 1]);
                }
            }
            handles[mask] = manager.createEntity(values.toArray(CleanComponent[]::new));
        }
        manager.flush();
        return new Fixture(components, manager, handles, markers);
    }

    private record Fixture(ComponentRegistry components, EntityManager manager,
                           CleanEntityHandle[] handles, CleanComponent[] markers) {}

    public static class DynamicJob implements ParallelJob {
        static boolean include;
        static int queryCalls;
        static int visits;

        public void query(EntityQuery query) {
            queryCalls++;
            if (include) {
                query.with(M1.class);
            } else {
                query.without(M1.class);
            }
        }
        public int estimateWorkload(int row) { return 1; }
        public void execute(EntityCommands commands, int row, int entity, int partition) {
            assertEquals(include, (entity & 1) != 0);
            visits++;
        }
    }

    public static class M0 implements CleanComponent {}
    public static class M1 implements CleanComponent {}
    public static class M2 implements CleanComponent {}
    public static class M3 implements CleanComponent {}
    public static class M4 implements CleanComponent {}
    public static class M5 implements CleanComponent {}
    public static class M6 implements CleanComponent {}
    public static class NewMarker implements CleanComponent {}
}
