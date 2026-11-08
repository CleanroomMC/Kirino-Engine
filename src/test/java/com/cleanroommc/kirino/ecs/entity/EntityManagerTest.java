package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldDef;
import com.cleanroommc.kirino.ecs.component.schema.def.field.FieldRegistry;
import com.cleanroommc.kirino.ecs.component.schema.def.field.scalar.ScalarType;
import com.cleanroommc.kirino.ecs.component.schema.def.field.struct.StructRegistry;
import com.cleanroommc.kirino.ecs.component.schema.meta.MemberLayout;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {
    @Test
    void lifecycle_reservedCommittedDestroyedAndReused_preservesIdentityAndVisibility() {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle first = entities.createEntity(position(7));
        assertTrue(first.valid());
        assertFalse(first.isAlive());
        assertTrue(entities.startQuery(entities.newQuery()).isEmpty());
        entities.flush();
        assertTrue(first.isAlive());
        ArchetypeDataPool pool = pool(entities, first.index, Position.class);
        assertEquals(7, ((Position) pool.getComponent(first.index, Position.class)).value);
        assertTrue(first.tryDestroy());
        assertFalse(first.valid());
        assertFalse(first.isAlive());
        assertTrue(pool.containsEntity(first.index));
        assertFalse(first.tryDestroy());
        assertThrows(IllegalStateException.class, () -> entities.destroyEntity(first.index));
        entities.flush();
        assertFalse(pool.containsEntity(first.index));
        CleanEntityHandle second = entities.createEntity(position(9));
        assertEquals(first.index, second.index);
        assertEquals(first.generation + 1, second.generation);
        assertFalse(first.trySetComponent(position(99)));
        assertFalse(first.tryAddComponent(extra(99)));
        assertFalse(first.tryRemoveComponent(Position.class));
        assertFalse(first.tryDestroy());
        entities.flush();
        assertEquals(9, ((Position) pool.getComponent(second.index, Position.class)).value);
    }

    @Test
    void callbacks_createAndDestroyBeforeFirstFlush_preserveOrderAndPreparedValues() {
        EntityManager entities = new EntityManager(registry());
        List<String> events = new ArrayList<>();
        EntityCreateContext[] create = new EntityCreateContext[1];
        EntityDestroyContext[] destroy = new EntityDestroyContext[1];
        CleanEntityHandle handle = entities.createEntity(context -> {
            events.add("destroy:" + ((Position) context.getComponent(Position.class)).value);
            destroy[0] = context;
        }, context -> {
            events.add("create");
            ((Position) context.getComponent(Position.class)).value = 42;
            create[0] = context;
        }, position(1));
        assertTrue(handle.tryDestroy());
        entities.flush();
        assertEquals(List.of("create", "destroy:42"), events);
        assertThrows(IllegalArgumentException.class, () -> create[0].getComponent(Position.class));
        assertThrows(IllegalArgumentException.class, () -> destroy[0].getComponent(Position.class));
        assertFalse(handle.valid());
        CleanEntityHandle replacement = entities.createEntity(position(2));
        entities.flush();
        assertEquals(handle.index, replacement.index);
        assertEquals(2, events.size());
    }

    @Test
    void commandSequence_addSetRemoveAndReadd_usesProjectedComposition() {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle handle = entities.createEntity(position(1));
        assertTrue(handle.tryAddComponent(extra(2)));
        assertTrue(handle.trySetComponent(extra(3)));
        assertFalse(handle.tryAddComponent(extra(4)));
        assertTrue(handle.tryRemoveComponent(Position.class));
        assertFalse(handle.trySetComponent(position(5)));
        assertFalse(handle.tryRemoveComponent(Position.class));
        assertTrue(handle.tryAddComponent(position(6)));
        entities.flush();
        ArchetypeDataPool pool = pool(entities, handle.index, Position.class);
        assertEquals(6, ((Position) pool.getComponent(handle.index, Position.class)).value);
        assertEquals(3, ((PositionExtra) pool.getComponent(handle.index, PositionExtra.class)).value);
        assertTrue(handle.tryRemoveComponent(Position.class));
        assertTrue(handle.tryRemoveComponent(PositionExtra.class));
        entities.flush();
        assertTrue(pool(entities, handle.index).containsEntity(handle.index));
        assertTrue(handle.isAlive());
        assertTrue(handle.tryDestroy());
        entities.flush();
    }

    @Test
    void migration_existingArchetypeAndDifferentComponentOrder_preservesOtherEntities() {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle first = entities.createEntity(position(10), extra(20));
        CleanEntityHandle second = entities.createEntity(extra(30), position(40));
        CleanEntityHandle third = entities.createEntity(position(50));
        entities.flush();
        ArchetypeDataPool common = pool(entities, first.index, PositionExtra.class);
        assertTrue(common.containsEntity(second.index));
        assertTrue(third.tryAddComponent(extra(60)));
        assertTrue(first.tryRemoveComponent(PositionExtra.class));
        entities.flush();
        assertTrue(common.containsEntity(third.index));
        assertFalse(common.containsEntity(first.index));
        assertEquals(40, ((Position) common.getComponent(second.index, Position.class)).value);
        assertEquals(30, ((PositionExtra) common.getComponent(second.index, PositionExtra.class)).value);
        assertEquals(60, ((PositionExtra) common.getComponent(third.index, PositionExtra.class)).value);
        assertEquals(10, ((Position) pool(entities, first.index).getComponent(first.index, Position.class)).value);
    }

    @Test
    void validation_invalidCreateAndAdd_doesNotReserveIdsOrChangeComposition() {
        EntityManager entities = new EntityManager(registry());
        assertThrows(IllegalArgumentException.class, () -> entities.createEntity(position(1), position(2)));
        assertThrows(IllegalArgumentException.class, () -> entities.createEntity(new Unknown()));
        assertThrows(IllegalArgumentException.class, () -> entities.createEntity(new Boxed()));
        assertThrows(NullPointerException.class, () -> entities.createEntity((CleanComponent) null));
        CleanEntityHandle valid = entities.createEntity(position(7));
        assertEquals(0, valid.index);
        assertThrows(IllegalArgumentException.class, () -> valid.tryAddComponent(new Unknown()));
        assertThrows(IllegalArgumentException.class, () -> valid.tryAddComponent(new Boxed()));
        assertFalse(valid.trySetComponent(extra(9)));
        entities.flush();
        assertTrue(valid.isAlive());
        assertEquals(7, ((Position) pool(entities, valid.index).getComponent(valid.index, Position.class)).value);
    }

    @Test
    void payload_callerReplacesArrayEntry_keepsAcceptedComponentReferences() {
        EntityManager entities = new EntityManager(registry());
        CleanComponent[] values = {position(17)};
        CleanEntityHandle handle = entities.createEntity(values);
        values[0] = extra(99);
        entities.flush();
        assertEquals(17, ((Position) pool(entities, handle.index).getComponent(handle.index, Position.class)).value);
    }

    @Test
    void flush_callbackFailure_stopsManagerWithoutReplayingCommittedCommands() {
        EntityManager entities = new EntityManager(registry());
        AtomicInteger creates = new AtomicInteger();
        CleanEntityHandle first = entities.createEntity(null, context -> creates.incrementAndGet(), position(1));
        RuntimeException failure = new IllegalStateException("callback failed");
        entities.createEntity(null, context -> { throw failure; }, position(2));
        assertSame(failure, assertThrows(IllegalStateException.class, entities::flush));
        assertEquals(1, creates.get());
        assertSame(failure, assertThrows(IllegalStateException.class, entities::flush).getCause());
        assertThrows(IllegalStateException.class, () -> entities.createEntity(position(3)));
        assertThrows(IllegalStateException.class, () -> entities.startQuery(entities.newQuery()));
        assertThrows(IllegalStateException.class, first::tryDestroy);
        assertFalse(first.valid());
        assertFalse(first.isAlive());
        assertEquals(1, creates.get());
    }

    @Test
    void flush_reentrantCallbackCommands_areRejectedBeforeChangingQueue() {
        EntityManager entities = new EntityManager(registry());
        entities.createEntity(null, context -> {
            assertThrows(IllegalStateException.class, () -> entities.createEntity(position(9)));
            assertThrows(IllegalStateException.class, entities::flush);
        }, position(5));
        entities.flush();
        CleanEntityHandle next = entities.createEntity(position(6));
        assertEquals(1, next.index);
        entities.flush();
    }

    @Test
    void flush_checkedCallbackFailure_preservesCauseAndStopsManager() {
        EntityManager entities = new EntityManager(registry());
        Exception failure = new Exception("checked callback failure");
        AtomicInteger calls = new AtomicInteger();
        CleanEntityHandle handle = entities.createEntity(null, context -> {
            calls.incrementAndGet();
            EntityManagerTest.<RuntimeException>raise(failure);
        }, position(1));

        assertSame(failure, assertThrows(CompletionException.class, entities::flush).getCause());
        assertSame(failure, assertThrows(IllegalStateException.class, entities::flush).getCause());
        assertFalse(handle.valid());
        assertThrows(IllegalStateException.class, () -> entities.createEntity(position(2)));
        assertEquals(1, calls.get());
    }

    @Test
    void migration_destinationFailure_preservesOldRowAndStopsFurtherCommit() {
        ComponentRegistry registry = registry();
        EntityManager entities = new EntityManager(registry) {
            @Override
            protected ArchetypeDataPool createPool(List<Class<? extends CleanComponent>> components) {
                if (components.contains(PositionExtra.class)) {
                    throw new IllegalStateException("destination failed");
                }
                return super.createPool(components);
            }
        };
        CleanEntityHandle handle = entities.createEntity(position(19));
        entities.flush();
        ArchetypeDataPool old = pool(entities, handle.index);
        assertTrue(handle.tryAddComponent(extra(21)));
        assertThrows(IllegalStateException.class, entities::flush);
        assertTrue(old.containsEntity(handle.index));
        assertEquals(19, ((Position) old.getComponent(handle.index, Position.class)).value);
        assertThrows(IllegalStateException.class, entities::flush);
    }

    @Test
    void executionScopes_activeAndNested_rejectFlushWithoutConsumingCommands() {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle handle = entities.createEntity(position(1));
        entities.flush();
        EntityManager.ExecutionScope first = entities.beginExecution(entities.newQuery());
        EntityManager.ExecutionScope second = entities.beginExecution(entities.newQuery());
        assertTrue(handle.trySetComponent(position(2)));
        assertThrows(IllegalStateException.class, entities::flush);
        first.close();
        first.close();
        assertThrows(IllegalStateException.class, entities::flush);
        second.close();
        entities.flush();
        assertEquals(2, ((Position) pool(entities, handle.index).getComponent(handle.index, Position.class)).value);
    }

    @Test
    void concurrentDestroy_sameGeneration_acceptsExactlyOnce() throws Exception {
        EntityManager entities = new EntityManager(registry());
        AtomicInteger callbacks = new AtomicInteger();
        CleanEntityHandle handle = entities.createEntity(context -> callbacks.incrementAndGet(), null, position(3));
        entities.flush();
        CountDownLatch gate = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(8)) {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 32; i++) {
                results.add(executor.submit(() -> {
                    gate.await();
                    return handle.tryDestroy();
                }));
            }
            gate.countDown();
            int accepted = 0;
            for (Future<Boolean> result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }
            assertEquals(1, accepted);
        }
        entities.flush();
        assertEquals(1, callbacks.get());
    }

    @Test
    void concurrentCreate_multipleProducers_preservesUniqueIdsAndAllCommands() throws Exception {
        EntityManager entities = new EntityManager(registry());
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Future<CleanEntityHandle>> results = new ArrayList<>();
            for (int i = 0; i < 128; i++) {
                int value = i;
                results.add(executor.submit(() -> entities.createEntity(position(value))));
            }
            List<CleanEntityHandle> handles = new ArrayList<>();
            for (Future<CleanEntityHandle> result : results) {
                handles.add(result.get(10, TimeUnit.SECONDS));
            }
            assertEquals(128, handles.stream().map(handle -> handle.index).distinct().count());
            entities.flush();
            for (int i = 0; i < handles.size(); i++) {
                CleanEntityHandle handle = handles.get(i);
                assertTrue(handle.isAlive());
                assertEquals(i, ((Position) pool(entities, handle.index).getComponent(handle.index, Position.class)).value);
            }
        }
    }

    @Test
    void queries_componentNamePrefix_matchesExactClassOnly() {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle handle = entities.createEntity(extra(1));
        entities.flush();
        assertTrue(entities.startQuery(entities.newQuery().with(Position.class)).isEmpty());
        assertTrue(entities.startQuery(entities.newQuery().without(Position.class)).getFirst().containsEntity(handle.index));
    }

    @Test
    void destroy_previouslyAcceptedCommands_areAppliedBeforeCallback() {
        EntityManager entities = new EntityManager(registry());
        List<Integer> destroyed = new ArrayList<>();
        CleanEntityHandle handle = entities.createEntity(context -> {
            destroyed.add(((Position) context.getComponent(Position.class)).value);
            destroyed.add(((PositionExtra) context.getComponent(PositionExtra.class)).value);
        }, null, position(1));
        assertTrue(handle.trySetComponent(position(2)));
        assertTrue(handle.tryAddComponent(extra(3)));
        assertTrue(handle.tryDestroy());
        assertFalse(handle.trySetComponent(position(4)));
        entities.flush();
        assertEquals(List.of(2, 3), destroyed);
    }

    @Test
    void executionScope_querySelectionAndGuard_areAtomicWithFlush() throws Exception {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle handle = entities.createEntity(position(1));
        entities.flush();
        CountDownLatch selected = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            Future<?> execution = executor.submit(() -> {
                try (var scope = entities.beginExecution(entities.newQuery())) {
                    assertEquals(1, scope.pools().size());
                    selected.countDown();
                    assertTrue(release.await(10, TimeUnit.SECONDS));
                    assertEquals(1, ((Position) scope.pools().getFirst().getComponent(handle.index, Position.class)).value);
                } catch (InterruptedException exception) {
                    throw new AssertionError(exception);
                }
            });
            try {
                assertTrue(selected.await(10, TimeUnit.SECONDS));
                assertTrue(handle.trySetComponent(position(2)));
                assertThrows(IllegalStateException.class, entities::flush);
            } finally {
                release.countDown();
            }
            execution.get(10, TimeUnit.SECONDS);
        }
        entities.flush();
        assertEquals(2, ((Position) pool(entities, handle.index).getComponent(handle.index, Position.class)).value);
    }

    @Test
    void generation_exhaustedSlot_isRetiredInsteadOfWrapping() throws Exception {
        EntityManager entities = new EntityManager(registry());
        CleanEntityHandle first = entities.createEntity(position(1));
        entities.flush();
        var slotsField = EntityManager.class.getDeclaredField("slots");
        slotsField.setAccessible(true);
        Object slot = ((Object[]) slotsField.get(entities))[first.index];
        var generationField = slot.getClass().getDeclaredField("generation");
        generationField.setAccessible(true);
        generationField.setInt(slot, Integer.MAX_VALUE);
        CleanEntityHandle last = new CleanEntityHandle(entities, first.index, Integer.MAX_VALUE);
        assertFalse(first.valid());
        assertTrue(last.tryDestroy());
        entities.flush();
        assertFalse(last.valid());
        CleanEntityHandle next = entities.createEntity(position(2));
        assertNotEquals(first.index, next.index);
        entities.flush();
        assertTrue(next.isAlive());
    }

    @Test
    void componentMetadata_sharedByArchetype_cannotBeMutatedByCaller() {
        class Exposed extends EntityManager {
            Exposed() { super(registry()); }
            List<Class<? extends CleanComponent>> types(int index) { return getComponentTypes(index); }
        }
        Exposed entities = new Exposed();
        CleanEntityHandle first = entities.createEntity(position(1), extra(2));
        CleanEntityHandle second = entities.createEntity(extra(3), position(4));
        assertSame(entities.types(first.index), entities.types(second.index));
        assertThrows(UnsupportedOperationException.class, () -> entities.types(first.index).clear());
    }

    @Test
    void handleEquality_differentManagersWithEqualValues_preservesOwnerIdentity() {
        class EqualManager extends EntityManager {
            EqualManager() { super(registry()); }
            @Override public boolean equals(Object other) { return other instanceof EqualManager; }
            @Override public int hashCode() { return 1; }
        }
        EntityManager firstManager = new EqualManager();
        EntityManager secondManager = new EqualManager();
        CleanEntityHandle first = firstManager.createEntity(position(1));
        CleanEntityHandle second = secondManager.createEntity(position(2));
        assertEquals(firstManager, secondManager);
        assertNotEquals(first, second);
        CleanEntityHandle copy = new CleanEntityHandle(firstManager, first.index, first.generation);
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void raise(Throwable failure) throws T {
        throw (T) failure;
    }

    private static ArchetypeDataPool pool(EntityManager entities, int index,
                                          Class<? extends CleanComponent>... components) {
        EntityQuery query = entities.newQuery();
        for (Class<? extends CleanComponent> component : components) {
            query.with(component);
        }
        return entities.startQuery(query).stream().filter(candidate -> candidate.containsEntity(index)).findFirst().orElseThrow();
    }

    private static ComponentRegistry registry() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry registry = new ComponentRegistry(fields);
        registry.registerComponent("position", Position.class, new MemberLayout("value"), "int");
        registry.registerComponent("extra", PositionExtra.class, new MemberLayout("value"), "int");
        registry.registerComponent("boxed", Boxed.class, new MemberLayout("value"), "int");
        registry.lock();
        return registry;
    }

    private static Position position(int value) {
        Position result = new Position();
        result.value = value;
        return result;
    }

    private static PositionExtra extra(int value) {
        PositionExtra result = new PositionExtra();
        result.value = value;
        return result;
    }

    public static class Position implements CleanComponent { public int value; }
    public static class PositionExtra implements CleanComponent { public int value; }
    public static class Boxed implements CleanComponent { public Integer value; }
    public static class Unknown implements CleanComponent {}
}
