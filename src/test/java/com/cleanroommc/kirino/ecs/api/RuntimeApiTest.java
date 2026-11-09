package com.cleanroommc.kirino.ecs.api;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.config.KirinoConfig;
import com.cleanroommc.kirino.ecs.CleanECSRuntime;
import com.cleanroommc.kirino.ecs.EntityCommands;
import com.cleanroommc.kirino.ecs.EntityQuery;
import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.scan.CleanComponentSignature;
import com.cleanroommc.kirino.ecs.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.ParallelJob;
import com.cleanroommc.kirino.ecs.job.event.JobRegistrationEvent;
import com.cleanroommc.kirino.ecs.storage.PrimitiveArray;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.Event;
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
        events.register(new Registration());
        CleanECSRuntime runtime = CleanECSRuntime.create(events, LogManager.getLogger(RuntimeApiTest.class));
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

    public static class Registration {
        @SubscribeEvent
        public void registration(Event event) {
            if (event instanceof ComponentScanningEvent components) {
                components.register(RuntimeApiTest.class.getPackageName());
            } else if (event instanceof JobRegistrationEvent jobs) {
                jobs.register(IncrementJob.class);
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
