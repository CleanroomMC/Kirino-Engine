package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class ExecutionBoundaryBenchmark {
    @Param({"0", "128"})
    public int entityCount;

    private static final Executor INLINE = Runnable::run;
    private EntityManager entities;
    private JobExecutor direct;
    private JobScheduler scheduler;

    @Setup
    public void setup() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            File home = Files.createTempDirectory("kirino-boundary-jmh").toFile();
            homeField.set(null, home);
            Launch.minecraftHome = home;
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunch;
        }
        ComponentRegistry components = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        components.lock();
        entities = new EntityManager(components);
        for (int i = 0; i < entityCount; i++) {
            entities.createEntity();
        }
        entities.flush();
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(NoopJob.class);
        direct = new JobExecutor(jobs);
        scheduler = RuntimeBootstrap.newJobScheduler(jobs);
        var expected = directAccess();
        var actual = schedulerBridge();
        expected.future().join();
        actual.future().join();
        if (expected.totalThreadCount() != (entityCount == 0 ? 0 : 1)
            || actual.totalThreadCount() != expected.totalThreadCount() || actual.async()) {
            throw new IllegalStateException("Invalid execution boundary.");
        }
        entities.flush();
    }

    @Benchmark
    public JobScheduler.ExecutionHandle directAccess() {
        return direct.executeParallelJob(entities, NoopJob.class, null, INLINE);
    }

    @Benchmark
    public JobScheduler.ExecutionHandle schedulerBridge() {
        return scheduler.executeParallelJob(entities, NoopJob.class, null, INLINE);
    }

    @TearDown
    public void verifyReleased() {
        entities.flush();
    }

    public static class NoopJob implements ParallelJob {
        @Override
        public void execute(EntityCommands commands, int index, int entityID, int ordinal) {}
        @Override
        public void query(EntityQuery query) {}
        @Override
        public int estimateWorkload(int index) { return 1; }
    }
}
