package com.cleanroommc.kirino.engine.render.platform.scene.scheduler;

import org.jspecify.annotations.Nullable;

/**
 * A <code>UpdateScheduler</code> should be a submodule of a higher level orchestrator,
 * and the return value of {@link #update(Object)} will tell the orchestrator whether to proceed.
 */
public interface UpdateScheduler {

    /**
     * @return Whether to abort the process (<code>return;</code>)
     */
    boolean update(@Nullable Object payload);
}
