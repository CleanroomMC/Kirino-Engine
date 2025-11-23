package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class EntityDestroyContext {
    private int entityID;
    private List<Class<? extends ICleanComponent>> components;
    private ArchetypeDataPool archetype;

    EntityDestroyContext() {
    }

    void set(int entityID, List<Class<? extends ICleanComponent>> components, ArchetypeDataPool archetype) {
        this.entityID = entityID;
        this.components = components;
        this.archetype = archetype;
    }

    @NonNull
    public ICleanComponent getComponent(Class<? extends ICleanComponent> component) {
        Preconditions.checkArgument(components.contains(component),
                "The target entity doesn't contain the argument \"component\" - %s.", component.getName());

        return archetype.getComponent(entityID, component);
    }
}
