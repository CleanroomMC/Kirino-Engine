package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ExternalDataApiTest {
    private static final String AMOUNT = "example.amount";
    private static final String PROBE = "example.probe";
    private static final String TEXT = "example.text";
    @TempDir Path home;

    @AfterEach
    void clearQueryHook() { NamedJob.beforeQuery = () -> {}; }

    @Test
    void explicitKeys_bindRenamedAndInheritedPrivateFieldsWithoutUsingJavaNames() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, NamedJob.class)) {
            var entity = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            Probe probe = new Probe();
            runtime.jobScheduler.executeParallelJob(runtime.entityManager, NamedJob.class,
                Map.of(AMOUNT, 7, PROBE, probe, "renamedAmount", "ignored", "renamedProbe", "ignored"), Runnable::run).future().join();
            assertEquals(7, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
            assertEquals(1, probe.visits.get());
            assertEquals(7, probe.inheritedAmount);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing", "wrongType", "primitiveNull"})
    void invalidInputs_failBeforeQuerySubmissionOrStorageAccessAndLeaveRuntimeUsable(String problem) throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, NamedJob.class)) {
            var entity = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            AtomicInteger queries = new AtomicInteger();
            AtomicInteger submissions = new AtomicInteger();
            NamedJob.beforeQuery = queries::incrementAndGet;
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(PROBE, new Probe());
            inputs.put("renamedAmount", 99);
            switch (problem) {
                case "wrongType" -> inputs.put(AMOUNT, 7L);
                case "primitiveNull" -> inputs.put(AMOUNT, null);
                case "missing" -> {}
                default -> throw new AssertionError(problem);
            }
            IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> runtime.jobScheduler.executeParallelJob(runtime.entityManager, NamedJob.class, inputs,
                    task -> submissions.incrementAndGet()));
            assertTrue(failure.getMessage().contains(AMOUNT));
            assertEquals(0, queries.get());
            assertEquals(0, submissions.get());
            assertEquals(0, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
            assertEquals(CleanECSRuntime.State.RUNNING, runtime.state());
            runtime.entityManager.flush();
        }
    }

    @Test
    void nullableReference_isDifferentFromAMissingBinding() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, NullableJob.class)) {
            runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            Probe probe = new Probe();
            Map<String, Object> inputs = new HashMap<>();
            inputs.put(PROBE, probe);
            inputs.put(TEXT, null);
            runtime.jobScheduler.executeParallelJob(runtime.entityManager, NullableJob.class, inputs, Runnable::run).future().join();
            assertNull(probe.text);
            assertEquals(1, probe.visits.get());
            inputs.remove(TEXT);
            assertThrows(IllegalArgumentException.class,
                () -> runtime.jobScheduler.executeParallelJob(runtime.entityManager, NullableJob.class, inputs, Runnable::run));
            inputs.put(TEXT, 7);
            assertThrows(IllegalArgumentException.class,
                () -> runtime.jobScheduler.executeParallelJob(runtime.entityManager, NullableJob.class, inputs, Runnable::run));
        }
    }

    @Test
    void captureBeforeQuery_andQueuedWorkersDoNotObserveReplacementMapEntries() throws Exception {
        assumeTrue(Runtime.getRuntime().availableProcessors() > 1);
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, NamedJob.class)) {
            var first = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            var second = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            Probe captured = new Probe();
            captured.workload = KirinoConfig.NEEDS_RESTART.targetWorkloadPerThread;
            Probe replacement = new Probe();
            Map<String, Object> inputs = new HashMap<>(Map.of(AMOUNT, 7, PROBE, captured));
            NamedJob.beforeQuery = () -> {
                inputs.put(AMOUNT, 99);
                inputs.put(PROBE, replacement);
            };
            ArrayDeque<Runnable> pending = new ArrayDeque<>();
            var execution = runtime.jobScheduler.executeParallelJob(runtime.entityManager, NamedJob.class, inputs, pending::addLast);
            try {
                assertTrue(execution.async());
                assertThrows(IllegalStateException.class, runtime.entityManager::flush);
                inputs.clear();
            } finally {
                while (!pending.isEmpty()) {
                    pending.removeFirst().run();
                }
            }
            execution.future().join();
            assertEquals(2, captured.visits.get());
            assertEquals(0, replacement.visits.get());
            assertEquals(7, first.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
            assertEquals(7, second.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        }
    }

    @Test
    void explicitKeyDefinitions_surviveParentCloseAndSessionReset() throws Exception {
        CleanECSRuntime parent = RuntimeApiTest.runtime(home, NamedJob.class);
        try (CleanECSRuntime session = parent.newSession()) {
            parent.close();
            for (int iteration = 0; iteration < 2; iteration++) {
                var entity = session.entityManager.createEntity(new RuntimeApiTest.Value());
                session.entityManager.flush();
                session.jobScheduler.executeParallelJob(session.entityManager, NamedJob.class,
                    Map.of(AMOUNT, 7, PROBE, new Probe()), Runnable::run).future().join();
                assertEquals(7, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
                session.reset();
            }
        } finally {
            parent.close();
        }
    }

    @Test
    void primitiveBindings_acceptExactlyTheirBoxedTypesIncludingChar() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, PrimitiveJob.class)) {
            runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            Map<String, Object> inputs = primitiveInputs();
            Probe probe = new Probe();
            inputs.put(PROBE, probe);
            runtime.jobScheduler.executeParallelJob(runtime.entityManager, PrimitiveJob.class, inputs, Runnable::run).future().join();
            assertEquals(1, probe.visits.get());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"b", "s", "i", "l", "f", "d", "flag", "c"})
    void primitiveBindings_rejectNullBeforeQueryEvenWhenThereAreNoEntities(String key) throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home, PrimitiveJob.class)) {
            Map<String, Object> inputs = primitiveInputs();
            inputs.put(PROBE, new Probe());
            inputs.put(key, null);
            assertThrows(IllegalArgumentException.class,
                () -> runtime.jobScheduler.executeParallelJob(runtime.entityManager, PrimitiveJob.class, inputs,
                    task -> fail("Invalid inputs reached the executor")));
        }
    }

    private static Map<String, Object> primitiveInputs() {
        return new HashMap<>(Map.of("b", Byte.MIN_VALUE, "s", Short.MAX_VALUE, "i", Integer.MAX_VALUE,
            "l", Long.MIN_VALUE, "f", -0.0f, "d", Double.POSITIVE_INFINITY, "flag", true, "c", '\uffff'));
    }

    public static class Probe {
        final AtomicInteger visits = new AtomicInteger();
        int inheritedAmount;
        int workload = 1;
        String text = "not assigned";
    }

    public static class BaseJob implements ParallelJob {
        @JobExternalDataQuery(AMOUNT) private int renamedAmount;
        @JobExternalDataQuery(PROBE) private Probe renamedProbe;
        @JobDataQuery(componentClass = RuntimeApiTest.Value.class, fieldAccessChain = "value") private PrimitiveArray values;
        public void query(EntityQuery query) { query.with(RuntimeApiTest.Value.class); }
        public int estimateWorkload(int index) { return renamedProbe.workload; }
        public void execute(EntityCommands commands, int row, int entity, int ordinal) {
            values.setInt(row, values.getInt(row) + renamedAmount);
            renamedProbe.inheritedAmount = renamedAmount;
            renamedProbe.visits.incrementAndGet();
        }
    }

    public static class NamedJob extends BaseJob {
        static Runnable beforeQuery = () -> {};
        @JobExternalDataQuery(PROBE) private Probe alias;
        public void query(EntityQuery query) { beforeQuery.run(); super.query(query); }
        public void execute(EntityCommands commands, int row, int entity, int ordinal) {
            assertSame(((BaseJob) this).renamedProbe, alias);
            super.execute(commands, row, entity, ordinal);
        }
    }

    public static class NullableJob implements ParallelJob {
        @JobExternalDataQuery(TEXT) private String renamedText;
        @JobExternalDataQuery(PROBE) private Probe probe;
        public void query(EntityQuery query) { query.with(RuntimeApiTest.Value.class); }
        public int estimateWorkload(int index) { return 1; }
        public void execute(EntityCommands commands, int row, int entity, int ordinal) {
            probe.text = renamedText;
            probe.visits.incrementAndGet();
        }
    }

    public static class PrimitiveJob implements ParallelJob {
        @JobExternalDataQuery private byte b;
        @JobExternalDataQuery private short s;
        @JobExternalDataQuery private int i;
        @JobExternalDataQuery private long l;
        @JobExternalDataQuery private float f;
        @JobExternalDataQuery private double d;
        @JobExternalDataQuery private boolean flag;
        @JobExternalDataQuery private char c;
        @JobExternalDataQuery(PROBE) private Probe probe;
        public void query(EntityQuery query) { query.with(RuntimeApiTest.Value.class); }
        public int estimateWorkload(int index) { return 1; }
        public void execute(EntityCommands commands, int row, int entity, int ordinal) {
            assertEquals(Byte.MIN_VALUE, b);
            assertEquals(Short.MAX_VALUE, s);
            assertEquals(Integer.MAX_VALUE, i);
            assertEquals(Long.MIN_VALUE, l);
            assertEquals(Float.floatToRawIntBits(-0.0f), Float.floatToRawIntBits(f));
            assertEquals(Double.doubleToRawLongBits(Double.POSITIVE_INFINITY), Double.doubleToRawLongBits(d));
            assertTrue(flag);
            assertEquals('\uffff', c);
            probe.visits.incrementAndGet();
        }
    }
}
