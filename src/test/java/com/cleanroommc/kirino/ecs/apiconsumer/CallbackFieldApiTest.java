package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.CleanComponentSignature;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyContext;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CallbackFieldApiTest {
    @TempDir Path home;

    @Test
    void fieldReads_matchCommittedCopiesForAllPrimitivesAndNestedValues() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            AtomicInteger calls = new AtomicInteger();
            Values input = value(713);
            var entity = runtime.entityManager.createEntity(context -> {
                Values copy = context.getComponent(Values.class);
                assertEquals(copy.b, context.getByte(Values.class, "b"));
                assertEquals(copy.s, context.getShort(Values.class, "s"));
                assertEquals(copy.i, context.getInt(Values.class, "i"));
                assertEquals(copy.l, context.getLong(Values.class, "l"));
                assertEquals(Float.floatToRawIntBits(copy.f), Float.floatToRawIntBits(context.getFloat(Values.class, "f")));
                assertEquals(Double.doubleToRawLongBits(copy.d), Double.doubleToRawLongBits(context.getDouble(Values.class, "d")));
                assertEquals(copy.flag, context.getBool(Values.class, "flag"));
                assertEquals(Float.floatToRawIntBits(copy.position.x),
                    Float.floatToRawIntBits(context.getFloat(Values.class, "position", "x")));
                assertEquals(Float.floatToRawIntBits(copy.position.y),
                    Float.floatToRawIntBits(context.getFloat(Values.class, "position", "y")));
                assertEquals(Float.floatToRawIntBits(copy.matrix.m30()),
                    Float.floatToRawIntBits(context.getFloat(Values.class, "matrix", "m30")));
                copy.i = -1;
                assertEquals(713, context.getInt(Values.class, "i"));
                calls.incrementAndGet();
            }, null, input);
            runtime.entityManager.flush();
            input.i = 999;
            entity.tryDestroy();
            runtime.entityManager.flush();
            assertEquals(1, calls.get());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"destroy", "reset", "close"})
    void callbackReads_followCommittedCommandOrderAndCleanupDiscardsPendingSets(String operation) throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            List<Integer> values = new ArrayList<>();
            Thread caller = Thread.currentThread();
            var entity = runtime.entityManager.createEntity(context -> {
                assertSame(caller, Thread.currentThread());
                values.add(context.getInt(Values.class, "i"));
            }, context -> context.getComponent(Values.class).i = 11, value(7));
            runtime.entityManager.flush();
            assertEquals(11, entity.readComponent(Values.class).orElseThrow().i);
            entity.trySetComponent(value(17));
            runtime.entityManager.flush();
            entity.trySetComponent(value(99));
            switch (operation) {
                case "destroy" -> {
                    entity.tryDestroy();
                    runtime.entityManager.flush();
                }
                case "reset" -> runtime.reset();
                case "close" -> runtime.close();
                default -> throw new AssertionError(operation);
            }
            assertEquals(List.of(operation.equals("destroy") ? 99 : 17), values);
        }
    }

    @Test
    void fieldReads_useTheCurrentPoolRowAfterMigrationHolesAndIdReuse() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            List<Integer> destroyed = new ArrayList<>();
            var unrelated = runtime.entityManager.createEntity(new RuntimeApiTest.Value());
            var first = runtime.entityManager.createEntity(context -> destroyed.add(context.getInt(Values.class, "i")), null, value(10));
            var hole = runtime.entityManager.createEntity(context -> destroyed.add(context.getInt(Values.class, "i")), null, value(20));
            var third = runtime.entityManager.createEntity(context -> destroyed.add(context.getInt(Values.class, "i")), null, value(30));
            runtime.entityManager.flush();
            hole.tryDestroy();
            runtime.entityManager.flush();
            var reused = runtime.entityManager.createEntity(context -> destroyed.add(context.getInt(Values.class, "i")), null, value(40));
            runtime.entityManager.flush();
            first.tryAddComponent(new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            first.tryRemoveComponent(RuntimeApiTest.Value.class);
            runtime.entityManager.flush();
            reused.tryDestroy();
            third.tryDestroy();
            first.tryDestroy();
            runtime.entityManager.flush();
            assertEquals(List.of(20, 40, 30, 10), destroyed);
            assertTrue(unrelated.isAlive());
        }
    }

    @Test
    void resetCallback_readsJobWritesAndDiscardsLaterUncommittedPayload() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            AtomicInteger result = new AtomicInteger();
            var entity = runtime.entityManager.createEntity(context -> result.set(context.getInt(RuntimeApiTest.Value.class, "value")),
                null, new RuntimeApiTest.Value());
            runtime.entityManager.flush();
            runtime.jobScheduler.executeParallelJob(runtime.entityManager, RuntimeApiTest.IncrementJob.class,
                null, Runnable::run).future().join();
            RuntimeApiTest.Value pending = new RuntimeApiTest.Value();
            pending.value = 99;
            entity.trySetComponent(pending);
            runtime.reset();
            assertEquals(1, result.get());
        }
    }

    @Test
    void invalidFieldRequests_areRejectedWhileOtherReadsAndCleanupRemainUsable() throws Exception {
        try (CleanECSRuntime runtime = RuntimeApiTest.runtime(home)) {
            AtomicReference<EntityDestroyContext> saved = new AtomicReference<>();
            var entity = runtime.entityManager.createEntity(context -> {
                assertThrows(NullPointerException.class, () -> context.getInt(null, "i"));
                assertThrows(NullPointerException.class, () -> context.getInt(Values.class, (String[]) null));
                assertThrows(NullPointerException.class, () -> context.getInt(Values.class, new String[]{null}));
                assertThrows(IllegalArgumentException.class, () -> context.getInt(RuntimeApiTest.Value.class, "value"));
                assertThrows(IllegalArgumentException.class, () -> context.getInt(Values.class));
                assertThrows(IllegalArgumentException.class, () -> context.getInt(Values.class, "missing"));
                assertThrows(IllegalArgumentException.class, () -> context.getFloat(Values.class, "position"));
                assertThrows(IllegalArgumentException.class, () -> context.getInt(Values.class, "i", "missing"));
                assertThrows(IllegalArgumentException.class, () -> context.getLong(Values.class, "i"));
                assertThrows(IllegalArgumentException.class, () -> context.getInt(Values.class, "f"));
                assertEquals(7, context.getInt(Values.class, "i"));
                String[] path = {"i"};
                assertEquals(7, context.getInt(Values.class, path));
                path[0] = "b";
                assertEquals(Byte.MIN_VALUE, context.getByte(Values.class, path));
                saved.set(context);
            }, null, value(7));
            runtime.entityManager.flush();
            entity.tryDestroy();
            runtime.entityManager.flush();
            assertInactive(saved.get());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"destroy", "reset", "close"})
    void callbackFailure_deactivatesEveryFieldReaderAndNeverReplaysIt(String operation) throws Exception {
        CleanECSRuntime runtime = RuntimeApiTest.runtime(home);
        AtomicReference<EntityDestroyContext> saved = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        IllegalStateException failure = new IllegalStateException("callback failed");
        var entity = runtime.entityManager.createEntity(context -> {
            assertEquals(7, context.getInt(Values.class, "i"));
            saved.set(context);
            calls.incrementAndGet();
            throw failure;
        }, null, value(7));
        runtime.entityManager.flush();
        switch (operation) {
            case "destroy" -> {
                entity.tryDestroy();
                assertSame(failure, assertThrows(IllegalStateException.class, runtime.entityManager::flush));
            }
            case "reset" -> assertSame(failure, assertThrows(IllegalStateException.class, runtime::reset));
            case "close" -> assertSame(failure, assertThrows(IllegalStateException.class, runtime::close));
            default -> throw new AssertionError(operation);
        }
        assertInactive(saved.get());
        if (operation.equals("destroy")) {
            runtime.close();
        } else {
            assertSame(failure, assertThrows(IllegalStateException.class, runtime::close));
        }
        assertEquals(1, calls.get());
    }

    private static void assertInactive(EntityDestroyContext context) {
        assertNotNull(context);
        assertThrows(IllegalStateException.class, () -> context.getByte(Values.class, "b"));
        assertThrows(IllegalStateException.class, () -> context.getShort(Values.class, "s"));
        assertThrows(IllegalStateException.class, () -> context.getInt(Values.class, "i"));
        assertThrows(IllegalStateException.class, () -> context.getLong(Values.class, "l"));
        assertThrows(IllegalStateException.class, () -> context.getFloat(Values.class, "f"));
        assertThrows(IllegalStateException.class, () -> context.getDouble(Values.class, "d"));
        assertThrows(IllegalStateException.class, () -> context.getBool(Values.class, "flag"));
    }

    private static Values value(int number) {
        Values value = new Values();
        value.b = Byte.MIN_VALUE;
        value.s = Short.MAX_VALUE;
        value.i = number;
        value.l = Long.MIN_VALUE;
        value.f = Float.intBitsToFloat(0x7fc01234);
        value.d = Double.NEGATIVE_INFINITY;
        value.flag = true;
        value.position.set(-0.0f, Float.POSITIVE_INFINITY, Float.MIN_VALUE);
        value.matrix.translation(17, 18, 19);
        return value;
    }

    @CleanComponentSignature
    public static class Values implements CleanComponent {
        public byte b;
        public short s;
        public int i;
        public long l;
        public float f;
        public double d;
        public boolean flag;
        public Vector3f position = new Vector3f();
        public Matrix4f matrix = new Matrix4f();
    }
}
