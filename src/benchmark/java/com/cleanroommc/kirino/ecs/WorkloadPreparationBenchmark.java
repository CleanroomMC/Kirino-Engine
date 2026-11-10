package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler.ExecutionHandle;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class WorkloadPreparationBenchmark {
    @Param({"sync", "uniform", "mixed", "sparse"})
    public String scenario;

    @Param({"0", "32"})
    public int estimateRounds;

    private EntityManager entities;
    private HeapPool pool;
    private JobExecutor prepared;
    private RepeatedWorkloadJobExecutor repeated;
    private FreshWorkloadJobExecutor fresh;
    private Map<String, Object> inputs;
    private int[] expectedPartitions;

    @Setup
    public void setup() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            File home = Files.createTempDirectory("kirino-workload-jmh").toFile();
            homeField.set(null, home);
            Launch.minecraftHome = home;
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunch;
        }
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("value", Value.class, new MemberLayout("weight", "partition"), "int", "int");
        components.lock();
        int rowCount = scenario.equals("sync") ? 128 : 8192;
        boolean sparse = scenario.equals("sparse");
        int base = scenario.equals("sync") ? 1 : Math.toIntExact(Math.ceilDiv(
            (long) KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread * 8, sparse ? rowCount / 8 : rowCount));
        pool = new HeapPool(components, List.of(Value.class), rowCount);
        entities = new EntityManager(components, types -> pool);
        CleanEntityHandle[] handles = new CleanEntityHandle[rowCount];
        for (int row = 0; row < rowCount; row++) {
            Value value = new Value();
            value.weight = base + (scenario.equals("uniform") ? 0 : row % 7);
            value.partition = -1;
            handles[row] = entities.createEntity(value);
        }
        entities.flush();
        if (sparse) {
            for (int row = 0; row < rowCount - 1; row++) {
                if (row % 8 != 0) {
                    handles[row].tryDestroy();
                }
            }
            entities.flush();
        }
        JobRegistry registry = new JobRegistry(components);
        registry.registerParallelJob(Job.class);
        prepared = new JobExecutor(registry);
        repeated = new RepeatedWorkloadJobExecutor(registry);
        fresh = new FreshWorkloadJobExecutor(registry);
        inputs = Map.of("rounds", estimateRounds);
        ExecutionHandle oldResult = repeatedEstimation();
        oldResult.future().join();
        PrimitiveArray partitions = pool.getArray(Value.class, "partition");
        expectedPartitions = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            expectedPartitions[row] = partitions.getInt(row);
            partitions.setInt(row, -1);
        }
        ExecutionHandle newResult = capturedEstimation();
        newResult.future().join();
        if (newResult.totalThreadCount() != oldResult.totalThreadCount()
            || newResult.async() != oldResult.async()) {
            throw new IllegalStateException("Workload partitions differ.");
        }
        if (scenario.equals("sync") && newResult.async()) {
            throw new IllegalStateException("Expected a synchronous workload.");
        }
        verify();
        ExecutionHandle reusedResult = reusedEstimation();
        reusedResult.future().join();
        if (reusedResult.totalThreadCount() != oldResult.totalThreadCount()
            || reusedResult.async() != oldResult.async()) {
            throw new IllegalStateException("Reused workload partitions differ.");
        }
        verify();
    }

    @Benchmark
    public ExecutionHandle repeatedEstimation() {
        return repeated.executeParallelJob(entities, Job.class, inputs, Runnable::run);
    }

    @Benchmark
    public ExecutionHandle capturedEstimation() {
        return fresh.executeParallelJob(entities, Job.class, inputs, Runnable::run);
    }

    @Benchmark
    public ExecutionHandle reusedEstimation() {
        return prepared.executeParallelJob(entities, Job.class, inputs, Runnable::run);
    }

    @TearDown
    public void verify() {
        entities.flush();
        PrimitiveArray partitions = pool.getArray(Value.class, "partition");
        for (int row = 0; row < expectedPartitions.length; row++) {
            if (partitions.getInt(row) != expectedPartitions[row]) {
                throw new IllegalStateException("Incorrect workload partition at row=" + row + ".");
            }
        }
    }

    public static class Value implements CleanComponent {
        public int weight;
        public int partition;
    }

    public static class Job implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "weight")
        private PrimitiveArray weights;
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = "partition")
        private PrimitiveArray partitions;
        @JobExternalDataQuery
        private int rounds;

        public void query(EntityQuery query) { query.with(Value.class); }

        public int estimateWorkload(int row) {
            int weight = weights.getInt(row);
            int mixed = weight;
            for (int round = 0; round < rounds; round++) {
                mixed = Integer.rotateLeft(mixed ^ 0x9e3779b9, 7) * 0x85ebca6b;
            }
            return weight + (mixed & 15);
        }

        public void execute(EntityCommands commands, int row, int entity, int partition) {
            partitions.setInt(row, partition);
        }
    }
}
