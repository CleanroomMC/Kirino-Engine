package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import net.minecraftforge.fml.relauncher.Side;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.ThreadParams;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class CommandContentionBenchmark {
    @State(Scope.Group)
    public static class Shared {
        EntityManager manager;
        CleanEntityHandle[] handles;
        Value value;

        @Setup(Level.Trial)
        public void setup() {
            CleanroomEnvironment.setSide(Side.CLIENT);
            FieldRegistry fields = new FieldRegistry(new StructRegistry());
            fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
            fields.lock();
            ComponentRegistry components = new ComponentRegistry(fields);
            components.registerComponent("value", Value.class, new MemberLayout("value"), "int");
            components.lock();
            manager = new EntityManager(components);
            handles = new CleanEntityHandle[1024];
            value = new Value();
            value.value = 713;
            for (int i = 0; i < handles.length; i++) {
                handles[i] = manager.createEntity(value);
            }
            manager.flush();
        }

        @TearDown(Level.Iteration)
        public void verify() {
            manager.flush();
            for (var handle : handles) {
                if (handle.readComponent(Value.class).orElseThrow().value != 713) {
                    throw new IllegalStateException("Concurrent commands lost data");
                }
            }
        }
    }

    @State(Scope.Thread)
    public static class Cursor {
        int index;
        @Setup public void setup(ThreadParams thread) { index = thread.getSubgroupThreadIndex() * 127; }
    }

    @Benchmark
    @Group("commands")
    @GroupThreads(1)
    public boolean aSubmit(Shared state, Cursor cursor) {
        boolean accepted = state.handles[cursor.index++ & 1023].trySetComponent(state.value);
        if (!accepted) {
            throw new IllegalStateException("Concurrent SET rejected");
        }
        return accepted;
    }

    @Benchmark
    @Group("commands")
    @GroupThreads(1)
    public int zFlush(Shared state) {
        state.manager.flush();
        return state.handles.length;
    }

    public static class Value implements CleanComponent {
        public int value;
    }
}
