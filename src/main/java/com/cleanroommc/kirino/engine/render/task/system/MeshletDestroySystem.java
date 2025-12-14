package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.task.job.MeshletDestroyJob;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class MeshletDestroySystem extends CleanSystem {
    private final Map<String, Object> externalData;

    public MeshletDestroySystem(List<MinecraftScene.ChunkPosKey> chunksDestroyedLastFrame) {
        externalData = new HashMap<>();
        externalData.put("chunksDestroyedLastFrame", chunksDestroyedLastFrame);
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(
                entityManager,
                MeshletDestroyJob.class,
                externalData,
                ForkJoinPool.commonPool());
        execution.updateExecutions(handle);
    }
}
