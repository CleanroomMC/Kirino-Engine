package com.cleanroommc.kirino.ecs;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class BulkEntityBenchmark {
    @Param({"1000", "25000", "100000"}) public int rows;
    @Param({"small", "meshlet"}) public String schema;
    @Param({"growth", "reserved"}) public String capacityMode;
    private EntityLoadFixture fixture;

    @Setup
    public void setup() throws Exception {
        fixture = new EntityLoadFixture(schema);
        EntityManager manager = fixture.manager();
        if (capacityMode.equals("reserved")) {
            manager.reserveCapacity(java.util.List.of(fixture.type), rows);
        }
        var handles = fixture.populate(manager, rows);
        manager.flush();
        fixture.verify(manager, handles);
        for (var handle : handles) {
            if (!handle.tryDestroy()) {
                throw new IllegalStateException("Destroy rejected");
            }
        }
        manager.flush();
        if (manager.startQuery(manager.newQuery().with(fixture.type)).getFirst().getRows().limit != 0) {
            throw new IllegalStateException("Destruction left live rows");
        }
    }

    @Benchmark
    public int createCommitDestroyCommit() {
        EntityManager manager = fixture.manager();
        if (capacityMode.equals("reserved")) {
            manager.reserveCapacity(java.util.List.of(fixture.type), rows);
        }
        CleanEntityHandle[] handles = fixture.populate(manager, rows);
        manager.flush();
        for (var handle : handles) {
            if (!handle.tryDestroy()) {
                throw new IllegalStateException("Destroy rejected");
            }
        }
        manager.flush();
        return handles[rows - 1].generation;
    }
}
