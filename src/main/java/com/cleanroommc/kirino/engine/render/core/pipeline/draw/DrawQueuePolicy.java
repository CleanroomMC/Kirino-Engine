package com.cleanroommc.kirino.engine.render.core.pipeline.draw;

import com.cleanroommc.kirino.engine.render.core.pipeline.draw.cmd.LowLevelDC;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;

public record DrawQueuePolicy(
        boolean allowsReordering,
        @NonNull Comparator<LowLevelDC> comparator) {
}
