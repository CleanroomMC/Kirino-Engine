package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.job.MeshletDebugJob;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class MeshletDebugSystem extends CleanSystem {
    private final Map<String, Object> externalData;

    public MeshletDebugSystem(GizmosManager gizmosManager) {
        externalData = new HashMap<>();
        externalData.put("gizmosManager", gizmosManager);
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(entityManager, MeshletDebugJob.class, externalData, ForkJoinPool.commonPool());
        if (handle.async()) {
            handle.future().join();
        }
        execution.updateExecutions(handle);
    }
}
