package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.List;
import java.util.Objects;

public final class ArchetypeKey {
    private final ReferenceOpenHashSet<Class<? extends CleanComponent>> components;
    private final int hash;
    public final String key;

    public ArchetypeKey(List<Class<? extends CleanComponent>> components) {
        Objects.requireNonNull(components);
        this.components = new ReferenceOpenHashSet<>();
        int hash = 0;
        for (Class<? extends CleanComponent> component : components) {
            Objects.requireNonNull(component);
            if (!this.components.add(component)) {
                throw new IllegalArgumentException("Duplicate component: " + component.getName());
            }
            hash += System.identityHashCode(component);
        }
        this.hash = hash;
        key = String.join(",", components.stream().map(Class::getName).sorted().toList());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ArchetypeKey that
            && components.size() == that.components.size() && components.containsAll(that.components);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "ArchetypeKey{" + key + "}";
    }

    public boolean contains(Class<? extends CleanComponent> component) {
        return components.contains(Objects.requireNonNull(component));
    }
}
