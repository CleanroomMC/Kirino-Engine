package com.cleanroommc.kirino.ecs.world;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;

public class CleanWorld {
    protected final EntityManager entityManager;
    protected final JobScheduler jobScheduler;

//    private final ISystemExeGraph systemExeGraph = new ISystemExeGraph();

    // need more params to indicate dep
    public final void addSystem(CleanSystem system) {
        // systemExeGraph.add
    }

    public CleanWorld(EntityManager entityManager, JobScheduler jobScheduler) {
        this.entityManager = entityManager;
        this.jobScheduler = jobScheduler;
    }

    public void update() {
//        systemExeGraph.execute();
        entityManager.flush();
    }
}
