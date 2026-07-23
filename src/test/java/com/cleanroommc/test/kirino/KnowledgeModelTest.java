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
    public void testFullUsage1() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 1));
        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertThrows(RuntimeException.class, () -> {
                glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 2));
            });
        }

        Assertions.assertDoesNotThrow(() -> {
            glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 2));
        });

        glKnowledge.require(GLKnowledgeKeys.VAO, 2);
    }

    @Test
    public void testFullUsage2() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 1));
        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertThrows(RuntimeException.class, () -> {
                glKnowledge.commit(cp -> cp.unknownDomain("gl"));
            });
        }

        Assertions.assertDoesNotThrow(() -> {
            glKnowledge.commit(cp -> cp.unknownDomain("gl"));
        });

        Assertions.assertThrows(RuntimeException.class, () -> {
            glKnowledge.requireKnown(GLKnowledgeKeys.VAO);
        });
    }

    @Test
    public void testUnknown() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        Assertions.assertThrows(RuntimeException.class, () -> {
            glKnowledge.requireKnown(GLKnowledgeKeys.VAO);
        });
    }

    @Test
    public void testNoCommitConflict1() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 1));
        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertDoesNotThrow(() -> {
                glKnowledge.commit(cp -> cp.know(GLKnowledgeKeys.VAO, 1));
            });
        }
    }

    @Test
    public void testNoCommitConflict2() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.unknown(GLKnowledgeKeys.VAO));
        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertDoesNotThrow(() -> {
                glKnowledge.commit(cp -> cp.unknown(GLKnowledgeKeys.VAO));
            });
        }
    }

    @Test
    public void testNoCommitConflict3() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        glKnowledge.commit(cp -> cp.unknown(GLKnowledgeKeys.VAO));
        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertDoesNotThrow(() -> {
                glKnowledge.commit(cp -> cp.unknownDomain("gl"));
            });
        }
    }

    @Test
    public void testNoCommitConflict4() {
        KnowledgeSupervisor supervisor = new KnowledgeSupervisor(new Policy());
        KnowledgeRuntime glKnowledge = supervisor.access(KnowledgeOwner.of("kirino"));

        try (var ignored = glKnowledge.claim(GLKnowledgeKeys.VAO)) {
            Assertions.assertDoesNotThrow(() -> {
                glKnowledge.commit(cp -> cp.unknown(GLKnowledgeKeys.VAO));
            });
        }
    }
}
