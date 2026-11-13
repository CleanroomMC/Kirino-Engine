package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class EntityIdIterationBenchmark {
    @Param({"4096", "65536"})
    public int rowCount;

    @Param({"0", "25"})
    public int removedPercent;

    private ArchetypeDataPool pool;
    private Int2IntOpenHashMap rows;
    private PoolRows range;
    private EntityManager.ExecutionScope execution;
    private EntityCommands commands;
    private ParallelJob job;
    private int[] output;

    @Setup
    public void setup() {
        ComponentRegistry registry = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        pool = new HeapPool(registry, List.of(), rowCount);
        rows = new Int2IntOpenHashMap();
        output = new int[rowCount];
        EntityManager entityManager = new EntityManager(registry);
        execution = entityManager.beginExecution(entityManager.newQuery());
        commands = execution.commands();
        job = new RecordingJob(output);
        for (int row = 0; row < rowCount; row++) {
            int id = entityId(row);
            pool.addEntity(id, new CleanComponent[0]);
            rows.put(row, id);
        }
        if (removedPercent == 25) {
            for (int row = 0; row < rowCount; row += 4) {
                pool.removeEntity(entityId(row));
                rows.remove(row);
            }
        } else if (removedPercent != 0) {
            throw new IllegalArgumentException("Unsupported removal percentage: " + removedPercent);
        }
        range = pool.getRows();
        verifyLookups();
        int[] expected = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            if (rows.containsKey(row)) {
                expected[row] = Integer.rotateLeft(entityId(row), 13) ^ row;
            }
        }
        verifyOutput(expected, boxedIds());
        Arrays.fill(output, 0);
        verifyOutput(expected, optionalIntIds());
        Arrays.fill(output, 0);
        verifyOutput(expected, checkedIntIds());
        Arrays.fill(output, 0);
        verifyOutput(expected, checkedIntSingleLookupIds());
        Arrays.fill(output, 0);
    }

    @Benchmark
    public int[] boxedIds() {
        for (int row = 0; row < range.limit; row++) {
            if (range.nextFree[row] != 0) {
                continue;
            }
            int id = boxedEntityId(row).orElseThrow(IllegalStateException::new);
            job.execute(commands, row, id, 0);
        }
        return output;
    }

    @Benchmark
    public int[] optionalIntIds() {
        for (int row = 0; row < range.limit; row++) {
            if (range.nextFree[row] != 0) {
                continue;
            }
            int id = optionalIntEntityId(row).orElseThrow(IllegalStateException::new);
            job.execute(commands, row, id, 0);
        }
        return output;
    }

    @Benchmark
    public int[] checkedIntIds() {
        for (int row = 0; row < range.limit; row++) {
            if (range.nextFree[row] != 0) {
                continue;
            }
            int id = pool.getEntityID(row);
            job.execute(commands, row, id, 0);
        }
        return output;
    }

    @Benchmark
    public int[] checkedIntSingleLookupIds() {
        for (int row = 0; row < range.limit; row++) {
            if (range.nextFree[row] != 0) {
                continue;
            }
            int id = checkedEntityIdSingleLookup(row);
            job.execute(commands, row, id, 0);
        }
        return output;
    }

    private Optional<Integer> boxedEntityId(int row) {
        return rows.containsKey(row) ? Optional.of(rows.get(row)) : Optional.empty();
    }

    @TearDown
    public void tearDown() {
        execution.close();
    }

    private OptionalInt optionalIntEntityId(int row) {
        return rows.containsKey(row) ? OptionalInt.of(rows.get(row)) : OptionalInt.empty();
    }

    private int checkedEntityIdSingleLookup(int row) {
        int id = rows.get(row);
        if (id == rows.defaultReturnValue() && !rows.containsKey(row)) {
            throw new IllegalStateException("No entity at index=" + row);
        }
        return id;
    }

    private void verifyLookups() {
        for (int row = -1; row <= rowCount; row++) {
            boolean present = row >= 0 && row < rowCount && !(removedPercent == 25 && row % 4 == 0);
            if (boxedEntityId(row).isPresent() != present || optionalIntEntityId(row).isPresent() != present) {
                throw new IllegalStateException("Wrong presence at row=" + row);
            }
            if (present) {
                int expected = entityId(row);
                if (boxedEntityId(row).orElseThrow() != expected || optionalIntEntityId(row).orElseThrow() != expected
                        || pool.getEntityID(row) != expected || checkedEntityIdSingleLookup(row) != expected) {
                    throw new IllegalStateException("Wrong entity at row=" + row);
                }
            } else {
                boolean firstFailed = false;
                boolean secondFailed = false;
                try {
                    pool.getEntityID(row);
                } catch (IllegalStateException expected) {
                    firstFailed = true;
                }
                try {
                    checkedEntityIdSingleLookup(row);
                } catch (IllegalStateException expected) {
                    secondFailed = true;
                }
                if (!firstFailed || !secondFailed) {
                    throw new IllegalStateException("Missing row was accepted: " + row);
                }
            }
        }
    }

    private static void verifyOutput(int[] expected, int[] actual) {
        if (!Arrays.equals(expected, actual)) {
            throw new IllegalStateException("Different job output");
        }
    }

    private static int entityId(int row) {
        return switch (row) {
            case 1 -> 0;
            case 2 -> Integer.MIN_VALUE;
            case 3 -> Integer.MAX_VALUE;
            default -> 1000 + row;
        };
    }

    private static final class RecordingJob implements ParallelJob {
        private final int[] output;

        private RecordingJob(int[] output) {
            this.output = output;
        }

        @Override
        public void execute(EntityCommands entityManager, int index, int entityID, int threadOrdinal) {
            output[index] = Integer.rotateLeft(entityID, 13) ^ index;
        }

        @Override
        public void query(EntityQuery entityQuery) {
        }

        @Override
        public int estimateWorkload(int index) {
            return 1;
        }
    }
}
