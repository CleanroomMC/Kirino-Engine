package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.camera.ICamera;
import com.cleanroommc.kirino.engine.render.task.job.ChunkPrioritizationJob;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkPrioritizationSystem extends CleanSystem {
    private final Map<String, Object> externalData;
    private final AtomicInteger maxLodCounter;

    public ChunkPrioritizationSystem(ICamera camera) {
        externalData = new HashMap<>();
        externalData.put("camera", camera);
        maxLodCounter = new AtomicInteger(-1);
        externalData.put("maxLodCounter", maxLodCounter);
    }

    public int getCurrentMaxLod() {
        return maxLodCounter.get();
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        maxLodCounter.set(-1);
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(entityManager, ChunkPrioritizationJob.class, externalData, ForkJoinPool.commonPool());
        if (handle.async()) {
            handle.future().join();
        }
        execution.updateExecutions(handle);
    }
}
