package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.openjdk.jmh.annotations.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class RowLifecycleBenchmark {
    @Param({"4096", "65536"})
    public int rowCount;
    @Param({"forward", "reverse", "shuffled"})
    public String order;
    private HeapPool candidate;
    private LegacyRowsHeapPool legacy;
    private int[] removalOrder;
    private final CleanComponent[] empty = new CleanComponent[0];

    @Setup
    public void setup() {
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        candidate = new HeapPool(registry, List.of(), rowCount);
        legacy = new LegacyRowsHeapPool(registry, List.of(), rowCount);
        removalOrder = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            candidate.addEntity(row, empty);
            legacy.addEntity(row, empty);
            removalOrder[row] = order.equals("reverse") ? rowCount - row - 1 : row;
        }
        if (order.equals("shuffled")) {
            Random random = new Random(0x4b4952494e4fL);
            for (int i = rowCount - 1; i > 0; i--) {
                int other = random.nextInt(i + 1);
                int value = removalOrder[i];
                removalOrder[i] = removalOrder[other];
                removalOrder[other] = value;
            }
        }
        candidateDrainAndRefill(); legacyDrainAndRefill();
        verify();
    }

    @Benchmark
    public int candidateDrainAndRefill() {
        for (int id : removalOrder) candidate.removeEntity(id);
        for (int id = 0; id < rowCount; id++) candidate.addEntity(id, empty);
        return candidate.getEntityID(rowCount - 1);
    }

    @Benchmark
    public int legacyDrainAndRefill() {
        for (int id : removalOrder) legacy.removeEntity(id);
        for (int id = 0; id < rowCount; id++) legacy.addEntity(id, empty);
        return legacy.getEntityID(rowCount - 1);
    }

    @TearDown
    public void verify() {
        for (int row = 0; row < rowCount; row++) {
            if (candidate.getEntityID(row) != row || legacy.getEntityID(row) != row) {
                throw new IllegalStateException("Entity-to-row mapping differs.");
            }
        }
    }
}
