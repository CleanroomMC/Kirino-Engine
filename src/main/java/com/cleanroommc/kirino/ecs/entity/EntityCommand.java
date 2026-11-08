package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import com.cleanroommc.kirino.ecs.storage.ArchetypeKey;
import com.cleanroommc.kirino.utils.collections.ImmutableObjectArrayList;

record EntityCommand(int index, int generation, Type type, ImmutableObjectArrayList<CleanComponent> components,
                     CleanComponent component, Class<? extends CleanComponent> componentType,
                     ArchetypeKey target) {
    enum Type {
        CREATE, DESTROY, SET_COM, ADD_COM, REMOVE_COM
    }
}
