package com.cleanroommc.kirino.engine.render.platform.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.core.debug.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.platform.task.job.MeshletDebugJob;
import com.cleanroommc.kirino.engine.resource.ResourceSlot;
import com.cleanroommc.kirino.engine.resource.ResourceStorage;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class MeshletDebugSystem extends CleanSystem {
    private final Map<String, Object> externalData;

    private final Executor executor;

    public MeshletDebugSystem(ResourceStorage storage, ResourceSlot<GizmosManager> gizmosManager, Executor executor) {
        externalData = new HashMap<>();
        externalData.put("storage", storage);
        externalData.put("gizmosManager", gizmosManager);
        this.executor = executor;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(
                entityManager,
                MeshletDebugJob.class,
                externalData,
                executor);
        execution.updateExecutions(handle);
    }
}
