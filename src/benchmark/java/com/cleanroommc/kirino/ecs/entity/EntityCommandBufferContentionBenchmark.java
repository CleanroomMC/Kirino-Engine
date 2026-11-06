package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.entity.EcsBenchFixture.Tiny;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Command enqueue throughput on a shared EntityManager. No flush is measured.
 * Compare -t 1, -t 4 and -t 16 for monitor contention.
 * A fresh manager each iteration limits command-buffer growth.
 *
 * Run with: .\gradlew.bat benchmark --args='EntityCommandBufferContentionBenchmark -t 4 -prof gc'
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 1, jvmArgsAppend = {"-Xms6g", "-Xmx6g"})
@Threads(4)
public class EntityCommandBufferContentionBenchmark {
    private static final int KNOWN_ENTITIES = 50_000;

    @State(Scope.Benchmark)
    public static class Shared {
        EcsBenchFixture fixture;
        volatile EntityManager manager;

        @Setup(Level.Trial)
        public void trial() {
            fixture = new EcsBenchFixture();
        }

        @Setup(Level.Iteration)
        public void iteration() {
            EntityManager fresh = fixture.newManager();
            Tiny shared = new Tiny();
            for (int i = 0; i < KNOWN_ENTITIES; i++) {
                fresh.createEntity(shared); // Reserve valid IDs without populating storage.
            }
            manager = fresh;
        }
    }

    /**
     * Reuse one component per worker to keep its allocation out of the measurement.
     */
    @State(Scope.Thread)
    public static class PerThread {
        final Tiny tiny = new Tiny();
    }

    @Benchmark
    public CleanEntityHandle enqueueCreate(Shared shared, PerThread local) {
        return shared.manager.createEntity(local.tiny);
    }

    /**
     * Calls destroyEntity directly so repeated IDs still queue commands.
     * This measures enqueue cost without pool removal.
     */
    @Benchmark
    public void enqueueDestroy(Shared shared) {
        shared.manager.destroyEntity(ThreadLocalRandom.current().nextInt(KNOWN_ENTITIES));
    }
}
