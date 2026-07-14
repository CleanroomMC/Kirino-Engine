package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public final class KnowledgeOwner {

    private final String name;

    private KnowledgeOwner(@NonNull String name) {
        Preconditions.checkNotNull(name);

        this.name = name;
    }

    @NonNull
    public static KnowledgeOwner of(@NonNull String name) {
        return new KnowledgeOwner(name);
    }

    @NonNull
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}