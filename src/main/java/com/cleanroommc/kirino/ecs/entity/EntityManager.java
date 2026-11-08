package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArchetypeKey;
import com.cleanroommc.kirino.ecs.storage.HeapPool;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import com.google.common.base.Throwables;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;

public class EntityManager {
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
    private final Map<ArchetypeKey, Archetype> archetypes = new Object2ObjectOpenHashMap<>();
    private final ObjectArrayList<EntityCommand> commands = new ObjectArrayList<>();
    private final IntArrayList freeSlots = new IntArrayList();
    private final EntityCreateContext createContext = new EntityCreateContext();
    private final EntityDestroyContext destroyContext = new EntityDestroyContext();
    private Slot[] slots = new Slot[16];
    private int slotCount;
    private int executions;
    private boolean flushing;
    private Throwable failure;

    public EntityManager(ComponentRegistry componentRegistry) {
        this.componentRegistry = Objects.requireNonNull(componentRegistry);
    }

    @NonNull
    public EntityQuery newQuery() {
        return EntityQuery.query();
    }

    @NonNull
    public synchronized List<@NonNull ArchetypeDataPool> startQuery(@NonNull EntityQuery query) {
        requireAvailable();
        Objects.requireNonNull(query);
        List<ArchetypeDataPool> result = new ArrayList<>();
        for (Archetype archetype : archetypes.values()) {
            if (archetype.pool == null) {
                continue;
            }
            boolean matches = true;
            for (Class<? extends CleanComponent> component : query.mustHave) {
                if (!archetype.key.contains(component)) {
                    matches = false;
                    break;
                }
            }
            if (!matches) {
                continue;
            }
            for (Class<? extends CleanComponent> component : query.mustNotHave) {
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

    public final synchronized ExecutionScope beginExecution(EntityQuery query) {
        requireAvailable();
        ExecutionScope scope = new ExecutionScope(ObjectArrayView.copyOf(startQuery(query), ArchetypeDataPool[]::new));
        int acquired = 0;
        try {
            for (ArchetypeDataPool pool : scope.pools.elements()) {
                pool.beginExecution();
                acquired++;
            }
        } catch (Throwable exception) {
            for (int i = acquired - 1; i >= 0; i--) {
                try {
                    scope.pools.elements()[i].endExecution();
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

    public final class ExecutionScope implements AutoCloseable {
        private final ObjectArrayView<ArchetypeDataPool> pools;
        private boolean closed;

        private ExecutionScope(ObjectArrayView<ArchetypeDataPool> pools) {
            this.pools = pools;
        }

        public ObjectArrayView<ArchetypeDataPool> pools() {
            return pools;
        }

        @Override
        public void close() {
            synchronized (EntityManager.this) {
                if (closed) {
                    return;
                }
                closed = true;
                Throwable releaseFailure = null;
                for (int i = pools.size() - 1; i >= 0; i--) {
                    try {
                        pools.elements()[i].endExecution();
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
        } catch (Throwable exception) {
            failure = exception;
            Throwables.throwIfUnchecked(exception);
            throw new CompletionException(exception);
        } finally {
            commands.clear();
            createContext.setInternal(List.of(), List.of());
            destroyContext.clearInternal();
            flushing = false;
        }
    }

    private void create(Slot slot, EntityCommand command) {
        Archetype target = archetypes.get(command.target());
        ArchetypeDataPool pool = pool(target);
        if (slot.createCallback != null) {
            createContext.setInternal(target.components, command.components());
            try {
                slot.createCallback.beforeCreate(createContext);
            } finally {
                createContext.setInternal(List.of(), List.of());
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
            destroyContext.setInternal(index, old.components, old.pool);
            try {
                slot.destroyCallback.beforeDestroy(destroyContext);
            } finally {
                destroyContext.clearInternal();
            }
        }
        old.pool.removeEntity(index);
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
        CleanComponent[] values = new CleanComponent[target.components.size()];
        Class<? extends CleanComponent>[] elements = target.components.elements();

        for (int i = 0; i < elements.length; i++) {
            Class<? extends CleanComponent> component = elements[i];
            values[i] = component == command.componentType() && command.type() == EntityCommand.Type.ADD_COM
                ? command.component() : old.pool.getComponent(command.index(), component);
        }

        pool(target).addEntity(command.index(), values);
        old.pool.removeEntity(command.index());
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

    public synchronized void destroyEntity(int entityID) {
        requireAvailable();
        Slot slot = slot(entityID);
        if (!acceptsCommands(slot, slot.generation)) {
            throw new IllegalStateException("Entity isn't available: " + entityID);
        }
        enqueueDestroy(entityID, slot);
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
        Objects.requireNonNull(component);
        if (!slot.projected.key.contains(component.getClass())) {
            return false;
        }
        validate(component);
        commands.add(new EntityCommand(index, generation, EntityCommand.Type.SET_COM,
            null, component, component.getClass(), null));
        return true;
    }

    synchronized boolean tryAddComponent(int index, int generation, CleanComponent component) {
        requireAvailable();
        Slot slot = slot(index);
        if (!acceptsCommands(slot, generation)) {
            return false;
        }
        Objects.requireNonNull(component);
        if (slot.projected.key.contains(component.getClass())) {
            return false;
        }
        validate(component);
        List<Class<? extends CleanComponent>> types = new ArrayList<>(slot.projected.components);
        types.add(component.getClass());
        Archetype target = archetype(types);
        commands.add(new EntityCommand(index, generation, EntityCommand.Type.ADD_COM,
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
        Objects.requireNonNull(component);
        if (!slot.projected.key.contains(component)) {
            return false;
        }
        List<Class<? extends CleanComponent>> types = new ArrayList<>(slot.projected.components);
        types.removeIf(type -> type == component);
        Archetype target = archetype(types);
        commands.add(new EntityCommand(index, generation, EntityCommand.Type.REMOVE_COM,
            null, null, component, target.key));
        slot.projected = target;
        return true;
    }

    synchronized boolean isValid(int index, int generation) {
        return failure == null && index >= 0 && index < slotCount && acceptsCommands(slots[index], generation);
    }

    public synchronized boolean isAlive(int index, int generation) {
        return failure == null && index >= 0 && index < slotCount
            && slots[index].generation == generation && slots[index].state == State.ALIVE;
    }

    protected synchronized int getLatestGeneration(int entityID) {
        return slot(entityID).generation;
    }

    protected synchronized List<Class<? extends CleanComponent>> getComponentTypes(int entityID) {
        Slot slot = slot(entityID);
        return slot.projected == null ? List.of() : slot.projected.components;
    }

    protected ArchetypeDataPool createPool(List<Class<? extends CleanComponent>> components) {
        return new HeapPool(componentRegistry, components, 100, 50, 50);
    }

    private ArchetypeDataPool pool(Archetype archetype) {
        if (archetype.pool == null) {
            archetype.pool = Objects.requireNonNull(createPool(archetype.components));
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

    private void requireAvailable() {
        if (failure != null) {
            throw new IllegalStateException("Entity manager failed during commit or execution release", failure);
        }
        if (flushing) {
            throw new IllegalStateException("Entity manager cannot be accessed from lifecycle callbacks");
        }
    }
}
