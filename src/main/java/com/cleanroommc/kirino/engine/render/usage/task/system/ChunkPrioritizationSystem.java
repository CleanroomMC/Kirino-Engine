package com.cleanroommc.kirino.engine.render.usage.task.system;

import com.cleanroommc.kirino.ecs.api.EntityManager;
import com.cleanroommc.kirino.ecs.api.job.JobScheduler;
import com.cleanroommc.kirino.ecs.api.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.core.camera.Camera;
import com.cleanroommc.kirino.engine.render.usage.task.job.ChunkPrioritizationJob;
import com.cleanroommc.kirino.engine.render.usage.task.RenderJobDataKeys;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkPrioritizationSystem extends CleanSystem {
    private final Map<String, Object> externalData;
    private final AtomicInteger maxLodCounter;

    private final Executor executor;

    public ChunkPrioritizationSystem(Camera camera, Executor executor) {
        externalData = new HashMap<>();
        externalData.put(RenderJobDataKeys.CAMERA, camera);
        maxLodCounter = new AtomicInteger(-1);
        externalData.put(RenderJobDataKeys.MAX_LOD_COUNTER, maxLodCounter);
        this.executor = executor;
    }

    public int getCurrentMaxLod() {
        return maxLodCounter.get();
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        maxLodCounter.set(-1);
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(
                entityManager,
                ChunkPrioritizationJob.class,
                externalData,
                executor);
        execution.updateExecutions(handle);
    }
}
