package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.api.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.usage.scene.callback.CallbackDrivenChunkDelta;
import com.cleanroommc.kirino.engine.render.usage.task.job.*;
import com.cleanroommc.kirino.engine.render.usage.task.system.*;
import com.cleanroommc.kirino.engine.render.usage.task.RenderJobDataKeys;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.relauncher.Side;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RendererExternalBindingTest {
    @Test
    void rendererProducers_andAllFiveJobsUseMatchingExplicitKeysBeforeAnyEntitiesExist() throws Throwable {
        CleanroomEnvironment.setSide(Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType scalar : ScalarType.values()) {
            fields.registerFieldType(scalar.name().toLowerCase(Locale.ROOT), scalar.clazz, new FieldDef(scalar));
        }
        StructScanningEvent structEvent = new StructScanningEvent();
        structEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.struct");
        for (var plan : StructScanningHelper.scanStructClasses(structEvent, fields)) {
            Class<?> type = Class.forName(plan.structClass());
            structs.registerStructType(plan.structName(), type, plan.memberLayout(), plan.structDef());
            fields.registerFieldType(plan.structName(), type, new FieldDef(plan.structName()));
        }
        structs.lock();
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        ComponentScanningEvent componentEvent = new ComponentScanningEvent();
        componentEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.component");
        for (var plan : ComponentScanningHelper.scanComponentClasses(componentEvent, fields)) {
            components.registerComponent(plan.componentName(), Class.forName(plan.componentClass()).asSubclass(CleanComponent.class),
                plan.memberLayout(), plan.fieldTypeNames());
        }
        components.lock();
        JobRegistry registry = new JobRegistry(components);
        List<Class<? extends ParallelJob>> jobs = List.of(ChunkMeshletGenJob.class, ChunkPrioritizationJob.class,
            MeshletDebugJob.class, MeshletDestroyJob.class, MeshletBufferWriteJob.class);
        for (var type : jobs) {
            registry.registerParallelJob(type);
            for (var field : type.getDeclaredFields()) {
                JobExternalDataQuery annotation = field.getAnnotation(JobExternalDataQuery.class);
                if (annotation != null) {
                    assertFalse(annotation.value().isBlank());
                    assertNotEquals(field.getName(), annotation.value());
                }
            }
        }
        EntityManager entities = new EntityManager(components);
        var scheduler = RuntimeBootstrap.newJobScheduler(registry);
        CleanWorld world = new CleanWorld(entities, scheduler) {
            public void update() {}
        };
        List<CleanSystem> producers = List.of(
            new ChunkMeshletGenSystem(null, null, null, null, Runnable::run),
            new ChunkPrioritizationSystem(null, Runnable::run),
            new MeshletDebugSystem(null, null, Runnable::run),
            new MeshletDestroySystem(new CallbackDrivenChunkDelta(), Runnable::run),
            new MeshletBufferWriteSystem(null, Runnable::run));
        for (CleanSystem producer : producers) {
            execute(world, producer.getClass(), producer);
        }
        ChunkMeshletGenSystem generation = (ChunkMeshletGenSystem) producers.getFirst();
        Map<String, Object> generationData = MethodHolder.data(generation);
        assertTrue(generationData.containsKey(RenderJobDataKeys.WORLD));
        assertTrue(generationData.containsKey(RenderJobDataKeys.CHUNK_PROVIDER));
        generation.releaseWorldReferences();
        assertFalse(generationData.containsKey(RenderJobDataKeys.WORLD));
        assertFalse(generationData.containsKey(RenderJobDataKeys.CHUNK_PROVIDER));
        assertTrue(((Map<?, ?>) generationData.get(RenderJobDataKeys.TEMP_BUFFERS)).isEmpty());
        entities.flush();
    }

    private static <T extends CleanSystem> void execute(CleanWorld world, Class<T> type, CleanSystem system) {
        SingleFlow.newBuilder(world, type)
            .addTransition(type.cast(system), SingleFlow.START_NODE, SingleFlow.END_NODE).build().execute();
    }

    private static final class MethodHolder {
        static final MethodHandle DATA = Preconditions.checkNotNull(
            ReflectionUtils.getFieldGetter(ChunkMeshletGenSystem.class, "externalData", Map.class));

        @SuppressWarnings("unchecked")
        static Map<String, Object> data(ChunkMeshletGenSystem system) throws Throwable {
            return (Map<String, Object>) DATA.invokeExact(system);
        }
    }
}
