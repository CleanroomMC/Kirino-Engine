package com.cleanroommc.kirino.ecs;

import java.util.Set;

final class ArrayRange {
    public final int start;
    public final int end;
    public final Set<Integer> deprecatedIndexes;

    ArrayRange(int start, int end, Set<Integer> deprecatedIndexes) {
        this.start = start;
        this.end = end;
        this.deprecatedIndexes = deprecatedIndexes;
    }
}
