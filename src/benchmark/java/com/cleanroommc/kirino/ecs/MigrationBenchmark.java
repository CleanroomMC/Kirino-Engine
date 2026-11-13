package com.cleanroommc.kirino.ecs;

import com.cleanroommc.common.CleanroomEnvironment;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import org.joml.*;
import org.openjdk.jmh.annotations.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MigrationBenchmark {
    @Param({"1", "256"})
    public int batchSize;
    @Param({"scalars", "wide"})
    public String schema;
    private ComponentRegistry registry;
    private Class<? extends CleanComponent> valueType;
    private CleanComponent value;
    private Added added;
    private HeapPool rebuildingSource;
    private HeapPool rebuildingTarget;
    private HeapPool directSource;
    private HeapPool directTarget;
    private EntityManager manager;
    private EntityManager rebuildingManager;
    private CleanEntityHandle[] rebuildingHandles;
    private CleanEntityHandle[] handles;
    private Object[] expected;

    @Setup
    public void setup() {
        CleanroomEnvironment.setSide(net.minecraftforge.fml.relauncher.Side.CLIENT);
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("child", Child.class, new MemberLayout("id", "position", "matrix"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef(ScalarType.VEC3), new FieldDef(ScalarType.MAT4)));
        structs.lock();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType type : ScalarType.values()) fields.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        fields.registerFieldType("child", Child.class, new FieldDef("child"));
        fields.lock();
        registry = new ComponentRegistry(fields);
        registry.registerComponent("scalars", Scalars.class, new MemberLayout("b", "s", "id", "l", "f", "d", "flag"),
            "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BOOL");
        registry.registerComponent("wide", Wide.class,
            new MemberLayout("b", "s", "id", "l", "f", "d", "flag", "v2", "v3", "v4", "m3", "m4", "a", "c"),
            "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BOOL", "VEC2", "VEC3", "VEC4", "MAT3", "MAT4", "child", "child");
        registry.registerComponent("added", Added.class, new MemberLayout("value"), "INT");
        registry.lock();
        Scalars data = schema.equals("wide") ? new Wide() : new Scalars();
        data.b = 7; data.s = 1000; data.id = 12345; data.l = 9876543210L;
        data.f = -0.0f; data.d = Double.POSITIVE_INFINITY; data.flag = true;
        value = data;
        valueType = value.getClass();
        added = new Added(); added.value = 54321;
        expected = registry.flattenComponent(value);
        rebuildingSource = new HeapPool(registry, List.of(valueType), batchSize);
        rebuildingTarget = new HeapPool(registry, List.of(Added.class, valueType), batchSize);
        directSource = new HeapPool(registry, List.of(valueType), batchSize);
        directTarget = new HeapPool(registry, List.of(Added.class, valueType), batchSize);
        manager = new EntityManager(registry);
        rebuildingManager = new EntityManager(registry, types -> new RebuildingPool(registry, types));
        rebuildingHandles = new CleanEntityHandle[batchSize];
        handles = new CleanEntityHandle[batchSize];
        CleanComponent[] payload = {value};
        for (int i = 0; i < batchSize; i++) {
            rebuildingSource.addEntity(i, payload);
            directSource.addEntity(i, payload);
            handles[i] = manager.createEntity(payload);
            rebuildingHandles[i] = rebuildingManager.createEntity(payload);
        }
        manager.flush();
        rebuildingManager.flush();
        rebuildingRoundTrip();
        directRoundTrip();
        rebuildingManagerRoundTrip();
        managerRoundTrip();
        verify();
    }

    @Benchmark
    public int rebuildingRoundTrip() {
        for (int id = 0; id < batchSize; id++) {
            rebuildingTarget.addEntity(id, new CleanComponent[]{added, rebuildingSource.getComponent(id, valueType)});
            rebuildingSource.removeEntity(id);
        }
        for (int id = 0; id < batchSize; id++) {
            rebuildingSource.addEntity(id, new CleanComponent[]{rebuildingTarget.getComponent(id, valueType)});
            rebuildingTarget.removeEntity(id);
        }
        return rebuildingSource.getEntityID(batchSize - 1);
    }

    @Benchmark
    public int directRoundTrip() {
        for (int id = 0; id < batchSize; id++) {
            directTarget.addEntityFrom(id, directSource, added);
            directSource.removeEntity(id);
        }
        for (int id = 0; id < batchSize; id++) {
            directSource.addEntityFrom(id, directTarget, null);
            directTarget.removeEntity(id);
        }
        return directSource.getEntityID(batchSize - 1);
    }

    @Benchmark
    public int rebuildingManagerRoundTrip() {
        for (CleanEntityHandle handle : rebuildingHandles) {
            if (!handle.tryAddComponent(added)) throw new IllegalStateException("ADD rejected");
        }
        rebuildingManager.flush();
        for (CleanEntityHandle handle : rebuildingHandles) {
            if (!handle.tryRemoveComponent(Added.class)) throw new IllegalStateException("REMOVE rejected");
        }
        rebuildingManager.flush();
        return rebuildingHandles[batchSize - 1].index;
    }

    @Benchmark
    public int managerRoundTrip() {
        for (CleanEntityHandle handle : handles) {
            if (!handle.tryAddComponent(added)) throw new IllegalStateException("ADD rejected");
        }
        manager.flush();
        for (CleanEntityHandle handle : handles) {
            if (!handle.tryRemoveComponent(Added.class)) throw new IllegalStateException("REMOVE rejected");
        }
        manager.flush();
        return handles[batchSize - 1].index;
    }

    @TearDown
    public void verify() {
        for (int id = 0; id < batchSize; id++) {
            if (!Arrays.equals(expected, registry.flattenComponent(rebuildingSource.getComponent(id, valueType)))
                || !Arrays.equals(expected, registry.flattenComponent(handles[id].readComponent(valueType).orElseThrow()))
                || !Arrays.equals(expected, registry.flattenComponent(directSource.getComponent(id, valueType)))
                || !Arrays.equals(expected, registry.flattenComponent(rebuildingHandles[id].readComponent(valueType).orElseThrow()))
                || rebuildingTarget.containsEntity(id) || directTarget.containsEntity(id)
                || handles[id].contains(Added.class) || rebuildingHandles[id].contains(Added.class)) {
                throw new IllegalStateException("Migration changed component values or membership.");
            }
        }
    }

    private static final class RebuildingPool extends ArchetypeDataPool {
        private final HeapPool delegate;

        RebuildingPool(ComponentRegistry registry, List<Class<? extends CleanComponent>> types) {
            super(registry, types, 100);
            delegate = new HeapPool(registry, types, 100);
        }

        @Override
        void addEntityFrom(int id, ArchetypeDataPool source, CleanComponent addition) {
            CleanComponent[] values = new CleanComponent[components.size()];
            for (int i = 0; i < values.length; i++) {
                Class<? extends CleanComponent> type = components.get(i);
                values[i] = addition != null && type == addition.getClass() ? addition : source.getComponent(id, type);
            }
            delegate.addEntity(id, values);
        }
        @Override public boolean containsEntity(int id) { return delegate.containsEntity(id); }
        @Override int getEntityRow(int id) { return delegate.getEntityRow(id); }
        @Override public CleanComponent getComponent(int id, Class<? extends CleanComponent> type) { return delegate.getComponent(id, type); }
        @Override public void setComponent(int id, CleanComponent value) { delegate.setComponent(id, value); }
        @Override public void addEntity(int id, CleanComponent[] values) { delegate.addEntity(id, values); }
        @Override public void removeEntity(int id) { delegate.removeEntity(id); }
        @Override public com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray getArray(Class<? extends CleanComponent> type, String... path) { return delegate.getArray(type, path); }
        @Override public PoolRows getRows() { return delegate.getRows(); }
        @Override public int getEntityID(int row) { return delegate.getEntityID(row); }
        @Override public String getSnapshot() { return delegate.getSnapshot(); }
        @Override public void beginExecution() { delegate.beginExecution(); }
        @Override public void endExecution() { delegate.endExecution(); }
        @Override void trimCapacity() { delegate.trimCapacity(); }
    }

    public static class Scalars implements CleanComponent {
        public byte b; public short s; public int id; public long l;
        public float f; public double d; public boolean flag;
    }
    public static class Child {
        public int id = 37;
        public Vector3f position = new Vector3f(1, 2, 3);
        public Matrix4f matrix = new Matrix4f().translation(4, 5, 6);
    }
    public static class Wide extends Scalars {
        public Vector2f v2 = new Vector2f(7, 8);
        public Vector3f v3 = new Vector3f(9, 10, 11);
        public Vector4f v4 = new Vector4f(12, 13, 14, 15);
        public Matrix3f m3 = new Matrix3f().rotationX(0.25f);
        public Matrix4f m4 = new Matrix4f().translation(16, 17, 18);
        public Child a = new Child();
        public Child c = new Child();
    }
    public static class Added implements CleanComponent { public int value; }
}
