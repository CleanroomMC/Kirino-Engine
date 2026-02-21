package com.cleanroommc.kirino.engine.render.platform.scene.callback;

import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyCallback;
import com.cleanroommc.kirino.ecs.entity.callback.EntityDestroyContext;
import com.cleanroommc.kirino.engine.render.platform.ecs.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.platform.scene.ChunkPosKey;
import org.jspecify.annotations.NonNull;

public class ChunkDestroyCallback implements EntityDestroyCallback {

    private final CallbackDrivenChunkDelta chunkDelta;

    public ChunkDestroyCallback(CallbackDrivenChunkDelta chunkDelta) {
        this.chunkDelta = chunkDelta;
    }

    @Override
    public void beforeDestroy(@NonNull EntityDestroyContext destroyContext) {
        ChunkComponent chunkComponent = (ChunkComponent) destroyContext.getComponent(ChunkComponent.class);
        chunkDelta.chunksDestroyedLastFrame.add(new ChunkPosKey(chunkComponent.chunkPosX, chunkComponent.chunkPosY, chunkComponent.chunkPosZ));
    }
}
