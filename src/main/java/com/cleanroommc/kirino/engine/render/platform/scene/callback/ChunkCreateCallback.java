package com.cleanroommc.kirino.engine.render.platform.scene.callback;

import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityCreateContext;
import org.jspecify.annotations.NonNull;

public class ChunkCreateCallback implements EntityCreateCallback {

    private final CallbackDrivenChunkDelta chunkDelta;

    public ChunkCreateCallback(CallbackDrivenChunkDelta chunkDelta) {
        this.chunkDelta = chunkDelta;
    }

    @Override
    public void beforeCreate(@NonNull EntityCreateContext createContext) {
        chunkDelta.newChunksAdded = true;
    }
}
