package com.cleanroommc.kirino.ecs.api.world;

import com.cleanroommc.kirino.ecs.api.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.exegraph.SystemExeFlowGraph;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;

/**
 * It's recommended to utilize the implementations of {@link SystemExeFlowGraph} to guide the execution of systems.
 * In order to do so, simply set up an instance of {@link SystemExeFlowGraph} like {@link SingleFlow} etc.
 */
public abstract class CleanWorld {
    protected final EntityManager entityManager;
    protected final JobScheduler jobScheduler;

    public CleanWorld(EntityManager entityManager, JobScheduler jobScheduler) {
        this.entityManager = entityManager;
        this.jobScheduler = jobScheduler;
    }

    protected final void flushECS() {
        entityManager.flush();
    }

    public final <T extends CleanSystem> SystemExeFlowGraph.Builder<SingleFlow<T>> newSingleFlowBuilder(Class<T> type) {
        return CleanSystem.newSingleFlowBuilder(entityManager, jobScheduler, type);
    }

    public abstract void update();
}
