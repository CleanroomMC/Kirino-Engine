package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.task.job.MeshletDestroyJob;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class MeshletDestroySystem extends CleanSystem {
    private final Map<String, Object> externalData;
    private int targetChunkPosX, targetChunkPosY, targetChunkPosZ;

    public MeshletDestroySystem() {
        externalData = new HashMap<>();
    }

    public void setTargetChunkPosX(int targetChunkPosX) {
        this.targetChunkPosX = targetChunkPosX;
    }

    public void setTargetChunkPosY(int targetChunkPosY) {
        this.targetChunkPosY = targetChunkPosY;
    }

    public void setTargetChunkPosZ(int targetChunkPosZ) {
        this.targetChunkPosZ = targetChunkPosZ;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        externalData.put("targetChunkPosX", targetChunkPosX);
        externalData.put("targetChunkPosY", targetChunkPosY);
        externalData.put("targetChunkPosZ", targetChunkPosZ);
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(entityManager, MeshletDestroyJob.class, externalData, ForkJoinPool.commonPool());
        if (handle.async()) {
            handle.future().join();
        }
    }
}
