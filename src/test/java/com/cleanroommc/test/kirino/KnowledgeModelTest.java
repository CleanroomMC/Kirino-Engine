package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.engine.render.core.gl.semantic.GLKnowledgeKeys;
import com.cleanroommc.kirino.engine.semantic.*;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KnowledgeModelTest {

    private static final class Policy implements ViolationPolicy {

        @Override
        public void onViolation(@NonNull KnowledgeViolation violation) {
            switch (violation.kind()) {
                case REQUIRE_UNKNOWN -> throw new RuntimeException();
                case REQUIRE_MISMATCH -> throw new RuntimeException();
                case FROZEN_MUTATION -> throw new RuntimeException();
                case COMMIT_CONFLICT -> throw new RuntimeException();
            }
        }
    }

    @Test
    public void testCommitConflict() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 1));
        try (var handle = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertThrows(RuntimeException.class, () -> {
                glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 2));
            });
        }
    }
}
