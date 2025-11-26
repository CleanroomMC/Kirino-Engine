package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;
import io.netty.util.Recycler;

public final class HighLevelDC implements IDrawCommand {

    public enum CommandSource {
        PASS_INTERNAL,
        SCENE_SUBMITTED
    }

    private static final Recycler<HighLevelDC> RECYCLER = new Recycler<>() {
        @Override
        protected HighLevelDC newObject(Handle<HighLevelDC> handle) {
            return new HighLevelDC(handle);
        }
    };

    private final Recycler.Handle<HighLevelDC> handle;

    private HighLevelDC(Recycler.Handle<HighLevelDC> handle) {
        this.handle = handle;
    }

    public static HighLevelDC get() {
        return RECYCLER.get();
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

    public void recycle() {
        reset();
        handle.recycle(this);
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
