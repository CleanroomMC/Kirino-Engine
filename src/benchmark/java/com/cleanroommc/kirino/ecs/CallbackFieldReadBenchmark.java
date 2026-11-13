package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.ComponentScanningEvent;
import com.cleanroommc.kirino.ecs.api.component.scan.event.StructScanningEvent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.usage.ecs.component.MeshletComponent;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.base.Preconditions;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class CallbackFieldReadBenchmark {
    private static final String[] MESHLET_ID = {"meshletId"};
    private static final String[] CHUNK_X = {"chunkPosX"};
    private static final String[] CHUNK_Y = {"chunkPosY"};
    private static final String[] CHUNK_Z = {"chunkPosZ"};

    @Param({"meshlet", "chunk"})
    public String shape;

    private EntityDestroyContext context;

    @Setup
    public void setup() throws Throwable {
        CleanroomEnvironment.setSide(Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType type : ScalarType.values()) {
            fields.registerFieldType(type.name().toLowerCase(Locale.ROOT), type.clazz, new FieldDef(type));
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
        ComponentRegistry registry = new ComponentRegistry(fields);
        ComponentScanningEvent componentEvent = new ComponentScanningEvent();
        componentEvent.register("com.cleanroommc.kirino.engine.render.usage.ecs.component");
        for (var plan : ComponentScanningHelper.scanComponentClasses(componentEvent, fields)) {
            @SuppressWarnings("unchecked")
            Class<? extends CleanComponent> type = (Class<? extends CleanComponent>) Class.forName(plan.componentClass());
            registry.registerComponent(plan.componentName(), type, plan.memberLayout(), plan.fieldTypeNames());
        }
        registry.lock();
        CleanComponent value;
        if (shape.equals("meshlet")) {
            MeshletComponent meshlet = new MeshletComponent();
            meshlet.meshletId = 1000;
            value = meshlet;
        } else {
            ChunkComponent chunk = new ChunkComponent();
            chunk.chunkPosX = 1000;
            chunk.chunkPosY = 2;
            chunk.chunkPosZ = 3;
            value = chunk;
        }
        HeapPool pool = new HeapPool(registry, List.of(value.getClass()), 16);
        pool.addEntity(713, new CleanComponent[]{value});
        context = MethodHolder.context(new EntityManager(registry), pool, 713);
        int expected = shape.equals("meshlet") ? 1000 : 1005;
        if (fullComponent() != expected || directField() != expected) {
            throw new IllegalStateException("Callback read lost its field value.");
        }
    }

    @Benchmark
    public int fullComponent() {
        if (shape.equals("meshlet")) {
            return context.getComponent(MeshletComponent.class).meshletId;
        }
        ChunkComponent chunk = context.getComponent(ChunkComponent.class);
        return chunk.chunkPosX + chunk.chunkPosY + chunk.chunkPosZ;
    }

    @Benchmark
    public int directField() {
        if (shape.equals("meshlet")) {
            return context.getInt(MeshletComponent.class, MESHLET_ID);
        }
        return context.getInt(ChunkComponent.class, CHUNK_X) + context.getInt(ChunkComponent.class, CHUNK_Y)
            + context.getInt(ChunkComponent.class, CHUNK_Z);
    }

    private static final class MethodHolder {
        static final Delegate DELEGATE;

        static {
            try {
                Class<?> type = Class.forName("com.cleanroommc.kirino.ecs.EntityManager$DestroyContext");
                DELEGATE = new Delegate(
                    Preconditions.checkNotNull(ReflectionUtils.getFieldGetter(EntityManager.class, "destroyContext", type))
                        .asType(MethodType.methodType(EntityDestroyContext.class, EntityManager.class)),
                    Preconditions.checkNotNull(ReflectionUtils.getFieldSetter(type, "pool", ArchetypeDataPool.class))
                        .asType(MethodType.methodType(void.class, EntityDestroyContext.class, ArchetypeDataPool.class)),
                    Preconditions.checkNotNull(ReflectionUtils.getFieldSetter(type, "entityID", int.class))
                        .asType(MethodType.methodType(void.class, EntityDestroyContext.class, int.class)));
            } catch (ClassNotFoundException failure) {
                throw new ExceptionInInitializerError(failure);
            }
        }

        static EntityDestroyContext context(EntityManager manager, ArchetypeDataPool pool, int entity) throws Throwable {
            EntityDestroyContext context = (EntityDestroyContext) DELEGATE.getter.invokeExact(manager);
            DELEGATE.pool.invokeExact(context, pool);
            DELEGATE.entity.invokeExact(context, entity);
            return context;
        }

        record Delegate(MethodHandle getter, MethodHandle pool, MethodHandle entity) {}
    }
}
