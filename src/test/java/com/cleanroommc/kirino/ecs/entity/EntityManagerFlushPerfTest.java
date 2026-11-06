package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.entity.EcsFixture.GpuRegistryStub;
import com.cleanroommc.kirino.ecs.entity.EcsFixture.Tiny;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opt-in timing checks: KIRINO_PERF=true, with an optional KIRINO_PERF_FLUSH_BUDGET_MS budget.
 * Gradle does not forward -D flags to the test JVM.
 * Ratios are a rough regression signal; use the JMH benchmarks for measurements.
 */
@EnabledIfEnvironmentVariable(named = "KIRINO_PERF", matches = "true")
class EntityManagerFlushPerfTest {
    private static final double MAX_RATIO_FOR_4X_INPUT = 9.0; // linear = 4, quadratic = 16
    private final EcsFixture fixture = new EcsFixture();

    /**
     * Times flush only; command preparation is outside the measurement.
     */
    private static long bestFlushNanos(int rounds, Supplier<EntityManager> prepare) {
        long best = Long.MAX_VALUE;
        for (int i = 0; i < rounds; i++) {
            EntityManager manager = prepare.get();
            long t0 = System.nanoTime();
            manager.flush();
            best = Math.min(best, System.nanoTime() - t0);
        }
        return best;
    }

    private static void report(String name, int n, long nanos) {
        System.out.printf("[ecs-perf] %-48s n=%-8d %9.2f ms (%7.0f ns/entity)%n", name, n, nanos / 1e6, (double) nanos / n);
    }

    private EntityManager meshletCreate(int count) {
        EntityManager manager = fixture.newManager();
        GpuRegistryStub gpu = new GpuRegistryStub();
        EntityCreateCallback create = EcsFixture.createCallback(gpu);
        EntityDestroyCallback destroy = EcsFixture.destroyCallback(gpu);
        for (int i = 0; i < count; i++) manager.createEntity(destroy, create, fixture.template(i));
        return manager;
    }

    private EntityManager tinyDestroy(int count, int[] order) {
        EntityManager manager = fixture.newManager();
        List<CleanEntityHandle> handles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) handles.add(manager.createEntity(new Tiny()));
        manager.flush();
        for (int id : order) handles.get(id).tryDestroy();
        return manager;
    }

    private static int[] forward(int n) {
        int[] ids = new int[n];
        for (int i = 0; i < n; i++) ids[i] = i;
        return ids;
    }

    private static int[] shuffled(int n) {
        int[] ids = forward(n);
        Random random = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int t = ids[i];
            ids[i] = ids[j];
            ids[j] = t;
        }
        return ids;
    }

    @Test
    void flushCreateMeshlets_quadrupleInput_costsRoughlyFourTimes() {
        meshletCreate(2_000).flush(); // JIT warm-up
        long small = bestFlushNanos(3, () -> meshletCreate(5_000));
        long large = bestFlushNanos(3, () -> meshletCreate(20_000));
        report("flush create meshlets", 5_000, small);
        report("flush create meshlets", 20_000, large);
        assertTrue((double) large / small < MAX_RATIO_FOR_4X_INPUT,
            "create flush scaled " + (double) large / small + "x for 4x input");
    }

    @Test
    void flushCreateMeshlets_frameBudget() {
        long nanos = bestFlushNanos(3, () -> meshletCreate(25_000));
        report("flush create meshlets (profile-sized)", 25_000, nanos);
        String configured = System.getenv("KIRINO_PERF_FLUSH_BUDGET_MS");
        long budgetMs = configured == null ? 1_000L : Long.parseLong(configured);
        assertTrue(nanos / 1_000_000 < budgetMs,
            "creating 25k meshlets took " + nanos / 1_000_000 + " ms, budget " + budgetMs + " ms");
    }

    @Test
    void flushDestroyForwardOrder_quadrupleInput_costsRoughlyFourTimes() {
        // MeshletDestroyJob walks pool order, so FORWARD is the production order.
        bestFlushNanos(1, () -> tinyDestroy(4_000, forward(4_000)));
        long small = bestFlushNanos(3, () -> tinyDestroy(10_000, forward(10_000)));
        long large = bestFlushNanos(3, () -> tinyDestroy(40_000, forward(40_000)));
        report("flush destroy forward (tiny)", 10_000, small);
        report("flush destroy forward (tiny)", 40_000, large);
        assertTrue((double) large / small < MAX_RATIO_FOR_4X_INPUT,
            "forward destroy flush scaled " + (double) large / small + "x for 4x input (free-list scan is O(n^2))");
    }

    @Test
    void flushDestroyShuffledOrder_quadrupleInput_costsRoughlyFourTimes() {
        bestFlushNanos(1, () -> tinyDestroy(4_000, shuffled(4_000)));
        long small = bestFlushNanos(3, () -> tinyDestroy(10_000, shuffled(10_000)));
        long large = bestFlushNanos(3, () -> tinyDestroy(40_000, shuffled(40_000)));
        report("flush destroy shuffled (tiny)", 10_000, small);
        report("flush destroy shuffled (tiny)", 40_000, large);
        assertTrue((double) large / small < MAX_RATIO_FOR_4X_INPUT,
            "shuffled destroy flush scaled " + (double) large / small + "x for 4x input");
    }
}
