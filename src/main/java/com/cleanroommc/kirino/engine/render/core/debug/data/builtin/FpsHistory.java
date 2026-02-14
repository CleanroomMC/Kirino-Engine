package com.cleanroommc.kirino.engine.render.core.debug.data.builtin;

import com.cleanroommc.kirino.engine.render.core.debug.data.DebugDataService;

public class FpsHistory implements DebugDataService {
    private static final int CAP = 512;
    private int maxFps = -1;
    private final int[] ring = new int[CAP];
    private int index = 0;

    public void record(int fps) {
        ring[index++ & (CAP - 1)] = fps;
        if (fps > maxFps) {
            maxFps = fps;
        }
    }

    public int[] snapshot() {
        return ring;
    }

    public int logicalIndex() {
        return index;
    }

    public int physicalIndex() {
        return index & (CAP - 1);
    }

    public int maxFpsEver() {
        return Math.max(maxFps, 1);
    }

    @Override
    public boolean isActive() {
        return true;
    }
}
