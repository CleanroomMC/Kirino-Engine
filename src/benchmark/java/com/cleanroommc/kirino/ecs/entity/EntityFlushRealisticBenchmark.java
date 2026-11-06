package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.GpuRegistryStub;
import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.Order;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Terrain flush workloads based on result/loading-* captures.
 * Commands are queued in invocation setup; only flush() is timed.
 *
 * Run with: .\gradlew.bat benchmark --args='EntityFlushRealisticBenchmark -prof gc'
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(value = 2, jvmArgsAppend = {"-Xms3g", "-Xmx3g"})
public class EntityFlushRealisticBenchmark {

    @State(Scope.Thread)
    public static class CreateState {
        @Param({"256", "2500", "25000"})
        public int meshlets;

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
            for (int i = 0; i < meshlets; i++) {
                manager.createEntity(destroy, create, fixture.template(i));
            }
        }
    }

    @Benchmark
    public EntityManager createMeshlets(CreateState state) {
        state.manager.flush();
        return state.manager;
    }

    @State(Scope.Thread)
    public static class DestroyState {
        @Param({"2500", "25000"})
        public int meshlets;

        /**
         * FORWARD matches MeshletDestroyJob; the other orders exercise different free-list patterns.
         */
        @Param({"FORWARD", "REVERSE", "SHUFFLED"})
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
            GpuRegistryStub gpu = new GpuRegistryStub();
            EntityCreateCallback create = EcsBenchFixture.createCallback(gpu);
            EntityDestroyCallback destroy = EcsBenchFixture.destroyCallback(gpu);
            List<CleanEntityHandle> handles = new ArrayList<>(meshlets);
            for (int i = 0; i < meshlets; i++) {
                handles.add(manager.createEntity(destroy, create, fixture.template(i)));
            }
            manager.flush();
            for (int id : order.indices(meshlets)) {
                handles.get(id).tryDestroy();
            }
        }
    }

    @Benchmark
    public EntityManager destroyAllMeshlets(DestroyState state) {
        state.manager.flush();
        return state.manager;
    }

    /**
     * Replace meshlets in a persistent 25k-entity world. Pool state carries over between invocations.
     */
    @State(Scope.Thread)
    public static class ChurnState {
        private static final int WORLD = 25_000;

        @Param({"16", "256", "2500", "12500"})
        public int changed;

        EcsBenchFixture fixture;
        EntityManager manager;
        CleanEntityHandle[] live;
        EntityCreateCallback create;
        EntityDestroyCallback destroy;
        int cursor;
        int created;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
            manager = fixture.newManager();
            GpuRegistryStub gpu = new GpuRegistryStub();
            create = EcsBenchFixture.createCallback(gpu);
            destroy = EcsBenchFixture.destroyCallback(gpu);
            live = new CleanEntityHandle[WORLD];
            for (int i = 0; i < WORLD; i++) {
                live[i] = manager.createEntity(destroy, create, fixture.template(i));
            }
            manager.flush();
            created = WORLD;
        }

        @Setup(Level.Invocation)
        public void queueChurn() {
            // Move the replacement window each frame to model remeshing successive regions.
            for (int k = 0; k < changed; k++) {
                live[(cursor + k) % WORLD].tryDestroy();
            }
            for (int k = 0; k < changed; k++) {
                live[(cursor + k) % WORLD] = manager.createEntity(destroy, create, fixture.template(created++));
            }
            cursor = (cursor + changed) % WORLD;
        }
    }

    @Benchmark
    public EntityManager churnFrame(ChurnState state) {
        state.manager.flush();
        return state.manager;
    }
}
