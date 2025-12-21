package com.cleanroommc.kirino.ecs.entity.callback;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.NoSuchElementException;

public class EntityCreateContext {
    private List<Class<? extends ICleanComponent>> components;
    private List<ICleanComponent> newComponents;

    public EntityCreateContext() {
    }

    /**
     * Must not be accessed by clients.
     */
    public void setInternal(List<Class<? extends ICleanComponent>> components, List<ICleanComponent> newComponents) {
        this.components = components;
        this.newComponents = newComponents;
    }

    @NonNull
    public ICleanComponent getComponent(Class<? extends ICleanComponent> component) {
        Preconditions.checkArgument(components.contains(component),
                "The target entity doesn't contain the argument \"component\" - %s.", component.getName());

        for (ICleanComponent instance : newComponents) {
            if (instance.getClass() == component) {
                return instance;
            }
        }

        throw new NoSuchElementException("Unexpected exception."); // impossible
    }
}
