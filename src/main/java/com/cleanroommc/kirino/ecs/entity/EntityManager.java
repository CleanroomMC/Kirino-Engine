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
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class EntityManager {
    private final ComponentRegistry componentRegistry;

    public EntityManager(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    /**
     * Every entity's component info.
     *
     * <hr>
     * <p><code>entityComponents</code>,
     * <code>{@link #entityArchetypeLocations}</code>,
     * <code>{@link #entityGenerations}</code>,
     * <code>{@link #entityDestroyCallbacks}</code>, and
     * <code>{@link #entityCreateCallbacks}</code>
     * share the same length. index is the identifier of an entity.</p>
     */
    private final List<List<Class<? extends CleanComponent>>> entityComponents = new ArrayList<>();

    /**
     * Every entity's archetype info.
     *
     * <hr>
     * <p><code>{@link #entityComponents}</code>,
     * <code>entityArchetypeLocations</code>,
     * <code>{@link #entityGenerations}</code>,
     * <code>{@link #entityDestroyCallbacks}</code>, and
     * <code>{@link #entityCreateCallbacks}</code>
     * share the same length. index is the identifier of an entity.</p>
     */
    private final List<ArchetypeKey> entityArchetypeLocations = new ArrayList<>();

    /**
     * Every entity's generation info.
     *
     * <hr>
     * <p><code>{@link #entityComponents}</code>,
     * <code>{@link #entityArchetypeLocations}</code>,
     * <code>entityGenerations</code>,
     * <code>{@link #entityDestroyCallbacks}</code>, and
     * <code>{@link #entityCreateCallbacks}</code>
     * share the same length. index is the identifier of an entity.</p>
     */
    private final List<Integer> entityGenerations = new ArrayList<>();

    /**
     * Every entity's destroy callback.
     *
     * <hr>
     * <p><code>{@link #entityComponents}</code>,
     * <code>{@link #entityArchetypeLocations}</code>,
     * <code>{@link #entityGenerations}</code>,
     * <code>entityDestroyCallbacks</code>, and
     * <code>{@link #entityCreateCallbacks}</code>
     * share the same length. index is the identifier of an entity.</p>
     */
    private final List<@Nullable EntityDestroyCallback> entityDestroyCallbacks = new ArrayList<>();

    /**
     * Every entity's create callback.
     *
     * <hr>
     * <p><code>{@link #entityComponents}</code>,
     * <code>{@link #entityArchetypeLocations}</code>,
     * <code>{@link #entityGenerations}</code>,
     * <code>{@link #entityDestroyCallbacks}</code>, and
     * <code>entityCreateCallbacks</code>
     * share the same length. index is the identifier of an entity.</p>
     */
    private final List<@Nullable EntityCreateCallback> entityCreateCallbacks = new ArrayList<>();

    private final EntityDestroyContext destroyContext = new EntityDestroyContext();
    private final EntityCreateContext createContext = new EntityCreateContext();

    private final List<Integer> freeIndexes = new ArrayList<>();
    private int indexCounter = 0;

    private final Map<ArchetypeKey, ArchetypeDataPool> archetypes = new HashMap<>();

    protected final List<EntityCommand> commandBuffer = new ArrayList<>();

    @NonNull
    public EntityQuery newQuery() {
        return EntityQuery.query();
    }

    @NonNull
    public List<@NonNull ArchetypeDataPool> startQuery(@NonNull EntityQuery query) {
        Preconditions.checkNotNull(query);

        List<ArchetypeDataPool> result = new ArrayList<>();
        for (Map.Entry<ArchetypeKey, ArchetypeDataPool> entry : archetypes.entrySet()) {
            boolean mustHave = true;
            for (Class<? extends CleanComponent> component : query.mustHave) {
                if (!entry.getKey().contains(component)) {
                    mustHave = false;
                    break;
                }
            }
            if (!mustHave) {
                continue;
            }
            boolean mustNotHave = true;
            for (Class<? extends CleanComponent> component : query.mustNotHave) {
                if (entry.getKey().contains(component)) {
                    mustNotHave = false;
                    break;
                }
            }
            if (!mustNotHave) {
                continue;
            }
            result.add(entry.getValue());
        }

        return result;
    }

    /**
     * Consume all buffered commands.
     * </br></br>
     * Thread safety is guaranteed, but never call it during job or system execution.
     * The only place to call it is the end of {@link CleanWorld#update()}.
     */
    public synchronized void flush() {
        synchronized (commandBuffer) {
            for (EntityCommand command : commandBuffer) {
                switch (command.type) {
                    case CREATE -> {
                        List<Class<? extends CleanComponent>> components = entityComponents.get(command.index);
                        ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                        ArchetypeDataPool pool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                        EntityCreateCallback createCallback = entityCreateCallbacks.get(command.index);
                        if (createCallback != null) {
                            createContext.setInternal(components, command.newComponents);
                            createCallback.beforeCreate(createContext);
                        }
                        pool.addEntity(command.index, command.newComponents);
                    }
                    case DESTROY -> {
                        ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                        ArchetypeDataPool pool = archetypes.get(archetypeKey);
                        EntityDestroyCallback destroyCallback = entityDestroyCallbacks.get(command.index);
                        if (destroyCallback != null) {
                            destroyContext.setInternal(command.index, entityComponents.get(command.index), pool);
                            destroyCallback.beforeDestroy(destroyContext);
                        }
                        pool.removeEntity(command.index);
                    }
                    case SET_COM -> {
                        ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                        ArchetypeDataPool pool = archetypes.get(archetypeKey);
                        pool.setComponent(command.index, command.componentToSet);
                    }
                    case ADD_COM -> {
                        List<Class<? extends CleanComponent>> components = entityComponents.get(command.index);
                        ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                        ArchetypeDataPool oldPool = archetypes.get(archetypeKey);

                        List<CleanComponent> newComponents = new ArrayList<>();
                        for (Class<? extends CleanComponent> component: components) {
                            newComponents.add(oldPool.getComponent(command.index, component));
                        }
                        newComponents.add(command.componentToAdd);

                        // update component info
                        components.add(command.componentToAdd.getClass());
                        // update archetype key
                        archetypeKey = new ArchetypeKey(components);
                        entityArchetypeLocations.set(command.index, archetypeKey);

                        ArchetypeDataPool newPool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                        oldPool.removeEntity(command.index);
                        newPool.addEntity(command.index, newComponents);
                    }
                    case REMOVE_COM -> {
                        List<Class<? extends CleanComponent>> components = entityComponents.get(command.index);
                        ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                        ArchetypeDataPool oldPool = archetypes.get(archetypeKey);

                        List<CleanComponent> newComponents = new ArrayList<>();
                        for (Class<? extends CleanComponent> component: components) {
                            newComponents.add(oldPool.getComponent(command.index, component));
                        }
                        newComponents.removeIf(c -> c.getClass().equals(command.componentToRemove));

                        // update component info
                        components.removeIf(c -> c.equals(command.componentToRemove));
                        // update archetype key
                        archetypeKey = new ArchetypeKey(components);
                        entityArchetypeLocations.set(command.index, archetypeKey);

                        ArchetypeDataPool newPool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                        oldPool.removeEntity(command.index);
                        newPool.addEntity(command.index, newComponents);
                    }
                }
            }
            commandBuffer.clear();
        }
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     *     <li>Stop modifying <code>components</code> from now on as the action is deferred</li>
     * </ul>
     * </br>
     * This method will allocate an entity handle and generate a command for all side effects.
     * Buffered commands will be consumed at {@link #flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @see #flush()
     *
     * @param components The component types this entity has
     * @return An entity handle
     */
    @NonNull
    public CleanEntityHandle createEntity(@NonNull CleanComponent @NonNull ... components) {
        return createEntity(null, null, components);
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     *     <li>Stop modifying <code>components</code> from now on as the action is deferred</li>
     * </ul>
     * </br>
     * This method will allocate an entity handle and generate a command for all side effects.
     * Buffered commands will be consumed at {@link #flush()}, and the destroy/create callback will be executed during {@link #flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @see #flush()
     *
     * @param destroyCallback The entity destroy callback
     * @param createCallback The entity create callback
     * @param components The component types this entity has
     * @return An entity handle
     */
    @NonNull
    public synchronized CleanEntityHandle createEntity(@Nullable EntityDestroyCallback destroyCallback, @Nullable EntityCreateCallback createCallback, @NonNull CleanComponent @NonNull ... components) {
        Preconditions.checkNotNull(components);
        for (CleanComponent component : components) {
            Preconditions.checkNotNull(component);
        }

        int index;
        if (freeIndexes.isEmpty()) {
            index = indexCounter++;
        } else {
            index = freeIndexes.removeLast();
        }

        // update component info
        List<Class<? extends CleanComponent>> comTypes = Arrays.stream(components).map(CleanComponent::getClass).collect(Collectors.toList());
        if (index > entityComponents.size() - 1) {
            entityComponents.add(comTypes);
        } else {
            entityComponents.set(index, comTypes);
        }

        // update archetype key
        ArchetypeKey archetypeKey = new ArchetypeKey(comTypes);
        if (index > entityArchetypeLocations.size() - 1) {
            entityArchetypeLocations.add(archetypeKey);
        } else {
            entityArchetypeLocations.set(index, archetypeKey);
        }

        // fetch generation
        int generation = 0;
        if (index > entityGenerations.size() - 1) {
            entityGenerations.add(generation);
        } else {
            generation = entityGenerations.get(index);
        }

        // update destroy callback
        if (index > entityDestroyCallbacks.size() - 1) {
            entityDestroyCallbacks.add(destroyCallback);
        } else {
            entityDestroyCallbacks.set(index, destroyCallback);
        }

        // update create callback
        if (index > entityCreateCallbacks.size() - 1) {
            entityCreateCallbacks.add(createCallback);
        } else {
            entityCreateCallbacks.set(index, createCallback);
        }

        synchronized (commandBuffer) {
            EntityCommand command = new EntityCommand(index, EntityCommand.Type.CREATE);
            command.newComponents = Arrays.asList(components);
            commandBuffer.add(command);
        }

        return new CleanEntityHandle(this, index, generation);
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>entityID</code> must be valid</li>
     * </ul>
     * </br>
     * This method will destroy an entity and generate a command for all side effects.
     * Buffered commands will be consumed at {@link #flush()}, and the destroy callback will be executed during {@link #flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @see #flush()
     * @param entityID The index of the entity
     */
    public synchronized void destroyEntity(int entityID) {
        Preconditions.checkElementIndex(entityID, entityGenerations.size());

        // update generation
        entityGenerations.set(entityID, entityGenerations.get(entityID) + 1);
        freeIndexes.add(entityID);

        synchronized (commandBuffer) {
            EntityCommand command = new EntityCommand(entityID, EntityCommand.Type.DESTROY);
            commandBuffer.add(command);
        }
    }

    protected int getLatestGeneration(int entityID) {
        Preconditions.checkElementIndex(entityID, entityGenerations.size());

        return entityGenerations.get(entityID);
    }

    protected List<Class<? extends CleanComponent>> getComponentTypes(int entityID) {
        Preconditions.checkElementIndex(entityID, entityGenerations.size());

        return entityComponents.get(entityID);
    }
}
