package com.cleanroommc.kirino.engine.render.pipeline.draw.cmd;

import com.cleanroommc.kirino.engine.render.pipeline.pass.PassHint;

import java.util.concurrent.atomic.AtomicInteger;

public final class HighLevelDC implements IDrawCommand {

    public enum CommandSource {
        PASS_INTERNAL,
        SCENE_SUBMITTED
    }

    private static final int POOL_CAPACITY = 8192;
    private static ObjectPoolArena POOL = new ObjectPoolArena(null);
    private static final AtomicInteger POOL_INDEX = new AtomicInteger(0);

    private HighLevelDC() {
    }

    public static HighLevelDC get() {
        int index = POOL_INDEX.getAndIncrement();
        if (index >= POOL_CAPACITY) {
            if (POOL.next == null) {
                POOL.next = new ObjectPoolArena(POOL);
            }
            POOL = POOL.next;
            POOL_INDEX.set(0);
        }
        return POOL.arena[0];
    }

    public void recycle() {
        reset();
        int index = POOL_INDEX.decrementAndGet();
        if (index < 0) {
            if (POOL.prev == null) {
                POOL_INDEX.set(0);
            } else {
                POOL = POOL.prev;
                if (POOL.next.next != null) {
                    POOL.next.next = null; // Should I call the GC after this?
                    System.gc();
                }
                index = POOL_CAPACITY - 1;
                POOL_INDEX.set(index);
            }
        }
        POOL.arena[index] = this;
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

    private static class ObjectPoolArena {
        ObjectPoolArena prev, next;
        final HighLevelDC[] arena = new HighLevelDC[POOL_CAPACITY];

        ObjectPoolArena(ObjectPoolArena prev) {
            this.prev = prev;
            this.next = null;

            for (int i = 0; i < POOL_CAPACITY; i++) {
                arena[i] = new HighLevelDC();
            }
        }
    }
}
