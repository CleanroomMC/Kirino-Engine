package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyCallback;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EntityLifecycleBenchmark {
    @Param({"1", "128"})
    public int batchSize;

    private EntityManager lifecycle;
    private EntityManager migration;
    private CleanEntityHandle[] created;
    private CleanEntityHandle[] migrating;
    private Value value;
    private Added added;
    private int callbackChecksum;
    private final EntityCreateCallback beforeCreate = context -> callbackChecksum += context.getComponent(Value.class).value;
    private final EntityDestroyCallback beforeDestroy = context -> callbackChecksum += context.getComponent(Value.class).value;

    @Setup
    public void setup() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class, new MemberLayout("value"), "int");
        registry.registerComponent("added", Added.class, new MemberLayout("value"), "int");
        registry.lock();
        lifecycle = new EntityManager(registry);
        migration = new EntityManager(registry);
        created = new CleanEntityHandle[batchSize];
        migrating = new CleanEntityHandle[batchSize];
        value = new Value();
        value.value = 1000;
        added = new Added();
        added.value = 2000;
        for (int i = 0; i < batchSize; i++) {
            migrating[i] = migration.createEntity(value);
        }
        migration.flush();
        createFlushDestroyFlush();
        if (createFlushDestroyFlushWithCallbacks() != batchSize * value.value * 2) {
            throw new IllegalStateException("Callbacks lost component values.");
        }
        addFlushRemoveFlush();
        verify();
    }

    @Benchmark
    public int createFlushDestroyFlush() {
        int result = 0;
        for (int i = 0; i < batchSize; i++) {
            created[i] = lifecycle.createEntity(value);
            result += created[i].generation;
        }
        lifecycle.flush();
        for (CleanEntityHandle handle : created) {
            if (!handle.tryDestroy()) {
                throw new IllegalStateException("Entity wasn't destroyed");
            }
        }
        lifecycle.flush();
        return result;
    }

    @Benchmark
    public int createFlushDestroyFlushWithCallbacks() {
        callbackChecksum = 0;
        for (int i = 0; i < batchSize; i++) {
            created[i] = lifecycle.createEntity(beforeDestroy, beforeCreate, value);
        }
        lifecycle.flush();
        for (CleanEntityHandle handle : created) {
            if (!handle.tryDestroy()) {
                throw new IllegalStateException("Entity wasn't destroyed.");
            }
        }
        lifecycle.flush();
        return callbackChecksum;
    }

    @Benchmark
    public int addFlushRemoveFlush() {
        int result = 0;
        for (CleanEntityHandle handle : migrating) {
            if (!handle.tryAddComponent(added)) {
                throw new IllegalStateException("Component wasn't added");
            }
            result += handle.index;
        }
        migration.flush();
        for (CleanEntityHandle handle : migrating) {
            if (!handle.tryRemoveComponent(Added.class)) {
                throw new IllegalStateException("Component wasn't removed");
            }
        }
        migration.flush();
        return result;
    }

    @TearDown
    public void verify() {
        for (CleanEntityHandle handle : created) {
            if (handle.valid()) {
                throw new IllegalStateException("Destroyed identity is still valid");
            }
        }
        var pools = migration.startQuery(migration.newQuery().with(Value.class).without(Added.class));
        for (CleanEntityHandle handle : migrating) {
            if (!handle.isAlive()) {
                throw new IllegalStateException("Migrated entity isn't alive");
            }
            var pool = pools.stream().filter(candidate -> candidate.containsEntity(handle.index)).findFirst().orElseThrow();
            if (((Value) pool.getComponent(handle.index, Value.class)).value != 1000) {
                throw new IllegalStateException("Migration lost a component value");
            }
        }
    }

    public static class Value implements CleanComponent { public int value; }
    public static class Added implements CleanComponent { public int value; }
}
