package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeSessionApiTest {
    @TempDir Path home;

    @Test
    void sessionHasSameRegistrationsButResetDoesNotAffectParentOrSibling() throws Exception {
        CleanECSRuntime parent = RuntimeApiTest.runtime(home);
        CleanECSRuntime scene = parent.newSession();
        CleanECSRuntime sibling = parent.newSession();
        var parentEntity = parent.entityManager.createEntity(new RuntimeApiTest.Value());
        var sceneEntity = scene.entityManager.createEntity(new RuntimeApiTest.Value());
        var siblingEntity = sibling.entityManager.createEntity(new RuntimeApiTest.Value());
        parent.entityManager.flush();
        scene.entityManager.flush();
        sibling.entityManager.flush();
        scene.jobScheduler.executeParallelJob(scene.entityManager, RuntimeApiTest.IncrementJob.class, null, Runnable::run).future().join();
        assertEquals(1, sceneEntity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        scene.reset();
        assertFalse(sceneEntity.valid());
        assertTrue(parentEntity.valid());
        assertTrue(siblingEntity.valid());
        assertEquals(CleanECSRuntime.State.RUNNING, parent.state());
        assertEquals(CleanECSRuntime.State.RUNNING, sibling.state());
        scene.close();
        sibling.close();
        parent.close();
    }

    @Test
    void liveSessionOutlivesParentAndOwnsItsScheduler() throws Exception {
        CleanECSRuntime parent = RuntimeApiTest.runtime(home);
        CleanECSRuntime scene = parent.newSession();
        parent.close();
        var entity = scene.entityManager.createEntity(new RuntimeApiTest.Value());
        scene.entityManager.flush();
        scene.jobScheduler.executeParallelJob(scene.entityManager, RuntimeApiTest.IncrementJob.class, null, Runnable::run).future().join();
        assertEquals(1, entity.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        assertThrows(IllegalArgumentException.class, () -> parent.jobScheduler.executeParallelJob(
            scene.entityManager, RuntimeApiTest.IncrementJob.class, null, Runnable::run));
        assertThrows(IllegalStateException.class, parent::newSession);
        scene.close();
    }

    @Test
    void sessionsCanLaunchConcurrentlyAndOnlyStoppedOwnerRejectsNewSessions() throws Exception {
        CleanECSRuntime parent = RuntimeApiTest.runtime(home);
        CleanECSRuntime first = parent.newSession();
        CleanECSRuntime second = parent.newSession();
        var one = first.entityManager.createEntity(new RuntimeApiTest.Value());
        var two = second.entityManager.createEntity(new RuntimeApiTest.Value());
        first.entityManager.flush();
        second.entityManager.flush();
        try (var workers = Executors.newFixedThreadPool(2)) {
            var a = workers.submit(() -> first.jobScheduler.executeParallelJob(first.entityManager,
                RuntimeApiTest.IncrementJob.class, null, Runnable::run).future().join());
            var b = workers.submit(() -> second.jobScheduler.executeParallelJob(second.entityManager,
                RuntimeApiTest.IncrementJob.class, null, Runnable::run).future().join());
            a.get(5, TimeUnit.SECONDS);
            b.get(5, TimeUnit.SECONDS);
        }
        assertEquals(1, one.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        assertEquals(1, two.readComponent(RuntimeApiTest.Value.class).orElseThrow().value);
        parent.quiesce().join();
        assertThrows(IllegalStateException.class, parent::newSession);
        CleanECSRuntime descendant = first.newSession();
        descendant.close();
        first.close();
        second.close();
        parent.close();
    }
}
