package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.GpuRegistryStub;
import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.Order;
import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.Tag;
import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.Tiny;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArrayRange;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Stress workloads for flush, fragmented pools and archetype queries.
 * Setup is outside the timed region. Large batches can take minutes; narrow with -p entities=...
 *
 * Run with: .\gradlew.bat benchmark --args='EntityFlushAbsurdBenchmark -prof gc'
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
@Fork(value = 1, jvmArgsAppend = {"-Xms8g", "-Xmx8g"})
public class EntityFlushAbsurdBenchmark {

    @State(Scope.Thread)
    public static class HugeMeshletCreateState {
        @Param({"100000", "200000"})
        public int entities;

        EcsBenchFixture fixture;
        EntityManager manager;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
        }

        @Setup(Level.Invocation)
        public void queue() {
            manager = fixture.newManager();
            GpuRegistryStub gpu = new GpuRegistryStub();
            EntityCreateCallback create = EcsBenchFixture.createCallback(gpu);
            EntityDestroyCallback destroy = EcsBenchFixture.destroyCallback(gpu);
            for (int i = 0; i < entities; i++) {
                manager.createEntity(destroy, create, fixture.template(i));
            }
        }
    }

    @Benchmark
    public EntityManager createHugeMeshletBatch(HugeMeshletCreateState state) {
        state.manager.flush();
        return state.manager;
    }

    @State(Scope.Thread)
    public static class TinyCreateState {
        @Param({"100000", "1000000"})
        public int entities;

        EcsBenchFixture fixture;
        EntityManager manager;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
        }

        @Setup(Level.Invocation)
        public void queue() {
            manager = fixture.newManager();
            Tiny shared = new Tiny();
            for (int i = 0; i < entities; i++) {
                shared.a = i;
                manager.createEntity(shared);
            }
        }
    }

    @Benchmark
    public EntityManager createTinyBatch(TinyCreateState state) {
        state.manager.flush();
        return state.manager;
    }

    @State(Scope.Thread)
    public static class TinyDestroyState {
        @Param({"10000", "50000", "100000"})
        public int entities;

        @Param({"FORWARD", "REVERSE", "SHUFFLED", "EVERY_OTHER_THEN_REST"})
        public Order order;

        EcsBenchFixture fixture;
        EntityManager manager;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
        }

        @Setup(Level.Invocation)
        public void populateAndQueueDestroys() {
            manager = fixture.newManager();
            Tiny shared = new Tiny();
            List<CleanEntityHandle> handles = new ArrayList<>(entities);
            for (int i = 0; i < entities; i++) {
                handles.add(manager.createEntity(shared));
            }
            manager.flush();
            for (int id : order.indices(entities)) {
                handles.get(id).tryDestroy();
            }
        }
    }

    @Benchmark
    public EntityManager destroyTiny(TinyDestroyState state) {
        state.manager.flush();
        return state.manager;
    }

    /**
     * Alternate adding and removing Tag on every entity.
     * Results combine both directions; MESHLET includes component reconstruction and flattening.
     */
    @State(Scope.Thread)
    public static class ArchetypeThrashState {
        @Param({"TINY", "MESHLET"})
        public String payload;

        @Param({"2000", "20000"})
        public int entities;

        EcsBenchFixture fixture;
        EntityManager manager;
        List<CleanEntityHandle> handles;
        boolean tagged;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
            manager = fixture.newManager();
            handles = new ArrayList<>(entities);
            GpuRegistryStub gpu = new GpuRegistryStub();
            EntityCreateCallback create = EcsBenchFixture.createCallback(gpu);
            for (int i = 0; i < entities; i++) {
                handles.add(payload.equals("TINY")
                    ? manager.createEntity(new Tiny())
                    : manager.createEntity(null, create, fixture.template(i)));
            }
            manager.flush();
        }

        @Setup(Level.Invocation)
        public void queueToggle() {
            for (CleanEntityHandle handle : handles) {
                if (tagged) {
                    handle.tryRemoveComponent(Tag.class);
                } else {
                    handle.tryAddComponent(new Tag());
                }
            }
            tagged = !tagged;
        }
    }

    @Benchmark
    public EntityManager toggleTagOnEveryEntity(ArchetypeThrashState state) {
        state.manager.flush();
        return state.manager;
    }

    /**
     * Measure range snapshots with interior holes in a 100k-slot pool.
     */
    @State(Scope.Benchmark)
    public static class FragmentedPoolState {
        @Param({"0", "50", "90"})
        public int holes;

        static final int SIZE = 100_000;
        ArchetypeDataPool pool;

        @Setup(Level.Trial)
        public void trial() {
            EcsBenchFixture fixture = new EcsBenchFixture();
            EntityManager manager = fixture.newManager();
            Tiny shared = new Tiny();
            List<CleanEntityHandle> handles = new ArrayList<>(SIZE);
            for (int i = 0; i < SIZE; i++) handles.add(manager.createEntity(shared));
            manager.flush();
            // Keep the last slot alive so removing interior slots does not shorten the range.
            int toPunch = (int) ((long) (SIZE - 1) * holes / 100);
            int stride = Math.max(1, (SIZE - 1) / Math.max(1, toPunch));
            int punched = 0;
            for (int i = 0; i < SIZE - 1 && punched < toPunch; i += stride) {
                handles.get(i).tryDestroy();
                punched++;
            }
            manager.flush();
            pool = manager.startQuery(manager.newQuery().with(Tiny.class)).get(0);
        }
    }

    @Benchmark
    public ArrayRange getArrayRangeOnFragmentedPool(FragmentedPoolState state) {
        return state.pool.getArrayRange();
    }

    /**
     * All 255 non-empty combinations of eight components.
     */
    @State(Scope.Benchmark)
    public static class ManyArchetypesState {
        @Param({"1", "8"})
        public int mustHaveParts;

        EntityManager manager;
        EntityQuery query;

        @Setup(Level.Trial)
        public void trial() throws Exception {
            EcsBenchFixture fixture = new EcsBenchFixture();
            manager = fixture.newManager();
            List<Class<? extends CleanComponent>> parts = EcsBenchFixture.ARCHETYPE_PARTS;
            for (int mask = 1; mask < (1 << parts.size()); mask++) {
                List<CleanComponent> components = new ArrayList<>();
                for (int bit = 0; bit < parts.size(); bit++) {
                    if ((mask & (1 << bit)) != 0) {
                        components.add(parts.get(bit).getDeclaredConstructor().newInstance());
                    }
                }
                manager.createEntity(components.toArray(new CleanComponent[0]));
            }
            manager.flush();
            query = manager.newQuery();
            for (int bit = 0; bit < mustHaveParts; bit++) query.with(parts.get(bit));
        }
    }

    @Benchmark
    public List<ArchetypeDataPool> startQueryAcross255Archetypes(ManyArchetypesState state) {
        return state.manager.startQuery(state.query);
    }
}
