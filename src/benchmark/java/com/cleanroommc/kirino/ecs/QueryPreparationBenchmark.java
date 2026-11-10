package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class QueryPreparationBenchmark {
    @Param({"1", "16", "64", "256"})
    public int archetypeCount;

    @Param({"single", "small", "selective"})
    public String conditions;

    @Param({"1", "8"})
    public int distinctQueries;

    @Param({"0"})
    public int invalidationEvery;

    private EntityManager entities;
    private JobExecutor executor;
    private CleanComponent[] markers;
    private int nextScan;
    private int nextLaunch;
    private int nextCached;
    private int[] expected;
    private QueryCache cache;
    private int queriesSinceInvalidation;

    @Setup
    public void setup() throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var field = FMLInjectionData.class.getDeclaredField("minecraftHome");
        field.setAccessible(true);
        Object previous = field.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            File home = Files.createTempDirectory("kirino-query-jmh").toFile();
            field.set(null, home);
            Launch.minecraftHome = home;
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            field.set(null, previous);
            Launch.minecraftHome = previousLaunch;
        }
        markers = new CleanComponent[]{new M0(), new M1(), new M2(), new M3(), new M4(),
            new M5(), new M6(), new M7(), new M8()};
        ComponentRegistry components = new ComponentRegistry(new FieldRegistry(new StructRegistry()));
        for (int i = 0; i < markers.length; i++) {
            components.registerComponent("marker" + i, markers[i].getClass(), new MemberLayout());
        }
        components.lock();
        entities = new EntityManager(components);
        for (int mask = 0; mask < archetypeCount; mask++) {
            List<CleanComponent> values = new ArrayList<>();
            values.add(markers[0]);
            for (int bit = 0; bit < 8; bit++) {
                if ((mask & (1 << bit)) != 0) {
                    values.add(markers[bit + 1]);
                }
            }
            entities.createEntity(values.toArray(CleanComponent[]::new));
        }
        entities.flush();
        JobRegistry jobs = new JobRegistry(components);
        jobs.registerParallelJob(QueryJob.class);
        executor = new JobExecutor(jobs);
        expected = new int[distinctQueries];
        QueryJob.small = conditions.equals("small");
        QueryJob.selective = conditions.equals("selective");
        for (int variant = 0; variant < distinctQueries; variant++) {
            QueryJob.variant = variant;
            expected[variant] = entities.startQuery(query(variant)).size();
            if (entities.selectPools(query(variant)).length != expected[variant]) {
                throw new IllegalStateException("Invalid cached query fixture.");
            }
            var handle = executor.executeParallelJob(entities, QueryJob.class, null, Runnable::run);
            handle.future().join();
            if (handle.totalThreadCount() != expected[variant] || handle.async()) {
                throw new IllegalStateException("Invalid query fixture.");
            }
        }
        cache = MethodHolder.cache(entities);
    }

    private EntityQuery query(int variant) {
        EntityQuery query = entities.newQuery();
        QueryJob.variant = variant;
        new QueryJob().query(query);
        return query;
    }

    @Benchmark
    public ArchetypeDataPool[] scan() {
        int variant = nextScan++ & (distinctQueries - 1);
        return entities.startQuery(query(variant)).toArray(ArchetypeDataPool[]::new);
    }

    @Benchmark
    public int fullLaunch() {
        invalidateIfNeeded();
        QueryJob.variant = nextLaunch++ & (distinctQueries - 1);
        var result = executor.executeParallelJob(entities, QueryJob.class, null, Runnable::run);
        result.future().join();
        return result.totalThreadCount();
    }

    @Benchmark
    public ArchetypeDataPool[] cachedSelection() {
        invalidateIfNeeded();
        int variant = nextCached++ & (distinctQueries - 1);
        return entities.selectPools(query(variant));
    }

    private void invalidateIfNeeded() {
        if (invalidationEvery != 0 && ++queriesSinceInvalidation == invalidationEvery) {
            if (cache != null) {
                cache.clear();
            }
            queriesSinceInvalidation = 0;
        }
    }

    private static final class MethodHolder {
        private static final MethodHandle GET_CACHE = Preconditions.checkNotNull(
            ReflectionUtils.getFieldGetter(EntityManager.class, "queryCache", QueryCache.class));

        static QueryCache cache(EntityManager manager) {
            try {
                return (QueryCache) GET_CACHE.invokeExact(manager);
            } catch (Throwable failure) {
                throw new AssertionError(failure);
            }
        }
    }

    @TearDown
    public void verifyReleased() {
        entities.flush();
        for (int variant = 0; variant < distinctQueries; variant++) {
            if (entities.startQuery(query(variant)).size() != expected[variant]) {
                throw new IllegalStateException("Query matches changed.");
            }
            if (entities.selectPools(query(variant)).length != expected[variant]) {
                throw new IllegalStateException("Cached query matches changed.");
            }
        }
    }

    public static class QueryJob implements ParallelJob {
        static boolean small;
        static boolean selective;
        static int variant;

        public void query(EntityQuery query) {
            query.with(M0.class);
            if (variant != 0 || small || selective) {
                for (int bit = 0; bit < (selective ? 8 : 3); bit++) {
                    Class<? extends CleanComponent> type = switch (bit) {
                        case 0 -> M1.class;
                        case 1 -> M2.class;
                        case 2 -> M3.class;
                        case 3 -> M4.class;
                        case 4 -> M5.class;
                        case 5 -> M6.class;
                        case 6 -> M7.class;
                        default -> M8.class;
                    };
                    if ((variant & (1 << bit)) != 0) {
                        query.with(type);
                    } else {
                        query.without(type);
                    }
                }
            }
        }

        public int estimateWorkload(int row) { return 1; }
        public void execute(EntityCommands commands, int row, int entity, int partition) {}
    }

    public static class M0 implements CleanComponent {}
    public static class M1 implements CleanComponent {}
    public static class M2 implements CleanComponent {}
    public static class M3 implements CleanComponent {}
    public static class M4 implements CleanComponent {}
    public static class M5 implements CleanComponent {}
    public static class M6 implements CleanComponent {}
    public static class M7 implements CleanComponent {}
    public static class M8 implements CleanComponent {}
}

