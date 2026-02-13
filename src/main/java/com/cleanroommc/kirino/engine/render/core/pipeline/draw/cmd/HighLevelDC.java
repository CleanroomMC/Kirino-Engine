package com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd;

import com.cleanroommc.kirino.KirinoCommonCore;
import com.cleanroommc.kirino.engine.render.core.pipeline.pass.PassHint;
import com.cleanroommc.kirino.schemata.pool.ThreadSafeGenPool;
import org.jspecify.annotations.NonNull;

public final class HighLevelDC implements DrawCommand {

    public enum CommandSource {
        PASS_INTERNAL,
        SCENE_SUBMITTED
    }

    private final static ThreadSafeGenPool<HighLevelDC> POOL = new ThreadSafeGenPool<>(KirinoCommonCore.KIRINO_CONFIG_HUB.getHighLevelDrawCommandPoolSize()) {
        @NonNull
        @Override
        public HighLevelDC newObject(@NonNull Handle<HighLevelDC> handle) {
            return new HighLevelDC(handle);
        }
    };

    public static void nextGen() {
        POOL.nextGen();
    }

    private final ThreadSafeGenPool.Handle<HighLevelDC> handle;

    private HighLevelDC(ThreadSafeGenPool.Handle<HighLevelDC> handle) {
        this.handle = handle;
    }

    public static HighLevelDC acquire() {
        return POOL.lend();
    }

    public void recycle() {
        if (handle.isInPool()) {
            reset();
            handle.recycle();
        }
    }

    public CommandSource source = null;
    public PassHint passHint = null;
    public String meshTicketID = null;
    public int mode = -1;
    public int elementType = -1;
    // todo: sort key, is visible, material

    private void reset() {
        source = null;
        passHint = null;
        meshTicketID = null;
        mode = -1;
        elementType = -1;
    }

    private HighLevelDC initHighLevelDC(
            HighLevelDC.CommandSource source,
            PassHint passHint,
            String meshTicketID,
            int mode,
            int elementType) {
        this.source = source;
        this.passHint = passHint;
        this.meshTicketID = meshTicketID;
        this.mode = mode;
        this.elementType = elementType;
        return this;
    }

    // ========== filler methods ==========

    public HighLevelDC fillPassInternal(String meshTicketID, int mode, int elementType) {
        return initHighLevelDC(CommandSource.PASS_INTERNAL, null, meshTicketID, mode, elementType);
    }

    public HighLevelDC fillSceneSubmitted(PassHint passHint, String meshTicketID, int mode, int elementType) {
        return initHighLevelDC(CommandSource.SCENE_SUBMITTED, passHint, meshTicketID, mode, elementType);
    }
}
