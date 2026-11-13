package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EntityCommandsBenchmark {
    @Param({"128", "1024"})
    public int batchSize;

    private EntityManager entities;
    private EntityQuery query;
    private CleanEntityHandle[] handles;
    private Value value;

    @Setup
    public void setup() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("value", Value.class, new MemberLayout("value"), "int");
        components.lock();
        entities = new EntityManager(components);
        query = entities.newQuery().with(Value.class);
        value = new Value();
        value.value = 17;
        handles = new CleanEntityHandle[batchSize];
        for (int i = 0; i < batchSize; i++) {
            handles[i] = entities.createEntity(value);
        }
        entities.flush();
        if (directManagerCommands() != batchSize || scopedCommands() != batchSize) {
            throw new IllegalStateException("A component command was rejected.");
        }
        verify();
    }

    @Benchmark
    public int directManagerCommands() {
        int accepted = 0;
        try (var execution = entities.beginExecution(query)) {
            for (CleanEntityHandle handle : handles) {
                if (entities.trySetComponent(handle.index, handle.generation, value)) {
                    accepted++;
                }
            }
        }
        entities.flush();
        return accepted;
    }

    @Benchmark
    public int scopedCommands() {
        int accepted = 0;
        try (var execution = entities.beginExecution(query)) {
            EntityCommands commands = execution.commands();
            for (CleanEntityHandle handle : handles) {
                if (commands.trySetComponent(handle.index, value)) {
                    accepted++;
                }
            }
        }
        entities.flush();
        return accepted;
    }

    @TearDown
    public void verify() {
        for (CleanEntityHandle handle : handles) {
            if (!handle.isAlive() || handle.readComponent(Value.class).orElseThrow().value != value.value) {
                throw new IllegalStateException("A component value was lost.");
            }
        }
    }

    public static class Value implements CleanComponent {
        public int value;
    }
}
