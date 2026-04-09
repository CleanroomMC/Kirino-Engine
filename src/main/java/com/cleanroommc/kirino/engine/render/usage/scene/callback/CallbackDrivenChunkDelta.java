package com.cleanroommc.kirino.engine.render.usage.scene.callback;

import com.cleanroommc.kirino.engine.render.usage.scene.ChunkPosKey;

import java.util.ArrayList;
import java.util.List;

public class CallbackDrivenChunkDelta {

    // will be modified at the end of update by the callback
    public final List<ChunkPosKey> chunksDestroyedLastFrame;
    // will be modified at the end of update by the callback
    public boolean newChunksAdded;

    public CallbackDrivenChunkDelta() {
        chunksDestroyedLastFrame = new ArrayList<>();
        newChunksAdded = false;
    }
}
