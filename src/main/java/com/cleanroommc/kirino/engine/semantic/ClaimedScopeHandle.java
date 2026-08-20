package com.cleanroommc.kirino.engine.semantic;

public interface ClaimedScopeHandle extends AutoCloseable {
    void release();

    @Override
    default void close() {
        release();
    }
}
