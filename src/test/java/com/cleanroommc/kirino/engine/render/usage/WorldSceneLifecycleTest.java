package com.cleanroommc.kirino.engine.render.usage;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class WorldSceneLifecycleTest {
    @Test
    void switchingWorld_waitsForBothCpuAndGpuBeforeResetAndAttach() {
        Fixture f = new Fixture();
        Object first = new Object();
        Object next = new Object();
        f.lifecycle.update(first);
        f.events.clear();
        f.lifecycle.update(next);
        assertEquals(List.of("detach", "quiesce", "fence"), f.events);
        f.gpuReady = true;
        f.lifecycle.update(next);
        assertEquals(3, f.events.size());
        f.drained.complete(null);
        f.lifecycle.update(next);
        assertEquals(List.of("detach", "quiesce", "fence", "reset", "attach", "update"), f.events);
        assertSame(next, f.world);
    }

    @Test
    void cpuCompletesFirst_doesNotReuseBuffersBeforeGpuCompletion() {
        Fixture f = new Fixture();
        f.lifecycle.update(new Object());
        Object next = new Object();
        f.lifecycle.update(next);
        f.drained.complete(null);
        int count = f.events.size();
        f.lifecycle.update(next);
        assertEquals(count, f.events.size());
        assertFalse(f.events.contains("reset"));
        f.gpuReady = true;
        f.lifecycle.update(next);
        assertSame(next, f.world);
    }

    @Test
    void latestRequestDuringDrain_winsWithoutAttachingIntermediateWorld() {
        Fixture f = new Fixture();
        f.lifecycle.update(new Object());
        Object intermediate = new Object();
        Object latest = new Object();
        f.lifecycle.update(intermediate);
        f.lifecycle.update(null);
        f.lifecycle.update(latest);
        f.drained.complete(null);
        f.gpuReady = true;
        f.lifecycle.update(latest);
        assertEquals(2, f.attachments.size());
        assertSame(latest, f.attachments.getLast());
        assertFalse(f.attachments.contains(intermediate));
        assertEquals(1, f.events.stream().filter("detach"::equals).count());
    }

    @Test
    void leaveAndReenterSameWorld_startsNewSessionAndMenuDoesNotTickScene() {
        Fixture f = new Fixture();
        Object world = new Object();
        f.lifecycle.update(null);
        assertTrue(f.events.isEmpty());
        f.lifecycle.update(world);
        f.lifecycle.update(null);
        f.drained.complete(null);
        f.gpuReady = true;
        f.lifecycle.update(null);
        assertNull(f.world);
        int count = f.events.size();
        f.lifecycle.update(null);
        assertEquals(count, f.events.size());
        f.lifecycle.update(world);
        assertEquals(2, f.attachments.size());
        assertSame(world, f.world);
    }

    @Test
    void exceptionalDrain_doesNotSkipCleanupAndNewSession() {
        Fixture f = new Fixture();
        f.lifecycle.update(new Object());
        Object next = new Object();
        f.lifecycle.update(next);
        f.drained.completeExceptionally(new IllegalStateException("stopped flow"));
        f.gpuReady = true;
        f.lifecycle.update(next);
        assertSame(next, f.world);
        assertTrue(f.events.contains("reset"));
    }

    @Test
    void failedScene_neverRetriesUpdateButCanRetireWhenWorldChanges() {
        Fixture f = new Fixture();
        Object world = new Object();
        IllegalStateException failure = new IllegalStateException("scene update");
        f.updateFailure = failure;
        assertSame(failure, assertThrows(IllegalStateException.class, () -> f.lifecycle.update(world)));
        int count = f.events.size();
        assertSame(failure, assertThrows(IllegalStateException.class, () -> f.lifecycle.update(world)));
        assertEquals(count, f.events.size());
        Object next = new Object();
        f.updateFailure = null;
        f.lifecycle.update(next);
        f.drained.completeExceptionally(failure);
        f.gpuReady = true;
        f.lifecycle.update(next);
        assertSame(next, f.world);
    }

    @Test
    void failedReset_doesNotAttachOrRepeatCleanup() {
        Fixture f = new Fixture();
        f.lifecycle.update(new Object());
        Object next = new Object();
        f.lifecycle.update(next);
        f.drained.complete(null);
        f.gpuReady = true;
        f.resetFailure = new IllegalArgumentException("cleanup failed");
        assertSame(f.resetFailure, assertThrows(IllegalArgumentException.class, () -> f.lifecycle.update(next)));
        assertSame(f.resetFailure, assertThrows(IllegalArgumentException.class, () -> f.lifecycle.update(next)));
        assertEquals(1, f.attachments.size());
        assertEquals(1, f.events.stream().filter("reset"::equals).count());
        f.lifecycle.close();
        assertTrue(f.events.contains("close"));
    }

    @Test
    void shutdownDuringTransition_waitsForGpuAndDoesNotAttachTargetOrReplayClose() {
        Fixture f = new Fixture();
        f.lifecycle.update(new Object());
        f.lifecycle.update(new Object());
        f.drained.complete(null);
        f.lifecycle.close();
        int size = f.events.size();
        f.lifecycle.close();
        f.lifecycle.update(new Object());
        assertEquals(size, f.events.size());
        assertEquals(1, f.attachments.size());
        assertEquals(List.of("awaitGpu", "close"), f.events.subList(size - 2, size));
    }

    @Test
    void sameWorld_framesNeverResetOrReattach() {
        Fixture f = new Fixture();
        Object world = new Object();
        f.lifecycle.update(world);
        f.lifecycle.update(world);
        f.lifecycle.update(world);
        assertEquals(List.of("attach", "update", "update", "update"), f.events);
    }

    private static final class Fixture implements WorldSceneLifecycle.Backend<Object> {
        final List<String> events = new ArrayList<>();
        final List<Object> attachments = new ArrayList<>();
        final CompletableFuture<Void> drained = new CompletableFuture<>();
        final WorldSceneLifecycle<Object> lifecycle = new WorldSceneLifecycle<>(this);
        boolean gpuReady;
        Object world;
        RuntimeException resetFailure;
        RuntimeException updateFailure;

        public void attach(Object value) { world = value; attachments.add(value); events.add("attach"); }
        public void detach() { events.add("detach"); }
        public CompletableFuture<Void> quiesce() { events.add("quiesce"); return drained; }
        public void beginGpuDrain() { events.add("fence"); }
        public boolean gpuReady() { return gpuReady; }
        public void awaitGpu() { events.add("awaitGpu"); }
        public void reset() {
            events.add("reset");
            if (resetFailure != null) {
                throw resetFailure;
            }
            world = null;
        }
        public void close() { events.add("close"); }
        public void update() {
            events.add("update");
            if (updateFailure != null) {
                throw updateFailure;
            }
        }
    }
}
