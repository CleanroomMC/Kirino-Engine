package com.cleanroommc.kirino.engine.render.platform.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.platform.scene.MinecraftScene;
import com.cleanroommc.kirino.engine.render.platform.task.job.MeshletDestroyJob;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class MeshletDestroySystem extends CleanSystem {
    private final Map<String, Object> externalData;

    private final Executor executor;

    public MeshletDestroySystem(List<MinecraftScene.ChunkPosKey> chunksDestroyedLastFrame, Executor executor) {
        externalData = new HashMap<>();
        externalData.put("chunksDestroyedLastFrame", chunksDestroyedLastFrame);
        this.executor = executor;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        JobScheduler.ExecutionHandle handle = jobScheduler.executeParallelJob(
                entityManager,
                MeshletDestroyJob.class,
                externalData,
                executor);
        execution.updateExecutions(handle);
    }
}
