package com.cleanroommc.kirino.engine.render.usage.scene;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SceneFailureTest {
    @Test
    void failureFromWorker_isVisibleAndRetainsFirstCauseAcrossLaterFailures() throws Exception {
        SceneFailure state = new SceneFailure();
        state.check();
        IllegalStateException first = new IllegalStateException("first worker");
        AssertionError second = new AssertionError("second worker");
        CountDownLatch recorded = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            var firstTask = workers.submit(() -> {
                state.record(first);
                recorded.countDown();
            });
            var secondTask = workers.submit(() -> {
                assertTrue(recorded.await(5, TimeUnit.SECONDS));
                state.record(second);
                return null;
            });
            firstTask.get(5, TimeUnit.SECONDS);
            secondTask.get(5, TimeUnit.SECONDS);
        }
        assertSame(first, assertThrows(IllegalStateException.class, state::check));
        assertSame(first, assertThrows(IllegalStateException.class, () -> { throw state.fail(second); }));
        assertSame(first, assertThrows(IllegalStateException.class, state::check));
    }

    @Test
    void checkedCause_isUnwrappedAndReportedWithoutGrowingExceptionChains() {
        SceneFailure state = new SceneFailure();
        Exception first = new Exception("checked");
        state.record(new CompletionException(new CompletionException(first)));
        assertSame(first, assertThrows(CompletionException.class, state::check).getCause());
        assertSame(first, assertThrows(CompletionException.class, state::check).getCause());
    }

    @Test
    void fatalCause_isRethrownAsOriginalError() {
        SceneFailure state = new SceneFailure();
        AssertionError cause = new AssertionError("worker error");
        assertSame(cause, assertThrows(AssertionError.class, () -> { throw state.fail(cause); }));
        assertSame(cause, assertThrows(AssertionError.class, state::check));
    }
}
