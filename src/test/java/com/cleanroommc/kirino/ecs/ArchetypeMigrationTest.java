package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;
import org.joml.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ArchetypeMigrationTest {
    @Test
    void transfer_allTypesAndNestedLayouts_preservesValuesAndRawFloatingBitsAtDifferentRows() {
        ComponentRegistry registry = registry();
        HeapPool source = new HeapPool(registry, List.of(Value.class, Marker.class), 0);
        HeapPool target = new HeapPool(registry, List.of(Added.class, Marker.class, Value.class), 0);
        source.addEntity(0, new CleanComponent[]{value(1), new Marker()});
        source.addEntity(Integer.MIN_VALUE, new CleanComponent[]{value(2), new Marker()});
        target.addEntity(100, new CleanComponent[]{added(100), new Marker(), value(100)});
        target.addEntity(101, new CleanComponent[]{added(101), new Marker(), value(101)});
        target.addEntity(102, new CleanComponent[]{added(102), new Marker(), value(102)});
        target.removeEntity(101);
        PrimitiveArray f = source.getArray(Value.class, "f");
        PrimitiveArray d = source.getArray(Value.class, "d");
        int floatBits = 0x7fc01234;
        long doubleBits = 0x7ff8123456789abcL;
        f.setFloat(0, Float.intBitsToFloat(floatBits));
        d.setDouble(0, Double.longBitsToDouble(doubleBits));
        Object[] expected = registry.flattenComponent(source.getComponent(0, Value.class));
        target.addEntityFrom(0, source, added(7));
        assertEquals(0, target.getEntityID(1));
        assertTrue(source.containsEntity(0));
        assertEquals(floatBits, Float.floatToRawIntBits(target.getArray(Value.class, "f").getFloat(1)));
        assertEquals(doubleBits, Double.doubleToRawLongBits(target.getArray(Value.class, "d").getDouble(1)));
        assertArrayEquals(expected, registry.flattenComponent(target.getComponent(0, Value.class)));
        assertEquals(7, ((Added) target.getComponent(0, Added.class)).value);
        assertEquals(100, ((Added) target.getComponent(100, Added.class)).value);
        source.removeEntity(0);
        source.addEntityFrom(0, target, null);
        target.removeEntity(0);
        assertEquals(0, source.getEntityID(0));
        assertArrayEquals(expected, registry.flattenComponent(source.getComponent(0, Value.class)));
        assertArrayEquals(registry.flattenComponent(value(2)),
            registry.flattenComponent(source.getComponent(Integer.MIN_VALUE, Value.class)));
    }

    @Test
    void cachedTransfer_afterBothPoolsGrowTrimAndRegrow_usesCurrentBackingArrays() {
        ComponentRegistry registry = registry();
        HeapPool source = new HeapPool(registry, List.of(Value.class), 0);
        HeapPool target = new HeapPool(registry, List.of(Added.class, Value.class), 0);
        PrimitiveArray retainedSource = source.getArray(Value.class, "i");
        PrimitiveArray retainedTarget = target.getArray(Value.class, "i");
        for (int cycle = 0; cycle < 3; cycle++) {
            for (int id = 0; id < 128; id++) source.addEntity(id, new CleanComponent[]{value(id + cycle)});
            for (int id = 0; id < 128; id++) {
                target.addEntityFrom(id, source, added(id + 1000));
                source.removeEntity(id);
            }
            source.trimCapacity();
            assertEquals(0, retainedSource.length());
            for (int id = 0; id < 128; id++) {
                assertArrayEquals(registry.flattenComponent(value(id + cycle)),
                    registry.flattenComponent(target.getComponent(id, Value.class)));
                source.addEntityFrom(id, target, null);
                target.removeEntity(id);
            }
            target.trimCapacity();
            assertEquals(0, retainedTarget.length());
            for (int id = 0; id < 128; id++) {
                assertArrayEquals(registry.flattenComponent(value(id + cycle)),
                    registry.flattenComponent(source.getComponent(id, Value.class)));
                source.removeEntity(id);
            }
            source.trimCapacity();
            assertSame(retainedSource, source.getArray(Value.class, "i"));
            assertSame(retainedTarget, target.getArray(Value.class, "i"));
        }
    }

    @Test
    void invalidTransfer_beforeRowPublication_preservesSourceDestinationAndFreeRow() {
        ComponentRegistry registry = registry();
        HeapPool source = new HeapPool(registry, List.of(Value.class), 0);
        HeapPool target = new HeapPool(registry, List.of(Added.class, Value.class), 0);
        source.addEntity(1, new CleanComponent[]{value(1)});
        target.addEntity(2, new CleanComponent[]{value(2), added(2)});
        target.addEntity(3, new CleanComponent[]{value(3), added(3)});
        target.removeEntity(2);
        int freeRow = target.getRows().nextRow();
        int capacity = target.getCurrentSize();
        Added invalid = added(1);
        invalid.value = null;
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(1, source, invalid));
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(1, source, null));
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(1, source, new Marker()));
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(3, source, added(1)));
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(99, source, added(1)));
        HeapPool differentRegistry = new HeapPool(registry(), List.of(Value.class), 0);
        differentRegistry.addEntity(1, new CleanComponent[]{value(1)});
        assertThrows(IllegalArgumentException.class, () -> target.addEntityFrom(1, differentRegistry, added(1)));
        HeapPool incompatible = new HeapPool(registry, List.of(Value.class, Added.class, Marker.class), 0);
        assertThrows(IllegalArgumentException.class, () -> incompatible.addEntityFrom(1, source, added(1)));
        assertEquals(0, incompatible.getRows().limit);
        assertEquals(capacity, target.getCurrentSize());
        assertEquals(freeRow, target.getRows().nextRow());
        assertFalse(target.containsEntity(1));
        assertTrue(source.containsEntity(1));
        assertArrayEquals(registry.flattenComponent(value(1)), registry.flattenComponent(source.getComponent(1, Value.class)));
        assertArrayEquals(registry.flattenComponent(value(3)), registry.flattenComponent(target.getComponent(3, Value.class)));
        target.addEntityFrom(1, source, added(9));
        assertEquals(1, target.getEntityID(freeRow));
    }

    @Test
    void transfer_activeSourceOrDestination_rejectsWithoutPublishingRows() {
        ComponentRegistry registry = registry();
        HeapPool source = new HeapPool(registry, List.of(Value.class), 0);
        HeapPool target = new HeapPool(registry, List.of(Added.class, Value.class), 0);
        source.addEntity(1, new CleanComponent[]{value(1)});
        source.beginExecution();
        assertThrows(IllegalStateException.class, () -> target.addEntityFrom(1, source, added(1)));
        source.endExecution();
        target.beginExecution();
        assertThrows(IllegalStateException.class, () -> target.addEntityFrom(1, source, added(1)));
        target.endExecution();
        assertFalse(target.containsEntity(1));
        assertEquals(0, target.getRows().limit);
        target.addEntityFrom(1, source, added(1));
        assertEquals(1, target.getEntityID(0));
    }

    @Test
    void manager_chainedCommandsAndDestroyCallback_preserveFinalValuesWithoutConstructingCopies() {
        ComponentRegistry registry = registry();
        EntityManager manager = new EntityManager(registry);
        AtomicInteger creates = new AtomicInteger();
        AtomicInteger destroys = new AtomicInteger();
        Value original = value(7);
        Value replacement = value(11);
        Object[] expected = registry.flattenComponent(replacement);
        CleanEntityHandle handle = manager.createEntity(context -> {
            assertArrayEquals(expected, registry.flattenComponent(context.getComponent(Value.class)));
            assertEquals(19, context.getComponent(Added.class).value);
            destroys.incrementAndGet();
        }, context -> {
            context.getComponent(Value.class).i = 91;
            creates.incrementAndGet();
        }, original);
        assertTrue(handle.tryAddComponent(added(13)));
        assertTrue(handle.trySetComponent(replacement));
        assertTrue(handle.tryRemoveComponent(Added.class));
        assertTrue(handle.tryAddComponent(added(19)));
        Value.rejectConstruction = true;
        Child.rejectConstruction = true;
        try {
            manager.flush();
            assertTrue(handle.isAlive());
            assertEquals(1, creates.get());
            assertEquals(0, destroys.get());
            assertTrue(handle.tryRemoveComponent(Added.class));
            assertTrue(handle.tryAddComponent(added(19)));
            manager.flush();
        } finally {
            Value.rejectConstruction = false;
            Child.rejectConstruction = false;
        }
        Value copy = handle.readComponent(Value.class).orElseThrow();
        assertArrayEquals(expected, registry.flattenComponent(copy));
        copy.first.position.x = 999;
        assertArrayEquals(expected, registry.flattenComponent(handle.readComponent(Value.class).orElseThrow()));
        assertTrue(handle.tryDestroy());
        manager.flush();
        assertEquals(1, destroys.get());
        assertFalse(handle.valid());
    }

    @Test
    void manager_invalidatedDeferredAddition_failsBeforeRemovingOldEntity() {
        ComponentRegistry registry = registry();
        EntityManager manager = new EntityManager(registry);
        CleanEntityHandle handle = manager.createEntity(value(17));
        manager.flush();
        ArchetypeDataPool source = manager.startQuery(manager.newQuery().with(Value.class)).getFirst();
        Added invalidated = added(1);
        assertTrue(handle.tryAddComponent(invalidated));
        invalidated.value = null;
        assertThrows(IllegalArgumentException.class, manager::flush);
        assertTrue(source.containsEntity(handle.index));
        assertArrayEquals(registry.flattenComponent(value(17)), registry.flattenComponent(source.getComponent(handle.index, Value.class)));
        assertThrows(IllegalStateException.class, manager::flush);
    }

    @Test
    void zeroColumnComponents_andEmptyArchetype_transferMembershipWithoutPayloadCopies() {
        ComponentRegistry registry = registry();
        EntityManager manager = new EntityManager(registry);
        CleanEntityHandle entity = manager.createEntity();
        assertTrue(entity.tryAddComponent(new Marker()));
        assertTrue(entity.tryRemoveComponent(Marker.class));
        assertTrue(entity.tryAddComponent(new Marker()));
        manager.flush();
        assertTrue(entity.isAlive());
        assertTrue(entity.contains(Marker.class));
        assertTrue(entity.tryRemoveComponent(Marker.class));
        manager.flush();
        assertFalse(entity.contains(Marker.class));
        assertTrue(entity.isAlive());
    }

    private static ComponentRegistry registry() {
        StructRegistry structs = new StructRegistry();
        structs.registerStructType("child", Child.class, new MemberLayout("id", "position", "matrix"),
            new StructDef(new FieldDef(ScalarType.INT), new FieldDef(ScalarType.VEC3), new FieldDef(ScalarType.MAT4)));
        structs.lock();
        FieldRegistry fields = new FieldRegistry(structs);
        for (ScalarType type : ScalarType.values()) fields.registerFieldType(type.name(), type.clazz, new FieldDef(type));
        fields.registerFieldType("child", Child.class, new FieldDef("child"));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("value", Value.class,
            new MemberLayout("b", "s", "i", "l", "f", "d", "flag", "v2", "v3", "v4", "m3", "m4", "first", "second"),
            "BYTE", "SHORT", "INT", "LONG", "FLOAT", "DOUBLE", "BOOL", "VEC2", "VEC3", "VEC4", "MAT3", "MAT4", "child", "child");
        registry.registerComponent("added", Added.class, new MemberLayout("value"), "INT");
        registry.registerComponent("marker", Marker.class, new MemberLayout());
        registry.lock();
        return registry;
    }

    private static Value value(int seed) {
        Value value = new Value();
        value.b = (byte) seed; value.s = (short) (seed * 3); value.i = seed * 5;
        value.l = Long.MIN_VALUE + seed; value.f = -0.0f; value.d = Double.NEGATIVE_INFINITY;
        value.flag = seed % 2 == 0;
        value.v2.set(seed, -seed); value.v3.set(seed + 1, seed + 2, seed + 3);
        value.v4.set(seed, 2, 3, 4); value.m3.rotationX(seed * 0.1f); value.m4.translation(seed, 9, 10);
        value.first.id = seed; value.second.id = -seed;
        value.first.position.set(seed, 8, 9); value.second.matrix.translation(3, seed, 7);
        return value;
    }

    private static Added added(int value) {
        Added added = new Added(); added.value = value; return added;
    }

    public static class Child {
        static boolean rejectConstruction;
        public int id;
        public Vector3f position = new Vector3f();
        public Matrix4f matrix = new Matrix4f();
        public Child() {
            if (rejectConstruction) throw new AssertionError("Nested component reconstruction during migration");
        }
    }
    public static class Value implements CleanComponent {
        static boolean rejectConstruction;
        public byte b; public short s; public int i; public long l; public float f; public double d; public boolean flag;
        public Vector2f v2 = new Vector2f();
        public Vector3f v3 = new Vector3f();
        public Vector4f v4 = new Vector4f();
        public Matrix3f m3 = new Matrix3f();
        public Matrix4f m4 = new Matrix4f();
        public Child first = new Child();
        public Child second = new Child();
        public Value() {
            if (rejectConstruction) throw new AssertionError("Component reconstruction during migration");
        }
    }
    public static class Added implements CleanComponent { public Integer value; }
    public static class Marker implements CleanComponent {}
}
