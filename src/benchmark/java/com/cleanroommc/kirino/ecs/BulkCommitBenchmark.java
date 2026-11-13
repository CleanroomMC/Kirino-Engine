package com.cleanroommc.kirino.ecs;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
public class BulkCommitBenchmark {
    @Param({"1000", "25000", "100000"}) public int rows;
    @Param({"small", "meshlet"}) public String schema;
    @Param({"create", "destroyForward", "destroyReverse", "destroyShuffled"}) public String operation;
    @Param({"growth", "reserved"}) public String capacityMode;
    private EntityLoadFixture fixture;
    private EntityManager manager;
    private CleanEntityHandle[] handles;
    private int[] order;

    @Setup(Level.Trial)
    public void prepareSchema() throws Exception {
        fixture = new EntityLoadFixture(schema);
        System.out.println("Fixture schema=" + schema + ", columns=" + fixture.registry.getLayout(fixture.type).unitCount());
        for (int iteration = 0; iteration < 64; iteration++) {
            EntityManager warm = fixture.manager();
            var handles = fixture.populate(warm, 1024);
            warm.flush();
            for (var handle : handles) {
                if (!handle.tryDestroy()) {
                    throw new IllegalStateException("Warmup destroy rejected");
                }
            }
            warm.flush();
        }
        order = EntityLoadFixture.order(rows, operation.equals("destroyReverse") ? "reverse"
            : operation.equals("destroyShuffled") ? "shuffled" : "forward");
    }

    @Setup(Level.Invocation)
    public void prepareCommands() {
        manager = fixture.manager();
        if (capacityMode.equals("reserved")) {
            manager.reserveCapacity(java.util.List.of(fixture.type), rows);
        }
        handles = fixture.populate(manager, rows);
        if (!operation.equals("create")) {
            manager.flush();
            for (int id : order) {
                if (!handles[id].tryDestroy()) {
                    throw new IllegalStateException("Destroy rejected");
                }
            }
        }
    }

    @Benchmark
    public int commit() {
        manager.flush();
        return rows;
    }

    @TearDown(Level.Invocation)
    public void verify() {
        if (operation.equals("create")) {
            fixture.verify(manager, handles);
        } else {
            for (var handle : handles) {
                if (handle.valid()) {
                    throw new IllegalStateException("Destroyed handle is valid");
                }
            }
            if (manager.startQuery(manager.newQuery().with(fixture.type)).getFirst().getRows().limit != 0) {
                throw new IllegalStateException("Destroyed rows remain live");
            }
        }
        handles = null;
        manager = null;
    }
}
