package com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd;

/**
 * This is a signature of draw commands. {@link HighLevelDC} and {@link LowLevelDC} are the only two implementations.
 */
public sealed interface DrawCommand permits HighLevelDC, LowLevelDC {
    void recycle();
}
