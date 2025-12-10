package com.cleanroommc.kirino.ecs.world;

import com.cleanroommc.kirino.ecs.system.exegraph.SingleFlow;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.exegraph.ISystemExeFlowGraph;

/**
 * It's recommended to utilize the implementations of {@link ISystemExeFlowGraph} to guide the execution of systems.
 * In order to do so, simply set up an instance of {@link ISystemExeFlowGraph} like {@link SingleFlow} etc.
 */
public class CleanWorld {
    protected final EntityManager entityManager;
    protected final JobScheduler jobScheduler;

    public CleanWorld(EntityManager entityManager, JobScheduler jobScheduler) {
        this.entityManager = entityManager;
        this.jobScheduler = jobScheduler;
    }

    public void update() {
        entityManager.flush();
    }
}
