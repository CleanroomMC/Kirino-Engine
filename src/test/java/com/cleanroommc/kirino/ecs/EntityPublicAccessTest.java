package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityQuery;

import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.api.job.ParallelJob;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class EntityPublicAccessTest {
    @Test
    void contains_pendingChanges_useProjectedCompositionWhileReadsUseCommittedValues() {
        EntityManager entities = entities();
        CleanEntityHandle handle = entities.createEntity(position(7));
        assertTrue(handle.contains(Position.class));
        assertFalse(handle.contains(Extra.class));
        assertTrue(handle.readComponent(Position.class).isEmpty());
        entities.flush();

        assertTrue(handle.trySetComponent(position(9)));
        assertTrue(handle.tryAddComponent(extra(11)));
        assertTrue(handle.tryRemoveComponent(Position.class));
        assertFalse(handle.contains(Position.class));
        assertTrue(handle.contains(Extra.class));
        assertEquals(7, handle.readComponent(Position.class).orElseThrow().value);
        assertTrue(handle.readComponent(Extra.class).isEmpty());

        entities.flush();
        assertTrue(handle.readComponent(Position.class).isEmpty());
        assertEquals(11, handle.readComponent(Extra.class).orElseThrow().value);
    }

    @Test
    void readComponent_returnsIndependentCopiesAndDoesNotApplyWrites() {
        EntityManager entities = entities();
        CleanEntityHandle handle = entities.createEntity(position(3));
        entities.flush();

        Position first = handle.readComponent(Position.class).orElseThrow();
        first.value = 99;
        Position second = handle.readComponent(Position.class).orElseThrow();

        assertNotSame(first, second);
        assertEquals(3, second.value);
        assertTrue(handle.trySetComponent(first));
        assertEquals(3, handle.readComponent(Position.class).orElseThrow().value);
        entities.flush();
        assertEquals(99, handle.readComponent(Position.class).orElseThrow().value);
    }

    @Test
    void readComponent_activeExecutionIsRejectedWhileMetadataLookupRemainsAvailable() {
        EntityManager entities = entities();
        CleanEntityHandle handle = entities.createEntity(position(3));
        entities.flush();

        try (var execution = entities.beginExecution(entities.newQuery())) {
            assertTrue(handle.contains(Position.class));
            assertThrows(IllegalStateException.class, () -> handle.readComponent(Position.class));
            assertThrows(IllegalStateException.class, () -> handle.readComponent(Extra.class));
        }
        assertEquals(3, handle.readComponent(Position.class).orElseThrow().value);
    }

    @Test
    void readAndContains_destroyedOrReusedHandleDoesNotReadReplacement() {
        EntityManager entities = entities();
        CleanEntityHandle handle = entities.createEntity(position(3));
        entities.flush();
        assertTrue(handle.tryDestroy());
        assertFalse(handle.contains(Position.class));
        assertTrue(handle.readComponent(Position.class).isEmpty());
        entities.flush();

        CleanEntityHandle replacement = entities.createEntity(position(17));
        entities.flush();
        assertEquals(handle.index, replacement.index);
        assertFalse(handle.contains(Position.class));
        assertTrue(handle.readComponent(Position.class).isEmpty());
        assertEquals(17, replacement.readComponent(Position.class).orElseThrow().value);
        assertThrows(NullPointerException.class, () -> replacement.contains(null));
        assertThrows(NullPointerException.class, () -> replacement.readComponent(null));
    }

    @Test
    void publicAccess_callbackReentryIsRejectedThroughHandle() {
        EntityManager entities = entities();
        CleanEntityHandle[] handle = new CleanEntityHandle[1];
        handle[0] = entities.createEntity(null, context -> {
            assertThrows(IllegalStateException.class, () -> handle[0].contains(Position.class));
            assertThrows(IllegalStateException.class, () -> handle[0].readComponent(Position.class));
        }, position(3));

        entities.flush();
        assertTrue(handle[0].isAlive());
    }

    @Test
    void commands_selectedEntitiesApplyOrderedChangesAndRejectOtherPools() {
        EntityManager entities = entities();
        CleanEntityHandle selected = entities.createEntity(position(5));
        CleanEntityHandle excluded = entities.createEntity(extra(7));
        entities.flush();

        try (var scope = entities.beginExecution(entities.newQuery().with(Position.class))) {
            EntityCommands commands = scope.commands();
            assertFalse(EntityManager.class.isInstance(commands));
            assertFalse(commands.tryDestroy(-1));
            assertFalse(commands.tryDestroy(Integer.MAX_VALUE));
            assertFalse(commands.tryDestroy(excluded.index));
            assertFalse(commands.tryAddComponent(excluded.index, position(1)));
            assertFalse(commands.trySetComponent(excluded.index, extra(1)));
            assertFalse(commands.tryRemoveComponent(excluded.index, Extra.class));
            assertTrue(commands.trySetComponent(selected.index, position(9)));
            assertTrue(commands.tryAddComponent(selected.index, extra(11)));
            assertTrue(commands.trySetComponent(selected.index, extra(13)));
            assertFalse(commands.tryAddComponent(selected.index, extra(15)));
            assertTrue(commands.tryRemoveComponent(selected.index, Position.class));
            assertFalse(commands.trySetComponent(selected.index, position(17)));
            assertTrue(commands.tryAddComponent(selected.index, position(19)));
        }

        entities.flush();
        assertEquals(19, selected.readComponent(Position.class).orElseThrow().value);
        assertEquals(13, selected.readComponent(Extra.class).orElseThrow().value);
        assertEquals(7, excluded.readComponent(Extra.class).orElseThrow().value);
    }

    @Test
    void commands_deferredCreateKeepsOwnershipAndCallbackOrder() {
        EntityManager entities = entities();
        List<Integer> events = new java.util.ArrayList<>();
        com.cleanroommc.kirino.ecs.api.CleanEntityHandle created;
        try (var scope = entities.beginExecution(entities.newQuery())) {
            CleanComponent[] input = {position(5)};
            created = scope.commands().createEntity(context -> events.add(2), context -> {
                events.add(1);
                ((Position) context.getComponent(Position.class)).value = 7;
            }, input);
            input[0] = extra(99);
            assertTrue(created.valid());
            assertFalse(created.isAlive());
            assertFalse(scope.commands().tryDestroy(((CleanEntityHandle) created).index));
        }

        assertTrue(events.isEmpty());
        entities.flush();
        assertEquals(7, created.readComponent(Position.class).orElseThrow().value);
        assertEquals(List.of(1), events);
        assertTrue(created.tryDestroy());
        entities.flush();
        assertEquals(List.of(1, 2), events);
    }

    @Test
    void commands_concurrentDestroyAcceptsOnlyOnce() throws Exception {
        EntityManager entities = entities();
        CleanEntityHandle handle = entities.createEntity(position(3));
        entities.flush();

        try (var scope = entities.beginExecution(entities.newQuery());
             var producers = Executors.newFixedThreadPool(4)) {
            var results = new java.util.ArrayList<java.util.concurrent.Future<Boolean>>();
            for (int i = 0; i < 32; i++) {
                results.add(producers.submit(() -> scope.commands().tryDestroy(handle.index)));
            }
            int accepted = 0;
            for (var result : results) {
                if (result.get(10, TimeUnit.SECONDS)) {
                    accepted++;
                }
            }
            assertEquals(1, accepted);
            assertFalse(handle.valid());
        }
        entities.flush();
        assertFalse(handle.isAlive());
    }

    @Test
    void commands_closedExecutionRejectsEveryOperationAfterSlotReuse() {
        EntityManager entities = entities();
        CleanEntityHandle first = entities.createEntity(position(3));
        entities.flush();
        var scope = entities.beginExecution(entities.newQuery());
        EntityCommands commands = scope.commands();
        assertTrue(commands.tryDestroy(first.index));
        scope.close();
        entities.flush();
        CleanEntityHandle replacement = entities.createEntity(position(17));
        entities.flush();
        assertEquals(first.index, replacement.index);

        assertThrows(IllegalStateException.class, () -> commands.tryDestroy(first.index));
        assertThrows(IllegalStateException.class, () -> commands.trySetComponent(first.index, position(1)));
        assertThrows(IllegalStateException.class, () -> commands.tryAddComponent(first.index, extra(1)));
        assertThrows(IllegalStateException.class, () -> commands.tryRemoveComponent(first.index, Position.class));
        assertThrows(IllegalStateException.class, () -> commands.createEntity(position(1)));
        assertThrows(IllegalStateException.class, () -> commands.createEntity(null, null, new CleanComponent[]{position(1)}));
        assertEquals(17, replacement.readComponent(Position.class).orElseThrow().value);
    }

    @Test
    void jobCommandApi_doesNotExposeManagerOrStorageOperations() throws Exception {
        assertEquals(EntityCommands.class,
            ParallelJob.class.getMethod("execute", EntityCommands.class, int.class, int.class, int.class).getParameterTypes()[0]);
        assertThrows(NoSuchMethodException.class, () -> EntityCommands.class.getMethod("flush"));
        assertThrows(NoSuchMethodException.class, () -> EntityCommands.class.getMethod("startQuery", EntityQuery.class));
        assertThrows(NoSuchMethodException.class, () -> EntityManager.class.getMethod("destroyEntity", int.class));
    }

    @Test
    void executionApi_queryScopeAndPoolAccessAreNotPublicOrProtected() throws Exception {
        assertThrows(NoSuchMethodException.class, () -> EntityManager.class.getMethod("startQuery", EntityQuery.class));
        assertThrows(NoSuchMethodException.class, () -> EntityManager.class.getMethod("beginExecution", EntityQuery.class));
        for (String name : List.of("startQuery", "beginExecution")) {
            int modifiers = EntityManager.class.getDeclaredMethod(name, EntityQuery.class).getModifiers();
            assertFalse(Modifier.isPublic(modifiers));
            assertFalse(Modifier.isProtected(modifiers));
        }
        Class<?> scopeType = EntityManager.class.getDeclaredMethod("beginExecution", EntityQuery.class).getReturnType();
        assertFalse(Modifier.isPublic(scopeType.getModifiers()));
        assertFalse(Modifier.isProtected(scopeType.getModifiers()));
        assertThrows(NoSuchMethodException.class, () -> scopeType.getMethod("pools"));
        assertThrows(NoSuchMethodException.class, () -> scopeType.getMethod("commands"));
    }

    @Test
    void callbackApi_onlyExposesTypedComponentCopiesAndPrimitiveFieldValues() throws Exception {
        for (Class<?> context : List.of(EntityCreateContext.class, EntityDestroyContext.class)) {
            assertTrue(context.isInterface());
            assertEquals(0, context.getConstructors().length);
            assertEquals(context == EntityCreateContext.class ? 1 : 8, context.getDeclaredMethods().length);
            assertEquals(1, context.getMethod("getComponent", Class.class).getTypeParameters().length);
        }
        var fields = java.util.Map.of("getByte", byte.class, "getShort", short.class, "getInt", int.class,
            "getLong", long.class, "getFloat", float.class, "getDouble", double.class, "getBool", boolean.class);
        for (var field : fields.entrySet()) {
            assertEquals(field.getValue(), EntityDestroyContext.class.getMethod(field.getKey(), Class.class, String[].class).getReturnType());
        }
    }

    @Test
    void callbacks_typedAccessValidatesArgumentsAndExpiresAfterReturn() {
        EntityManager entities = entities();
        Position payload = position(5);
        EntityCreateContext[] creating = new EntityCreateContext[1];
        EntityDestroyContext[] destroying = new EntityDestroyContext[1];
        CleanEntityHandle handle = entities.createEntity(context -> {
            destroying[0] = context;
            Position first = context.getComponent(Position.class);
            assertEquals(7, first.value);
            first.value = 99;
            Position second = context.getComponent(Position.class);
            assertNotSame(first, second);
            assertEquals(7, second.value);
            assertThrows(NullPointerException.class, () -> context.getComponent(null));
            assertThrows(IllegalArgumentException.class, () -> context.getComponent(Extra.class));
        }, context -> {
            creating[0] = context;
            Position component = context.getComponent(Position.class);
            assertSame(payload, component);
            component.value = 7;
            assertThrows(NullPointerException.class, () -> context.getComponent(null));
            assertThrows(IllegalArgumentException.class, () -> context.getComponent(Extra.class));
        }, payload);

        entities.flush();
        assertEquals(7, handle.readComponent(Position.class).orElseThrow().value);
        assertThrows(IllegalStateException.class, () -> creating[0].getComponent(Position.class));
        assertTrue(handle.tryDestroy());
        entities.flush();
        assertThrows(IllegalStateException.class, () -> destroying[0].getComponent(Position.class));
    }

    @Test
    void callbacks_failureClearsCreateAndDestroyContexts() {
        EntityManager creatingEntities = entities();
        EntityCreateContext[] creating = new EntityCreateContext[1];
        RuntimeException createFailure = new IllegalStateException("create failed");
        creatingEntities.createEntity(null, context -> {
            creating[0] = context;
            throw createFailure;
        }, position(1));
        assertSame(createFailure, assertThrows(IllegalStateException.class, creatingEntities::flush));
        assertThrows(IllegalStateException.class, () -> creating[0].getComponent(Position.class));

        EntityManager destroyingEntities = entities();
        EntityDestroyContext[] destroying = new EntityDestroyContext[1];
        RuntimeException destroyFailure = new IllegalStateException("destroy failed");
        CleanEntityHandle handle = destroyingEntities.createEntity(context -> {
            destroying[0] = context;
            throw destroyFailure;
        }, null, position(1));
        destroyingEntities.flush();
        assertTrue(handle.tryDestroy());
        assertSame(destroyFailure, assertThrows(IllegalStateException.class, destroyingEntities::flush));
        assertThrows(IllegalStateException.class, () -> destroying[0].getComponent(Position.class));
    }

    private static EntityManager entities() {
        FieldRegistry fields = new FieldRegistry(new StructRegistry());
        fields.registerFieldType("int", int.class, new FieldDef(ScalarType.INT));
        fields.lock();
        ComponentRegistry components = new ComponentRegistry(fields);
        components.registerComponent("position", Position.class, new MemberLayout("value"), "int");
        components.registerComponent("extra", Extra.class, new MemberLayout("value"), "int");
        components.lock();
        return new EntityManager(components);
    }

    private static Position position(int value) {
        Position component = new Position();
        component.value = value;
        return component;
    }

    private static Extra extra(int value) {
        Extra component = new Extra();
        component.value = value;
        return component;
    }

    public static class Position implements CleanComponent {
        public int value;
    }

    public static class Extra implements CleanComponent {
        public int value;
    }
}
