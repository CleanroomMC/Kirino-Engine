package com.cleanroommc.kirino.ecs;

import com.cleanroommc.kirino.ecs.api.component.CleanComponent;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class ArchetypeKey {
    private static final int LINEAR_LIMIT = 8;
    private final Class<?>[] components;
    private final ReferenceOpenHashSet<Class<?>> membership;
    private final int hash;

    public ArchetypeKey(List<Class<? extends CleanComponent>> components) {
        Objects.requireNonNull(components);
        this.components = components.toArray(Class<?>[]::new);
        membership = this.components.length > LINEAR_LIMIT ? new ReferenceOpenHashSet<>(this.components.length) : null;
        int hash = 0;
        for (int i = 0; i < this.components.length; i++) {
            Class<?> component = Objects.requireNonNull(this.components[i]);
            if (membership != null) {
                if (!membership.add(component)) {
                    throw new IllegalArgumentException("Duplicate component: " + component.getName());
                }
            } else {
                for (int previous = 0; previous < i; previous++) {
                    if (this.components[previous] == component) {
                        throw new IllegalArgumentException("Duplicate component: " + component.getName());
                    }
                }
            }
            hash += System.identityHashCode(component);
        }
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ArchetypeKey that) || components.length != that.components.length) return false;
        for (Class<?> component : components) {
            if (!that.containsType(component)) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        String[] names = new String[components.length];
        for (int i = 0; i < components.length; i++) names[i] = components[i].getName();
        Arrays.sort(names);
        return "ArchetypeKey{" + String.join(",", names) + "}";
    }

    public boolean contains(Class<? extends CleanComponent> component) {
        return containsType(Objects.requireNonNull(component));
    }

    private boolean containsType(Class<?> component) {
        if (membership != null) return membership.contains(component);
        for (Class<?> stored : components) {
            if (stored == component) return true;
        }
        return false;
    }
}
