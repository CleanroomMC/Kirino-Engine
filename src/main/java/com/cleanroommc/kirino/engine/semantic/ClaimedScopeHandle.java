package com.cleanroommc.kirino.engine.semantic;

import com.google.common.base.Preconditions;

public final class ClaimedScopeHandle<T> implements AutoCloseable {

    private final KnowledgeSupervisor supervisor;
    private final KnowledgeKey<T> key;
    private boolean released = false;

    ClaimedScopeHandle(KnowledgeSupervisor supervisor, KnowledgeKey<T> key) {
        this.supervisor = supervisor;
        this.key = key;
    }

    public void release() {
        Preconditions.checkState(!released, "This handle is already released!");

        supervisor.release(key);
        released = true;
    }

    @Override
    public void close() {
        release();
    }
}
