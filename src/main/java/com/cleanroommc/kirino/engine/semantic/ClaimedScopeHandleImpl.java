package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;

public final class ClaimedScopeHandleImpl<T> implements ClaimedScopeHandle {

    private final KnowledgeSupervisor supervisor;
    private final KnowledgeKey<T> key;
    private boolean released = false;

    ClaimedScopeHandleImpl(KnowledgeSupervisor supervisor, KnowledgeKey<T> key) {
        this.supervisor = supervisor;
        this.key = key;
    }

    @Override
    public void release() {
        Preconditions.checkState(!released, "This handle is already released!");

        supervisor.release(key);
        released = true;
    }
}
