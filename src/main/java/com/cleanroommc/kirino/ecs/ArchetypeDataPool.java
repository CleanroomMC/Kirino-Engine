package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.storage.PrimitiveArray;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import com.cleanroommc.kirino.schemata.collections.ObjectArrayView;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

abstract class ArchetypeDataPool {
    public final ObjectArrayView<Class<? extends CleanComponent>> components;

    protected int currentSize;
    public final int initSize;

    protected final ComponentRegistry componentRegistry;

    /**
     * The number of entities this pool can contain
     *
     * @return The current pool size
     */
    public final int getCurrentSize() {
        return currentSize;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    ArchetypeDataPool(ComponentRegistry componentRegistry, List<Class<? extends CleanComponent>> components, int initSize) {
        if (initSize < 0 || initSize > PoolCapacity.MAX_CAPACITY) {
            throw new IllegalArgumentException("Invalid initial pool capacity");
        }
        this.componentRegistry = componentRegistry;
        this.components = ObjectArrayView.copyOf(components,
            size -> (Class<? extends CleanComponent>[]) new Class[size]);
        this.initSize = initSize;
        currentSize = initSize;
    }

    void trimCapacity() {
    }

    public abstract boolean containsEntity(int entityID);

    public void beginExecution() {
    }

    public void endExecution() {
    }

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li><code>component</code> is in {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID  The id of the entity
     * @param component The component class
     * @return The instantiated component
     */
    @NonNull
    public abstract CleanComponent getComponent(int entityID, Class<? extends CleanComponent> component);

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li>The class of <code>component</code> is in {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID  The id of the entity
     * @param component The component
     */
    public abstract void setComponent(int entityID, CleanComponent component);

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li>Classes of <code>components</code> must correspond to and form a bijection with {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID   The id of the entity
     * @param components The components
     */
    public abstract void addEntity(int entityID, CleanComponent[] components);

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     */
    public abstract void removeEntity(int entityID);

    abstract void addEntityFrom(int entityID, ArchetypeDataPool source, @Nullable CleanComponent addition);

    /**
     * <p>Prerequisites include:</p>
     * <ul>
     *     <li><code>component</code> is in {@link ArchetypeDataPool#components}</li>
     *     <li><code>fieldAccessChain</code> points to a valid field</li>
     * </ul>
     *
     * @param component        The component class
     * @param fieldAccessChain A list of field names to identify a field
     * @return An array of field values
     */
    @NonNull
    public abstract PrimitiveArray getArray(Class<? extends CleanComponent> component, String... fieldAccessChain);

    public PrimitiveArray getArray(Class<? extends CleanComponent> component, SchemaLayout.Column column) {
        return getArray(component, column.path().elements());
    }

    @NonNull
    public abstract PoolRows getRows();

    public abstract int getEntityID(int index);

    public abstract String getSnapshot();
}
