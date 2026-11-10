package com.cleanroommc.kirino.ecs.api.system;

import com.cleanroommc.kirino.ecs.api.world.CleanWorld;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import org.jspecify.annotations.NonNull;

/**
 * It's recommended to utilize the implementations of {@link SystemExeFlowGraph} to guide the execution of systems in {@link CleanWorld}.
 *
 * @see CleanWorld
 */
public abstract class CleanSystem {
    protected final ExecutionContainer execution = new ExecutionContainer();

    public abstract void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler);
}
