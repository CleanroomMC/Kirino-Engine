package com.cleanroommc.kirino.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

/**
 * Lightweight Minecraft ResourceLocation replacement with no dependencies.
 */
public record ResourceLocation(String namespace, String path) implements Comparable<ResourceLocation> {

    public ResourceLocation(@NonNull String namespace, @NonNull String path) {
        // deliberately not using Google Preconditions
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(path);

        this.namespace = namespace.isEmpty() ? "minecraft" : namespace.toLowerCase(Locale.ROOT);
        this.path = path.toLowerCase(Locale.ROOT);
    }

    public ResourceLocation(@NonNull String resourceName) {
        // deliberately not using Google Preconditions
        Objects.requireNonNull(resourceName);

        int separator = resourceName.indexOf(':');
        String namespace = separator < 0 ? "minecraft" : resourceName.substring(0, separator);
        this(namespace, separator < 0 ? resourceName : resourceName.substring(separator + 1));
    }

    @Override
    @NonNull
    public String toString() {
        return namespace + ':' + path;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || other instanceof ResourceLocation(String namespace1, String path1)
                && namespace.equals(namespace1)
                && path.equals(path1);
    }

    @Override
    public int compareTo(@NonNull ResourceLocation other) {
        int result = namespace.compareTo(other.namespace);
        return result == 0 ? path.compareTo(other.path) : result;
    }
}
