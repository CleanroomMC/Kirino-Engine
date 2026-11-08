package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import org.jspecify.annotations.NonNull;

public class CleanEntityHandle {
    private final EntityManager entityManager;
    public final int index;
    public final int generation;

    protected CleanEntityHandle(EntityManager entityManager, int index, int generation) {
        this.entityManager = entityManager;
        this.index = index;
        this.generation = generation;
    }

    /**
     * Thread safety is guaranteed.
     *
     * @return Whether this entity handle is valid
     */
    public boolean valid() {
        return entityManager.isValid(index, generation);
    }

    public boolean isAlive() {
        return entityManager.isAlive(index, generation);
    }

    /**
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed).
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * However, this entity handle becomes invalid immediately after this call.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @return Whether you successfully executed the method
     * @see EntityManager#flush()
     */
    public boolean tryDestroy() {
        return entityManager.tryDestroy(index, generation);
    }

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The component type is valid and registered in the component registry from its {@link EntityManager}</li>
     *     <li>Stop modifying <code>component</code> from now on as the action is deferred</li>
     * </ul>
     * <br/>
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed),
     * OR the entity doesn't have this type of component.
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     * @see EntityManager#flush()
     */
    public boolean trySetComponent(@NonNull CleanComponent component) {
        return entityManager.trySetComponent(index, generation, component);
    }

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The component type is valid and registered in the component registry from its {@link EntityManager}</li>
     *     <li>Stop modifying <code>component</code> from now on as the action is deferred</li>
     * </ul>
     * <br/>
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed).
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     * @see EntityManager#flush()
     */
    public boolean tryAddComponent(@NonNull CleanComponent component) {
        return entityManager.tryAddComponent(index, generation, component);
    }

    /**
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed),
     * OR the entity doesn't have this type of component.
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component type
     * @return Whether you successfully executed the method
     * @see EntityManager#flush()
     */
    public boolean tryRemoveComponent(@NonNull Class<? extends CleanComponent> component) {
        return entityManager.tryRemoveComponent(index, generation, component);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CleanEntityHandle that)) {
            return false;
        }

        return index == that.index && generation == that.generation && entityManager == that.entityManager;
    }

    @Override
    public int hashCode() {
        int hash = System.identityHashCode(entityManager);
        hash = 31 * hash + index;
        return 31 * hash + generation;
    }
}
