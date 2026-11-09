package com.cleanroommc.kirino.ecs.job;

import com.cleanroommc.kirino.ecs.EntityCommands;
import com.cleanroommc.kirino.ecs.EntityQuery;
import org.jspecify.annotations.NonNull;

/**
 * Jobs will be instantiated per thread. A job must have an empty (no parameters) constructor.
 */
public interface ParallelJob {
    void execute(@NonNull EntityCommands commands, int index, int entityID, int threadOrdinal);

    void query(@NonNull EntityQuery entityQuery);

    /**
     * Return value must be greater than or equal to 1.
     *
     * <p>For example, the O(n) algorithm with input size around 10 should return 10.</p>
     *
     * @param index The index
     * @return The estimated workload
     */
    int estimateWorkload(int index);
}
