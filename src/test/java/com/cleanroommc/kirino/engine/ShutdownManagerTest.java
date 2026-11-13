package com.cleanroommc.kirino.engine;

import com.cleanroommc.kirino.utils.ReflectionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ShutdownManagerTest {
    private static final MethodHandle MAIN = ReflectionUtils.getFieldGetter(ShutdownManager.class, "mainThreadHooks", List.class);
    private static final MethodHandle BEFORE = ReflectionUtils.getFieldGetter(ShutdownManager.class, "beforeResourceHooks", List.class);
    private static final MethodHandle WORKER = ReflectionUtils.getFieldGetter(ShutdownManager.class, "workerThreadHooks", List.class);
    private List<Runnable> savedMain;
    private List<Runnable> savedBefore;
    private List<Runnable> savedWorker;

    @BeforeEach
    void isolateHooks() throws Throwable {
        savedMain = new ArrayList<>(main());
        savedBefore = new ArrayList<>(before());
        savedWorker = new ArrayList<>(worker());
        main().clear();
        before().clear();
        worker().clear();
    }

    @AfterEach
    void restoreHooks() throws Throwable {
        main().clear();
        before().clear();
        worker().clear();
        main().addAll(savedMain);
        before().addAll(savedBefore);
        worker().addAll(savedWorker);
    }

    @Test
    void drainRunsBeforeAllResourceDisposalAndSuccessfulHooksAreReleased() throws Throwable {
        List<String> events = new ArrayList<>();
        ShutdownManager.register(() -> events.add("old resource"));
        ShutdownManager.registerBeforeResources(() -> events.add("drain"));
        ShutdownManager.register(() -> events.add("new resource"));
        ShutdownManager.runMainHooks();
        assertEquals(List.of("drain", "new resource", "old resource"), events);
        assertTrue(main().isEmpty());
        assertTrue(before().isEmpty());
        ShutdownManager.runMainHooks();
        assertEquals(3, events.size());
    }

    @Test
    void failedPreparation_preservesResourceHooksUntilSuccessfulRetry() throws Throwable {
        List<String> events = new ArrayList<>();
        AtomicBoolean fail = new AtomicBoolean(true);
        ShutdownManager.registerBeforeResources(() -> {
            events.add("prepare");
            if (fail.getAndSet(false)) {
                throw new IllegalStateException("drain not completed");
            }
        });
        ShutdownManager.register(() -> events.add("dispose"));
        ShutdownManager.runMainHooks();
        assertEquals(List.of("prepare"), events);
        assertEquals(1, main().size());
        assertEquals(1, before().size());
        ShutdownManager.runMainHooks();
        assertEquals(List.of("prepare", "prepare", "dispose"), events);
        assertTrue(main().isEmpty());
        assertTrue(before().isEmpty());
    }

    @Test
    void failedResourceHook_isNotReplayedAndDoesNotKeepOtherResourcesAlive() throws Throwable {
        List<String> events = new ArrayList<>();
        ShutdownManager.register(() -> events.add("remaining resource"));
        ShutdownManager.register(() -> {
            events.add("broken resource");
            throw new AssertionError("dispose failed");
        });
        ShutdownManager.runMainHooks();
        ShutdownManager.runMainHooks();
        assertEquals(List.of("broken resource", "remaining resource"), events);
        assertTrue(main().isEmpty());
    }

    @Test
    void mainThreadDrain_canUnregisterWorkerFallbackAfterResourcesAreSafe() throws Throwable {
        Runnable fallback = () -> fail("Fallback should have been detached");
        worker().add(fallback);
        ShutdownManager.registerBeforeResources(() -> ShutdownManager.unregisterAsync(fallback));
        ShutdownManager.runMainHooks();
        assertTrue(worker().isEmpty());
        assertTrue(before().isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static List<Runnable> main() throws Throwable { return (List<Runnable>) MAIN.invokeExact(); }
    @SuppressWarnings("unchecked")
    private static List<Runnable> before() throws Throwable { return (List<Runnable>) BEFORE.invokeExact(); }
    @SuppressWarnings("unchecked")
    private static List<Runnable> worker() throws Throwable { return (List<Runnable>) WORKER.invokeExact(); }
}
