package com.cleanroommc.kirino.engine.semantic;

import org.jspecify.annotations.NonNull;

public interface ViolationPolicy {
    void onViolation(@NonNull KnowledgeViolation violation);
}
