package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.CleanComponent;

import java.util.List;

public class EntityCommand {
    public enum Type {
        CREATE,
        DESTROY,
        SET_COM,
        ADD_COM,
        REMOVE_COM
    }

    public final int index;
    public final Type type;

    List<CleanComponent> newComponents;
    CleanComponent componentToSet;
    CleanComponent componentToAdd;
    Class<? extends CleanComponent> componentToRemove;

    protected EntityCommand(int index, Type type) {
        this.index = index;
        this.type = type;
    }
}
