package com.cleanroommc.kirino.ecs.entity.callback;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.NoSuchElementException;

public class EntityCreateContext {
    private List<Class<? extends CleanComponent>> components;
    private List<CleanComponent> newComponents;

    public EntityCreateContext() {
    }

    /**
     * Must not be accessed by clients.
     */
    public void setInternal(List<Class<? extends CleanComponent>> components, List<CleanComponent> newComponents) {
        this.components = components;
        this.newComponents = newComponents;
    }

    @NonNull
    public CleanComponent getComponent(Class<? extends CleanComponent> component) {
        Preconditions.checkArgument(components.contains(component),
                "The target entity doesn't contain the argument \"component\" - %s.", component.getName());

        for (CleanComponent instance : newComponents) {
            if (instance.getClass() == component) {
                return instance;
            }
        }

        throw new NoSuchElementException("Unexpected exception."); // impossible
    }
}
