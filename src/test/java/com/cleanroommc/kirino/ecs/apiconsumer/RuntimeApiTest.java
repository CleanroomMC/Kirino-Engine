package com.cleanroommc.kirino.ecs.apiconsumer;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.api.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.CleanComponentSignature;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class RuntimeApiTest {
    @TempDir Path home;

    @Test
    void runtimeFromExternalPackage_registersCreatesRunsReadsAndDestroys() throws Exception {
        CleanECSRuntime runtime = runtime(home);
        AtomicInteger createdValue = new AtomicInteger();
        AtomicInteger destroyedValue = new AtomicInteger();
        Value input = new Value();
        input.value = 7;
        var handle = runtime.entityManager.createEntity(
            context -> destroyedValue.set(context.getComponent(Value.class).value),
            context -> createdValue.set(context.getComponent(Value.class).value), input);
        assertTrue(handle.valid());
        assertFalse(handle.isAlive());
        assertTrue(handle.contains(Value.class));
        runtime.entityManager.flush();
        assertEquals(7, createdValue.get());
        runtime.jobScheduler.executeParallelJob(runtime.entityManager, IncrementJob.class, null, Runnable::run)
            .future().join();
        Value copy = handle.readComponent(Value.class).orElseThrow();
        assertEquals(8, copy.value);
        copy.value = 99;
        assertEquals(8, handle.readComponent(Value.class).orElseThrow().value);
        assertTrue(handle.tryDestroy());
        runtime.entityManager.flush();
        assertFalse(handle.valid());
        assertEquals(8, destroyedValue.get());
    }

    @Test
    void handlesFromDifferentRuntimes_keepIdentityAndIndependentState() throws Exception {
        CleanECSRuntime first = runtime(home);
        CleanECSRuntime second = runtime(home);
        var firstEntity = first.entityManager.createEntity(new Value());
        var secondEntity = second.entityManager.createEntity(new Value());
        first.entityManager.flush();
        second.entityManager.flush();
        assertNotEquals(firstEntity, secondEntity);
        assertEquals(firstEntity, firstEntity);
        assertTrue(firstEntity.tryDestroy());
        first.entityManager.flush();
        assertFalse(firstEntity.valid());
        assertTrue(secondEntity.isAlive());
        assertTrue(secondEntity.contains(Value.class));
    }

    @Test
    void scheduler_foreignManagerImplementation_isRejectedBeforeInvokingIt() throws Exception {
        CleanECSRuntime runtime = runtime(home);
        var foreign = (com.cleanroommc.kirino.ecs.api.EntityManager) java.lang.reflect.Proxy.newProxyInstance(
            RuntimeApiTest.class.getClassLoader(), new Class<?>[]{com.cleanroommc.kirino.ecs.api.EntityManager.class},
            (proxy, method, arguments) -> { throw new AssertionError("Foreign manager invoked"); });
        assertThrows(IllegalArgumentException.class,
            () -> runtime.jobScheduler.executeParallelJob(foreign, IncrementJob.class, null, Runnable::run));
        runtime.entityManager.flush();
    }

    @SafeVarargs
    static CleanECSRuntime runtime(Path home, Class<? extends ParallelJob>... extraJobs) throws Exception {
        CleanroomEnvironment.setSide(Side.CLIENT);
        var homeField = FMLInjectionData.class.getDeclaredField("minecraftHome");
        homeField.setAccessible(true);
        Object previousHome = homeField.get(null);
        File previousLaunch = Launch.minecraftHome;
        try {
            homeField.set(null, home.toFile());
            Launch.minecraftHome = home.toFile();
            Class.forName(KirinoConfig.class.getName(), true, KirinoConfig.class.getClassLoader());
        } finally {
            homeField.set(null, previousHome);
            Launch.minecraftHome = previousLaunch;
        }
        EventBus events = new EventBus();
        Registration registration = new Registration(extraJobs);
        events.register(registration);
        try {
            return CleanECSRuntime.create(events, LogManager.getLogger(RuntimeApiTest.class));
        } finally {
            events.unregister(registration);
        }
    }

    public static class Registration {
        private final Class<? extends ParallelJob>[] extraJobs;

        Registration(Class<? extends ParallelJob>[] extraJobs) { this.extraJobs = extraJobs; }

        @SubscribeEvent
        public void registration(Event event) {
            if (event instanceof ComponentScanningEvent components) {
                components.register(RuntimeApiTest.class.getPackageName());
            } else if (event instanceof JobRegistrationEvent jobs) {
                jobs.register(IncrementJob.class);
                for (Class<? extends ParallelJob> job : extraJobs) {
                    jobs.register(job);
                }
            }
        }
    }

    @CleanComponentSignature
    public static class Value implements CleanComponent {
        public int value;
    }

    public static class IncrementJob implements ParallelJob {
        @JobDataQuery(componentClass = Value.class, fieldAccessChain = {"value"})
        private PrimitiveArray values;

        @Override
        public void execute(EntityCommands commands, int index, int entityID, int ordinal) {
            values.setInt(index, values.getInt(index) + 1);
        }
        @Override
        public void query(EntityQuery query) { query.with(Value.class); }
        @Override
        public int estimateWorkload(int index) { return 1; }
    }
}
