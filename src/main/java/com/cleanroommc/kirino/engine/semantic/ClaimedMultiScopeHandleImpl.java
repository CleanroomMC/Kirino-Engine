package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;

public final class ClaimedMultiScopeHandleImpl implements ClaimedScopeHandle {

    private final KnowledgeSupervisor supervisor;
    private final KnowledgeKey<?>[] keys;
    private boolean released = false;

    ClaimedMultiScopeHandleImpl(KnowledgeSupervisor supervisor, KnowledgeKey<?>[] keys) {
        this.supervisor = supervisor;
        this.keys = keys;
    }

    @Override
    public void release() {
        Preconditions.checkState(!released, "This handle is already released!");

        for (KnowledgeKey<?> key : keys) {
            supervisor.release(key);
        }
        released = true;
    }
}
