package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class KnowledgeKey<T> {

    private final String domain;
    private final String name;
    private final Class<T> type;

    private KnowledgeKey(
            @NonNull String domain,
            @NonNull String name,
            @NonNull Class<T> type) {

        Preconditions.checkNotNull(domain);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(type);

        this.domain = domain;
        this.name = name;
        this.type = type;
    }

    @NonNull
    public static <T> KnowledgeKey<T> of(
            @NonNull String domain,
            @NonNull String name,
            @NonNull Class<T> type) {

        return new KnowledgeKey<>(domain, name, type);
    }

    @NonNull
    public String domain() {
        return domain;
    }

    @NonNull
    public String name() {
        return name;
    }

    @NonNull
    public Class<T> type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KnowledgeKey<?> other)) {
            return false;
        }

        return domain.equals(other.domain)
                && name.equals(other.name)
                && type.equals(other.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, name, type);
    }

    @Override
    public String toString() {
        return domain + "." + name;
    }
}
