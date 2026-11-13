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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class JobBindingBenchmark {
    @Param({"1", "16"})
    public int dataFields;

    @Param({"1", "8"})
    public int partitions;

    private static final int ENTITY_COUNT = 128;
    private static final Executor INLINE = Runnable::run;
    private EntityManager entities;
    private LegacyJobExecutor legacy;
    private JobExecutor prepared;
    private Class<? extends ParallelJob> jobType;
    private Map<String, Object> externalData;
    private CleanEntityHandle first;

    @Setup
    public void setup() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            File home = Files.createTempDirectory("kirino-job-binding-jmh").toFile();
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
        String[] names = new String[16];
        String[] types = new String[16];
        Arrays.fill(types, "int");
        for (int i = 0; i < names.length; i++) {
            names[i] = "f" + i;
        }
        components.registerComponent("values", Values.class, new MemberLayout(names), types);
        components.lock();
        entities = new EntityManager(components);
        first = entities.createEntity(new Values());
        for (int i = 1; i < ENTITY_COUNT; i++) {
            entities.createEntity(new Values());
        }
        entities.flush();
        jobType = dataFields == 1 ? SingleFieldJob.class : WideJob.class;
        JobRegistry registry = new JobRegistry(components);
        registry.registerParallelJob(jobType);
        LegacyJobRegistry legacyRegistry = new LegacyJobRegistry(components);
        legacyRegistry.registerParallelJob(jobType);
        prepared = new JobExecutor(registry);
        legacy = new LegacyJobExecutor(legacyRegistry);
        int workload = partitions == 1 ? 1 : Math.toIntExact(
            Math.ceilDiv((long) KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread * partitions, ENTITY_COUNT));
        externalData = Map.of("salt", 7, "workload", workload);

        ExecutionHandle expected = legacyMaps();
        verify(expected);
        ExecutionHandle actual = preparedBindings();
        verify(actual);
        if (actual.totalThreadCount() != expected.totalThreadCount() || actual.async() != expected.async()) {
            throw new IllegalStateException("Execution plans differ.");
        }
    }

    @Benchmark
    public ExecutionHandle legacyMaps() {
        return legacy.executeParallelJob(entities, jobType, externalData, INLINE);
    }

    @Benchmark
    public ExecutionHandle preparedBindings() {
        return prepared.executeParallelJob(entities, jobType, externalData, INLINE);
    }

    private void verify(ExecutionHandle handle) {
        handle.future().join();
        if (first.readComponent(Values.class).orElseThrow().f0 != (dataFields == 1 ? 7 : 127)) {
            throw new IllegalStateException("Invalid bound job result.");
        }
        if (handle.totalThreadCount() < 1 || (partitions == 1 && handle.async())) {
            throw new IllegalStateException("Invalid execution partitioning.");
        }
        entities.flush();
    }

    @TearDown
    public void verifyReleased() {
        entities.flush();
        if (first.readComponent(Values.class).orElseThrow().f0 != (dataFields == 1 ? 7 : 127)) {
            throw new IllegalStateException("Bound columns changed.");
        }
    }

    public static class Values implements CleanComponent {
        public int f0 = 0;
        public int f1 = 1;
        public int f2 = 2;
        public int f3 = 3;
        public int f4 = 4;
        public int f5 = 5;
        public int f6 = 6;
        public int f7 = 7;
        public int f8 = 8;
        public int f9 = 9;
        public int f10 = 10;
        public int f11 = 11;
        public int f12 = 12;
        public int f13 = 13;
        public int f14 = 14;
        public int f15 = 15;
    }

    public static class SingleFieldJob implements ParallelJob {
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f0")
        private PrimitiveArray f0;
        @JobExternalDataQuery
        private int salt;
        @JobExternalDataQuery
        private int workload;

        public void execute(EntityCommands commands, int row, int entity, int partition) {
            f0.setInt(row, salt);
        }

        public void query(EntityQuery query) { query.with(Values.class); }
        public int estimateWorkload(int row) { return workload; }
    }

    public static class WideJob implements ParallelJob {
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f0")
        private PrimitiveArray f0;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f1")
        private PrimitiveArray f1;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f2")
        private PrimitiveArray f2;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f3")
        private PrimitiveArray f3;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f4")
        private PrimitiveArray f4;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f5")
        private PrimitiveArray f5;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f6")
        private PrimitiveArray f6;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f7")
        private PrimitiveArray f7;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f8")
        private PrimitiveArray f8;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f9")
        private PrimitiveArray f9;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f10")
        private PrimitiveArray f10;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f11")
        private PrimitiveArray f11;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f12")
        private PrimitiveArray f12;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f13")
        private PrimitiveArray f13;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f14")
        private PrimitiveArray f14;
        @JobDataQuery(componentClass = Values.class, fieldAccessChain = "f15")
        private PrimitiveArray f15;
        @JobExternalDataQuery
        private int salt;
        @JobExternalDataQuery
        private int workload;

        public void execute(EntityCommands commands, int row, int entity, int partition) {
            f0.setInt(row, f1.getInt(row) + f2.getInt(row) + f3.getInt(row) + f4.getInt(row) + f5.getInt(row) + f6.getInt(row) + f7.getInt(row) + f8.getInt(row) + f9.getInt(row) + f10.getInt(row) + f11.getInt(row) + f12.getInt(row) + f13.getInt(row) + f14.getInt(row) + f15.getInt(row) + salt);
        }

        public void query(EntityQuery query) { query.with(Values.class); }
        public int estimateWorkload(int row) { return workload; }
    }
}

