package com.cleanroommc.kirino.engine.render.usage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lwjgl.opengl.GL32C;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class SceneGpuFenceTest {
    @ParameterizedTest
    @ValueSource(ints = {GL32C.GL_ALREADY_SIGNALED, GL32C.GL_CONDITION_SATISFIED})
    void poll_waitsWithoutFinishingAndDeletesSignaledFenceOnce(int success) {
        Driver driver = new Driver();
        SceneGpuFence fence = new SceneGpuFence(driver);
        assertTrue(fence.ready());
        fence.begin();
        assertFalse(fence.ready());
        assertEquals(0, driver.deleted);
        assertEquals(0, driver.finishes);
        driver.status = success;
        assertTrue(fence.ready());
        assertTrue(fence.ready());
        fence.await();
        assertEquals(1, driver.deleted);
        assertEquals(0, driver.finishes);
        fence.begin();
        assertTrue(fence.ready());
        assertEquals(2, driver.deleted);
    }

    @Test
    void failedWait_neverCountsAsReadyAndShutdownCanFinishPendingCommands() {
        Driver driver = new Driver();
        SceneGpuFence fence = new SceneGpuFence(driver);
        fence.begin();
        driver.status = GL32C.GL_WAIT_FAILED;
        assertThrows(IllegalStateException.class, fence::ready);
        assertEquals(0, driver.deleted);
        fence.await();
        assertEquals(1, driver.finishes);
        assertEquals(1, driver.deleted);
    }

    @Test
    void failedCreation_requiresSuccessfulShutdownFinishBeforeResourcesCanBeReleased() {
        Driver driver = new Driver();
        driver.handle = 0;
        SceneGpuFence fence = new SceneGpuFence(driver);
        assertThrows(IllegalStateException.class, fence::begin);
        assertThrows(IllegalStateException.class, fence::ready);
        driver.finishFailure = new IllegalStateException("context failure");
        assertSame(driver.finishFailure, assertThrows(IllegalStateException.class, fence::await));
        driver.finishFailure = null;
        fence.await();
        assertEquals(0, driver.deleted);
        assertTrue(fence.ready());
    }

    @Test
    void shutdown_waitsAndDoesNotOverwriteAnOutstandingFence() {
        Driver driver = new Driver();
        SceneGpuFence fence = new SceneGpuFence(driver);
        fence.begin();
        assertThrows(IllegalStateException.class, fence::begin);
        fence.await();
        fence.await();
        assertEquals(1, driver.finishes);
        assertEquals(1, driver.deleted);
    }

    @Test
    void foreignThread_cannotPollOrReleaseTheOwningContextsFence() {
        Driver driver = new Driver();
        SceneGpuFence fence = new SceneGpuFence(driver);
        fence.begin();
        CompletableFuture.runAsync(() -> {
            assertThrows(IllegalStateException.class, fence::begin);
            assertThrows(IllegalStateException.class, fence::ready);
            assertThrows(IllegalStateException.class, fence::await);
        }).join();
        assertEquals(0, driver.deleted);
        assertEquals(0, driver.finishes);
        fence.await();
        assertEquals(1, driver.deleted);
    }

    private static final class Driver implements SceneGpuFence.Driver {
        long handle = 7;
        int status = GL32C.GL_TIMEOUT_EXPIRED;
        int deleted;
        int finishes;
        RuntimeException finishFailure;
        public long insert() { return handle; }
        public int poll(long value) { assertEquals(handle, value); return status; }
        public void delete(long value) { assertEquals(handle, value); deleted++; }
        public void finish() {
            finishes++;
            if (finishFailure != null) {
                throw finishFailure;
            }
        }
    }
}
