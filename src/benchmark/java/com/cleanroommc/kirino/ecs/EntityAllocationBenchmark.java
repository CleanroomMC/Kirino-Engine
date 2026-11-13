package com.cleanroommc.kirino.ecs;

import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EntityAllocationBenchmark {
    @Param({"1024", "25000"}) public int rows;
    @Param({"small", "meshlet"}) public String schema;

    private EntityLoadFixture fixture;
    private EntityManager reused;
    private EntityManager churn;
    private EntityManager migrating;
    private CleanEntityHandle[] reusedHandles;
    private CleanEntityHandle[] churnHandles;
    private CleanEntityHandle[] migratingHandles;
    private final EntityLoadFixture.Marker marker = new EntityLoadFixture.Marker();

    @Setup
    public void setup() throws Exception {
        fixture = new EntityLoadFixture(schema);
        reused = fixture.manager();
        reusedHandles = new CleanEntityHandle[rows];
        churn = fixture.manager();
        churn.reserveCapacity(List.of(fixture.type), rows);
        churnHandles = fixture.populate(churn, rows);
        churn.flush();
        migrating = fixture.manager();
        migrating.reserveCapacity(List.of(fixture.type), rows);
        migrating.reserveCapacity(List.of(fixture.type, EntityLoadFixture.Marker.class), rows);
        migratingHandles = fixture.populate(migrating, rows);
        migrating.flush();
        coldReserved();
        reusedCycle();
        liveChurn();
        migration();
        verify();
    }

    @Benchmark
    public int coldReserved() {
        EntityManager manager = fixture.manager();
        manager.reserveCapacity(List.of(fixture.type), rows);
        CleanEntityHandle[] handles = fixture.populate(manager, rows);
        manager.flush();
        destroy(manager, handles, handles.length);
        return handles[rows - 1].generation;
    }

    @Benchmark
    public int reusedCycle() {
        for (int i = 0; i < rows; i++) {
            reusedHandles[i] = reused.createEntity(fixture.payload);
        }
        reused.flush();
        destroy(reused, reusedHandles, rows);
        return reusedHandles[rows - 1].generation;
    }

    @Benchmark
    public int liveChurn() {
        int replaced = rows / 2;
        destroy(churn, churnHandles, replaced);
        for (int i = 0; i < replaced; i++) {
            churnHandles[i] = churn.createEntity(fixture.payload);
        }
        churn.flush();
        return churnHandles[replaced - 1].generation;
    }

    @Benchmark
    public int migration() {
        for (CleanEntityHandle handle : migratingHandles) {
            if (!handle.tryAddComponent(marker)) {
                throw new IllegalStateException("ADD rejected");
            }
        }
        migrating.flush();
        for (CleanEntityHandle handle : migratingHandles) {
            if (!handle.tryRemoveComponent(EntityLoadFixture.Marker.class)) {
                throw new IllegalStateException("REMOVE rejected");
            }
        }
        migrating.flush();
        return migratingHandles[rows - 1].index;
    }

    private static void destroy(EntityManager manager, CleanEntityHandle[] handles, int count) {
        for (int i = 0; i < count; i++) {
            if (!handles[i].tryDestroy()) {
                throw new IllegalStateException("Destroy rejected");
            }
        }
        manager.flush();
    }

    @TearDown
    public void verify() {
        for (CleanEntityHandle handle : reusedHandles) {
            if (handle.valid()) {
                throw new IllegalStateException("Destroyed handle is still valid");
            }
        }
        verify(churn, churnHandles);
        verify(migrating, migratingHandles);
        for (CleanEntityHandle handle : migratingHandles) {
            if (handle.contains(EntityLoadFixture.Marker.class)) {
                throw new IllegalStateException("Migration retained the marker");
            }
        }
    }

    private void verify(EntityManager manager, CleanEntityHandle[] handles) {
        ArchetypeDataPool pool = manager.startQuery(manager.newQuery().with(fixture.type)
            .without(EntityLoadFixture.Marker.class)).getFirst();
        var values = pool.getArray(fixture.type, fixture.idField);
        int count = 0;
        for (int row = 0; row < pool.getRows().limit; row++) {
            if (pool.getRows().isOccupied(row)) {
                count++;
            }
        }
        if (count != handles.length) {
            throw new IllegalStateException("Incorrect live row count");
        }
        for (CleanEntityHandle handle : handles) {
            if (!handle.isAlive() || values.getInt(pool.getEntityRow(handle.index)) != 713) {
                throw new IllegalStateException("Entity data or identity changed");
            }
        }
    }
}
