package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.EntityCommands;
import com.cleanroommc.kirino.ecs.api.EntityQuery;
import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.api.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

final class EntityManager implements com.cleanroommc.kirino.ecs.api.EntityManager {
    private enum State {
        FREE, RESERVED, ALIVE, PENDING_DESTROY, RETIRED
    }

    private static final class Slot {
        int generation;
        State state = State.FREE;
        Archetype committed;
        Archetype projected;
        EntityCreateCallback createCallback;
        EntityDestroyCallback destroyCallback;
    }

    private static final class Archetype {
        final ArchetypeKey key;
        final ObjectArrayView<Class<? extends CleanComponent>> components;
        ArchetypeDataPool pool;

        @SuppressWarnings({"unchecked", "rawtypes"})
        Archetype(ArchetypeKey key, List<Class<? extends CleanComponent>> components) {
            this.key = key;
            this.components = ObjectArrayView.copyOf(components,
                size -> (Class<? extends CleanComponent>[]) new Class[size]);
        }
    }

    private final ComponentRegistry componentRegistry;
    final RuntimeLifecycle lifecycle = new RuntimeLifecycle(this);
    private final Function<List<Class<? extends CleanComponent>>, ArchetypeDataPool> poolFactory;
    private final Object2ObjectOpenHashMap<ArchetypeKey, Archetype> archetypes = new Object2ObjectOpenHashMap<>();
    private QueryCache queryCache;
    private int poolCount;
    private final ObjectArrayList<EntityCommand> commands = new ObjectArrayList<>();
    private final ReferenceOpenHashSet<ArchetypeDataPool> shrinkCandidates = new ReferenceOpenHashSet<>();
    private final IntArrayList freeSlots = new IntArrayList();
    private final CreateContext createContext = new CreateContext();
    private final DestroyContext destroyContext = new DestroyContext();
    private Slot[] slots = new Slot[16];
    private int slotCount;
    private int executions;
    private boolean flushing;
    private Throwable failure;

    EntityManager(ComponentRegistry componentRegistry) {
        this(componentRegistry, components -> new HeapPool(componentRegistry, components, 100));
    }

    EntityManager(ComponentRegistry componentRegistry,
                  Function<List<Class<? extends CleanComponent>>, ArchetypeDataPool> poolFactory) {
        this.componentRegistry = Objects.requireNonNull(componentRegistry);
        this.poolFactory = Objects.requireNonNull(poolFactory);
    }

    @NonNull
    public EntityQuery newQuery() {
        return EntityQuery.query();
    }

    @NonNull
    final synchronized List<@NonNull ArchetypeDataPool> startQuery(@NonNull EntityQuery query) {
        requireAvailable();
        Objects.requireNonNull(query);
        List<ArchetypeDataPool> result = new ArrayList<>();
        var mustHave = QueryAccess.mustHave(query);
        var mustNotHave = QueryAccess.mustNotHave(query);
        for (Archetype archetype : archetypes.values()) {
            if (archetype.pool == null) {
                continue;
            }
            boolean matches = true;
            for (Class<? extends CleanComponent> component : mustHave) {
                if (!archetype.key.contains(component)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) {
                continue;
            }
            for (Class<? extends CleanComponent> component : mustNotHave) {
                if (archetype.key.contains(component)) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                result.add(archetype.pool);
            }
        }
        return result;
    }

    final synchronized ArchetypeDataPool[] selectPools(EntityQuery query) {
        requireAvailable();
        Objects.requireNonNull(query);
        if (poolCount < QueryCache.MIN_POOLS) {
            return startQuery(query).toArray(ArchetypeDataPool[]::new);
        }

        var required = QueryAccess.mustHave(query);
        var excluded = QueryAccess.mustNotHave(query);
        if ((long) required.size() + excluded.size() > QueryCache.MAX_CONDITIONS) {
            return startQuery(query).toArray(ArchetypeDataPool[]::new);
        }
        if (queryCache == null) {
            queryCache = new QueryCache();
        }
        int hash = QueryCache.hash(required, excluded);
        ArchetypeDataPool[] pools = queryCache.find(hash, required, excluded);
        if (pools != null) {
            return pools;
        }

        pools = startQuery(query).toArray(ArchetypeDataPool[]::new);
        queryCache.store(hash, required, excluded, pools);
        return pools;
    }

    final synchronized ExecutionScope beginExecution(EntityQuery query) {
        requireAvailable();
        ExecutionScope scope = new ExecutionScope(selectPools(query));
        int acquired = 0;
        try {
            for (ArchetypeDataPool pool : scope.pools) {
                pool.beginExecution();
                acquired++;
            }
        } catch (Throwable exception) {
            for (int i = acquired - 1; i >= 0; i--) {
                try {
                    scope.pools[i].endExecution();
                } catch (Throwable releaseFailure) {
                    failure = exception;
                    if (releaseFailure != exception) {
                        exception.addSuppressed(releaseFailure);
                    }
                }
            }
            Throwables.throwIfUnchecked(exception);
            throw new CompletionException(exception);
        }
        executions++;
        return scope;
    }

    synchronized void requireLifecycleAccess() {
        Preconditions.checkState(!flushing, "Runtime lifecycle operations are forbidden inside entity callbacks.");
    }

    synchronized Throwable clearEntities(boolean reset) {
        Preconditions.checkState(executions == 0, "Cannot clear entities while jobs use their storage.");
        flushing = true;
        Throwable cleanupFailure = null;
        try {
            commands.clear();
            freeSlots.clear();
            for (int i = 0; i < slotCount; i++) {
                Slot slot = slots[i];
                try {
                    if (slot.committed != null && slot.destroyCallback != null) {
                        destroyContext.entityID = i;
                        destroyContext.pool = slot.committed.pool;
                        slot.destroyCallback.beforeDestroy(destroyContext);
                    }
                } catch (Throwable callbackFailure) {
                    cleanupFailure = ExecutionFailures.merge(cleanupFailure, callbackFailure);
                } finally {
                    destroyContext.pool = null;
                    destroyContext.entityID = -1;
                    slot.committed = null;
                    slot.projected = null;
                    slot.createCallback = null;
                    slot.destroyCallback = null;
                    if (slot.generation == Integer.MAX_VALUE || slot.state == State.RETIRED) {
                        slot.state = State.RETIRED;
                    } else {
                        slot.generation++;
                        slot.state = State.FREE;
                        freeSlots.add(i);
                    }
                }
            }
        } finally {
            commands.clear();
            commands.trim();
            shrinkCandidates.clear();
            shrinkCandidates.trim();
            archetypes.clear();
            archetypes.trim();
            if (queryCache != null) {
                queryCache.clear();
            }
            queryCache = null;
            poolCount = 0;
            createContext.components = null;
            destroyContext.pool = null;
            destroyContext.entityID = -1;
            failure = null;
            if (!reset) {
                slots = new Slot[16];
                slotCount = 0;
                freeSlots.clear();
                freeSlots.trim();
            }
            flushing = false;
        }
        return cleanupFailure;
    }

    final class ExecutionScope implements AutoCloseable {
        private final ArchetypeDataPool[] pools;
        private final EntityCommands commandWriter = new ExecutionCommands(this);
        private boolean closed;

        private ExecutionScope(ArchetypeDataPool[] pools) {
            this.pools = pools;
        }

        ArchetypeDataPool[] pools() {
            return pools;
        }

        EntityCommands commands() {
            return commandWriter;
        }

        @Override
        public void close() {
            synchronized (EntityManager.this) {
                if (closed) {
                    return;
                }
                closed = true;
                Throwable releaseFailure = null;
                for (int i = pools.length - 1; i >= 0; i--) {
                    try {
                        pools[i].endExecution();
                    } catch (Throwable exception) {
                        if (releaseFailure == null) {
                            releaseFailure = exception;
                        } else if (releaseFailure != exception) {
                            releaseFailure.addSuppressed(exception);
                        }
                    }
                }
                executions--;
                if (releaseFailure != null) {
                    failure = releaseFailure;
                    Throwables.throwIfUnchecked(releaseFailure);
                    throw new CompletionException(releaseFailure);
                }
            }
        }
    }

    private final class ExecutionCommands implements EntityCommands {
        private final ExecutionScope execution;

        private ExecutionCommands(ExecutionScope execution) {
            this.execution = execution;
        }

        @Override
        public CleanEntityHandle createEntity(CleanComponent... components) {
            return createEntity(null, null, components);
        }

        @Override
        public CleanEntityHandle createEntity(EntityDestroyCallback destroyCallback,
                                              EntityCreateCallback createCallback, CleanComponent... components) {
            synchronized (EntityManager.this) {
                requireOpen();
                return createEntityInternal(destroyCallback, createCallback, components);
            }
        }

        @Override
        public boolean tryDestroy(int entityID) {
            synchronized (EntityManager.this) {
                Slot slot = target(entityID);
                if (slot == null) {
                    return false;
                }

                enqueueDestroy(entityID, slot);
                return true;
            }
        }

        @Override
        public boolean trySetComponent(int entityID, CleanComponent component) {
            synchronized (EntityManager.this) {
                Slot slot = target(entityID);
                return slot != null && enqueueSetComponent(entityID, slot, component);
            }
        }

        @Override
        public boolean tryAddComponent(int entityID, CleanComponent component) {
            synchronized (EntityManager.this) {
                Slot slot = target(entityID);
                return slot != null && enqueueAddComponent(entityID, slot, component);
            }
        }

        @Override
        public boolean tryRemoveComponent(int entityID, Class<? extends CleanComponent> component) {
            synchronized (EntityManager.this) {
                Slot slot = target(entityID);
                return slot != null && enqueueRemoveComponent(entityID, slot, component);
            }
        }

        private Slot target(int entityID) {
            requireOpen();
            if (entityID < 0 || entityID >= slotCount) {
                return null;
            }

            Slot slot = slots[entityID];
            if (slot.state != State.ALIVE || slot.committed == null) {
                return null;
            }

            for (ArchetypeDataPool pool : execution.pools) {
                if (slot.committed.pool == pool) {
                    return slot;
                }
            }
            return null;
        }

        private void requireOpen() {
            requireHealthy();
            Preconditions.checkState(!execution.closed, "The entity command writer's execution has ended.");
        }
    }

    public synchronized void flush() {
        requireAvailable();
        if (executions != 0) {
            throw new IllegalStateException("Cannot flush while jobs use this entity manager");
        }

        int size = commands.size();

        if (size == 0) {
            return;
        }
        flushing = true;
        try {
            Object[] objElements = commands.elements();
            for (int i = 0; i < size; i++) {
                EntityCommand command = (EntityCommand) objElements[i];
                Slot slot = slot(command.index());
                if (slot.generation != command.generation()
                    || slot.state == State.FREE || slot.state == State.RETIRED) {
                    throw new IllegalStateException("Expired entity command at index=" + command.index());
                }
                switch (command.type()) {
                    case CREATE -> create(slot, command);
                    case DESTROY -> destroy(slot, command.index());
                    case SET_COM -> slot.committed.pool.setComponent(command.index(), command.component());
                    case ADD_COM, REMOVE_COM -> migrate(slot, command);
                }
            }
            if (!shrinkCandidates.isEmpty()) {
                for (ArchetypeDataPool pool : shrinkCandidates) {
                    pool.trimCapacity();
                }
            }
        } catch (Throwable exception) {
            failure = exception;
            Throwables.throwIfUnchecked(exception);
            throw new CompletionException(exception);
        } finally {
            commands.clear();
            shrinkCandidates.clear();
            createContext.components = null;
            destroyContext.pool = null;
            destroyContext.entityID = -1;
            flushing = false;
        }
    }

    private void create(Slot slot, EntityCommand command) {
        Archetype target = archetypes.get(command.target());
        ArchetypeDataPool pool = pool(target);
        if (slot.createCallback != null) {
            createContext.components = command.components().elements();
            try {
                slot.createCallback.beforeCreate(createContext);
            } finally {
                createContext.components = null;
            }
        }
        pool.addEntity(command.index(), command.components().elements());
        slot.committed = target;
        if (slot.state == State.RESERVED) {
            slot.state = State.ALIVE;
        }
        slot.createCallback = null;
    }

    private void destroy(Slot slot, int index) {
        Archetype old = slot.committed;
        if (slot.destroyCallback != null) {
            EntityDestroyCallback callback = slot.destroyCallback;
            slot.destroyCallback = null;
            destroyContext.entityID = index;
            destroyContext.pool = old.pool;
            try {
                callback.beforeDestroy(destroyContext);
            } finally {
                destroyContext.pool = null;
                destroyContext.entityID = -1;
            }
        }
        old.pool.removeEntity(index);
        shrinkCandidates.add(old.pool);
        slot.committed = null;
        slot.projected = null;
        slot.createCallback = null;
        slot.destroyCallback = null;
        if (slot.generation == Integer.MAX_VALUE) {
            slot.state = State.RETIRED;
        } else {
            slot.generation++;
            slot.state = State.FREE;
            freeSlots.add(index);
        }
    }

    private void migrate(Slot slot, EntityCommand command) {
        Archetype old = slot.committed;
        Archetype target = archetypes.get(command.target());
        pool(target).addEntityFrom(command.index(), old.pool,
            command.type() == EntityCommand.Type.ADD_COM ? command.component() : null);
        old.pool.removeEntity(command.index());
        shrinkCandidates.add(old.pool);
        slot.committed = target;
    }

    @NonNull
    public CleanEntityHandle createEntity(@NonNull CleanComponent @NonNull ... components) {
        return createEntity(null, null, components);
    }

    @NonNull
    public synchronized CleanEntityHandle createEntity(@Nullable EntityDestroyCallback destroyCallback,
                                                       @Nullable EntityCreateCallback createCallback, @NonNull CleanComponent @NonNull ... components) {
        requireAvailable();
        return createEntityInternal(destroyCallback, createCallback, components);
    }

    private CleanEntityHandle createEntityInternal(EntityDestroyCallback destroyCallback,
                                                   EntityCreateCallback createCallback, CleanComponent[] components) {
        Objects.requireNonNull(components);
        ObjectArrayView<CleanComponent> values = ObjectArrayView.copyOf(components);
        List<Class<? extends CleanComponent>> types = new ArrayList<>(values.size());
        for (CleanComponent value : values.elements()) {
            validate(value);
            types.add(value.getClass());
        }
        Archetype target = archetype(types);
        int index;
        Slot slot;
        if (freeSlots.isEmpty()) {
            index = slotCount;
            if (index == slots.length) {
                slots = Arrays.copyOf(slots, Math.multiplyExact(slots.length, 2));
            }
            slot = new Slot();
        } else {
            index = freeSlots.getInt(freeSlots.size() - 1);
            slot = slots[index];
        }
        CleanEntityHandle handle = new CleanEntityHandle(this, index, slot.generation);
        commands.add(new EntityCommand(index, slot.generation, EntityCommand.Type.CREATE,
            values, null, null, target.key));
        if (index == slotCount) {
            slots[slotCount++] = slot;
        } else {
            freeSlots.removeInt(freeSlots.size() - 1);
        }
        slot.state = State.RESERVED;
        slot.projected = target;
        slot.createCallback = createCallback;
        slot.destroyCallback = destroyCallback;
        return handle;
    }

    synchronized boolean tryDestroy(int index, int generation) {
        requireAvailable();
        Slot slot = slot(index);
        if (!acceptsCommands(slot, generation)) {
            return false;
        }
        enqueueDestroy(index, slot);
        return true;
    }

    private void enqueueDestroy(int index, Slot slot) {
        commands.add(new EntityCommand(index, slot.generation, EntityCommand.Type.DESTROY,
            null, null, null, null));
        slot.state = State.PENDING_DESTROY;
    }

    synchronized boolean trySetComponent(int index, int generation, CleanComponent component) {
        requireAvailable();
        Slot slot = slot(index);
        if (!acceptsCommands(slot, generation)) {
            return false;
        }

        return enqueueSetComponent(index, slot, component);
    }

    private boolean enqueueSetComponent(int index, Slot slot, CleanComponent component) {
        Objects.requireNonNull(component);
        if (!slot.projected.key.contains(component.getClass())) {
            return false;
        }
        validate(component);
        commands.add(new EntityCommand(index, slot.generation, EntityCommand.Type.SET_COM,
            null, component, component.getClass(), null));
        return true;
    }

    synchronized boolean tryAddComponent(int index, int generation, CleanComponent component) {
        requireAvailable();
        Slot slot = slot(index);
        if (!acceptsCommands(slot, generation)) {
            return false;
        }

        return enqueueAddComponent(index, slot, component);
    }

    private boolean enqueueAddComponent(int index, Slot slot, CleanComponent component) {
        Objects.requireNonNull(component);
        if (slot.projected.key.contains(component.getClass())) {
            return false;
        }
        validate(component);
        List<Class<? extends CleanComponent>> types = new ArrayList<>(slot.projected.components);
        types.add(component.getClass());
        Archetype target = archetype(types);
        commands.add(new EntityCommand(index, slot.generation, EntityCommand.Type.ADD_COM,
            null, component, component.getClass(), target.key));
        slot.projected = target;
        return true;
    }

    synchronized boolean tryRemoveComponent(int index, int generation, Class<? extends CleanComponent> component) {
        requireAvailable();
        Slot slot = slot(index);
        if (!acceptsCommands(slot, generation)) {
            return false;
        }

        return enqueueRemoveComponent(index, slot, component);
    }

    private boolean enqueueRemoveComponent(int index, Slot slot, Class<? extends CleanComponent> component) {
        Objects.requireNonNull(component);
        if (!slot.projected.key.contains(component)) {
            return false;
        }
        List<Class<? extends CleanComponent>> types = new ArrayList<>(slot.projected.components);
        types.removeIf(type -> type == component);
        Archetype target = archetype(types);
        commands.add(new EntityCommand(index, slot.generation, EntityCommand.Type.REMOVE_COM,
            null, null, component, target.key));
        slot.projected = target;
        return true;
    }

    synchronized boolean isValid(int index, int generation) {
        return lifecycle.state() == com.cleanroommc.kirino.ecs.api.CleanECSRuntime.State.RUNNING
            && failure == null && index >= 0 && index < slotCount && acceptsCommands(slots[index], generation);
    }

    synchronized boolean contains(int index, int generation, Class<? extends CleanComponent> component) {
        requireAvailable();
        Preconditions.checkNotNull(component);

        return isValid(index, generation) && slots[index].projected.key.contains(component);
    }

    synchronized <T extends CleanComponent> Optional<T> readComponent(int index, int generation, Class<T> component) {
        requireAvailable();
        Preconditions.checkNotNull(component);
        Preconditions.checkState(executions == 0, "Cannot read component copies while jobs use this entity manager.");

        if (!isAlive(index, generation) || !slots[index].committed.key.contains(component)) {
            return Optional.empty();
        }

        return Optional.of(component.cast(slots[index].committed.pool.getComponent(index, component)));
    }

    synchronized boolean isAlive(int index, int generation) {
        return lifecycle.state() == com.cleanroommc.kirino.ecs.api.CleanECSRuntime.State.RUNNING
            && failure == null && index >= 0 && index < slotCount
            && slots[index].generation == generation && slots[index].state == State.ALIVE;
    }

    synchronized int getLatestGeneration(int entityID) {
        return slot(entityID).generation;
    }

    synchronized List<Class<? extends CleanComponent>> getComponentTypes(int entityID) {
        Slot slot = slot(entityID);
        return slot.projected == null ? List.of() : slot.projected.components;
    }

    private ArchetypeDataPool createPool(List<Class<? extends CleanComponent>> components) {
        return poolFactory.apply(components);
    }

    private ArchetypeDataPool pool(Archetype archetype) {
        if (archetype.pool == null) {
            archetype.pool = Objects.requireNonNull(createPool(archetype.components));
            poolCount++;
            if (queryCache != null) {
                queryCache.clear();
            }
        }
        return archetype.pool;
    }

    private Archetype archetype(List<Class<? extends CleanComponent>> components) {
        ArchetypeKey key = new ArchetypeKey(components);
        Archetype archetype = archetypes.get(key);
        if (archetype == null) {
            archetype = new Archetype(key, components);
            archetypes.put(key, archetype);
        }
        return archetype;
    }

    private void validate(CleanComponent component) {
        Objects.requireNonNull(component);
        componentRegistry.getCodec(component.getClass()).validate(component);
    }

    private Slot slot(int index) {
        Objects.checkIndex(index, slotCount);
        return slots[index];
    }

    private static boolean acceptsCommands(Slot slot, int generation) {
        return slot.generation == generation && (slot.state == State.RESERVED || slot.state == State.ALIVE);
    }

    private static final class CreateContext implements EntityCreateContext {
        private CleanComponent[] components;

        @Override
        public <T extends CleanComponent> T getComponent(Class<T> component) {
            Preconditions.checkState(components != null, "The entity create context is no longer active.");
            Preconditions.checkNotNull(component);

            for (CleanComponent value : components) {
                if (value.getClass() == component) {
                    return component.cast(value);
                }
            }
            throw new IllegalArgumentException("The entity does not contain component " + component.getName() + ".");
        }
    }

    private static final class DestroyContext implements EntityDestroyContext {
        private int entityID = -1;
        private ArchetypeDataPool pool;

        @Override
        public <T extends CleanComponent> T getComponent(Class<T> component) {
            Preconditions.checkState(pool != null, "The entity destroy context is no longer active.");
            Preconditions.checkNotNull(component);
            Preconditions.checkArgument(pool.components.contains(component),
                "The entity does not contain component %s.", component.getName());

            return component.cast(pool.getComponent(entityID, component));
        }
    }

    private void requireAvailable() {
        lifecycle.requireRunning();
        requireHealthy();
    }

    private void requireHealthy() {
        if (failure != null) {
            throw new IllegalStateException("Entity manager failed during commit or execution release", failure);
        }
        if (flushing) {
            throw new IllegalStateException("Entity manager cannot be accessed from lifecycle callbacks");
        }
    }
}
